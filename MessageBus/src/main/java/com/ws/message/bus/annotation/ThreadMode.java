package com.ws.message.bus.annotation;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Create time 2023/11/9 10:09
 * Email: shuaiwang@grandstream.cn
 *
 * @author wangshuai
 * @version 1.0
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({ThreadMode.MAIN, ThreadMode.DEFAULT})
@Target({
        ElementType.ANNOTATION_TYPE,
        ElementType.TYPE,
        ElementType.METHOD,
        ElementType.LOCAL_VARIABLE,
        ElementType.PARAMETER,
        ElementType.TYPE_PARAMETER
})
public @interface ThreadMode {

    int MAIN = 1;
    int DEFAULT = 0;

}
