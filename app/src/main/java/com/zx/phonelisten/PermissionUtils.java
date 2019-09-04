package com.zx.phonelisten;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * create by zx on 2019-9-3 17:51
 * 项目名称：PhoneListen
 * 类注释：
 * 备注：
 */
public class PermissionUtils {
    private static PermissionUtils sInstance;
    private IPermissionsResult mPermissionsResult;
    // 权限请求码
    private final int mRequestCode = 100;
    public static boolean showSystemSetting = true;

    public PermissionUtils() {
    }

    /**
     * 单例
     *
     * @return
     */
    public static PermissionUtils getInstance() {
        if (sInstance == null) {
            sInstance = new PermissionUtils();
        }
        return sInstance;
    }

    /**
     * 检测权限
     *
     * @param context           上下文环境
     * @param permissions       权限列表
     * @param permissionsResult 检测后的返回值,接口回调
     */
    public void checkPermission(Activity context, String[] permissions, @NonNull IPermissionsResult permissionsResult) {
        mPermissionsResult = permissionsResult;

        // 6.0 采用动态权限
        if (Build.VERSION.SDK_INT < 23) {
            permissionsResult.grantPermission();
            return;
        }

        // 创建一个 mPermissionList ,逐个判断哪些权限未授予,未授予的权限存储到 mPermissionList 中
        List<String> mPermissionList = new ArrayList<>();
        // 逐个判断你要检测的权限是否已经通过
        for (int i = 0; i < permissions.length; i++) {
            if (ActivityCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                // 添加还未授予的权限到 mPermissionList 中
                mPermissionList.add(permissions[i]);
            }
        }

        // 申请权限
        if (mPermissionList.size() > 0) {
            // 有权限没有通过,需要申请
            ActivityCompat.requestPermissions(context, permissions, mRequestCode);
        } else {
            // 说明权限都已经通过,可以做你想做的事情去
            permissionsResult.grantPermission();
            return;
        }
    }

    /**
     * 请求权限后回调的方法
     *
     * @param context      上下文
     * @param requestCode  自己定义的权限请求码
     * @param permissions  请求的权限名称数组
     * @param grantResults 弹出页面后是否允许权限的标识数组,数组的长度对应的是权限名称数组的长度,数组的数据0表示允许权限,-1表示禁止权限
     */
    public void onRequestPermissionsResult(Activity context, int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // 有权限没有通过
        boolean hasPermissionDismiss = false;
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }

            // 如果有权限没有被允许
            if (hasPermissionDismiss) {
                if (showSystemSetting) {
                    // 跳转到系统设置权限页面,或者直接关闭页面,不让他继续访问
                    showSystemPermissionsSettingDialog(context);
                } else {
                    mPermissionsResult.forbidPermission();
                }
            } else {
                // 全部权限通过,可以进行下一步操作
                mPermissionsResult.grantPermission();
            }
        }
    }

    /**
     * 不再提示权限时的展示对话框
     */
    private void showSystemPermissionsSettingDialog(final Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.test_s_help_tip);
        builder.setMessage(R.string.test_s_permission_help_tip);
        builder.setPositiveButton(context.getString(R.string.test_s_help_tip), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openAppSettings(context);
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void openAppSettings(Context context) {
        Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        if (!(context instanceof Activity)) {
            settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        settings.setData(Uri.parse("package:".concat(context.getPackageName())));
        try {
            context.startActivity(settings);
        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public interface IPermissionsResult {
        void grantPermission();

        void forbidPermission();
    }
}
