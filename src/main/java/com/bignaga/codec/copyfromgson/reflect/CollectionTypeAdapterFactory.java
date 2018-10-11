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
import com.bignaga.codec.copyfromgson.internal.ConstructorConstructor;
import com.bignaga.codec.copyfromgson.internal.ObjectConstructor;
import com.bignaga.codec.exception.LengthException;
import com.bignaga.codec.utils.CodecUtils;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Type;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Iterator;

/**
 * Adapt a homogeneous collection of objects.
 */
public final class CollectionTypeAdapterFactory implements TypeAdapterFactory {
    private final ConstructorConstructor constructorConstructor;

    public CollectionTypeAdapterFactory(ConstructorConstructor constructorConstructor) {
        this.constructorConstructor = constructorConstructor;
    }

    @Override
    public <T> TypeAdapter<T> create(TypeToken<T> typeToken, MessageElementType targetElementType) {
        Type type = typeToken.getType();

        Class<? super T> rawType = typeToken.getRawType();
        if (!Collection.class.isAssignableFrom(rawType)) {
            return null;
        }

        Type elementType = $Gson$Types.getCollectionElementType(type, rawType);
        TypeAdapter<?> elementTypeAdapter = TypeAdapters.getAdapter(TypeToken.get(elementType), targetElementType);
        ObjectConstructor<T> constructor = constructorConstructor.get(typeToken);

        @SuppressWarnings({"unchecked", "rawtypes"}) // create() doesn't define a type parameter
                TypeAdapter<T> result = new Adapter(elementType, elementTypeAdapter, constructor);
        return result;
    }

    private static final class Adapter<E> extends TypeAdapter<Collection<E>> {
        private final TypeAdapter<E> elementTypeAdapter;
        private final ObjectConstructor<? extends Collection<E>> constructor;

        public Adapter(Type elementType,
                       TypeAdapter<E> elementTypeAdapter,
                       ObjectConstructor<? extends Collection<E>> constructor) {
            this.elementTypeAdapter =
                    new TypeAdapterRuntimeTypeWrapper<E>(elementTypeAdapter, elementType);
            this.constructor = constructor;
        }

        @Override
        public Collection<E> read(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
            if (ArrayUtils.isEmpty(lengthStack)) {
                throw new IllegalArgumentException("collection field must have length specified!");
            }

            Collection<E> collection = constructor.construct();
            //
            Length length = lengthStack[0];
            int collLen = 0;
            int discardLen = 0;
            if (length.length() == LengthLengthEnum.NONE) {
                //如果length.length() 为NONE，需要读取的元素个数由 length.value() 指定
                if (length.value() <= 0) {
                    throw new IllegalArgumentException("if length.length() is NONE, length.value() must > 0");
                }
                collLen = length.value();
            } else {
                //否则，先读取实际元素个数，如果 length.value() <= 0，要读取的元素个数即为实际元素个数；
                collLen = CodecUtils.readLength(byteBuf, byteOrder, length.length());
                if (collLen < 0) {
                    throw new LengthException("the length of collection is < 0");
                }
                //否则，要读取的元素个数为指定的 length.value() 和 实际元素个数二者中较小的
                if (length.value() > 0) {
                    //取二者中较小的
                    discardLen = collLen - length.value();
                    collLen = Math.min(collLen, length.value());
                }
            }
            //
            if (collLen > 0) {
                lengthStack = ArrayUtils.subarray(lengthStack, 1, lengthStack.length);
                for (int ii = 0; ii < collLen; ++ii) {
                    collection.add(elementTypeAdapter.read(byteBuf, byteOrder, elementType, lengthStack));
                }
                //需要丢弃的数据
                for (int i = 0; i < discardLen; ++i) {
                    elementTypeAdapter.read(byteBuf, byteOrder, elementType, lengthStack);
                }
            }
            return collection;
        }


        @Override
        public void write(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
            this.write(null, byteBuf, byteOrder, elementType, lengthStack);
        }

        @Override
        public void write(Collection<E> collection, ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
            if(ArrayUtils.isEmpty(lengthStack)) {
                throw new IllegalArgumentException("collection field must have length specified!");
            }
            //
            int actualCollLen = null == collection ? 0 : collection.size();
            Length length = lengthStack[0];
            //如果指定了 length.value() > 0，则写入的元素个数为 length.value()；否则为数组实际的元素个数
            int collLen = length.value() <= 0 ? actualCollLen : length.value();
            //
            CodecUtils.writeLength(byteBuf, byteOrder, length.length(), collLen);
            //
            if (collLen > 0) {
                lengthStack = ArrayUtils.subarray(lengthStack, 1, lengthStack.length);
                int count = Math.min(collLen, actualCollLen);
                for(int ii = 0; ii < count; ++ii) {

                }
                if(count > 0) {
                    Iterator<E> iterator = collection.iterator();
                    elementTypeAdapter.write(iterator.next(), byteBuf, byteOrder, elementType, lengthStack);
                }
                //
                int paddingLen = collLen - count;
                if(paddingLen > 0) {
                    for(int ii = 0; ii < paddingLen; ++ii) {
                        elementTypeAdapter.write(byteBuf, byteOrder, elementType, lengthStack);
                    }
                }
            }
        }
    }
}
