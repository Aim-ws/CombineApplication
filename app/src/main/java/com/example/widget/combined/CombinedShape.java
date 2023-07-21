package com.example.widget.combined;

import androidx.annotation.IntDef;

/**
 * Create time 2023/7/18 16:19
 * Email: shuaiwang@grandstream.cn
 *
 * @author wangshuai
 * @version 1.0
 */
@IntDef({CombinedShape.ROUND, CombinedShape.CIRCLE})
public @interface CombinedShape {
    /**
     * 圆角方形
     */
    int ROUND = 1;
    /**
     * 圆形
     */
    int CIRCLE = 2;
}
