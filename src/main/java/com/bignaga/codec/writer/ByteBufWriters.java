/**
 * @project lbsgap
 * @file ByteBufWriters.java
 * @package com.bignaga.codec.writer
 * @author zhonghao
 * @date 2018/6/8 12:20
 * @copyright bignaga
 */
package com.bignaga.codec.writer;


import com.bignaga.utils.ThrowAs;
import com.bignaga.codec.MessageElementType;
import com.bignaga.exception.UnsupportedFieldTypeException;
import io.netty.buffer.ByteBuf;

import java.nio.ByteOrder;

import static com.bignaga.codec.MessageElementType.*;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author zhonghao
 * @date 2018/6/8 12:20
 * @see
 * @since
 */
public final class ByteBufWriters {
    private ByteBufWriters() {
        throw new UnsupportedOperationException();
    }

    public static void write(int i, ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType) {
        if(elementType == U8 || elementType == S8) {
            byteBuf.writeByte(i);
            return;
        }
        if(elementType == U16 || elementType == S16) {
            if(byteOrder == ByteOrder.BIG_ENDIAN) {
                byteBuf.writeShort(i);
            } else {
                byteBuf.writeShortLE(i);
            }
            return;
        }
//        if(elementType == U24 || elementType == S24) {
//            if(byteOrder == ByteOrder.BIG_ENDIAN) byteBuf.writeMedium(i); else byteBuf.writeMediumLE(i);
//            return;
//        }

        if(elementType == U32 || elementType == S32) {
            if(byteOrder == ByteOrder.BIG_ENDIAN) {
                byteBuf.writeInt(i);
            } else {
                byteBuf.writeIntLE(i);
            }
            return;
        }
        //
        ThrowAs.ThrowAsRuntimeException(new UnsupportedFieldTypeException());
    }


    public static void write(long l, ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType) {
        if(elementType == U32) {
            if(byteOrder == ByteOrder.BIG_ENDIAN) {
                byteBuf.writeInt((int)l);
            } else {
                byteBuf.writeIntLE((int)l);
            }
            return;
        }
        if(elementType == LONG) {
            if(byteOrder == ByteOrder.BIG_ENDIAN) {
                byteBuf.writeLong(l);
            } else {
                byteBuf.writeLongLE(l);
            }
            return;
        }
        //
        ThrowAs.ThrowAsRuntimeException(new UnsupportedFieldTypeException());
    }

    public static void write(byte[] bytes, ByteBuf byteBuf) {
        byteBuf.writeBytes(bytes);
    }

    public static ByteBufWriter<Integer> INT8_WRITER = (i, byteBuf, byteOrder) -> byteBuf.writeByte(i);

    public static ByteBufWriter<Integer> INT16_WRITER = (i, byteBuf, byteOrder) -> {
        if(byteOrder == ByteOrder.BIG_ENDIAN) {
            byteBuf.writeShort(i);
        } else {
            byteBuf.writeShortLE(i);
        }
    };

    public static ByteBufWriter<Integer> INT24_WRITER = (i, byteBuf, byteOrder) -> {
        if(byteOrder == ByteOrder.BIG_ENDIAN) {
            byteBuf.writeMedium(i);
        } else {
            byteBuf.writeMediumLE(i);
        }
    };

    public static ByteBufWriter<Integer> INT32_WRITER = (i, byteBuf, byteOrder) -> {
        if(byteOrder == ByteOrder.BIG_ENDIAN) {
            byteBuf.writeInt(i);
        } else {
            byteBuf.writeIntLE(i);
        }
    };

    public static ByteBufWriter<Long> UINT32_WRITER = (l, byteBuf, byteOrder) -> {
        int i = (int) (l&0xffffffff);
        if(byteOrder == ByteOrder.BIG_ENDIAN) {
            byteBuf.writeInt(i);
        } else {
            byteBuf.writeIntLE(i);
        }
    };

    public static ByteBufWriter<Long> LONG_WRITER = (l, byteBuf, byteOrder) -> {
        if(byteOrder == ByteOrder.BIG_ENDIAN) {
            byteBuf.writeLong(l);
        } else {
            byteBuf.writeLongLE(l);
        }
    };

    public static ByteBufWriter<byte[]> BYTES_WRITER = (bytes, byteBuf, byteOrder) -> byteBuf.writeBytes(bytes);

    public static ByteBufWriter<?> getWirter(MessageElementType type) {
        switch (type) {
            case U8:
            case S8: return INT8_WRITER;
            case U16:
            case S16: return INT16_WRITER;
            case U32: return UINT32_WRITER;
            case S32: return INT32_WRITER;
            case BYTES: return BYTES_WRITER;
        }
        return null;
    }
}
