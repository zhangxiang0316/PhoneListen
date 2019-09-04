package com.zx.phonelisten;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * create by zx on 2019-9-3 17:50
 * 项目名称：PhoneListen
 * 类注释：
 * 备注：
 */
public class PhoneListenService extends Service {
    private static final String tag = "PhoneListenService";

    // 电话管理者对象
    private TelephonyManager mTelephonyManager;
    // 电话状态监听者
    private MyPhoneStateListener myPhoneStateListener;
    // 动态监听去电的广播接收器
    private InnerOutCallReceiver mInnerOutCallReceiver;

    @Override
    public void onCreate() {
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        myPhoneStateListener = new MyPhoneStateListener();
        mTelephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        // 动态注册广播接收器监听去电信息
        mInnerOutCallReceiver = new InnerOutCallReceiver();
        // 手机拨打电话时会发送：android.intent.action.NEW_OUTGOING_CALL的广播
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        registerReceiver(mInnerOutCallReceiver, intentFilter);
        super.onCreate();
    }

    /**
     * 动态注册广播接收器监听去电信息
     */
    class InnerOutCallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取播出的去电号码
            String outPhone = getResultData();
            Log.i(tag, "outPhone:" + outPhone);
        }
    }

    /**
     * 自定义内部类对来电的电话状态进行监听
     */
    class MyPhoneStateListener extends PhoneStateListener {
        // 重写电话状态改变时触发的方法
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.i(tag, "响铃:" + incomingNumber);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.i(tag, "接听");
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.i(tag, "挂断");
                    openDinding();
                    break;
            }
        }
    }

    private void openDinding() {
        //屏幕唤醒
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "BootBroadcastReceiver");
        wl.acquire();
        //屏幕解锁
        KeyguardManager km = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("BootBroadcastReceiver");
        kl.disableKeyguard();
        OpenDing.openDing("com.alibaba.android.rimet", this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        // 取消来电的电话状态监听服务
        if (mTelephonyManager != null && myPhoneStateListener != null) {
            mTelephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        // 取消去电的广播监听
        if (mInnerOutCallReceiver != null) {
            unregisterReceiver(mInnerOutCallReceiver);
        }
        super.onDestroy();
    }
}
