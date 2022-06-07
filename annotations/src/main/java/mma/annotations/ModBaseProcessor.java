package mma.annotations;

import arc.files.*;
import arc.func.*;
import arc.struct.ObjectMap.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.serialization.*;
import arc.util.serialization.Jval.*;
import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.Modifier.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.google.common.collect.*;
import com.squareup.javapoet.*;
import com.sun.source.tree.*;
import mindustry.annotations.*;
import mindustry.annotations.util.*;
import mindustry.io.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mma.annotations.ModAnnotations.*;

import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.*;
import java.io.*;
import java.lang.annotation.*;
import java.nio.file.*;
import java.util.*;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public abstract class ModBaseProcessor extends BaseProcessor{
    static final StringMap annotationProperties = new StringMap();
    public static String rootPackageName = null;
    static boolean markAnnotationSettingsPathElement = true;
    private static boolean createdSettingClass = false;
    AnnotationSettings annotationSettingsAnnotation;
    Element annotationSettingsAnnotationElement;
    AnnotationPropertiesPath annotationSettingsPath;
    Element annotationSettingsPathElement;

    {
        enableTimer = true;
    }


    public static void print(String obj, Object... args){
        String message = Strings.format(obj, args);
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
        String annotationsSettings = annotationsSettings(AnnotationSetting.modInfoPath, "\n");
//        System.out.println("annotationsSettings: "+annotationsSettings);
        if(!annotationsSettings.equals("\n")){
            Fi file = rootDirectory.child(annotationsSettings);
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
        boolean debug = list.length == 1 && list[0].name().equals("mma") && annotationSettingsPath == null && annotationSettingsAnnotation == null;
        if(debug){
            debugLog("debug annotation settings");
//            annotationProperties.put("debug", "true");
            return annotationProperties;
        }
        if(annotationPropertiesFile.exists() && !createdSettingClass){
            Reader reader = annotationPropertiesFile.reader();
            PropertiesUtils.load(annotationProperties, reader);
            try{
                reader.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }else{
//            annotationPropertiesFile.writeString("");
        }
        Fi classPrefixTxt = rootDirectory.child("annotations/classPrefix.txt");
        if(classPrefixTxt.exists()){
            annotationProperties.put("classPrefix", classPrefixTxt.readString());
            try{
                if(!createdSettingClass) PropertiesUtils.store(annotationProperties, annotationPropertiesFile.writer(false), null);
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
           /* try{
//                PropertiesUtils.store(annotationProperties, annotationPropertiesFile.writer(false), null);
            }catch(IOException exception){
                exception.printStackTrace();
            }*/
        }
        if(annotationPropertiesFile.exists() && !createdSettingClass){
            createdSettingClass = true;

            StringBuilder annotation = new StringBuilder("@" + AnnotationSettings.class.getName().replace("$", ".") + "(");
            int i = 0;
            for(Entry<String, String> property : annotationProperties){
                if(i > 0){
                    annotation.append(", ");
                }
                annotation.append(property.key);
                annotation.append(" = \"");
                annotation.append(property.value);
                annotation.append("\"");
                i++;
            }
            annotation.append(")");
//            err("Creating settings class");
            if(annotationSettingsAnnotation == null){
                createAnnotationSettingsClass(annotation);
            }else{

                CompilationUnitTree anyCompUnit = trees.getPath(annotationSettingsAnnotationElement).getCompilationUnit();
                JavaParser parser = new JavaParser();
//                parser.getParserConfiguration().setC
                Fi file = Fi.get(anyCompUnit.getSourceFile().getName());
                CompilationUnit compilationUnit = parser.parse(file.readString()).getResult().get();
                for(AnnotationExpr expr : compilationUnit.findAll(AnnotationExpr.class)){
                    if(!expr.getNameAsString().endsWith(AnnotationSettings.class.getSimpleName())){
                        continue;
                    }
                    AnnotationExpr node = parser.parseAnnotation(annotation.toString()).getResult().get();
                    node.setName(expr.getNameAsString());
                    expr.replace(node);
                }


                file.writeString(compilationUnit.toString());
//                System.out.println("anyCompUnit.toString(): "+);
                /*Fi file=Fi.get( anyCompUnit.getSourceFile().getName());
                if(!file.extension().equals("java")){
                    throw new RuntimeException("OH NO");
                }
                for(int j = 0; j < anyCompUnit.getPackageName().toString().split("\\.").length+1; j++){
                    file=file.parent();
                }
                file
                .child(compilationUnit.getPackageDeclaration().get().getNameAsString().replace(".","//"))
                .child(declaration.getNameAsString()+".java")
                .writeString(compilationUnit.toString());*/
            }
            if(!annotationPropertiesFile.delete()){
                System.out.println("cannot delete");
            }
            if(annotationSettingsPathElement != null){
                CompilationUnitTree unitTree = trees.getPath(annotationSettingsPathElement).getCompilationUnit();
                Fi file = Fi.get(unitTree.getSourceFile().getName());

                CompilationUnit compilationUnit = StaticJavaParser.parse(file.readString());
                for(AnnotationExpr expr : compilationUnit.findAll(AnnotationExpr.class)){
                    if(expr.getNameAsString().endsWith(AnnotationPropertiesPath.class.getSimpleName())){
                        expr.remove();
                    }
                }

                file.writeString(compilationUnit.toString());
                annotationSettingsPathElement = null;
                annotationSettingsPath = null;
            }
//            System.out.println("annotationPropertiesFile: "+annotationPropertiesFile);
//            System.out.println(zeroPackage);
//            System.out.println("trees.getPath(element.e).getCompilationUnit().getSourceFile().getName(): "+zeroPackage.absolutePath());

        }
//        System.out.println("annotationProperties: "+annotationProperties);
        return annotationProperties;
    }

    private void createAnnotationSettingsClass(StringBuilder annotation){
        packageName = getPackageName();

        CompilationUnit compilationUnit = new CompilationUnit(rootPackageName + ".mma");
        ClassOrInterfaceDeclaration declaration = compilationUnit.addClass("AnnotationProcessorSettings", new Keyword[0]);

        declaration.addAnnotation(StaticJavaParser.parseAnnotation(annotation.toString()));
        Selement<?> element = Seq.with(env.getRootElements()).map(Selement::new).sort(Structs.comps(
        Structs.comparingBool(Selement::isType),
        Structs.comparingBool(it -> {
            if(!it.isType()) return false;
            Stype stype = it.asType();
            return stype.superclasses().contains(it2 -> it2.fullName().equals(Mod.class.getName()));
        }))).firstOpt();
        CompilationUnitTree anyCompUnit = trees.getPath(element.e).getCompilationUnit();
        Fi zeroPackage = Fi.get(anyCompUnit.getSourceFile().getName());
        if(!zeroPackage.extension().equals("java")){
            throw new RuntimeException("OH NO");
        }
        for(int j = 0; j < anyCompUnit.getPackageName().toString().split("\\.").length + 1; j++){
            zeroPackage = zeroPackage.parent();
        }
        zeroPackage
        .child(compilationUnit.getPackageDeclaration().get().getNameAsString().replace(".", "//"))
        .child(declaration.getNameAsString() + ".java")
        .writeString(compilationUnit.toString());
    }

    public String classPrefix(){
        String classNamePrefix = "Mod";
        if(!rootPackageName.equals("mma")){
            classNamePrefix = annotationsSettings(AnnotationSetting.classPrefix, Strings.capitalize(rootPackageName));
        }
        return classNamePrefix;
    }

    public String annotationsSettings(AnnotationSetting settings, String defvalue){
        StringMap map = annotationsSettings();
        return map.containsKey(settings.name()) ? map.get(settings.name()) : defvalue;
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
                annotationSettingsPathElement = selement.e;
                Fi file = getRootDirectory().child(annotationSettingsPath.propertiesPath());
                if(!file.exists() && !createdSettingClass){
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

    @Override
    public Set<String> getSupportedAnnotationTypes(){
        javax.annotation.processing.SupportedAnnotationTypes sat = this.getClass().getAnnotation(javax.annotation.processing.SupportedAnnotationTypes.class);
        SupportedAnnotationTypes sat2 = this.getClass().getAnnotation(SupportedAnnotationTypes.class);
        boolean initialized = isInitialized();
        if(sat2 != null){
            boolean stripModulePrefixes =
            initialized &&
            processingEnv.getSourceVersion().compareTo(SourceVersion.RELEASE_8) <= 0;

            Class<? extends Annotation>[] classes = sat2.value();
            String[] strings = new String[classes.length];
            for(int i = 0; i < strings.length; i++){
                strings[i]=classes[i].getCanonicalName();
            }
            System.out.println("supported->"+Arrays.toString(strings));
            return arrayToSet(strings, stripModulePrefixes,
            "annotation type", "@SupportedAnnotationTypes");
        }
        return super.getSupportedAnnotationTypes();
    }

    private Set<String> arrayToSet(String[] array,
                                   boolean stripModulePrefixes,
                                   String contentType,
                                   String annotationName){
        assert array != null;
        Set<String> set = new HashSet<>();
        for(String s : array){
            boolean stripped = false;
            if(stripModulePrefixes){
                int index = s.indexOf('/');
                if(index != -1){
                    s = s.substring(index + 1);
                    stripped = true;
                }
            }
            boolean added = set.add(s);
            // Don't issue a duplicate warning when the module name is
            // stripped off to avoid spurious warnings in a case like
            // "foo/a.B", "bar/a.B".
            if(!added && !stripped && isInitialized()){
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                "Duplicate " + contentType +
                " ``" + s + "'' for processor " +
                this.getClass().getName() +
                " in its " + annotationName +
                "annotation.");
            }
        }
        return Collections.unmodifiableSet(set);
    }
}
