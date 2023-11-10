package com.ws.message.bus.test;

import com.ws.message.bus.annotation.Subscribe;
import com.ws.message.bus.annotation.ThreadMode;

/**
 * Create time 2023/11/9 9:19
 * Email: shuaiwang@grandstream.cn
 *
 * @author wangshuai
 * @version 1.0
 */
public class Test {


    @Subscribe(action = "100", threadMode = ThreadMode.MAIN)
    public void test1() {

    }


}
