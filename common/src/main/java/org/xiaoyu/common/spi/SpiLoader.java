package org.xiaoyu.common.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.xiaoyu.common.serializer.gumiSerializer.Serializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SpiLoader
 * SPI加载器
 */
@Slf4j
public class SpiLoader {
    // 存储已加载的SPI实现类的映射
    private static final Map<String, Map<String, Class<? extends Serializer>>> loadedSpiMap = new ConcurrentHashMap<>();

    // 缓存实例，避免重复实例化
    private static final Map<String, Serializer> instanceCache = new ConcurrentHashMap<>();

    // SPI配置文件路径
    private static final String SPI_CONFIG_PATH = "META-INF/serializer/";

    /**
     * 加载指定接口的SPI实现类
     */
    public static void loadSpi(Class<?> serviceInterface) {
        String interfaceName = serviceInterface.getName();

        // 如果已经加载过，则直接返回
        if (loadedSpiMap.containsKey(interfaceName)) {
            return;
        }

        Map<String, Class<? extends Serializer>> keyClassMap = new HashMap<>();

        // 读取SPI配置文件
        List<URL> resources = ResourceUtil.getResources(SPI_CONFIG_PATH + interfaceName);
        for (URL resource : resources) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty() && !line.startsWith("#")) {
                        String[] parts = line.split("=");
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String className = parts[1].trim();
                            Class<?> implClass = Class.forName(className);
                            if (serviceInterface.isAssignableFrom(implClass)) {
                                keyClassMap.put(key, (Class<? extends Serializer>) implClass);
                            }
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                log.error("Failed to load SPI resource: " + resource, e);
            }
        }

        loadedSpiMap.put(interfaceName, keyClassMap);
    }

    /**
     * 根据接口和 key 获取 SPI 实现类实例
     *
     * @param serviceInterface 接口类
     * @param key 序列化器的 key
     * @param <T> 接口类型
     * @return 对应的 SPI 实现类实例
     */
    public static <T> T getInstance(Class<T> serviceInterface, String key) {
        String interfaceName = serviceInterface.getName();
        Map<String, Class<? extends Serializer>> keyClassMap = loadedSpiMap.get(interfaceName);

        if (keyClassMap == null) {
            throw new RuntimeException("SPI not loaded for " + interfaceName);
        }

        Class<? extends Serializer> implClass = keyClassMap.get(key);
        if (implClass == null) {
            throw new RuntimeException("No SPI implementation found for key " + key);
        }

        // 从缓存中获取实例，如果不存在则创建
        String implClassName = implClass.getName();
        if (!instanceCache.containsKey(implClassName)) {
            try {
                instanceCache.put(implClassName, implClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Failed to instantiate SPI implementation: " + implClassName, e);
            }
        }

        return (T) instanceCache.get(implClassName);
    }
}
