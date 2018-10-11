/**
 * @project lbsgap
 * @file ByteBufReaders.java
 * @package com.bignaga.codec.reader
 * @author zhonghao
 * @date 2018/6/8 13:55
 * @copyright bignaga
 */
package com.bignaga.codec.reader;

import com.bignaga.codec.MessageElementType;

import java.nio.ByteOrder;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author zhonghao
 * @date 2018/6/8 13:55
 * @see
 * @since
 */
public final class ByteBufReaders {
    private ByteBufReaders() {
        throw new UnsupportedOperationException();
    }

    public static final ByteBufReader<Integer> INT8_READER = (byteBuf, byteOrder, length) -> Integer.valueOf(byteBuf.readByte());
    public static final ByteBufReader<Integer> UINT8_READER = (byteBuf, byteOrder, length) -> Integer.valueOf(byteBuf.readUnsignedByte());

    public static final ByteBufReader<Integer> INT16_READER = (byteBuf, byteOrder, length) -> Integer.valueOf(byteOrder == ByteOrder.BIG_ENDIAN ? byteBuf.readShort() : byteBuf.readShortLE());
    public static final ByteBufReader<Integer> UINT16_READER = (byteBuf, byteOrder, length) -> Integer.valueOf(byteOrder == ByteOrder.BIG_ENDIAN ? byteBuf.readUnsignedShort() : byteBuf.readUnsignedShortLE());

    public static final ByteBufReader<Integer> INT24_READER = (byteBuf, byteOrder, length) -> Integer.valueOf(byteOrder == ByteOrder.BIG_ENDIAN ? byteBuf.readMedium() : byteBuf.readMediumLE());
    public static final ByteBufReader<Integer> UINT24_READER = (byteBuf, byteOrder, length) -> Integer.valueOf(byteOrder == ByteOrder.BIG_ENDIAN ? byteBuf.readUnsignedMedium() : byteBuf.readUnsignedMediumLE());

    public static final ByteBufReader<Integer> INT32_READER = (byteBuf, byteOrder, length) -> byteOrder == ByteOrder.BIG_ENDIAN ? byteBuf.readInt() : byteBuf.readInt();
    public static final ByteBufReader<Long> UINT32_READER = (byteBuf, byteOrder, length) -> byteOrder == ByteOrder.BIG_ENDIAN ? byteBuf.readUnsignedInt() : byteBuf.readUnsignedIntLE();

    public static final ByteBufReader<Long> LONG_READER = (byteBuf, byteOrder, length) -> byteOrder == ByteOrder.BIG_ENDIAN ? byteBuf.readLong() : byteBuf.readLongLE();

    public static final ByteBufReader<byte[]> BYTES_READER = (byteBuf, byteOrder, length) -> {
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    };

    public static ByteBufReader<?> getReader(MessageElementType type) {
        switch (type) {
            case U8: return UINT8_READER;
            case S8: return INT8_READER;
            case U16: return UINT16_READER;
            case S16: return INT16_READER;
            case U32: return UINT32_READER;
            case S32: return INT32_READER;
            case BYTES: return BYTES_READER;
            case LONG : return LONG_READER;
        }
        return null;

    }

}
