package mmc;

import groovy.lang.*;
import org.jetbrains.annotations.*;

import java.util.function.*;

public class ClosureFactory{


    @NotNull
    public static Closure<String> stringToStringClosure(Function<String, String> runnable){
        return new SimpleClosure<String>(){

            public String doCall(String name){
                return runnable.apply(name);
            }
        };
    }

    public static <T> Closure<T> fromSupplier(Supplier<T> supplier){
        return new SimpleClosure<T>(){
            public T doCall(){
                return supplier.get();
            }
        };
    }

    static abstract class SimpleClosure<RETURN> extends Closure<RETURN>{

        public SimpleClosure(){
            super(null);
        }
    }
}
