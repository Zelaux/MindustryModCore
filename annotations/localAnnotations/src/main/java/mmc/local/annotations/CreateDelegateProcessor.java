package mmc.local.annotations;

import arc.struct.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.Modifier.*;
import com.github.javaparser.ast.body.*;
import mindustry.annotations.util.*;
import org.jetbrains.annotations.*;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import java.io.*;
import java.util.stream.*;

@SupportedAnnotationTypes({
    "javax.annotation.processing.SupportedAnnotationTypes",
    "mmc.annotations.SupportedAnnotationTypes"
})
public class CreateDelegateProcessor extends LocalBaseProcessor{
    private static <T> @NotNull Collector<T, Seq<T>, Seq<T>> toSeq(){
        return Collector.of(Seq::new, Seq::add, Seq::addAll);
    }

    @Override
    public void process(RoundEnvironment env) throws Exception{
        Seq<Stype> roots = env.getRootElements()
                              .stream()
                              .filter(it -> it instanceof TypeElement)
                              .map(it -> (TypeElement)it)
                              .map(Stype::new)
                              .collect(toSeq());
        for(Stype stype : roots){
            if(!stype.superclass().fullName().endsWith("BaseProcessor")) continue;
            if(stype.name().equals("ModBaseProcessor")) continue;
            if(stype.name().equals("BaseProcessor")) continue;
            makeDelegate(stype);
        }

    }

    private void makeDelegate(Stype type) throws IOException{
        CompilationUnit unit = new CompilationUnit(type.cname().packageName());
        ClassOrInterfaceDeclaration clazz = unit.addClass(type.name() + "Delegate", Keyword.PUBLIC)
                                                               .addExtendedType("mmc.annotations.internal.DelegateProcessor");
        write(unit, clazz.getNameAsString());
    }
}
