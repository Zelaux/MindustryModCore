package mmc.utils;

import groovy.lang.*;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.HandleMetaClass;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.runtime.metaclass.ReflectionMetaMethod;
import org.gradle.api.plugins.ExtensionAware;

import java.lang.reflect.Method;
import java.util.Arrays;

public class Decorator {
    public static void decorate(Object o, String name, Object value) {
        HandleMetaClass hmc = new HandleMetaClass(InvokerHelper.getMetaClass(o), o);
        hmc.setProperty(name, value);
    }

    public static void decorate(Object o, String name, Method value) {
        HandleMetaClass hmc = new HandleMetaClass(InvokerHelper.getMetaClass(o), o);

        hmc.setProperty(name, value);
    }

    public static void decorateMethod(Object o, String name, Closure value, Class returnType) {
//        MetaClass metaClass = InvokerHelper.getMetaClass(o);
        ;
//        Class theClass = metaClass.getTheClass();
//        MetaClassRegistry metaRegistry = InvokerHelper.getMetaRegistry();
//        metaRegistry.getMetaClass()
//        metaRegistry.setMetaClass(theClass,new ProxyMetaClass());

//        metaClass.setProperty(metaClass,o,name,value,false,false);

    }

    public static void decorateMethod(Object o, Method method) {
        MetaClass metaClass = InvokerHelper.getMetaClass(o);
        DecoratorProxyMeta proxy;
        if (!(metaClass instanceof DecoratorProxyMeta decoratorProxyMeta)) {
            MutableMetaClass mutableMetaClass = (MutableMetaClass) metaClass;
            Class theClass = metaClass.getTheClass();
            MetaClassRegistryImpl metaRegistry = (MetaClassRegistryImpl)InvokerHelper.getMetaRegistry();
            proxy = new DecoratorProxyMeta(metaRegistry, theClass, metaClass);
            metaRegistry.setMetaClass(o, proxy);
        } else {
            proxy = decoratorProxyMeta;
        }

        System.out.println(Arrays.toString(o.getClass().getInterfaces()));

        proxy.addMetaMethod(new ReflectionMetaMethod(new CachedMethod(method)));
    }

    static class DecoratorProxyMeta extends ProxyMetaClass {

        /**
         * @param registry
         * @param theClass
         * @param adaptee  the MetaClass to decorate with interceptability
         */
        public DecoratorProxyMeta(MetaClassRegistry registry, Class theClass, MetaClass adaptee) {
            super(registry, theClass, adaptee);
        }

        @Override
        public Object invokeMethod(Object object, String methodName, Object[] arguments) {
            System.out.println("Method "+methodName);
            return super.invokeMethod(object, methodName, arguments);
        }

        @Override
        public Object invokeMethod(Class sender, Object object, String methodName, Object[] arguments, boolean isCallToSuper, boolean fromInsideClass) {
            System.out.println("Method1 "+methodName);
            return super.invokeMethod(sender, object, methodName, arguments, isCallToSuper, fromInsideClass);
        }

        @Override
        public void addMetaMethod(MetaMethod method) {
            boolean wasInit = isInitialized();
            if (wasInit) {
                setInitialized(false);
                clearInvocationCaches();
            }
            super.addMetaMethod(method);
            if (wasInit) {

                initialize();
            }
        }
    }
}