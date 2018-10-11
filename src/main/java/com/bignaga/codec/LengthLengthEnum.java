/**
 * @project lbsgap
 * @file LengthLengthEnum.java
 * @package com.bignaga.codec
 * @author zhonghao
 * @date 2018/9/7 11:59
 * @copyright bignaga
 */
package com.bignaga.codec;

/**
 * 长度字段自身的长度
 *
 * @author zhonghao
 * @date 2018/9/7 11:59
 * @see
 * @since
 */
public enum LengthLengthEnum {
    /**
     * 不包含长度字段
     */
    NONE,
    /**
     * 一个字节
     */
    ONE_BYTES,
    /**
     * 两个字节
     */
    TWO_BYTES,
    /**
     * 四个字节
     */
    FOUR_BYTES
}
