package mma.annotations;

import arc.files.Fi;
import arc.struct.*;
import arc.util.Log;
import arc.util.OS;
import arc.util.Strings;
import arc.util.Time;
import arc.util.io.*;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import mindustry.annotations.BaseProcessor;
import mindustry.annotations.util.Selement;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public abstract class ModBaseProcessor extends BaseProcessor {
    static final String parentName = "mindustry.gen";
    public static String rootPackageName = null;
    static final StringMap annotationProperties=new StringMap();
    {
        enableTimer=true;
    }
    public  StringMap annotationsSettings(){
        Fi annotationPropertiesFile = rootDirectory.child("annotation.properties");

        if (annotationPropertiesFile.exists()){
            PropertiesUtils.load(annotationProperties,annotationPropertiesFile.reader());
        } else {
            annotationPropertiesFile.writeString("");
        }
        Fi classPrefixTxt = rootDirectory.child("annotations/classPrefix.txt");
        if (classPrefixTxt.exists()) {
            annotationProperties.put("classPrefix",classPrefixTxt.readString());
            try{
                PropertiesUtils.store(annotationProperties,annotationPropertiesFile.writer(false),null);
                classPrefixTxt.delete();
            }catch(IOException exception){
                exception.printStackTrace();
            }
        }
        return annotationProperties;
    }
    public static void print(String obj, Object... args) {
        String message = Strings.format(obj.toString(), args);
        System.out.println(message);
    }

    public static void write(TypeSpec.Builder builder, String packageName) throws Exception {
        write(builder, packageName, (Seq<String>) null);
    }

    protected static Fi getFilesFi(StandardLocation location) throws IOException {
        return getFilesFi(location, "no", "no").parent().parent();
    }

    protected static Fi getFilesFi(StandardLocation location, String packageName, String className) throws IOException {
        return Fi.get(filer.getResource(location, packageName, className)
                .toUri().toURL().toString().substring(OS.isWindows ? 6 : "file:".length()));
    }

    public String classPrefix() {
        String classNamePrefix = "Mod";
        if (!rootPackageName.equals("mma")) {
            classNamePrefix = annotationsSettings().get("classPrefix",Strings.capitalize(rootPackageName));
        }
        return classNamePrefix;
    }

    @Override
    protected String getPackageName() {
        packageName=(rootPackageName = annotationsSettings().get("rootPackage",rootDirectory.child("core/src").list()[0].name())) + ".gen";
        return packageName;
    }

    public void delete(String packageName, String name) throws IOException {
//        print("delete name: @",name);
        FileObject resource;
        resource = filer.getResource(StandardLocation.SOURCE_OUTPUT, packageName, name);
//        boolean delete = resource.delete();
//        print("delete: @ ,named: @, filer: @",delete,resource.getName(),resource.getClass().getName());
        Files.delete(Paths.get(resource.getName() + ".java"));
    }

    public void delete(String name) throws IOException {
        delete(packageName, name);
    }

    public void debugLog(String text,Object... args){
        System.out.println("[D]"+Strings.format(text, args));
    }
}
