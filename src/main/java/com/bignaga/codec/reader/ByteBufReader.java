/**
 * @project lbsgap
 * @file ByteBufReader.java
 * @package com.bignaga.codec.reader
 * @author zhonghao
 * @date 2018/6/6 9:56
 * @copyright bignaga
 */
package com.bignaga.codec.reader;

import io.netty.buffer.ByteBuf;

import java.nio.ByteOrder;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author zhonghao
 * @date 2018/6/6 9:56
 * @see
 * @since
 */
public interface ByteBufReader<T>  {
    T read(ByteBuf byteBuf, ByteOrder byteOrder, int length);
}
