package com.bignaga.codec.annotation;


import com.bignaga.codec.LengthLengthEnum;

/**
 * length indicator
 *
 *
 * @author zhonghao
 * @date 2018/9/7 11:54
 * @see
 * @since
 */
public @interface Length {
    /**
     * length of Collection/Array/String,
     * that is the element number of Collection/Array/String
     * @return
     */
    int value() default 0;

    /**
     * the length of length field self
     * NONE:    no containing length field；
     * ONE_BYTES:   the length of length field is one byte；
     * TWO_BYTES:    the length of length field is two bytes；
     * FOUR_BYTES:  the length of length field is four bytes
     * @return
     */
    LengthLengthEnum length() default LengthLengthEnum.NONE;
}
