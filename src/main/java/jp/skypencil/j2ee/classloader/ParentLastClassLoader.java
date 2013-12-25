package jp.skypencil.j2ee.classloader;

import java.net.URL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @see http://stackoverflow.com/a/5446671
 */
public final class ParentLastClassLoader extends ClassLoader {
    @Nonnull
    private final ClassLoaderWithoutParent classLoaderForOwnClassPath;

    ParentLastClassLoader(@Nullable ClassLoader parent, URL... classPath) {
        super(parent);
        this.classLoaderForOwnClassPath = new ClassLoaderWithoutParent(classPath);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return classLoaderForOwnClassPath.findClass(name);
        } catch (ClassNotFoundException e) {
            return super.loadClass(name, resolve);
        }
    }
}
