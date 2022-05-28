package mma.annotations;

import arc.files.*;
import arc.func.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.serialization.*;
import arc.util.serialization.Jval.*;
import com.squareup.javapoet.*;
import mindustry.annotations.*;
import mindustry.io.*;
import mindustry.mod.Mods.*;

import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.tools.*;
import java.io.*;
import java.nio.file.*;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public abstract class ModBaseProcessor extends BaseProcessor{
    static final String parentName = "mindustry.gen";
    static final StringMap annotationProperties = new StringMap();
    public static String rootPackageName = null;

    {
        enableTimer = true;
    }

    public static void print(String obj, Object... args){
        String message = Strings.format(obj.toString(), args);
        System.out.println(message);
    }

    public static void write(TypeSpec.Builder builder, String packageName) throws Exception{
        write(builder, packageName, (Seq<String>)null);
    }

    protected static Fi getFilesFi(StandardLocation location) throws IOException{
        return getFilesFi(location, "no", "no").parent().parent();
    }

    protected static Fi getFilesFi(StandardLocation location, String packageName, String className) throws IOException{
        return Fi.get(filer.getResource(location, packageName, className)
        .toUri().toURL().toString().substring(OS.isWindows ? 6 : "file:".length()));
    }

    public ModMeta modInfo(){
        ModMeta meta = modInfoNull();
        if(meta == null){
            err("Cannot find mod info file");
            throw new RuntimeException("Cannot find mod info file");
        }

        return meta;
    }

    @Nullable
    public ModMeta modInfoNull(){
        String[] paths = {
        "mod.json",
        "mod.hjson",
        "plugin.json",
        "plugin.hjson",
        };
        Fi file = null;
        for(int i = 0; i < paths.length * 2; i++){
            boolean coreAssets = i >= paths.length;
            int index = i % paths.length;
            if(coreAssets){
                file = rootDirectory.child("core/assets").child(paths[index]);
            }else{

                file = rootDirectory.child(paths[index]);
            }
            if(file.exists()){
                break;
            }
            file = null;
        }
        if(file == null) return null;

        return JsonIO.json.fromJson(ModMeta.class, Jval.read(file.readString()).toString(Jformat.plain));
    }

    /**
     * revisionsPath
     * classPrefix
     * rootPackage
     */
    public StringMap annotationsSettings(){
        Fi annotationPropertiesFile = rootDirectory.child("annotation.properties");
        Fi[] list = rootDirectory.child("core/src").list();
        boolean debug = list.length == 1 && list[0].name().equals("mma");
        if(debug){
//            annotationProperties.put("debug", "true");
            return annotationProperties;
        }
        if(annotationPropertiesFile.exists()){
            PropertiesUtils.load(annotationProperties, annotationPropertiesFile.reader());
        }else{
            annotationPropertiesFile.writeString("");
        }
        Fi classPrefixTxt = rootDirectory.child("annotations/classPrefix.txt");
        if(classPrefixTxt.exists()){
            annotationProperties.put("classPrefix", classPrefixTxt.readString());
            try{
                PropertiesUtils.store(annotationProperties, annotationPropertiesFile.writer(false), null);
                classPrefixTxt.delete();
            }catch(IOException exception){
                exception.printStackTrace();
            }
        }
        return annotationProperties;
    }

    public String classPrefix(){
        String classNamePrefix = "Mod";
        if(!rootPackageName.equals("mma")){
            classNamePrefix = annotationsSettings(AnnotationSetting.classPrefix, Strings.capitalize(rootPackageName));
        }
        return classNamePrefix;
    }

    public String annotationsSettings(AnnotationSetting settings, String defvalue){
        return annotationsSettings().get(settings.name(), defvalue);
    }

    public String annotationsSettings(AnnotationSetting settings, Prov<String> defvalue){
        StringMap map = annotationsSettings();
        return map.containsKey(settings.name()) ? map.get(settings.name()) : defvalue.get();
    }

    @Override
    protected String getPackageName(){
        packageName = (rootPackageName = annotationsSettings(AnnotationSetting.rootPackage, () -> {
            Fi[] list = rootDirectory.child("core/src").list();
            if(list.length == 0) err("Cannot find rootPackage, please write rootPackage in annotation.properties");
            return list[0].name();
        })) + ".gen";
        return packageName;
    }

    public void delete(String packageName, String name) throws IOException{
//        print("delete name: @",name);
        FileObject resource;
        resource = filer.getResource(StandardLocation.SOURCE_OUTPUT, packageName, name);
//        boolean delete = resource.delete();
//        print("delete: @ ,named: @, filer: @",delete,resource.getName(),resource.getClass().getName());
        Files.delete(Paths.get(resource.getName() + ".java"));
    }

    public void delete(String name) throws IOException{
        delete(packageName, name);
    }

    public void debugLog(String text, Object... args){
        System.out.println("[D]" + Strings.format(text, args));
    }

    public Fi rootDirectory(){
        return rootDirectory;
    }
}
