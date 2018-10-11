/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bignaga.codec.copyfromgson.reflect;

import com.bignaga.codec.LengthLengthEnum;
import com.bignaga.codec.MessageElementType;
import com.bignaga.codec.annotation.Length;
import com.bignaga.codec.copyfromgson.internal.$Gson$Types;
import com.bignaga.codec.exception.LengthException;
import com.bignaga.codec.utils.CodecUtils;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapt an array of objects.
 */
public final class ArrayTypeAdapter<E> extends TypeAdapter<Object> {


    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public <T> TypeAdapter<T> create(TypeToken<T> typeToken, MessageElementType targetElementType) {
            Type type = typeToken.getType();
            if (!(type instanceof GenericArrayType || type instanceof Class && ((Class<?>) type).isArray())) {
                return null;
            }

            Type componentType = $Gson$Types.getArrayComponentType(type);
            TypeAdapter<?> componentTypeAdapter = TypeAdapters.getAdapter(TypeToken.get(componentType), targetElementType);
            return new ArrayTypeAdapter(
                    componentTypeAdapter, $Gson$Types.getRawType(componentType));
        }
    };

    private final Class<E> componentType;
    private final TypeAdapter<E> componentTypeAdapter;

    public ArrayTypeAdapter(TypeAdapter<E> componentTypeAdapter, Class<E> componentType) {
        this.componentTypeAdapter =
                new TypeAdapterRuntimeTypeWrapper<E>(componentTypeAdapter, componentType);
        this.componentType = componentType;
    }

    @Override
    public void write(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
        this.write(null, byteBuf, byteOrder, elementType, lengthStack);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(Object array, ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
        if(ArrayUtils.isEmpty(lengthStack)) {
            throw new IllegalArgumentException("array field must have length specified!");
        }
        //
        int actualArrayLen = null == array ? 0 : Array.getLength(array);
        Length length = lengthStack[0];
        //如果指定了 length.value() > 0，则写入的元素个数为 length.value()；否则为数组实际的元素个数
        int arrayLen = length.value() <= 0 ? actualArrayLen : length.value();
        //
        CodecUtils.writeLength(byteBuf, byteOrder, length.length(), arrayLen);
        //
        if (arrayLen > 0) {
            lengthStack = ArrayUtils.subarray(lengthStack, 1, lengthStack.length);
            int count = Math.min(arrayLen, actualArrayLen);
            if(count > 0) {
                for (int ii = 0; ii < count; ii++) {
                    E value = (E) Array.get(array, ii);
                    componentTypeAdapter.write(value, byteBuf, byteOrder, elementType, lengthStack);
                }
            }
            //
            int paddingLen = arrayLen - count;
            if(paddingLen > 0) {
                for(int ii = 0; ii < paddingLen; ++ii) {
                    componentTypeAdapter.write(byteBuf, byteOrder, elementType, lengthStack);
                }
            }
        }
    }

    @Override
    public Object read(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
        if(ArrayUtils.isEmpty(lengthStack)) {
            throw new IllegalArgumentException("array field must have length specified!");
        }

        List<E> list = new ArrayList<>();
        Length length = lengthStack[0];
        //
        int arrayLen = 0;
        int discardLen = 0;
        if(length.length() == LengthLengthEnum.NONE) {
            //如果length.length() 为NONE，需要读取的元素个数由 length.value() 指定
            if(length.value() <= 0) {
                throw new IllegalArgumentException("if length.length() is NONE, length.value() must > 0");
            }
            arrayLen = length.value();
        } else {
            //否则，先读取实际元素个数，如果 length.value() <= 0，要读取的元素个数即为实际元素个数；
            arrayLen = CodecUtils.readLength(byteBuf, byteOrder, length.length());
            if(arrayLen < 0) {
                throw new LengthException("the length of array is < 0");
            }
            //否则，要读取的元素个数为指定的 length.value() 和 实际元素个数二者中较小的
            if(length.value() > 0) {
                //取二者中较小的
                discardLen = arrayLen - length.value();
                arrayLen = Math.min(arrayLen, length.value());
            }
        }
        //
        if (arrayLen > 0) {
            lengthStack = ArrayUtils.subarray(lengthStack, 1, lengthStack.length);
            for (int i = 0; i < arrayLen; ++i) {
                E instance = componentTypeAdapter.read(byteBuf, byteOrder, elementType, lengthStack);
                list.add(instance);
            }
            //需要丢弃的数据
            for (int i = 0; i < discardLen; ++i) {
                componentTypeAdapter.read(byteBuf, byteOrder, elementType, lengthStack);
            }
        }
        //
        int size = list.size();
        Object array = Array.newInstance(componentType, size);
        for (int i = 0; i < size; i++) {
            Array.set(array, i, list.get(i));
        }
        return array;
    }
}
