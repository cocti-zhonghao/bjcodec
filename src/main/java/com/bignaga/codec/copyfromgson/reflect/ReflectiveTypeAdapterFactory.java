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

import com.bignaga.codec.annotation.Length;
import com.bignaga.utils.ThrowAs;
import com.bignaga.codec.MessageElementType;
import com.bignaga.codec.annotation.MessageElement;
import com.bignaga.codec.copyfromgson.internal.*;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.ByteOrder;
import java.util.*;

/**
 * Type adapter that reflects over the fields and methods of a class.
 */
public final class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {
  private final ConstructorConstructor constructorConstructor;
  private final ReflectionAccessor accessor = ReflectionAccessor.getInstance();

  public ReflectiveTypeAdapterFactory(ConstructorConstructor constructorConstructor) {
    this.constructorConstructor = constructorConstructor;
  }

  @Override
  public <T> TypeAdapter<T> create(final TypeToken<T> type, MessageElementType elementType) {
    Class<? super T> raw = type.getRawType();

    if (!Object.class.isAssignableFrom(raw)) {
        // it's a primitive!
        return null;
    }

    ObjectConstructor<T> constructor = constructorConstructor.get(type);
    return new Adapter<T>(constructor, getBoundFields(type, raw));
  }

  private BoundField createBoundField(
          final Field field,
          final TypeToken<?> fieldType,
          final MessageElementType elementType,
          final ByteOrder byteOrder,
          final Length[] lengthStack,
          final int index) {
    final TypeAdapter<?> typeAdapter = TypeAdapters.getAdapter(fieldType, elementType);

    return new BoundField(field,
            elementType,
            byteOrder,
            lengthStack,
            index) {

      @Override
      public void write(Object value, ByteBuf byteBuf, ByteOrder byteOrder) {
        TypeAdapter ta = typeAdapter;
        ta.write(value, byteBuf, null == this.byteOrder ? byteOrder : this.byteOrder, this.elementType, this.lengthStack);
      }

      @Override
      public Object read(ByteBuf byteBuf, ByteOrder byteOrder) {
        TypeAdapter ta = typeAdapter;
        return ta.read(byteBuf, null == this.byteOrder ? byteOrder : this.byteOrder, this.elementType, this.lengthStack);
      }
    };
  }

  private List<BoundField> getBoundFields(TypeToken<?> type, Class<?> raw) {
    List<BoundField> result = new LinkedList<>();
    if (raw.isInterface()) {
      return result;
    }

    while (raw != Object.class) {
      Field[] fields = raw.getDeclaredFields();
      for (Field field : fields) {
        MessageElement annotation = field.getAnnotation(MessageElement.class);
        if(null == annotation) {
          continue;
        }
        accessor.makeAccessible(field);
        Type fieldType = $Gson$Types.resolve(type.getType(), raw, field.getGenericType());
        BoundField boundField = createBoundField(field,
                TypeToken.get(fieldType),
                annotation.type(),
                annotation.byteOrder().byteOrder,
                annotation.lengthStack(),
                annotation.index());
        result.add(boundField);
      }
      type = TypeToken.get($Gson$Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
      raw = type.getRawType();
    }
    //
    result.sort(Comparator.comparingInt(BoundField::getIndex));
    return result;
  }

  static abstract class BoundField {
    final Field field;
    final MessageElementType elementType;
    final Length[] lengthStack;
    final int index;
    final ByteOrder byteOrder;
    protected BoundField(Field field,
                         MessageElementType elementType,
                         ByteOrder byteOrder,
                         Length[] lengthStack,
                         int index) {
      this.elementType = elementType;
      this.lengthStack = lengthStack;
      this.index = index;
      this.field = field;
      this.byteOrder = byteOrder;
    }

//    public abstract void write(Object value, ByteBuf byteBuf, ByteOrder byteOrder, int[] lengthStack, MessageElementType elementType);
//
//    public abstract Object read(ByteBuf byteBuf, ByteOrder byteOrder, int[] lengthStack, MessageElementType elementType);

    public abstract void write(Object value, ByteBuf byteBuf, ByteOrder byteOrder);

    public abstract Object read(ByteBuf byteBuf, ByteOrder byteOrder);

    int getIndex() {return index;}
    Object getFieldValue(Object o) {
      try {
        return this.field.get(o);
      } catch (IllegalAccessException e) {
        ThrowAs.ThrowAsRuntimeException(e);
      }
      return null;
    }
    void setFieldVlaue(Object o, Object v) {
      try {
        this.field.set(o, v);
      } catch (IllegalAccessException e) {
        ThrowAs.ThrowAsRuntimeException(e);
      }
    }
  }

  public static final class Adapter<T> extends TypeAdapter<T> {
    private final ObjectConstructor<T> constructor;
    private final List<BoundField> boundFields;

    Adapter(ObjectConstructor<T> constructor, List<BoundField> boundFields) {
      this.constructor = constructor;
      this.boundFields = boundFields;
    }


    @Override
    public void write(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
      //TODO 优化
      T t = this.constructor.construct();
      this.write(t, byteBuf, byteOrder, elementType, lengthStack);
    }

    @Override
    public void write(T value, ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
      for (BoundField boundField : boundFields) {
        boundField.write(boundField.getFieldValue(value), byteBuf, byteOrder);
      }
    }

    @Override
    public T read(ByteBuf byteBuf, ByteOrder byteOrder, MessageElementType elementType, Length[] lengthStack) {
      T t = this.constructor.construct();
      for (BoundField boundField : boundFields) {
        boundField.setFieldVlaue(t, boundField.read(byteBuf, byteOrder));
      }
      return t;
    }
  }
}
