package jp.skypencil.j2ee.classloader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.google.common.eventbus.Subscribe;

public class ClassLoaderLeakTest {
    /**
     * <p>Class loader will be finalized when no one uses it.
     * At that time, all class which is loaded by it will be unloaded.</p>
     */
    @Test
    public void classLoaderShouldBeFinalizedIfNoOneReferIt() throws Exception {
        UnloadEventListener listener = new UnloadEventListener();
        loadSingletonFromNewClassLoader(listener);
        assertThat(listener.unloaded, is(false));

        runFinalization(listener);
        assertThat("class loader should be finalized", listener.unloaded, is(true));
    }

    /**
     * <p>GC cannot dispose class loader if someone uses it or class which is loaded by it.
     * So if you pass your instance to application server or another web application,
     * rebooting web application cannot free Java heap and permanent area.</p>
     */
    @Test
    public void classLoaderShouldNotBeFinalizedIfSomeoneRefersIt() throws Exception {
        UnloadEventListener listener = new UnloadEventListener();
        Object singletonInstance = loadSingletonFromNewClassLoader(listener);
        assertThat(listener.unloaded, is(false));

        runFinalization(listener);
        System.out.printf("Singleton instance is %s%n", singletonInstance.getClass().getClassLoader()); // we should use singletonInstance like this, or optimization removes singletonInstance local variable
        assertThat("class loader should not be finalized, because we still use Singleton instance", listener.unloaded, is(false));
    }

    /**
     * <p>There is no way to ensure that finalization runs.
     * We need retry to improve its reliability.</p>
     */
    private void runFinalization(UnloadEventListener listener)
            throws InterruptedException {
        int retry = 3;
        do {
            TimeUnit.MILLISECONDS.sleep(10);
            System.gc();
            System.runFinalization();
        } while (!listener.unloaded && retry-- > 0);
    }

    /**
     * <p>This method creates a class loader and returns Singleton instance which is loaded by created class loader.
     * Class loader will be finalized when finalizer runs.</p>
     */
    Object loadSingletonFromNewClassLoader(UnloadEventListener listener) throws IOException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        URL classPath = new File("target", "classes").toURI().toURL();

        try (ParentFirstClassLoader classLoader = new ParentFirstClassLoader(null, classPath)) {
            classLoader.getEventBus().register(listener);
            Class<?> singletonClass = classLoader.loadClass(Singleton.class.getName());
            return singletonClass.getDeclaredMethod("getInstance").invoke(null);
        }
    }

    private static final class UnloadEventListener {
        private boolean unloaded;

        @Subscribe
        public void onUnload(UnloadEvent event) {
            this.unloaded = true;
        }
    }
}
