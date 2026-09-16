package com.wang.light.api.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * SPI 加载工具，统一使用线程上下文类加载器（兜底本类加载器）。
 */
public final class SpiLoader {

    private SpiLoader() {
    }

    public static <T> List<T> loadAll(Class<T> type) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = SpiLoader.class.getClassLoader();
        }
        List<T> result = new ArrayList<>();
        Iterator<T> it = ServiceLoader.load(type, cl).iterator();
        while (true) {
            try {
                if (!it.hasNext()) {
                    break;
                }
                result.add(it.next());
            } catch (Throwable t) {
                // 单个实现加载失败（如可选依赖缺失）时跳过，不影响其他实现
            }
        }
        return result;
    }

    public static <T> List<T> loadAllOrEmpty(Class<T> type) {
        try {
            return loadAll(type);
        } catch (Throwable t) {
            return Collections.emptyList();
        }
    }
}
