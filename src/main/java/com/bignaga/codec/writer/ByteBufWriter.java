/**
 * @project lbsgap
 * @file ByteBufWriter.java
 * @package com.bignaga.codec.writer
 * @author zhonghao
 * @date 2018/6/6 11:46
 * @copyright bignaga
 */
package com.bignaga.codec.writer;

import io.netty.buffer.ByteBuf;

import java.nio.ByteOrder;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author zhonghao
 * @date 2018/6/6 11:46
 * @see
 * @since
 */
@FunctionalInterface
public interface ByteBufWriter<T> {
    void write(T t, ByteBuf byteBuf, ByteOrder byteOrder);
}
