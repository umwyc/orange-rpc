package com.wyc.orange.rpc.core.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.wyc.orange.rpc.core.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SpiLoader {

    /**
     * 存储已经被加载的类: 接口全限定名名 => (key => 实现类)
     */
    private static Map<String, Map<String, Class<?>>> loaderMap = new ConcurrentHashMap<>();

    /**
     * 存储类的实例对象: 类全限定名 => 类的单例对象
     */
    private static Map<String, Object> instanceCache = new ConcurrentHashMap<>();

    /**
     * 系统目录
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    /**
     * 用户目录
     */
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIR = new String[]{RPC_SYSTEM_SPI_DIR, RPC_CUSTOM_SPI_DIR};

    /**
     * 动态加载的类的列表
     */
    private static final List<Class<?>> classList = Arrays.asList(Serializer.class);

    /**
     * 加载所有类型
     */
    public static void loadAll(){
        log.info("加载所有 SPI");
        for (Class<?> loadClass : classList) {
            load(loadClass);
        }
    }

    /**
     * 加载指定类型
     *
     * @param loadClass
     * @return
     */
    public static Map<String, Class<?>> load(Class<?> loadClass){
        log.info("加载类型为 {} 的SPI", loadClass.getName());
        Map<String, Class<?>> keyClassMap = new HashMap<>();
        // 扫描路径
        for (String scanDir : SCAN_DIR) {
            // 根据接口的全限定名在resources目录下加载实现类
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());
            for (URL resource : resources) {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    String line;

                    while ((line = reader.readLine()) != null) {
                        String[] split = line.split("=");
                        if(split.length > 1){
                            String key = split[0];
                            String className = split[1];
                            log.info("加载实现类: key={},className={}", key, className);
                            keyClassMap.put(key, Class.forName(className));
                        }
                    }
                } catch (Exception e) {
                    log.error("spi resource load error", e);
                }
            }
        }
        // 加载完成后将信息存储至loaderMap中
        loaderMap.put(loadClass.getName(), keyClassMap);
        return keyClassMap;
    }

    /**
     * 获取某个接口的实例
     *
     * @param clazz
     * @param key
     * @return
     * @param <T>
     */
    public static <T> T getInstance(Class<?> clazz, String key){
        Map<String, Class<?>> keyClassMap = loaderMap.get(clazz.getName());
        if(keyClassMap == null){
            throw new RuntimeException(String.format("SpiLoader 未加载 %s 类型", clazz.getName()));
        }
        if(!keyClassMap.containsKey(key)){
            throw new RuntimeException(String.format("SpiLoader的 %s 不含有 key=%s 类型", clazz.getName(), key));
        }

        // 获取要加载的实现类型
        Class<?> implClass = keyClassMap.get(key);
        // 从实例缓存中加载指定的类型
        String implClassName = implClass.getName();
        if(!instanceCache.containsKey(implClassName)){
            try {
                instanceCache.put(implClassName, implClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                String errorMessage = String.format("%s 实例化失败", implClassName);
                throw new RuntimeException(errorMessage, e);
            }
        }
        return (T) instanceCache.get(implClassName);
    }
}
