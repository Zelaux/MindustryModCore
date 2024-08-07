package mmc;

import groovy.lang.*;
import kotlin.jvm.functions.*;
import org.jetbrains.annotations.*;

import java.util.function.*;

public class ClosureFactory {


    @NotNull
    public static Closure<String> stringToStringClosure(Function<String, String> runnable) {
        return new SimpleClosure<String>() {

            public String doCall(String name) {
                return runnable.apply(name);
            }
        };
    }


    @NotNull
    public static <P> Closure<Void> consume(Consumer<P> runnable) {
        return new SimpleClosure<Void>() {
            public Void doCall(P name) {
                runnable.accept(name);
                return null;
            }
        };
    }
    @NotNull
    public static  Closure<Void> consumeString(Consumer<String> runnable) {
        return new SimpleClosure<Void>() {
            public Void doCall(String name) {
                runnable.accept(name);
                return null;
            }
        };
    }

    @NotNull
    public static  Closure<Void> consumeString2(BiConsumer<String,String> runnable) {
        return new SimpleClosure<Void>() {
            public Void doCall(String user,String repo) {
                runnable.accept(user,repo);
                return null;
            }
        };
    }

    @NotNull
    public static <P, R> Closure<R> functionClosure(Function<P, R> runnable) {
        return new SimpleClosure<R>() {

            public R doCall(P name) {
                return runnable.apply(name);
            }
        };
    }


    public static <T> Closure<T> fromSupplier(Supplier<T> supplier) {
        return new SimpleClosure<T>() {
            public T doCall() {
                return supplier.get();
            }
        };
    }

    public static <P1, P2, R> Closure<R> function2(Function2<P1, P2, R> function) {
        return new SimpleClosure<R>() {
            public R doCall(P1 p1, P2 p2) {
                return function.invoke(p1, p2);
            }
        };
    }

    public static abstract class SimpleClosure<RETURN> extends Closure<RETURN> {

        public SimpleClosure() {
            super(null);
        }
    }
}
