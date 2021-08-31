package mma.annotations.comps;

import arc.files.Fi;
import arc.util.Log;
import com.sun.tools.javac.code.Symbol;
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
            if (rootElement instanceof Symbol.ClassSymbol){
                Symbol.ClassSymbol symbol = (Symbol.ClassSymbol) rootElement;
                String packageName = symbol.packge().name.toString();
                String symbolName = symbol.name.toString();
                if (packageName.equals("compByAnuke") && symbolName.endsWith("Comp")){
                    file.writeString(symbolName + "\n",true);
                }
            }
//            Log.info("rootElement: @(@)",rootElement.getSimpleName(),rootElement.getClass());
        }

    }
}
