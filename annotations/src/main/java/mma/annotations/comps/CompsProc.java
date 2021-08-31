package mma.annotations.comps;

import arc.files.Fi;
import arc.util.Log;
import mindustry.annotations.util.Stype;
import mma.annotations.ModBaseProcessor;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("mma.annotations.ModAnnotations.CompsProcessor")
public class CompsProc extends ModBaseProcessor {
    @Override
    public void process(RoundEnvironment env) throws Exception {
        if (!rootPackageName.equals("mma"))return;
        Fi file = rootDirectory.child("anukeCompsList.txt");
        file.writeString("");
        for (Element rootElement : env.getRootElements()) {
            String toString = rootElement.toString();
            if (toString.contains(".entities.compByAnuke.")){
                String symbolName=toString.substring(toString.lastIndexOf(".")+1);
                file.writeString(symbolName + "\n",true);
            }
//            Log.info("rootElement: @(@)",rootElement.getSimpleName(),rootElement.getClass());
        }

    }
}
