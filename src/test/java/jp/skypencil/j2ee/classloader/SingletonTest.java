package jp.skypencil.j2ee.classloader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

public class SingletonTest {
    private ClassLoaderWithoutParent firstClassLoader, secondClassLoader;
    /**
     * <p>A child class-loader of &quot;firstClassLoader&quot;, which has &quot;parent first&quot; class loading policy.</p>
     */
    private ParentFirstClassLoader childOfFirstClassLoader;

    /**
     * <p>A child class-loader of &quot;firstClassLoader&quot;, which has &quot;parent first&quot; class loading policy.</p>
     */
    private ParentFirstClassLoader anotherChildOfFirstClassLoader;

    /**
     * <p>A child class-loader of &quot;secondClassLoader&quot;, which has &quot;parent last&quot; class loading policy.</p>
     */
    private ParentLastClassLoader childOfSecondClassLoader;

    /**
     * <p>Another child class-loader of &quot;secondClassLoader&quot;, which has &quot;parent last&quot; class loading policy.</p>
     */
    private ParentLastClassLoader anotherChildOfSecondClassLoader;

    @Before
    public void buildClassLoader() throws MalformedURLException {
        URL classPath = new File("target", "classes").toURI().toURL();
        firstClassLoader = new ClassLoaderWithoutParent(classPath);
        secondClassLoader = new ClassLoaderWithoutParent(classPath);
        childOfFirstClassLoader = new ParentFirstClassLoader(firstClassLoader, classPath);
        anotherChildOfFirstClassLoader = new ParentFirstClassLoader(firstClassLoader, classPath);
        childOfSecondClassLoader = new ParentLastClassLoader(secondClassLoader, classPath);
        anotherChildOfSecondClassLoader = new ParentLastClassLoader(secondClassLoader, classPath);
    }

    /**
     * <p>Each web application has own class loader.
     * Even if both class loader use same class path, class in both class loader aren&apos;t same.
     * This test case explain it by using Singleton.</p>
     */
    @Test
    public void eachClassLoaderHasOwnSingletonInstance() throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Object singletonInFirstClassLoader = findSingletonInstanceFrom(firstClassLoader);
        Object singletonInSecondClassLoader = findSingletonInstanceFrom(secondClassLoader);
        assertThat(singletonInFirstClassLoader, is(not(sameInstance(singletonInSecondClassLoader))));
    }

    /**
     * <p>Each web application has parent class loader which is managed by application server.
     * If class loading policy of class loader of web application is parent first,
     * your application has no trouble which is related to &quot;different classes which have same name&quot; problem.
     * But in some case it is not useful. Because your web application uses library 
     * in application server&apos;s classpath instead of library which is packaged into your ear/war.</p>
     */
    @Test
    public void parentFirstHasNoMultiSingletonProblem() throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Object singletonInParent = findSingletonInstanceFrom(firstClassLoader);
        Object singletonInChild = findSingletonInstanceFrom(childOfFirstClassLoader);
        assertThat(singletonInChild, is(sameInstance(singletonInParent)));

        Object singletonInAnotherChild = findSingletonInstanceFrom(anotherChildOfFirstClassLoader);
        assertThat(singletonInAnotherChild, is(sameInstance(singletonInParent)));
        assertThat(singletonInAnotherChild, is(sameInstance(singletonInChild)));
    }

    /**
     * <p>&quot;parent last&quot; class loading policy is a famous solution for conflict of library.
     * By this policy, you can use own library which is packaged into your ear/war.</p>
     * <p>But your web application and application server will load different Class, so
     * in some cases you will face complex problem.</p>
     */
    @Test
    public void parentLastMayHaveMultiSingletonProblem() throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Object singletonInParent = findSingletonInstanceFrom(secondClassLoader);
        Object singletonInChild = findSingletonInstanceFrom(childOfSecondClassLoader);
        assertThat(singletonInChild, is(not(sameInstance(singletonInParent))));

        Object singletonInAnotherChild = findSingletonInstanceFrom(anotherChildOfSecondClassLoader);
        assertThat(singletonInAnotherChild, is(not(sameInstance(singletonInParent))));
        assertThat(singletonInAnotherChild, is(not(sameInstance(singletonInChild))));
    }

    /**
     * <p>One of the famous &quot;complec problem&quot; is ClassCastException.
     * When you pass instance directly between 2 web applications, you may find this problem.</p>
     */
    @Test(expected = ClassCastException.class)
    public void singletonInChildCannotBeCastedToSingletonClassInParent() throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Class<?> singletonClassInParent = findSingletonInstanceFrom(secondClassLoader).getClass();
        Object singletonInChild = findSingletonInstanceFrom(childOfSecondClassLoader);
        singletonClassInParent.cast(singletonInChild);
    }

    private Object findSingletonInstanceFrom(ClassLoader classLoader) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Class<?> singletonClass = classLoader.loadClass(Singleton.class.getName());
        return singletonClass.getDeclaredMethod("getInstance").invoke(null);
    }
}
