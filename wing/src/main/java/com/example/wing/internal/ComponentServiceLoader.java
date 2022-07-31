package com.example.wing.internal;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * 组件服务加载类
 */
public class ComponentServiceLoader<I> implements Iterator<I> {
    private final Iterator<I> mIterator;
    private final ServiceLoader<I> mLoader;

    public ComponentServiceLoader(Class<I> interfaceClass) {
        this(interfaceClass, Thread.currentThread().getContextClassLoader());
    }

    public ComponentServiceLoader(Class<I> interfaceClass, ClassLoader loader) {
        if (interfaceClass == null || !interfaceClass.isInterface()) {
            throw new IllegalArgumentException("interfaceClass must be a Interface!");
        }
        mLoader = ServiceLoader.load(interfaceClass, loader);
        mIterator = mLoader.iterator();
    }

    public void reload() {
        mLoader.reload();
    }

    @Override
    public boolean hasNext() {
        return mIterator.hasNext();
    }

    @Override
    public I next() {
        return mIterator.next();
    }

}

