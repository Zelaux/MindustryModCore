package mmc.annotations.impl;

import arc.files.*;
import arc.func.*;
import arc.struct.*;
import arc.util.*;
import com.squareup.javapoet.*;
import mindustry.*;
import mindustry.annotations.*;
import mindustry.mod.Mods.*;
import mmc.annotations.SupportedAnnotationTypes;
import mmc.annotations.*;

import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import java.util.*;

@SupportedAnnotationTypes(mmc.annotations.ModAnnotations.ModAssetsAnnotation.class)
public class ModAssetsProcess extends ModBaseProcessor{
    static String capitalize(String s){
        StringBuilder result = new StringBuilder(s.length());

        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if(c != '_' && c != '-'){
                if(i > 0 && (s.charAt(i - 1) == '_' || s.charAt(i - 1) == '-')){
                    result.append(Character.toUpperCase(c));
                }else{
                    result.append(c);
                }
            }
        }

        return result.toString();
    }

    @Override
    public void process(RoundEnvironment env) throws Exception{
        boolean root = rootPackageName.equals("mmc");
        if(root) return;
        String path = getAssetsPath();
        processSounds(classPrefix() + "Sounds", path + "/sounds", "arc.audio.Sound");
        processSounds(classPrefix() + "Musics", path + "/music", "arc.audio.Music");
        processUI(env.getElementsAnnotatedWith(Annotations.StyleDefaults.class));
    }

    private String getAssetsPath(){
        return rootDirectory + "/" + annotationsSettings(AnnotationSettingsEnum.assetsPath, "core/assets");
    }

    void processUI(Set<? extends Element> elements) throws Exception{
        TypeSpec.Builder type = TypeSpec.classBuilder(classPrefix() + "Tex").addModifiers(Modifier.PUBLIC);
//        TypeSpec.Builder ictype = TypeSpec.classBuilder("Icon").addModifiers(Modifier.PUBLIC);
//        TypeSpec.Builder ichtype = TypeSpec.classBuilder("Iconc").addModifiers(Modifier.PUBLIC);
        MethodSpec.Builder loadStyles = MethodSpec.methodBuilder("loadStyles").addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        MethodSpec.Builder load = MethodSpec.methodBuilder("load").addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        load.addStatement("$T __loadedMod__=$T.mods.getMod($S)", LoadedMod.class, Vars.class,modInfo().name);
        String assetsRawPath = annotationsSettings(AnnotationSettingsEnum.assetsRawPath, rootDirectory + "/core/assets-raw");
        if(assetsRawPath.equals("null")) assetsRawPath = getAssetsPath();
        String[] resourcesArray = {assetsRawPath + "/sprites/ui"/*, assetsRawPath + "/sprites/cui"*/};
        Cons<Fi> walker = p -> {
            if(!p.extEquals("png")) return;

            String filename = p.name();
            filename = filename.substring(0, filename.indexOf("."));

            String sfilen = filename;
            String dtype = p.name().endsWith(".9.png") ? "arc.scene.style.NinePatchDrawable" : "arc.scene.style.TextureRegionDrawable";

            String varname = capitalize(sfilen);

            if(SourceVersion.isKeyword(varname)) varname += "s";

            type.addField(ClassName.bestGuess(dtype), varname, Modifier.STATIC, Modifier.PUBLIC);
            load.addStatement(varname + " = (" + dtype + ")arc.Core.atlas.drawable(__loadedMod__.name+\"_\"+$S)", sfilen);
        };


        for(Element elem : elements){
            Seq.with(elem.getEnclosedElements()).each(e -> e.getKind() == ElementKind.FIELD, field -> {
                String fname = field.getSimpleName().toString();
                if(fname.startsWith("default")){
                    loadStyles.addStatement("arc.Core.scene.addStyle(" + field.asType().toString() + ".class, " + field.getEnclosingElement().toString() + "." + fname + ")");
                }
            });
        }
        for(String resources : resourcesArray){
            Fi folder = rootDirectory.child(resources);
            if(folder.exists()){
                folder.walk(walker);
//                warn("Found ui folder "+ folder.file().getAbsolutePath());
            }else{
//                warn("Cannot find ui folder "+ folder.file().getAbsolutePath());
            }
        }

        type.addMethod(load.build());
        type.addMethod(loadStyles.build());
        JavaFile.builder(packageName, type.build()).build().writeTo(BaseProcessor.filer);
    }

    void processSounds(String classname, String path, String rtype) throws Exception{
        System.out.println("classname: " + classname + ", path: " + path);
        TypeSpec.Builder type = TypeSpec.classBuilder(classname).addModifiers(Modifier.PUBLIC);
        MethodSpec.Builder loadBegin = MethodSpec.methodBuilder("load").addModifiers(Modifier.PUBLIC, Modifier.STATIC).addException(ClassName.get(Exception.class));
        MethodSpec.Builder loadNowBegin = MethodSpec.methodBuilder("loadNow").addModifiers(Modifier.PUBLIC, Modifier.STATIC).addException(ClassName.get(Exception.class));

        HashSet<String> names = new HashSet<>();

        loadNowBegin.addStatement("$T __loadedMod__=$T.mods.getMod($S)", LoadedMod.class, Vars.class,modInfo().name);
        Fi.get(path).walk(p -> {
//            System.out.println("p: "+p);
            String name = p.nameWithoutExtension();
            if(names.contains(name)){
                BaseProcessor.err("Duplicate file name: " + p + "!");
            }else{
                names.add(name);
            }

            if(SourceVersion.isKeyword(name)) name += "s";

            String filepath = path.substring(path.lastIndexOf("/") + 1) + p.path().substring(p.path().lastIndexOf(path) + path.length());

            String filename = "\"" + filepath + "\"";

//            loadBegin.addStatement(Strings.format("@=new @(ModVars.modVars.modAssets.get(\"@\",\"@\"));", name, rtype, path.substring(path.lastIndexOf("/")+1) ,p.path().substring(p.path().lastIndexOf(path) + path.length()+1)));
//            loadBegin.addStatement(                    Strings.format("@=new @(ModVars.modVars.modInfo.root.child(@));",name,rtype,filename));
            loadBegin.addStatement("arc.Core.assets.load(" + filename + ", " + rtype + ".class).loaded = a -> " + name + " = (" + rtype + ")a", filepath, filepath.replace(".ogg", ".mp3"));
            loadNowBegin.addStatement(name + " = new " + rtype + "(__loadedMod__.root.child("+ Strings.join("\").child(\"",filename.split("[/\\\\]")) +"))");
//            loadNowBegin.addStatement("arc.Core.assets.load(" + filename + ", " + rtype + ".class).loaded = a -> " + name + " = (" + rtype + ")a", filepath, filepath.replace(".ogg", ".mp3"));

            type.addField(FieldSpec.builder(ClassName.bestGuess(rtype), name, Modifier.STATIC, Modifier.PUBLIC).initializer("new arc.audio." + rtype.substring(rtype.lastIndexOf(".") + 1) + "()").build());
        });

        if(classname.equals(classPrefix() + "Sounds")){
            type.addField(FieldSpec.builder(ClassName.bestGuess(rtype), "none", Modifier.STATIC, Modifier.PUBLIC).initializer("new arc.audio." + rtype.substring(rtype.lastIndexOf(".") + 1) + "()").build());
        }

        type.addMethod(loadBegin.build());
        type.addMethod(loadNowBegin.build());
        JavaFile.builder(packageName, type.build()).build().writeTo(BaseProcessor.filer);
    }
}
