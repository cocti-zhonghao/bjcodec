/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed toByteBufValue in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bignaga.codec.copyfromgson.reflect;

import com.bignaga.codec.LengthLengthEnum;
import com.bignaga.codec.MessageElementType;
import com.bignaga.codec.annotation.Length;
import com.bignaga.codec.exception.LengthException;
import com.bignaga.codec.reader.ByteBufReader;
import com.bignaga.codec.reader.ByteBufReaders;
import com.bignaga.codec.writer.ByteBufWriter;
import com.bignaga.codec.writer.ByteBufWriters;
import com.bignaga.codec.utils.CodecUtils;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class TypeAdapter<T> {
    public static final byte[] zeroLenBytes = new byte[0];
    public static byte[] padding(byte[] source, byte[] padding, int length) {
        int paddingLen = length - source.length;
        if (paddingLen <= 0) {
            return source;
        }
        //do padding
        byte[] padded = new byte[source.length + paddingLen];
        System.arraycopy(source, 0, padded, 0, source.length);
        int destPos = source.length;
        while (paddingLen > 0) {
            int len = paddingLen < padding.length ? paddingLen : padding.length;
            System.arraycopy(padding, 0, padded, destPos, len);
            paddingLen -= len;
            destPos += len;
        }
        return padded;
    }

    public abstract void write(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack);

    public abstract void write(T value, ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack);

    public abstract T read(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack);

    public static abstract class SimpleTypeAdapter<T, U> extends TypeAdapter<T> {
        @Override
        public void write(T value, ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
            this.write(value, byteBuf, byteOrder, elementType, ArrayUtils.isEmpty(lengthStack) ? null : lengthStack[0]);
        }

        @Override
        public T read(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
            return this.read(byteBuf, byteOrder, elementType, ArrayUtils.isEmpty(lengthStack) ? null : lengthStack[0]);
        }


        public void write(T value, ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length length) {
            ((ByteBufWriter<U>) ByteBufWriters.getWirter(elementType)).write(convertTo(value, length), byteBuf, byteOrder);
        }


        public T read(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length length) {
            return convertFrom(((ByteBufReader<U>) ByteBufReaders.getReader(elementType)).read(byteBuf, byteOrder, 0));
        }

        /**
         * 从 T 转换为 U
         * @param t
         * @param length
         * @return
         */
        public abstract U convertTo(T t, Length length);

        /**
         * 从 U 转换为 T
         * @param u
         * @return
         */
        public abstract T convertFrom(U u);

    }

    /**
     * 保留字段adapter
     */
    public static class ReserveTypeAdapter extends TypeAdapter<Object> {
        @Override
        public void write(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
            this.write(null, byteBuf, byteOrder, elementType, lengthStack);
        }

        @Override
        public void write(Object value, ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
            if (ArrayUtils.isEmpty(lengthStack)) {
                throw new IllegalArgumentException("reserve field must have length specified!");
            }

            int length = lengthStack[0].value();
            //填充0x00
            byteBuf.writeZero(length);
        }

        @Override
        public Object read(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
            if (ArrayUtils.isEmpty(lengthStack)) {
                throw new IllegalArgumentException("reserve field must have length specified!");
            }

            int length = lengthStack[0].value();
            byteBuf.skipBytes(length);
            return null;
        }
    }

    /**
     * Integer类型字段adapter
     * @param <T>
     */
    public static abstract class IntegerTypeAdapter<T> extends SimpleTypeAdapter<T, Integer> {
        /**
         * T 转换为Integer
         * @param t
         * @param length
         * @return
         */
        @Override
        public abstract Integer convertTo(T t, Length length);

        /**
         * Integer 转换为 T
         * @param i
         * @return
         */
        @Override
        public abstract T convertFrom(Integer i);
    }

    /**
     * Long类型字段 adapter
     * @param <T>
     */
    public static abstract class LongTypeAdapter<T> extends SimpleTypeAdapter<T, Long> {
        /**
         * T 转换为 Long
         * @param t
         * @param length
         * @return
         */
        @Override
        public abstract Long convertTo(T t, Length length);

        /**
         * Long 转换为 T
         * @param l
         * @return
         */
        @Override
        public abstract T convertFrom(Long l);
    }

    /**
     * byte[] 类型字段adapter
     * @param <T>
     */
    public static abstract class BytesTypeAdapter<T> extends SimpleTypeAdapter<T, byte[]> {

        @Override
        public void write(T value, ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length length) {
            if(length == null) {
                throw new LengthException("bytes field must have length specified!");
            }
            //
            byte[] bytes = convertTo(value, length);
            if(length.length() != LengthLengthEnum.NONE) {
                CodecUtils.writeLength(byteBuf, byteOrder, length.length(), bytes.length);
            }
            ((ByteBufWriter<byte[]>) ByteBufWriters.getWirter(elementType)).write(bytes, byteBuf, byteOrder);
        }

        @Override
        public T read(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length length) {
            if(length == null) {
                throw new LengthException("bytes field must have length specified!");
            }
            //
            int toReadLength = 0;
            if(length.length() != LengthLengthEnum.NONE) {
                //从byteBuf中读取字段实际长度
                toReadLength = CodecUtils.readLength(byteBuf, byteOrder, length.length());
            } else {
                //通过注解指定了字段长度
                toReadLength = length.value();
            }
            if(toReadLength < 0) {
                throw new LengthException("the length of byte[] will be reading is < 0");
            }
            return convertFrom(((ByteBufReader<byte[]>) ByteBufReaders.getReader(elementType)).read(byteBuf, byteOrder, toReadLength));
        }



        /**
         * T 转换为 byte[]
         * @param t
         * @param length
         * @return
         */
        @Override
        public abstract byte[] convertTo(T t, Length length);

        /**
         * byte[] 转换为 T
         * @param bytes
         * @return
         */
        @Override
        public abstract T convertFrom(byte[] bytes);
    }

    /**
     * Boolean<->byte[],Integer,Long
     */
    public static class BooleanIntegerTypeAdapter extends IntegerTypeAdapter<Boolean> {

        @Override
        public void write(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
            this.write(false, byteBuf, byteOrder, elementType, lengthStack);
        }


        @Override
        public Integer convertTo(Boolean aBoolean, Length length) {
            return (null == aBoolean || !aBoolean) ? 0 : 1;
        }

        @Override
        public Boolean convertFrom(Integer i) {
            return i.equals(0) ? false : true;
        }
    }

    public static class BooleanLongTypeAdapter extends LongTypeAdapter<Boolean> {
        @Override
        public void write(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
            this.write(false, byteBuf, byteOrder, elementType, lengthStack);
        }


        @Override
        public Long convertTo(Boolean aBoolean, Length length) {
            return (null == aBoolean || !aBoolean) ? 0 : 1L;
        }

        @Override
        public Boolean convertFrom(Long i) {
            return i.equals(0) ? false : true;
        }
    }

    public static class BooleanBytesTypeAdapter extends BytesTypeAdapter<Boolean> {
        private final Function<Boolean, List<String>> textSupplier;
        private final Supplier<byte[]> paddingSupplier;

        public BooleanBytesTypeAdapter(Function<Boolean, List<String>> textSupplier,
                                       Supplier<byte[]> paddingSupplier) {
            this.textSupplier = textSupplier;
            this.paddingSupplier = paddingSupplier;
        }

        @Override
        public void write(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
            this.write(false, byteBuf, byteOrder, elementType, lengthStack);
        }


        @Override
        public byte[] convertTo(Boolean aBoolean, final Length length) {
            List<String> texts = textSupplier == null ? null : textSupplier.apply(null == aBoolean ? false : aBoolean);
            if (null == texts || texts.size() == 0) {
                throw new IllegalArgumentException("convert boolean to text: the text supplier must not empty!");
            }
            //
            byte[] textBytes = null;
            if(length.value() <= 0) {
                //未指定长度，取texts中的第一个就好
                textBytes = texts.get(0).getBytes(StandardCharsets.UTF_8);
            } else {
                textBytes = texts.stream().
                    filter(s -> !s.isEmpty()).
                    map(s -> s.getBytes(StandardCharsets.UTF_8)).
                    filter(bytes -> bytes.length <= length.value()).
                    findFirst().
                    orElseThrow(() -> new InvalidParameterException("convert boolean to text: the text provided by supplier do not compatible with lengthStack parameter"));
            }
            //
            int paddingLen = length.value() - textBytes.length;
            if (paddingLen <= 0) {
                //no need for padding
                return textBytes;
            }
            //do padding
            byte[] padding = paddingSupplier == null ? null : paddingSupplier.get();
            if (null == padding || padding.length == 0) {
                throw new IllegalArgumentException("convert boolean to text: the padding bytes is null");
            }

            return padding(textBytes, padding, length.value());
        }

        @Override
        public Boolean convertFrom(byte[] bytes) {
            String value = new String(bytes, StandardCharsets.UTF_8).trim();
            if(StringUtils.isEmpty(value)) {
                return false;
            }

            List<String> trueTexts = textSupplier == null ? null : textSupplier.apply(true);

            if (null != trueTexts) {
                for(String trueText : trueTexts) {
                    if(value.equals(trueText)) {
                        return true;
                    }
                }
            }

            List<String> falseTexts = textSupplier == null ? null : textSupplier.apply(false);
            if(null != falseTexts) {
                for(String falseText : falseTexts) {
                    if(value.equals(falseText)) {
                        return false;
                    }
                }
            }
            //TODO warning unknown boolean text, assume false!!!
            return false;
        }
    }

    /**
     * Number<->byte[],Integer,Long
     */
    public static class NumberIntegerTypeAdapter<T extends Number> extends IntegerTypeAdapter<T> {
        @Override
        public void write(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
            this.write((T)Integer.valueOf(0), byteBuf, byteOrder, elementType, lengthStack);
        }


        @Override
        public Integer convertTo(T t, Length length) {
            return null == t ? 0 : t.intValue();
        }

        @Override
        public T convertFrom(Integer i) {
            return (T) i;
        }
    }

    public static class NumberLongTypeAdapter<T extends Number> extends LongTypeAdapter<T> {
        @Override
        public void write(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
            this.write((T)Integer.valueOf(0), byteBuf, byteOrder, elementType, lengthStack);
        }


        @Override
        public Long convertTo(T t, Length length) {
            return null == t ? 0L : t.longValue();
        }

        @Override
        public T convertFrom(Long l) {
            return (T) l;
        }
    }

    public static class NumberBytesTypeAdapter<T extends Number> extends BytesTypeAdapter<T> {
        protected final Supplier<byte[]> paddingSupplier;

        public NumberBytesTypeAdapter(Supplier<byte[]> paddingSupplier) {
            this.paddingSupplier = paddingSupplier;
        }

        @Override
        public void write(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
            this.write((T)Integer.valueOf(0), byteBuf, byteOrder, elementType, lengthStack);
        }

        @Override
        public byte[] convertTo(T number, Length length) {
            if(length.value() <= 0) {
                //未指定长度，使用实际长度
                if(length.length() == LengthLengthEnum.NONE && null == number) {
                    //这种情况无法确定编码后的byte[] 中是否包含number
                    throw new IllegalArgumentException("convert number to byte[]: the number is null and length.length() is LengthLengthEnum.NONE");
                }
                if(null == number) {
                    return zeroLenBytes;
                } else {
                    return String.valueOf(number).getBytes(StandardCharsets.UTF_8);
                }
            } else {
                //指定了长度
                byte[] bytes = null;
                if(null == number) {
                    //直接填充
                    bytes = zeroLenBytes;
                } else {
                    bytes = String.valueOf(number).getBytes(StandardCharsets.UTF_8);
                    if (bytes.length > length.value()) {
                        throw new IllegalArgumentException("convert number to byte[]: parameter lengthStack short than the length of byte[] that converted from number value");
                    }
                }
                //
                int paddingLen = length.value() - bytes.length;
                if (paddingLen <= 0) {
                    //no need for padding
                    return bytes;
                }
                //do padding
                byte[] padding = paddingSupplier == null ? null : paddingSupplier.get();
                if (null == padding || padding.length == 0) {
                    throw new IllegalArgumentException("convert boolean to text: the padding bytes is null");
                }

                return padding(bytes, padding, length.value());
            }
        }

        @Override
        public T convertFrom(byte[] bytes) {
            String s = new String(bytes, StandardCharsets.UTF_8);
            return fromString(s);
        }

        public T fromString(String s) {
            Integer i = StringUtils.isEmpty(s) ? 0 : Integer.valueOf(s);
            return (T) i;
        }
    }

    /**
     * Byte<->byte[],Integer,Long
     */
    public static class ByteIntegerTypeAdapter extends NumberIntegerTypeAdapter<Byte> {
        @Override
        public Byte convertFrom(Integer i) {
            return i.byteValue();
        }
    }

    public static class ByteLongTypeAdapter extends NumberLongTypeAdapter<Byte> {
        @Override
        public Byte convertFrom(Long l) {
            return l.byteValue();
        }

    }

    public static class ByteBytesTypeAdapter extends NumberBytesTypeAdapter<Byte> {

        public ByteBytesTypeAdapter(Supplier<byte[]> paddingSupplier) {
            super(paddingSupplier);
        }

        @Override
        public Byte fromString(String s) {
            Byte b = StringUtils.isEmpty(s) ? 0 : Byte.valueOf(s);
            return b;
        }

    }

    /**
     * Short<->byte[],Integer,Long
     */
    public static class ShortIntegerTypeAdapter extends NumberIntegerTypeAdapter<Short> {
        @Override
        public Short convertFrom(Integer i) {
            return i.shortValue();
        }
    }

    public static class ShortLongTypeAdapter extends NumberLongTypeAdapter<Short> {
        @Override
        public Short convertFrom(Long l) {
            return l.shortValue();
        }
    }

    public static class ShortBytesTypeAdapter extends NumberBytesTypeAdapter<Short> {
        public ShortBytesTypeAdapter(Supplier<byte[]> paddingSupplier) {
            super(paddingSupplier);
        }


        @Override
        public Short fromString(String str) {
            Short s = StringUtils.isEmpty(str) ? 0 : Short.valueOf(str);
            return s;
        }

    }

    /**
     * Integer<->byte[],Integer,Long
     */
    public static class IntegerIntegerTypeAdapter extends NumberIntegerTypeAdapter<Integer> {
        @Override
        public Integer convertFrom(Integer i) {
            return i;
        }
    }

    public static class IntegerLongTypeAdapter extends NumberLongTypeAdapter<Integer> {
        @Override
        public Integer convertFrom(Long l) {
            return l.intValue();
        }
    }

    public static class IntegerBytesTypeAdapter extends NumberBytesTypeAdapter<Integer> {
        public IntegerBytesTypeAdapter(Supplier<byte[]> paddingSupplier) {
            super(paddingSupplier);
        }

        @Override
        public Integer fromString(String s) {
            return StringUtils.isEmpty(s) ? 0 : Integer.valueOf(s);
        }
    }

    /**
     * Long<->byte[],Integer,Long
     */
    public static class LongIntegerTypeAdapter extends NumberIntegerTypeAdapter<Long> {
        @Override
        public Long convertFrom(Integer i) {
            return i.longValue();
        }
    }

    public static class LongLongTypeAdapter extends NumberLongTypeAdapter<Long> {
        @Override
        public Long convertFrom(Long l) {
            return l;
        }
    }

    public static class LongBytesTypeAdapter extends NumberBytesTypeAdapter<Long> {
        public LongBytesTypeAdapter(Supplier<byte[]> paddingSupplier) {
            super(paddingSupplier);
        }

        @Override
        public Long fromString(String s) {
            return StringUtils.isEmpty(s) ? 0 : Long.valueOf(s);
        }
    }

    /**
     * String<->byte[],Integer,Long
     */
    public static class StringIntegerTypeAdapter extends IntegerTypeAdapter<String> {
        @Override
        public void write(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
            this.write("", byteBuf, byteOrder, elementType, lengthStack);
        }

        @Override
        public Integer convertTo(String s, Length length) {
            return StringUtils.isEmpty(s) ? 0 : Integer.valueOf(s);
        }

        @Override
        public String convertFrom(Integer i) {
            return String.valueOf(i);
        }
    }

    public static class StringLongTypeAdapter extends LongTypeAdapter<String> {

        @Override
        public void write(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
            this.write("", byteBuf, byteOrder, elementType, lengthStack);
        }

        @Override
        public Long convertTo(String s, Length length) {
            return StringUtils.isEmpty(s) ? 0 : Long.valueOf(s);
        }

        @Override
        public String convertFrom(Long l) {
            return String.valueOf(l);
        }
    }

    public static class StringBytesTypeAdapter extends BytesTypeAdapter<String> {
        protected final Supplier<byte[]> paddingSupplier;

        public StringBytesTypeAdapter(Supplier<byte[]> paddingSupplier) {
            this.paddingSupplier = paddingSupplier;
        }

        @Override
        public void write(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
            this.write("", byteBuf, byteOrder, elementType, lengthStack);
        }


        @Override
        public byte[] convertTo(String s, Length length) {
            if(length.value() <= 0) {
                //未指定长度的情况
                if(StringUtils.isEmpty(s) && length.length() == LengthLengthEnum.NONE) {
                    //这种情况无法确定编码后的byte[]中是否包含 s
                    throw new IllegalArgumentException("convert String to byte[]: string is empty and length.length() is LengthLengthEnum.NONE");
                }
                if(StringUtils.isEmpty(s)) {
                    return zeroLenBytes;
                } else {
                    return s.getBytes(StandardCharsets.UTF_8);
                }
            } else {
                //指定长度的情况
                byte[] bytes = StringUtils.isEmpty(s) ? zeroLenBytes : s.getBytes(StandardCharsets.UTF_8);
                if (bytes.length > length.value()) {
                    //不截断，而是抛异常
                    throw new IllegalArgumentException("convert String to byte[]: parameter length shorter than the length of byte[] that converted from String");
                }
                //
                int paddingLen = length.value() - bytes.length;
                if (paddingLen <= 0) {
                    //no need for padding
                    return bytes;
                }
                //do padding
                byte[] padding = paddingSupplier == null ? null : paddingSupplier.get();
                if (null == padding || padding.length == 0) {
                    throw new IllegalArgumentException("convert String to byte[]: the padding bytes is null");
                }

                return padding(bytes, padding, length.value());
            }
        }

        @Override
        public String convertFrom(byte[] bytes) {
            //remove '\0's
            return new String(bytes, StandardCharsets.UTF_8).replaceAll("\0+$", "");
        }
    }
}
