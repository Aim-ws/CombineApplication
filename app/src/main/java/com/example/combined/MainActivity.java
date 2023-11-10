package com.example.combined;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ws.message.bus.MessageBus;
import com.ws.message.bus.annotation.Subscribe;
import com.ws.message.bus.annotation.ThreadMode;

/**
 * Create time 2023/11/9 11:09
 * Email: shuaiwang@grandstream.cn
 *
 * @author wangshuai
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MessageBus.getDefault().register(this);

        findViewById(R.id.btn_combined).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, SampleActivity.class)));
        findViewById(R.id.btn_message_bus).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, MessageBusSampleActivity.class)));
    }

    @Subscribe(threadMode = ThreadMode.MAIN, action = "test1")
    public void onMessage(String content) {
        Log.d(TAG, "onMessage: " + content);
        Toast.makeText(MainActivity.this, content, Toast.LENGTH_SHORT).show();
    }


    @Subscribe(threadMode = ThreadMode.MAIN, action = "test1")
    public void onMessage(int result) {
        Log.d(TAG, "onMessage: " + result);
        Toast.makeText(MainActivity.this, "result: " + result, Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, action = "test1")
    public void onMessage() {
        Log.d(TAG, "onMessage: no parameter ");
        Toast.makeText(MainActivity.this, "no parameter ", Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, action = "test1")
    public void onMessage(long ret) {

    }

    @Subscribe
    public void onNoActionMessage() {
        Toast.makeText(MainActivity.this, "onNoActionMessage", Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onNoActionMessage(int a) {
        Toast.makeText(MainActivity.this, "onNoActionMessage event: " + a, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MessageBus.getDefault().unregister(this);
    }
}
