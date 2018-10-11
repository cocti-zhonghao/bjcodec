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


import com.bignaga.codec.MessageElementType;
import com.bignaga.codec.annotation.Length;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.ByteOrder;

final class TypeAdapterRuntimeTypeWrapper<T> extends TypeAdapter<T> {
  private final TypeAdapter<T> delegate;
  private final Type type;

  TypeAdapterRuntimeTypeWrapper(TypeAdapter<T> delegate, Type type) {
    this.delegate = delegate;
    this.type = type;
  }

  @Override
  public void write(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
      //TODO check this !!!
      // Order of preference for choosing type adapters
      // First preference: a type adapter registered for the runtime type
      // Second preference: a type adapter registered for the declared type
      // Third preference: reflective type adapter for the runtime type (if it is a sub class of the declared type)
      // Fourth preference: reflective type adapter for the declared type

      TypeAdapter chosen = delegate;
      Type runtimeType = getRuntimeTypeIfMoreSpecific(type, null);
      if (runtimeType != type) {
          TypeAdapter runtimeTypeAdapter = TypeAdapters.getAdapter(TypeToken.get(runtimeType), elementType);
          if (!(runtimeTypeAdapter instanceof ReflectiveTypeAdapterFactory.Adapter)) {
              // The user registered a type adapter for the runtime type, so we will use that
              chosen = runtimeTypeAdapter;
          } else if (!(delegate instanceof ReflectiveTypeAdapterFactory.Adapter)) {
              // The user registered a type adapter for Base class, so we prefer it over the
              // reflective type adapter for the runtime type
              chosen = delegate;
          } else {
              // Use the type adapter for runtime type
              chosen = runtimeTypeAdapter;
          }
      }
      chosen.write(byteBuf, byteOrder, elementType, lengthStack);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public void write(T value, ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
    // Order of preference for choosing type adapters
    // First preference: a type adapter registered for the runtime type
    // Second preference: a type adapter registered for the declared type
    // Third preference: reflective type adapter for the runtime type (if it is a sub class of the declared type)
    // Fourth preference: reflective type adapter for the declared type

    TypeAdapter chosen = delegate;
    Type runtimeType = getRuntimeTypeIfMoreSpecific(type, value);
    if (runtimeType != type) {
      TypeAdapter runtimeTypeAdapter = TypeAdapters.getAdapter(TypeToken.get(runtimeType), elementType);
      if (!(runtimeTypeAdapter instanceof ReflectiveTypeAdapterFactory.Adapter)) {
        // The user registered a type adapter for the runtime type, so we will use that
        chosen = runtimeTypeAdapter;
      } else if (!(delegate instanceof ReflectiveTypeAdapterFactory.Adapter)) {
        // The user registered a type adapter for Base class, so we prefer it over the
        // reflective type adapter for the runtime type
        chosen = delegate;
      } else {
        // Use the type adapter for runtime type
        chosen = runtimeTypeAdapter;
      }
    }
    chosen.write(value, byteBuf, byteOrder, elementType, lengthStack);
  }

  @Override
  public T read(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
    return delegate.read(byteBuf, byteOrder, elementType, lengthStack);
  }

  /**
   * Finds a compatible runtime type if it is more specific
   */
  private Type getRuntimeTypeIfMoreSpecific(Type type, Object value) {
    if (value != null
        && (type == Object.class || type instanceof TypeVariable<?> || type instanceof Class<?>)) {
      type = value.getClass();
    }
    return type;
  }
}
