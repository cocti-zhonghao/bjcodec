/**
 * @project lbsgap
 * @file CodecUtils.java
 * @package com.bignaga.utils
 * @author zhonghao
 * @date 2018/9/7 18:20
 * @copyright bignaga
 */
package com.bignaga.codec.utils;

import com.bignaga.codec.LengthLengthEnum;
import io.netty.buffer.ByteBuf;

import java.nio.ByteOrder;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author zhonghao
 * @date 2018/9/7 18:20
 * @see
 * @since
 */
public class CodecUtils {
    public static void writeLength(ByteBuf byteBuf, ByteOrder byteOrder, LengthLengthEnum lengthLengthEnum, int lengthLen) {
        switch (lengthLengthEnum) {
            case FOUR_BYTES: {
                if(ByteOrder.BIG_ENDIAN.equals(byteOrder)) {
                    byteBuf.writeInt(lengthLen);
                } else {
                    byteBuf.writeIntLE(lengthLen);
                }
            } break;
            case TWO_BYTES: {
                if(ByteOrder.BIG_ENDIAN.equals(byteOrder)) {
                    byteBuf.writeShort(lengthLen);
                } else {
                    byteBuf.writeShortLE(lengthLen);
                }
            } break;
            case ONE_BYTES: {
                byteBuf.writeByte(lengthLen);
            } break;
            default: break;
        }
    }

    public static int readLength(ByteBuf byteBuf, ByteOrder byteOrder, LengthLengthEnum lengthLengthEnum) {
        switch (lengthLengthEnum) {
            case FOUR_BYTES: {
                if(ByteOrder.BIG_ENDIAN.equals(byteOrder)) {
                    return byteBuf.readInt();
                } else {
                    return byteBuf.readIntLE();
                }
            }
            case TWO_BYTES: {
                if(ByteOrder.BIG_ENDIAN.equals(byteOrder)) {
                    return byteBuf.readUnsignedShort();
                } else {
                    return byteBuf.readUnsignedShortLE();
                }
            }
            case ONE_BYTES: {
                return byteBuf.readUnsignedByte();
            }
            default: break;
        }

        return Integer.MIN_VALUE;
    }

}
