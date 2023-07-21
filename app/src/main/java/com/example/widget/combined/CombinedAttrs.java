package com.example.widget.combined;

/**
 * Create time 2023/7/18 16:02
 * Email: shuaiwang@grandstream.cn
 *
 * @author wangshuai
 * @version 1.0
 */
public class CombinedAttrs {
    /**
     * 头像的形状，圆角方形和圆形
     */
    @CombinedShape
    int shape;
    /**
     * 圆角的半径
     */
    float roundRadius;
    /**
     * 宫格分割线的宽度
     */
    float dividerWidth;
    /**
     * 宫格分割线的颜色
     */
    int dividerColor;
    /**
     * 整个头像的背景色
     */
    int backgroundColor;
    /**
     * 文字头像文字最大值
     */
    float textSize;
    /**
     * 文字头像文字的颜色
     */
    int textColor;

    public CombinedAttrs(@CombinedShape int shape, float roundRadius, float dividerWidth,
                         int dividerColor, int backgroundColor, float textSize, int textColor) {
        this.shape = shape;
        this.roundRadius = roundRadius;
        this.dividerWidth = dividerWidth;
        this.dividerColor = dividerColor;
        this.backgroundColor = backgroundColor;
        this.textSize = textSize;
        this.textColor = textColor;
    }
}
