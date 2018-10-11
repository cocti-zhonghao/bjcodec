/**
 * @project lbsgap
 * @file Codec.java
 * @package com.bignaga.codec
 * @author zhonghao
 * @date 2018/6/12 17:11
 * @copyright bignaga
 */
package com.bignaga.codec;

import com.bignaga.codec.annotation.Length;
import com.bignaga.utils.ReflectionUtils;
import com.bignaga.utils.ThrowAs;
import com.bignaga.codec.annotation.Message;
import com.bignaga.codec.copyfromgson.internal.Primitives;
import com.bignaga.codec.copyfromgson.reflect.TypeAdapter;
import com.bignaga.codec.copyfromgson.reflect.TypeAdapters;
import com.bignaga.codec.copyfromgson.reflect.TypeToken;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.ByteOrder;
import java.util.List;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author zhonghao
 * @date 2018/6/12 17:11
 * @see
 * @since
 */
public class Codec {
    public static <T> T decode(ByteBuf byteBuf, Class<T> classOfT, ByteOrder byteOrder, Length[] lengthStack, MessageElementType elementType) {
        Object object = decode(byteBuf, (Type) classOfT, byteOrder, lengthStack, elementType);
        return Primitives.wrap(classOfT).cast(object);
    }

    public static <T> T decode(ByteBuf byteBuf, Type typeOfT, ByteOrder byteOrder, Length[] lengthStack, MessageElementType elementType) {
        if (byteBuf == null || !byteBuf.isReadable()) {
            return null;
        }
        T target = (T) fromByteBuf(byteBuf, typeOfT, byteOrder, lengthStack, elementType);
        return target;
    }

    @SuppressWarnings("unchecked")
    private static <T> T fromByteBuf(ByteBuf byteBuf, Type typeOfT, ByteOrder byteOrder, Length[] lengthStack, MessageElementType elementType) {
        //TODO
        TypeToken<T> typeToken = (TypeToken<T>) TypeToken.get(typeOfT);
        TypeAdapter<T> typeAdapter = TypeAdapters.getAdapter(typeToken, elementType);
        T object = typeAdapter.read(byteBuf, byteOrder, elementType, lengthStack);
        return object;
    }

    public static ByteBuf encode(Object o, ByteOrder byteOrder, Length[] lengthStack, MessageElementType elementType) {
        if(o == null) {
            return null;
        }
        return encode(o, o.getClass(), byteOrder, lengthStack, elementType);
    }

    private static ByteBuf encode(Object src, Type typeOfSrc, ByteOrder byteOrder, Length[] lengthStack, MessageElementType elementType) {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(64);
        try {
            encode(byteBuf, src, typeOfSrc, byteOrder, lengthStack, elementType);
        } catch (Exception e) {
            byteBuf.release();
            ThrowAs.ThrowAsRuntimeException(e);
        }
        return byteBuf;
    }

    public static void encode(ByteBuf byteBuf, Object o, ByteOrder byteOrder, Length[] lengthStack, MessageElementType elementType) {
        if(o == null) {
            return;
        }
        encode(byteBuf, o, o.getClass(), byteOrder, lengthStack, elementType);
    }

    private static void encode(ByteBuf byteBuf, Object src, Type typeOfSrc, ByteOrder byteOrder, Length[] lengthStack, MessageElementType elementType) {
        TypeAdapter<?> adapter = TypeAdapters.getAdapter(TypeToken.get(typeOfSrc), elementType);
        ((TypeAdapter<Object>) adapter).write(src, byteBuf, byteOrder, elementType, lengthStack);
    }

    private static Length dummyLength = new Length() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return Length.class;
        }

        @Override
        public int value() {
            return 0;
        }

        @Override
        public LengthLengthEnum length() {
            return LengthLengthEnum.NONE;
        }

    };
    private static Length[] dummyLengthStack = new Length[] {dummyLength};
    public static <T> T decode(ByteBuf byteBuf, Class<T> classOfT) {
        List<Annotation> annotations = ReflectionUtils.getAllAnnotations(classOfT, Message.class);
        Message annotation = annotations.isEmpty() ? null : (Message) annotations.get(0);
        ByteOrder byteOrder = null == annotation ? ByteOrder.BIG_ENDIAN : annotation.byteOrder().byteOrder;
        T t = Codec.decode(byteBuf, classOfT, byteOrder, dummyLengthStack, MessageElementType.STRUCT);
        return t;
    }

    public static <T> void encode(T t, ByteBuf byteBuf) {
        List<Annotation> annotations = ReflectionUtils.getAllAnnotations(t.getClass(), Message.class);
        Message annotation = annotations.isEmpty() ? null : (Message) annotations.get(0);
        ByteOrder byteOrder = null == annotation ? ByteOrder.BIG_ENDIAN : annotation.byteOrder().byteOrder;
        Codec.encode(byteBuf, t, byteOrder, dummyLengthStack, MessageElementType.STRUCT);
    }

}
