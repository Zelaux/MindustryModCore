package mmc.annotations.internal;

import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import java.util.*;

public class DelegateProcessorRaw implements Processor{
     Processor delegate;



    @Override
    public Set<String> getSupportedOptions(){
        return delegate.getSupportedOptions();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes(){
        return delegate.getSupportedAnnotationTypes();
    }

    @Override
    public SourceVersion getSupportedSourceVersion(){
        return delegate.getSupportedSourceVersion();
    }

    @Override
    public void init(ProcessingEnvironment processingEnv){
        delegate.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){
        return delegate.process(annotations, roundEnv);
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText){
        return delegate.getCompletions(element, annotation, member, userText);
    }
}
