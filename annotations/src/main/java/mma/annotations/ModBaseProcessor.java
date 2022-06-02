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
import mindustry.annotations.util.*;
import mindustry.io.*;
import mindustry.mod.Mods.*;
import mma.annotations.ModAnnotations.*;

import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public abstract class ModBaseProcessor extends BaseProcessor{
    static final String parentName = "mindustry.gen";
    static final StringMap annotationProperties = new StringMap();
    public static String rootPackageName = null;
    static boolean markAnnotationSettingsPathElement = true;
    AnnotationSettings annotationSettingsAnnotation;
    Element annotationSettingsAnnotationElement;
    AnnotationPropertiesPath annotationSettingsPath;

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
            if(annotationSettingsAnnotation != null && !annotationSettingsAnnotation.modInfoPath().equals("\n")){
                err("Cannot find mod info file", annotationSettingsAnnotationElement);
            }else{
                err("Cannot find mod info file");
            }
            throw new RuntimeException("Cannot find mod info file");
        }

        return meta;
    }

    @Nullable
    public ModMeta modInfoNull(){
        if(!annotationsSettings(AnnotationSetting.modInfoPath, "\n").equals("\n")){
            Fi file = rootDirectory.child(annotationsSettings(AnnotationSetting.modInfoPath, "\n"));
//            System.out.println("path: "+file);
            if(!file.exists()) return null;

            return JsonIO.json.fromJson(ModMeta.class, Jval.read(file.readString()).toString(Jformat.plain));
        }
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
     * assetsPath
     * assetsRawPath
     * modInfoPath
     * revisionsPath
     * classPrefix
     * rootPackage
     */
    public StringMap annotationsSettings(){
        Fi annotationPropertiesFile = rootDirectory.child(annotationSettingsPath != null ? annotationSettingsPath.propertiesPath() : "annotation.properties");
        Fi[] list = rootDirectory.child("core/src").list();
        boolean debug = list.length == 1 && list[0].name().equals("mma");
        if(debug && annotationSettingsPath == null){
            debugLog("debug annotation settings");
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
        if(annotationSettingsAnnotation != null){
            for(AnnotationSetting value : AnnotationSetting.values()){
                Object invoke = Reflect.invoke(AnnotationSettings.class, annotationSettingsAnnotation, value.name(), new Object[]{});
                if(String.valueOf(invoke).equals("\n")) continue;
                annotationProperties.put(value.name(), String.valueOf(invoke));
            }
            try{
                PropertiesUtils.store(annotationProperties, annotationPropertiesFile.writer(false), null);
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

    @Override
    public Fi getRootDirectory(){
        Fi rootDirectory;
        try{
            Stype stype = types(RootDirectoryPath.class).firstOpt();

            String path = Fi.get(filer.getResource(StandardLocation.CLASS_OUTPUT, "no", "no").toUri().toURL().toString().substring(OS.isWindows ? 6 : "file:".length())).parent().parent().parent().parent().parent().parent().parent().toString().replace("%20", " ");
            Fi fi = Fi.get(path);

            String rootDirectoryPath = stype == null ? "../" : stype.annotation(RootDirectoryPath.class).rootDirectoryPath();
            rootDirectory = new Fi(fi.child(
            !rootDirectoryPath.equals("\n") ? rootDirectoryPath : "../"
            ).file().getCanonicalFile());
//            System.out.println("fi1: " + fi);
//            rootDirectory = fi.parent();
//            System.out.println("fi2: " + rootDirectory);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
        return rootDirectory;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){

        this.env = roundEnv;
        if(annotationSettingsAnnotation == null){
            Stype selement = types(AnnotationSettings.class).firstOpt();
            annotationSettingsAnnotation = selement == null ? null : selement.annotation(AnnotationSettings.class);
            if(annotationSettingsAnnotation != null){
                annotationSettingsAnnotationElement = selement.e;
            }
        }
        if(annotationSettingsPath == null){
            Stype selement = types(AnnotationPropertiesPath.class).firstOpt();
            annotationSettingsPath = selement == null ? null : selement.annotation(AnnotationPropertiesPath.class);
            if(annotationSettingsPath != null){
                Fi file = getRootDirectory().child(annotationSettingsPath.propertiesPath());
                if(!file.exists()){
                    if(markAnnotationSettingsPathElement){
                        markAnnotationSettingsPathElement = false;
                        err("Cannot find file \"" + annotationSettingsPath.propertiesPath() + "\"", selement.e);
                    }
                    round = rounds;
                }
            }
        }
//        debugLog("annotationSettingsPath: @",annotationSettingsPath);
//        debugLog("annotationSettingsAnnotation: @",annotationSettingsAnnotation);
//        System.out.println("");
        return super.process(annotations, roundEnv);
    }

    public Fi rootDirectory(){
        return rootDirectory;
    }
}
