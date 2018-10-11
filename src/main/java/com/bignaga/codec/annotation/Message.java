package com.bignaga.codec.annotation;


import com.bignaga.codec.ByteOrderEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author zhonghao
 * @date 2018/6/1 10:48
 * @see
 * @since
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Message {
    int value();
    ByteOrderEnum byteOrder() default ByteOrderEnum.BIG_ENDIAN;
}
