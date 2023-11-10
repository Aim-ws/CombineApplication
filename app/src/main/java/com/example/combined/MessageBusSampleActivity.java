package com.example.combined;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ws.message.bus.MessageBus;

/**
 * Create time 2023/11/9 11:08
 * Email: shuaiwang@grandstream.cn
 *
 * @author wangshuai
 * @version 1.0
 */
public class MessageBusSampleActivity extends AppCompatActivity {
    int max = 2000;
    int index = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_bus_sample);

        findViewById(R.id.btn_send_message).setOnClickListener(view -> {
            MessageBus.getDefault().post("test1", "111111");
        });

        findViewById(R.id.btn_send_message_thread).setOnClickListener(view -> {
            start();
        });
    }

    private void start() {
        MessageBus.getDefault().post(1285477);
    }
}
