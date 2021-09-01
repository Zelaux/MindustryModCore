package mma.annotations.impl;

import arc.files.Fi;
import arc.func.Cons;
import arc.util.Log;
import com.squareup.javapoet.*;
import mindustry.annotations.BaseProcessor;
import mma.annotations.ModAnnotations;
import mma.annotations.ModBaseProcessor;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.HashSet;
import java.util.Set;

@SupportedAnnotationTypes("mma.annotations.ModAnnotations.ModAssetsAnnotation")
public class ModAssetsProcess extends ModBaseProcessor {
    static String capitalize(String s) {
        StringBuilder result = new StringBuilder(s.length());

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '_' && c != '-') {
                if (i > 0 && (s.charAt(i - 1) == '_' || s.charAt(i - 1) == '-')) {
                    result.append(Character.toUpperCase(c));
                } else {
                    result.append(c);
                }
            }
        }

        return result.toString();
    }

    @Override
    public void process(RoundEnvironment env) throws Exception {
        processSounds(classPrefix()+"Sounds", rootDirectory + "/core/assets/sounds", "arc.audio.Sound");
        processSounds(classPrefix()+"Musics", rootDirectory + "/core/assets/music", "arc.audio.Music");
        processUI(env.getElementsAnnotatedWith(ModAnnotations.StyleDefaults.class));
    }

    void processUI(Set<? extends Element> elements) throws Exception {
        TypeSpec.Builder type = TypeSpec.classBuilder(classPrefix()+"Tex").addModifiers(Modifier.PUBLIC);
//        TypeSpec.Builder ictype = TypeSpec.classBuilder("Icon").addModifiers(Modifier.PUBLIC);
//        TypeSpec.Builder ichtype = TypeSpec.classBuilder("Iconc").addModifiers(Modifier.PUBLIC);
        MethodSpec.Builder load = MethodSpec.methodBuilder("load").addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        String[] resourcesArray = {rootDirectory + "/core/assets-raw/sprites/ui",rootDirectory + "/core/assets-raw/sprites/cui"};
        Cons<Fi> walker = p -> {
            if (!p.extEquals("png")) return;

            String filename = p.name();
            filename = filename.substring(0, filename.indexOf("."));

            String sfilen = filename;
            String dtype = p.name().endsWith(".9.png") ? "arc.scene.style.NinePatchDrawable" : "arc.scene.style.TextureRegionDrawable";

            String varname = capitalize(sfilen);

            if (SourceVersion.isKeyword(varname)) varname += "s";

            type.addField(ClassName.bestGuess(dtype), varname, Modifier.STATIC, Modifier.PUBLIC);
            load.addStatement(varname + " = (" + dtype + ")arc.Core.atlas.drawable(mma.ModVars.fullName($S))", sfilen);
        };
        for (String resources : resourcesArray) {
            if (Fi.get(resources).exists()) {
                Fi.get(resources).walk(walker);
            }
        }

        type.addMethod(load.build());
        JavaFile.builder(packageName, type.build()).build().writeTo(BaseProcessor.filer);
    }

    void processSounds(String classname, String path, String rtype) throws Exception {
        TypeSpec.Builder type = TypeSpec.classBuilder(classname).addModifiers(Modifier.PUBLIC);
        MethodSpec.Builder loadBegin = MethodSpec.methodBuilder("load").addModifiers(Modifier.PUBLIC, Modifier.STATIC).addException(ClassName.get(Exception.class));

        HashSet<String> names = new HashSet<>();
        Fi.get(path).walk(p -> {
            String name = p.nameWithoutExtension();

            if (names.contains(name)) {
                BaseProcessor.err("Duplicate file name: " + p.toString() + "!");
            } else {
                names.add(name);
            }

            if (SourceVersion.isKeyword(name)) name += "s";

            String filepath = path.substring(path.lastIndexOf("/") + 1) + p.path().substring(p.path().lastIndexOf(path) + path.length());

            String filename = "\"" + filepath + "\"";

//            loadBegin.addStatement(Strings.format("@=new @(ModVars.modVars.modAssets.get(\"@\",\"@\"));", name, rtype, path.substring(path.lastIndexOf("/")+1) ,p.path().substring(p.path().lastIndexOf(path) + path.length()+1)));
//            loadBegin.addStatement(                    Strings.format("@=new @(ModVars.modVars.modInfo.root.child(@));",name,rtype,filename));
            loadBegin.addStatement("arc.Core.assets.load(" + filename + ", " + rtype + ".class).loaded = a -> " + name + " = (" + rtype + ")a", filepath, filepath.replace(".ogg", ".mp3"));

            type.addField(FieldSpec.builder(ClassName.bestGuess(rtype), name, Modifier.STATIC, Modifier.PUBLIC).initializer("new arc.audio." + rtype.substring(rtype.lastIndexOf(".") + 1) + "()").build());
        });

        if (classname.equals(classPrefix()+"Sounds")) {
            type.addField(FieldSpec.builder(ClassName.bestGuess(rtype), "none", Modifier.STATIC, Modifier.PUBLIC).initializer("new arc.audio." + rtype.substring(rtype.lastIndexOf(".") + 1) + "()").build());
        }

        type.addMethod(loadBegin.build());
        JavaFile.builder(packageName, type.build()).build().writeTo(BaseProcessor.filer);
    }
}
