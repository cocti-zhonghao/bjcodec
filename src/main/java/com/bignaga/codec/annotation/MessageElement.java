package com.bignaga.codec.annotation;

import com.bignaga.codec.ByteOrderEnum;
import com.bignaga.codec.MessageElementType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author zhonghao
 * @date 2018/6/1 10:50
 * @see
 * @since
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageElement {
    /**
     * byte order, default ByteOrderEnum.BIG_ENDIAN
     * @return
     */
    ByteOrderEnum byteOrder() default ByteOrderEnum.BIG_ENDIAN;

    /**
     * field index
     * @return
     */
    int index();

    /**
     * field type
     * @return
     */
    MessageElementType type();

    /**
     * field length, for Collection/Array/String
     * @return
     */
    Length []lengthStack() default {@Length};

    /**
     * reserved
     * @return
     */
    int version() default Integer.MAX_VALUE;
}
