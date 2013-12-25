package jp.skypencil.j2ee.classloader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * <p>Simple class-loader which has no parent class loader.</p>
 */
class ClassLoaderWithoutParent extends URLClassLoader {
    ClassLoaderWithoutParent(URL... classPath) {
        super(classPath, null);
    }

    /**
     * <p>{@code ParentLastClassLoader} needs to call this protected method, so
     * we should override this method to let it calls this method.</p>
     * @see http://stackoverflow.com/a/5446671
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }
}