package jp.skypencil.j2ee.classloader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * <p>&quot;Parent first&quot; is the default behavior, so
 * this class does not override default methods</p>
 */
public class ParentFirstClassLoader extends URLClassLoader {

    public ParentFirstClassLoader(ClassLoaderWithoutParent parent,
            URL... classPath) {
        super(classPath, parent);
    }

}
