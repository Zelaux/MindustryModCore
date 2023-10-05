package mmc.local.annotations;

import arc.util.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.Modifier.*;
import com.github.javaparser.ast.body.*;
import mindustry.annotations.*;

import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.tools.*;
import java.io.*;
import java.util.*;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
abstract class LocalBaseProcessor extends BaseProcessor{
    @Override
    protected String getPackageName(){
        return "mmc.annotations";
    }

    public void write(CompilationUnit unit, String className) throws IOException{
        if(className == null){
            List<ClassOrInterfaceDeclaration> classes = unit.findAll(ClassOrInterfaceDeclaration.class);
            classes.sort(Structs.comparingBool(it -> {
                return it.hasModifier(Keyword.PUBLIC);
            }));
            className = classes.get(0).getNameAsString();
        }
//        ClassOrInterfaceDeclaration declaration = unit.getClassByName(className).get();
        PackageDeclaration declaration = unit.getPackageDeclaration().get();
        JavaFileObject object = null;
        try{
            object = filer.createSourceFile(declaration.getNameAsString() + "." + className);
        }catch(IOException e){
            e.printStackTrace();
        }
        Writer stream = object.openWriter();
        stream.write(unit.toString());
        stream.close();
    }
}
