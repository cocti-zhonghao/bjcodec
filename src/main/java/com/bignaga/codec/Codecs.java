/**
 * @project lbsgap
 * @file Codecs.java
 * @package com.bignaga.codec
 * @author zhonghao
 * @date 2018/9/28 11:39
 * @copyright bignaga
 */
package com.bignaga.codec;

import com.bignaga.codec.annotation.Message;
import io.netty.buffer.ByteBuf;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author zhonghao
 * @date 2018/9/28 11:39
 * @see
 * @since
 */
public class Codecs {
    private static Logger logger = LoggerFactory.getLogger(Codecs.class);

    private static final Map<Integer, Class<?>> codecMap = new ConcurrentHashMap<>(256);

    public static void scan(String ... basePackages) {
        if(null == basePackages || basePackages.length == 0) {
            return;
        }
        //
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(basePackages)
                .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner())
                .filterInputsBy(new FilterBuilder().includePackage(basePackages)))
                ;
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Message.class);
        for(Class<?> clazz : annotated) {
            Set<Annotation> annotations = ReflectionUtils.getAnnotations(clazz, a -> a instanceof Message);
            Message m = (Message)annotations.iterator().next();
            codecMap.put(m.value(), clazz);
            logger.debug("map messageType -> messageClass: {} -> {}", m.value(), clazz.getName());
        }
    }

    public static <T> T decode(int messagId, ByteBuf byteBuf) {
        return (T)Codec.decode(byteBuf, codecMap.get(messagId));

    }
}
