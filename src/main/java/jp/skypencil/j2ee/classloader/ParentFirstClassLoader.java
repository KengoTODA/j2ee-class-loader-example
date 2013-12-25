package jp.skypencil.j2ee.classloader;

import java.net.URL;
import java.net.URLClassLoader;

import com.google.common.eventbus.EventBus;

/**
 * <p>&quot;Parent first&quot; is the default behavior, so
 * this class does not override default methods</p>
 */
public class ParentFirstClassLoader extends URLClassLoader {
    private final EventBus eventBus = new EventBus();

    public ParentFirstClassLoader(ClassLoaderWithoutParent parent,
            URL... classPath) {
        super(classPath, parent);
    }

    EventBus getEventBus() {
        return eventBus;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        eventBus.post(new UnloadEvent());
    }
}
