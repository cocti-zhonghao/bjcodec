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


import com.bignaga.codec.MessageElementType;
import com.bignaga.codec.copyfromgson.internal.ConstructorConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.bignaga.codec.MessageElementType.*;

/**
 * Type adapters for basic types.
 */
public final class TypeAdapters {
    private TypeAdapters() {
        throw new UnsupportedOperationException();
    }

    public static final List<String> trueText = Arrays.asList("true", "TRUE", "ON", "on", "1");
    public static final List<String> falseText = Arrays.asList("false", "FALSE", "OFF", "off", "0");
    public static final byte[] padding8 = {0, 0, 0, 0, 0, 0, 0, 0};
    public static final Function<Boolean, List<String>> textSupplier = (aBoolean) -> aBoolean ? trueText : falseText;
    public static final Supplier<byte[]> paddingSupplier = () -> padding8;

    /**
     * Reserve
     */
    public static final TypeAdapter.ReserveTypeAdapter RESERVE_TYPE_ADAPTER = new TypeAdapter.ReserveTypeAdapter();
    /**
     * Boolean
     */
    public static final TypeAdapter.BooleanIntegerTypeAdapter BOOLEAN_INTEGER = new TypeAdapter.BooleanIntegerTypeAdapter();
    public static final TypeAdapter.BooleanLongTypeAdapter BOOLEAN_LONG = new TypeAdapter.BooleanLongTypeAdapter();
    public static final TypeAdapter.BooleanBytesTypeAdapter BOOLEAN_BYTES = new TypeAdapter.BooleanBytesTypeAdapter(textSupplier, paddingSupplier);
    /**
     * Number
     */
    public static final TypeAdapter.NumberIntegerTypeAdapter<Number> NUMBER_INTEGER = new TypeAdapter.NumberIntegerTypeAdapter();
    public static final TypeAdapter.NumberLongTypeAdapter<Number> NUMBER_LONG = new TypeAdapter.NumberLongTypeAdapter();
    public static final TypeAdapter.NumberBytesTypeAdapter<Number> NUMBER_BYTES = new TypeAdapter.NumberBytesTypeAdapter(paddingSupplier);
    /**
     * Byte
     */
    public static final TypeAdapter.ByteIntegerTypeAdapter BYTE_INTEGER = new TypeAdapter.ByteIntegerTypeAdapter();
    public static final TypeAdapter.ByteLongTypeAdapter BYTE_LONG = new TypeAdapter.ByteLongTypeAdapter();
    public static final TypeAdapter.ByteBytesTypeAdapter BYTE_BYTES = new TypeAdapter.ByteBytesTypeAdapter(paddingSupplier);
    /**
     * Short
     */
    public static final TypeAdapter.ShortIntegerTypeAdapter SHORT_INTEGER = new TypeAdapter.ShortIntegerTypeAdapter();
    public static final TypeAdapter.ShortLongTypeAdapter SHORT_LONG = new TypeAdapter.ShortLongTypeAdapter();
    public static final TypeAdapter.ShortBytesTypeAdapter SHORT_BYTES = new TypeAdapter.ShortBytesTypeAdapter(paddingSupplier);
    /**
     * Integer
     */
    public static final TypeAdapter.IntegerIntegerTypeAdapter INTEGER_INTEGER = new TypeAdapter.IntegerIntegerTypeAdapter();
    public static final TypeAdapter.IntegerLongTypeAdapter INTEGER_LONG = new TypeAdapter.IntegerLongTypeAdapter();
    public static final TypeAdapter.IntegerBytesTypeAdapter INTEGER_BYTES = new TypeAdapter.IntegerBytesTypeAdapter(paddingSupplier);
    /**
     * Long
     */
    public static final TypeAdapter.LongIntegerTypeAdapter LONG_INTEGER = new TypeAdapter.LongIntegerTypeAdapter();
    public static final TypeAdapter.LongLongTypeAdapter LONG_LONG = new TypeAdapter.LongLongTypeAdapter();
    public static final TypeAdapter.LongBytesTypeAdapter LONG_BYTES = new TypeAdapter.LongBytesTypeAdapter(paddingSupplier);
    /**
     * String
     */
    public static final TypeAdapter.StringIntegerTypeAdapter STRING_INTEGER = new TypeAdapter.StringIntegerTypeAdapter();
    public static final TypeAdapter.StringLongTypeAdapter STRING_LONG = new TypeAdapter.StringLongTypeAdapter();
    public static final TypeAdapter.StringBytesTypeAdapter STRING_BYTES = new TypeAdapter.StringBytesTypeAdapter(paddingSupplier);


    public static final TypeAdapterFactory BOOLEAN_FACTORY
            = newFactory(boolean.class, Boolean.class, BOOLEAN_INTEGER, BOOLEAN_LONG, BOOLEAN_BYTES);

    public static final TypeAdapterFactory BYTE_FACTORY
            = newFactory(byte.class, Byte.class, BYTE_INTEGER, BYTE_LONG, BYTE_BYTES);

    public static final TypeAdapterFactory SHORT_FACTORY
            = newFactory(short.class, Short.class, SHORT_INTEGER, SHORT_LONG, SHORT_BYTES);

    public static final TypeAdapterFactory INTEGER_FACTORY
            = newFactory(int.class, Integer.class, INTEGER_INTEGER, INTEGER_LONG, INTEGER_BYTES);

    public static final TypeAdapterFactory NUMBER_FACTORY =
            newFactory(Number.class, NUMBER_INTEGER, NUMBER_LONG, NUMBER_BYTES);

    public static final TypeAdapterFactory STRING_FACTORY =
            newFactory(String.class, STRING_INTEGER, STRING_LONG, STRING_BYTES);

    private static final Map<Type, InstanceCreator<?>> instanceCreators = Collections.emptyMap();
    private static final ConstructorConstructor constructorConstructor = new ConstructorConstructor(instanceCreators);

    public static final TypeAdapterFactory REFLECTIVE_TYPE_FACTORY = new ReflectiveTypeAdapterFactory(constructorConstructor);

    private static final TypeAdapterFactory COLLECTION_TYPE_FACTORY = new CollectionTypeAdapterFactory(constructorConstructor);

    private static final TypeAdapterFactory ARRAY_FACTORY = ArrayTypeAdapter.FACTORY;

    public static <TT> TypeAdapterFactory newFactory(
            final Class<TT> type,
            final TypeAdapter<? super TT> typeAdapterInteger,
            final TypeAdapter<? super TT> typeAdapterLong,
            final TypeAdapter<? super TT> typeAdapterBytes) {
        return new TypeAdapterFactory() {
            @SuppressWarnings("unchecked") // we use a runtime check toByteBufValue make sure the 'T's equal
            @Override
            public <T> TypeAdapter<T> create(TypeToken<T> typeToken, MessageElementType elementType) {
                if (typeToken.getRawType() == type) {
                    if (elementType == U8 ||
                            elementType == S8 ||
                            elementType == U16 ||
                            elementType == S16 || elementType == S32) {
                        return (TypeAdapter<T>) typeAdapterInteger;
                    }
                    if (elementType == U32 || elementType == MessageElementType.LONG) {
                        return (TypeAdapter<T>) typeAdapterLong;
                    } if (elementType == BYTES) {
                        return (TypeAdapter<T>) typeAdapterBytes;
                    }
                    return null;
                } else {
                    return null;
                }
            }

            @Override
            public String toString() {
                return "Factory[type=" + type.getName() + ",adapterInteger=" + typeAdapterInteger + "]"
                        + ",adapterLong=" + typeAdapterLong + "]"
                        + ",adapterBytes=" + typeAdapterBytes + "]";
            }
        };
    }

    public static <TT> TypeAdapterFactory newFactory(
            final Class<TT> unboxed,
            final Class<TT> boxed,
            final TypeAdapter<? super TT> typeAdapterInteger,
            final TypeAdapter<? super TT> typeAdapterLong,
            final TypeAdapter<? super TT> typeAdapterBytes) {
        return new TypeAdapterFactory() {
            @SuppressWarnings("unchecked") // we use a runtime check toByteBufValue make sure the 'T's equal
            @Override
            public <T> TypeAdapter<T> create(TypeToken<T> typeToken, MessageElementType elementType) {
                Class<? super T> rawType = typeToken.getRawType();
                if (rawType == unboxed || rawType == boxed) {
                    if (elementType == U8 ||
                            elementType == S8 ||
                            elementType == U16 ||
                            elementType == S16 || elementType == S32) {
                        return (TypeAdapter<T>) typeAdapterInteger;
                    }
                    if (elementType == U32 || elementType == MessageElementType.LONG) {
                        return (TypeAdapter<T>) typeAdapterLong;
                    }
                    if (elementType == BYTES) {
                        return (TypeAdapter<T>) typeAdapterBytes;
                    }
                    return null;
                } else {
                    return null;
                }
            }

            @Override
            public String toString() {
                return "Factory[type=" + boxed.getName()
                        + "+" + unboxed.getName() + ",adapterInteger=" + typeAdapterInteger + "]"
                        + ",adapterLong=" + typeAdapterLong + "]"
                        + ",adapterBytes=" + typeAdapterBytes + "]";
            }
        };
    }


    private final static List<TypeAdapterFactory> factories = new ArrayList<TypeAdapterFactory>();
    private final static TypeToken<?> NULL_KEY_SURROGATE = TypeToken.get(Object.class);
    private final static Map<Pair<TypeToken<?>, MessageElementType>, TypeAdapter<?>> typeTokenCache = new ConcurrentHashMap<>();

    static {
        factories.add(BOOLEAN_FACTORY);
        factories.add(BYTE_FACTORY);
        factories.add(SHORT_FACTORY);
        factories.add(INTEGER_FACTORY);
        factories.add(NUMBER_FACTORY);
        factories.add(STRING_FACTORY);
        factories.add(ARRAY_FACTORY);
        factories.add(COLLECTION_TYPE_FACTORY);
        factories.add(REFLECTIVE_TYPE_FACTORY);
    }



    private final static TypeAdapter<?> getFromCache(TypeToken<?> type, MessageElementType elementType) {
        return typeTokenCache.get(ImmutablePair.of(type == null ? NULL_KEY_SURROGATE : type, elementType));
    }


    private final static void putToCache(TypeToken<?> type, MessageElementType elementType, TypeAdapter<?> typeAdapter) {
        typeTokenCache.put(ImmutablePair.of(type, elementType), typeAdapter);
    }


    /**
     * Returns the type adapter for {@code} type.
     *
     * @throws IllegalArgumentException if this GSON cannot serialize and
     *                                  deserialize {@code type}.
     */
    @SuppressWarnings("unchecked")
    public static <T> TypeAdapter<T> getAdapter(TypeToken<T> type, MessageElementType elementType) {
        if (elementType == RESERVE) {
            return (TypeAdapter<T>) RESERVE_TYPE_ADAPTER;
        }
        //
        TypeAdapter<?> cached = getFromCache(type, elementType);
        if (cached != null) {
            return (TypeAdapter<T>) cached;
        }

        for (TypeAdapterFactory factory : factories) {
            TypeAdapter<T> candidate = factory.create(type, elementType);
            if (candidate != null) {
                putToCache(type, elementType, candidate);
                return candidate;
            }
        }
        throw new IllegalArgumentException("TypeAdapters cannot handle " + type + ":" + elementType);

    }
}