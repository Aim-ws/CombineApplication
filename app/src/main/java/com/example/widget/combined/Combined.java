package com.example.widget.combined;

import androidx.annotation.ColorInt;

/**
 * Create time 2023/7/18 15:50
 * Email: shuaiwang@grandstream.cn
 *
 * @author wangshuai
 * @version 1.0
 */
public class Combined {
    /**
     * 文本框的背景色
     */
    @ColorInt
    private int backgroundColor;
    /**
     * 没有自定义头像的联系人采用文本绘制头像
     */
    private String text;
    /**
     * 自定义头像链接
     */
    private String url;

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getText() {
        return text == null ? "" : text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl() {
        return url == null ? "" : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
