package com.example.combined;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.widget.combined.Combined;
import com.example.widget.combined.CombinedView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Create time 2023/7/19 14:42
 * Email: shuaiwang@grandstream.cn
 *
 * @author wangshuai
 * @version 1.0
 */
public class SampleActivity extends AppCompatActivity {
    CombinedView combinedView;

    private final String[] avatars = new String[]{
            "https://img2.baidu.com/it/u=367273999,4201595611&fm=253&fmt=auto&app=138&f=JPEG?w=535&h=500",
            "https://img2.baidu.com/it/u=2957045803,1622480034&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500",
            "https://img0.baidu.com/it/u=1691000662,1326044609&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500",
            "https://img2.baidu.com/it/u=473659940,2616707866&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500",
            "https://img0.baidu.com/it/u=345359896,661384602&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500",
            "https://img0.baidu.com/it/u=2954567999,959431819&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500",
            "https://img2.baidu.com/it/u=3377923072,1972706124&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500",
            "https://img2.baidu.com/it/u=2826562948,3171725130&fm=253&fmt=auto&app=138&f=JPEG?w=400&h=400",
            "https://img1.baidu.com/it/u=2076707725,3468393586&fm=253&fmt=auto&app=138&f=JPEG?w=508&h=500",
    };

    private final String[] avatarTexts = new String[]{
            "凌云阁",
            "大唐醉",
            "听雨楼",
            "云梦泽",
            "中华楼",
            "水云涧",
            "落日谷",
            "千机阁",
            "万宝楼",
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        combinedView = findViewById(R.id.combinedView);
        combinedView.drawCombined(getData());
        findViewById(R.id.btn).setOnClickListener(v -> combinedView.drawCombined(getData()));
    }

    private List<Combined> getData() {
        Random random = new Random();
        int count = random.nextInt(9) + 1;
        List<Combined> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Combined combined = new Combined();
            if (isText()) {
                combined.setText(getAvatarText(i));
            } else {
                combined.setUrl(getUrl(i));
            }
            combined.setBackgroundColor(ActivityCompat.getColor(this,R.color.color_cell_background));
            list.add(combined);
        }
        return list;
    }

    private String getAvatarText(int index) {
        return avatarTexts[index % avatarTexts.length];
    }

    private String getUrl(int index) {
        return avatars[index % avatars.length];
    }

    private boolean isText() {
        Random random = new Random();
        int value = random.nextInt(10);
        return value % 3 == 0;
    }
}
