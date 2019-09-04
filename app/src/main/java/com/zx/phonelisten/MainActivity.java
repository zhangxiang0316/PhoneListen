package com.zx.phonelisten;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 启动监听服务
        openService();
    }

    /**
     * 启动监听服务
     */
    private void openService() {
        Intent intent = new Intent(MainActivity.this, PhoneListenService.class);
        startService(intent);
    }

}
