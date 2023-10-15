package mmc.annotations;

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
import com.squareup.javapoet.*;
import com.sun.source.tree.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.comp.*;
import com.sun.tools.javac.main.*;
import com.sun.tools.javac.processing.*;
import com.sun.tools.javac.util.*;
import mindustry.annotations.*;
import mindustry.annotations.util.*;
import mindustry.io.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mmc.annotations.ModAnnotations.*;
import org.jetbrains.annotations.Nullable;
import org.reflections.*;
import org.reflections.scanners.*;
import org.reflections.util.*;

import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.*;
import javax.tools.Diagnostic.*;
import java.io.*;
import java.lang.annotation.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.*;

@SuppressWarnings("deprecation")
@SupportedSourceVersion(SourceVersion.RELEASE_8)

public abstract class ModBaseProcessor extends BaseProcessor{
    static final StringMap annotationProperties = new StringMap();
    public static String rootPackageName = null;
    static boolean markAnnotationSettingsPathElement = true;
    private static boolean createdSettingClass = false;

    private static Map<String, Set<String>> hierarchy;

    private static Set<String> allClasses;
    private static Reflections classPathReflections;
    AnnotationSettings annotationSettingsAnnotation;
    Element annotationSettingsAnnotationElement;
    AnnotationPropertiesPath annotationSettingsPath;
    Element annotationSettingsPathElement;

    {
        enableTimer = true;
    }

    @SuppressWarnings("unused")
    public static Set<String> allClasses(){
        computeHierarchy();
        return Collections.unmodifiableSet(allClasses);
    }

    @SuppressWarnings("unused")
    public static Map<String, Set<String>> hierarchy(){
        computeHierarchy();
        HashMap<String, Set<String>> m = new HashMap<>();
        hierarchy.forEach((key, values) -> m.put(key, Collections.unmodifiableSet(values)));
        return Collections.unmodifiableMap(m);
    }

    @SuppressWarnings("unused")
    public static void print(String obj, Object... args){
        String message = Strings.format(obj, args);
        System.out.println(message);
    }

    public static void write(TypeSpec.Builder builder, String packageName) throws Exception{
        //noinspection RedundantCast
        write(builder, packageName, (Seq<String>)null);
    }

    protected static Fi getFilesFi(StandardLocation location) throws IOException{
        return getFilesFi(location, "no", "no").parent().parent();
    }

    @SuppressWarnings("SameParameterValue")
    protected static Fi getFilesFi(StandardLocation location, String packageName, String className) throws IOException{
        return Fi.get(filer.getResource(location, packageName, className)
            .toUri().toURL().toString().substring(OS.isWindows ? 6 : "file:".length()));
    }

    @SuppressWarnings("unused")
    protected static List<TypeElement> collectAllTypeElement(){
        return allClasses.stream().map(elementu::getTypeElement).collect(Collectors.toList());
    }

    private static void collectEachChild(Cons<ClassSymbol> collector, Symbol symbol){
        List<Symbol> enclosedElements = symbol.getEnclosedElements();
        if(enclosedElements.size() == 0 || symbol instanceof ClassSymbol){
            if(symbol instanceof ClassSymbol){
                collector.get((ClassSymbol)symbol);
            }
            /*byte[] bytes = new byte[indent];
            Arrays.fill(bytes, (byte)' ');
            bytes[indent-1]='-';
            System.out.print(new String(bytes));
            System.out.print(symbol);
            System.out.println();*/
            return;
        }
        for(Symbol element : enclosedElements){
            collectEachChild(collector, element);
        }
    }

    @SuppressWarnings("unused")
    public static void warn(String message){
        messager.printMessage(Kind.WARNING, message);
    }

    @SuppressWarnings("unused")
    public static void warn(String message, Element elem){
        messager.printMessage(Kind.WARNING, message, elem);
    }

    @SuppressWarnings("unused")
    public static void note(String message){
        messager.printMessage(Kind.NOTE, message);
    }

    @SuppressWarnings("unused")
    public static void note(String message, Element elem){
        messager.printMessage(Kind.NOTE, message, elem);
    }

    private static void index(ClassSymbol it){
        Stype stype = new Stype(it);
        if(allClasses.add(stype.fullName())){
            try{
                Stype superclass = stype.superclass();
                if(superclass.e != null){
                    hierarchy.computeIfAbsent(superclass.fullName(), ignore -> new HashSet<>()).add(stype.fullName());
                }
            }catch(IndexOutOfBoundsException ignored){
                //it is interface or annotation, I guess
            }
        }
    }

    public static Reflections reflections(){
        if(classPathReflections == null){
            computeReflections();
        }
        return classPathReflections;
    }

    private static void computeHierarchy(){
        if(hierarchy != null) return;
        Reflections reflections = reflections();
        Context context = context();
        hierarchy = new HashMap<>();
        allClasses = new HashSet<>();
        Store store = reflections.getStore();
        store.get(Scanners.SubTypes.index()).forEach((key, values) -> {
            Set<String> strings = hierarchy.computeIfAbsent(key, ignore -> new HashSet<>());
            strings.addAll(values);
            allClasses.addAll(values);
        });
        Modules modules = Modules.instance(context);
        for(ModuleSymbol allModule : modules.allModules()){
            collectEachChild(ModBaseProcessor::index, allModule);
        }
    }

    private static void computeReflections(){
        if(classPathReflections != null) return;
        System.out.println("Init Reflections");
        long startNano = System.nanoTime();
        Options options = Options.instance(context());
        ConfigurationBuilder configuration = new ConfigurationBuilder();
        configuration.addUrls(Stream.of(options.get(Option.CLASS_PATH).split(";"))
            .map(it -> {
                try{
                    return new File(it).toURI().toURL();
                }catch(MalformedURLException e){
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toList())
        );
        configuration.addScanners(Scanners.values());
        long endNano = System.nanoTime();
        startNano = TimeUnit.NANOSECONDS.toMillis(startNano);
        endNano = TimeUnit.NANOSECONDS.toMillis(endNano);
        System.out.println("Time taken to init Reflections " + (endNano - startNano) + "ms");
        classPathReflections = new Reflections(configuration);
    }

    private static Context context(){
        return ((JavacProcessingEnvironment)processingEnvironment).getContext();
    }

    public ModMeta modInfo(){
        ModMeta meta = modInfoNull();
        if(meta == null){
            if(annotationSettingsAnnotation != null && !annotationSettingsAnnotation.modInfoPath().equals("\n")){

                err("Cannot find mod info file(" + rootDirectory.child(annotationSettingsAnnotation.modInfoPath()).file().getAbsolutePath() + ")", annotationSettingsAnnotationElement);
            }else{
                err("Cannot find mod info file");
            }
            throw new RuntimeException("Cannot find mod info file");
        }

        return meta;
    }

    @Nullable
    public ModMeta modInfoNull(){
        String annotationsSettings = annotationsSettings(AnnotationSettingsEnum.modInfoPath, "\n");
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
        boolean debug = list.length == 1 && list[0].name().equals("mmc") && annotationSettingsPath == null && annotationSettingsAnnotation == null;
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
        }// else           annotationPropertiesFile.writeString("");

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
            for(AnnotationSettingsEnum value : AnnotationSettingsEnum.values()){
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
                //noinspection OptionalGetWithoutIsPresent
                CompilationUnit compilationUnit = parser.parse(file.readString()).getResult().get();
                for(AnnotationExpr expr : compilationUnit.findAll(AnnotationExpr.class)){
                    if(!expr.getNameAsString().endsWith(AnnotationSettings.class.getSimpleName())){
                        continue;
                    }
                    //noinspection OptionalGetWithoutIsPresent
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

        CompilationUnit compilationUnit = new CompilationUnit(rootPackageName + ".mmc");
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
        //noinspection OptionalGetWithoutIsPresent
        zeroPackage
            .child(compilationUnit.getPackageDeclaration().get().getNameAsString().replace(".", "//"))
            .child(declaration.getNameAsString() + ".java")
            .writeString(compilationUnit.toString());
    }

    public String classPrefix(){
        String classNamePrefix = "Mod";
        if(!rootPackageName.equals("mmc")){
            classNamePrefix = annotationsSettings(AnnotationSettingsEnum.classPrefix, Strings.capitalize(rootPackageName));
        }
        return classNamePrefix;
    }

    public String annotationsSettings(AnnotationSettingsEnum settings, String defvalue){
        StringMap map = annotationsSettings();
        return map.containsKey(settings.name()) ? map.get(settings.name()) : defvalue;
    }

    public String annotationsSettings(AnnotationSettingsEnum settings, Prov<String> defvalue){
        StringMap map = annotationsSettings();
        return map.containsKey(settings.name()) ? map.get(settings.name()) : defvalue.get();
    }

    @Override
    protected String getPackageName(){
        packageName = (rootPackageName = annotationsSettings(AnnotationSettingsEnum.rootPackage, () -> {
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

    @SuppressWarnings("unused")
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

            String path = Fi.get(filer.getResource(StandardLocation.CLASS_OUTPUT, "no", "no").toUri().toURL().toString().substring(OS.isWindows ? 6 : "file:".length()))
                .parent().parent().parent().parent().parent().parent().parent().toString().replace("%20", " ");
            Fi fi = Fi.get(path);

            String rootDirectoryPath = stype == null ? "../" : stype.annotation(RootDirectoryPath.class).rootDirectoryPath();
            if(stype == null && annotationProperties.containsKey("ROOT_DIRECTORY")){
                String directory = annotationProperties.get("ROOT_DIRECTORY");
                System.out.println("ROOT_DIRECTORY "+directory);
                rootDirectory = Fi.get(directory);
            }else{
                rootDirectory = new Fi(fi.child(
                    !rootDirectoryPath.equals("\n") ? rootDirectoryPath : "../"
                ).file().getCanonicalFile());
            }
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
        if(hierarchy != null && round > 0){
            for(Element element : roundEnv.getRootElements()){
                if(element instanceof Symbol){
                    collectEachChild(ModBaseProcessor::index, (Symbol)element);
                }

            }
        }
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
        //noinspection UnnecessaryLocalVariable
        boolean process = super.process(annotations, roundEnv);
//        process=round-2  < rounds;
//        process=round-2  < rounds;
//        Log.info("@(@)", getClass().getCanonicalName(), process);
        return process;
    }

    public Fi rootDirectory(){
        return rootDirectory;
    }

    @Override
    public synchronized void init(ProcessingEnvironment env){
        super.init(env);
        Map<String, String> options = env.getOptions();
        for(AnnotationSettingsEnum setting : AnnotationSettingsEnum.values()){
            if(options.containsKey(setting.name())){
                String value = options.get(setting.name());
                if(value != null) annotationProperties.put(setting.name(), value);
            }
        }
        if(options.containsKey("ROOT_DIRECTORY")){
            annotationProperties.put("ROOT_DIRECTORY",options.get("ROOT_DIRECTORY"));
        }
//        System.out.println();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes(){
//        javax.annotation.processing.SupportedAnnotationTypes sat = this.getClass().getAnnotation(javax.annotation.processing.SupportedAnnotationTypes.class);
        SupportedAnnotationTypes sat2 = this.getClass().getAnnotation(SupportedAnnotationTypes.class);
        boolean initialized = isInitialized();
        if(sat2 != null){
            boolean stripModulePrefixes =
                initialized &&
                processingEnv.getSourceVersion().compareTo(SourceVersion.RELEASE_8) <= 0;

            Class<? extends Annotation>[] classes = sat2.value();
            String[] strings = new String[classes.length];
            for(int i = 0; i < strings.length; i++){
                strings[i] = classes[i].getCanonicalName();
            }
//            System.out.println("supported->"+Arrays.toString(strings));
            return arrayToSet(strings, stripModulePrefixes,
                "annotation type", "@SupportedAnnotationTypes");
        }
        return super.getSupportedAnnotationTypes();
    }

    @SuppressWarnings("SameParameterValue")
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
