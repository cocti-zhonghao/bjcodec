/**
 * @project lbsgap
 * @file ReflectionUtils.java
 * @package com.bignaga.utils
 * @author zhonghao
 * @date 2018/6/4 13:55
 * @copyright bignaga
 */
package com.bignaga.utils;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.reflections.ReflectionUtils.getAllFields;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author zhonghao
 * @date 2018/6/4 13:55
 * @see
 * @since
 */
public class ReflectionUtils {
    public static <A extends Annotation, T extends AnnotatedElement> List<Annotation> getAllAnnotations(T type, Class<A> annotaionClazz) {
        Set<Annotation> annotations = org.reflections.ReflectionUtils.getAllAnnotations(type, a -> annotaionClazz.isAssignableFrom(a.getClass()));
        return annotations.stream().collect(Collectors.toList());
    }


    public static <A extends Annotation,T> Stream<Pair<A, Field>> getAllAnnotatedField(Class<T> clazz, Class<A> annotaionClazz) {
        Set<Field> fields = getAllFields(clazz);
        return fields.stream().
                map(f -> {
                    List<Annotation> annotations = ReflectionUtils.getAllAnnotations(f, annotaionClazz);
                    Optional<A> annotation = annotations.stream().
                            map(a -> (A)a).
                            findFirst();
                    Pair<A, Field> pair = new ImmutablePair<>(annotation.isPresent()? annotation.get() : null, f);
                    return pair;
                }).
                filter(p -> p.getLeft() != null);
    }

}
