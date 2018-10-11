/**
 * @project lbsgap
 * @file ByteOrderEnum.java
 * @package com.bignaga.codec
 * @author zhonghao
 * @date 2018/9/6 13:23
 * @copyright bignaga
 */
package com.bignaga.codec;

import java.nio.ByteOrder;

/**
 * byte order
 *
 * @author zhonghao
 * @date 2018/9/6 13:23
 * @see
 * @since
 */
public enum ByteOrderEnum {
    BIG_ENDIAN(ByteOrder.BIG_ENDIAN),
    LITTLE_ENDIAN(ByteOrder.LITTLE_ENDIAN);

    ByteOrderEnum(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }
    public final ByteOrder byteOrder;
}
