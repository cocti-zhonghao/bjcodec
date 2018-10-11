/**
 * @project lbsgap
 * @file ThrowAs.java
 * @package com.bignaga.utils
 * @author zhonghao
 * @date 2018/6/1 18:00
 * @copyright bignaga
 */
package com.bignaga.utils;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author zhonghao
 * @date 2018/6/1 18:00
 * @see
 * @since
 */
public class ThrowAs {
    public static <T extends Throwable> T Throw(Throwable t) throws T {
        throw (T)t;
    }

    public static void ThrowAsRuntimeException(Throwable t) {
        ThrowAs.<RuntimeException>Throw(t);
    }
}
