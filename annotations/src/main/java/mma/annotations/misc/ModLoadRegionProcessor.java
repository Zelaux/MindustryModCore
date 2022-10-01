package mma.annotations.misc;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import com.squareup.javapoet.*;
import mindustry.annotations.*;
import mindustry.annotations.util.*;
import mma.annotations.*;
import mma.annotations.SupportedAnnotationTypes;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import java.util.*;
import java.util.regex.*;

@SupportedAnnotationTypes(mindustry.annotations.Annotations.Load.class)
public class ModLoadRegionProcessor extends ModBaseProcessor{


    static Pattern accessExpressionPattern = Pattern.compile("@(\\w+(\\(\\))?(\\.\\w+(\\(\\))?)*)*");
    static Pattern indexAccessPattern = Pattern.compile("#\\d*");

    private static int count(String str, String substring){
        int lastIndex = 0;
        int count = 0;

        while(lastIndex != -1){

            lastIndex = str.indexOf(substring, lastIndex);

            if(lastIndex != -1){
                count++;
                lastIndex += substring.length();
            }
        }
        return count;
    }

    private static String replacePattern(String value, Pattern pattern, Func<String, String> replacement){
        Matcher matcher = pattern.matcher(value);
        //replacing @access -> "+((type)content).access+"
//        System.out.println("==========================================");
//        System.out.println("value: "+value);
        if(matcher.find()){

//            System.out.println("found: "+value);
            StringJoiner joiner = new StringJoiner("");
            int prevIndex = 0;
            for(int i = 0; matcher.find(prevIndex); i++){

//                System.out.println("i: "+i+" field: "+expression);
                joiner.add(value.substring(prevIndex, matcher.start()));
                joiner.add(replacement.get(matcher.group()));
                prevIndex = matcher.end();
            }
            joiner.add(value.substring(prevIndex));
            value = joiner.toString();

        }
        return value;
    }

    @Override
    public void process(RoundEnvironment env) throws Exception{
        TypeSpec.Builder regionClass = TypeSpec.classBuilder(classPrefix() + "ContentRegions")
        .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "\"deprecation\"").build())
        .addModifiers(Modifier.PUBLIC);
        MethodSpec.Builder method = MethodSpec.methodBuilder("loadRegions")
        .addParameter(tname("mindustry.ctype.MappableContent"), "content")
        .addModifiers(Modifier.STATIC, Modifier.PUBLIC);

        ObjectMap<Stype, Seq<Svar>> fieldMap = new ObjectMap<>();

        for(Svar field : fields(Annotations.Load.class)){

//            if (field.enclosingType().fullName().equals("mma.MMAMod") && field.name().equals("fieldForGeneratingModContentRegions"))continue;
            if(!field.is(Modifier.PUBLIC)){
                err("@LoadRegion field must be public", field);
            }

            fieldMap.get(field.enclosingType(), Seq::new).add(field);
        }

        Seq<Stype> entries = Seq.with(fieldMap.keys());
        entries.sortComparing(e -> e.name());
        for(Stype type : entries){
            Seq<Svar> fields = fieldMap.get(type);
            fields.sortComparing(s -> s.name());
            method.beginControlFlow("if(content instanceof $L)", type.fullName());

            for(Svar field : fields){
                Annotations.Load an = field.annotation(Annotations.Load.class);
                //get # of array dimensions
                int dims = count(field.mirror().toString(), "[]");
                boolean doFallback = !an.fallback().equals("error");
                String fallbackString = doFallback ? ", " + parse(an.fallback(), type.fullName()) : "";

                //not an array
                if(dims == 0){
                    method.addStatement("(($T)content).$L = $T.atlas.find($L$L)", type.tname(), field.name(), Core.class, parse(an.value(), type.fullName()), fallbackString);
                }else{
                    //is an array, create length string
                    int[] lengths = an.lengths();
                    if(lengths.length == 0) lengths = new int[]{an.length()};

                    if(dims != lengths.length){
                        err("Length dimensions must match array dimensions: " + dims + " != " + lengths.length, field);
                    }

                    StringBuilder lengthString = new StringBuilder();
                    for(int value : lengths) lengthString.append("[").append(value).append("]");

                    method.addStatement("(($T)content).$L = new $T$L", type.tname(), field.name(), TextureRegion.class, lengthString.toString());

                    for(int i = 0; i < dims; i++){
                        method.beginControlFlow("for(int INDEX$L = 0; INDEX$L < $L; INDEX$L ++)", i, i, lengths[i], i);
                    }

                    StringBuilder indexString = new StringBuilder();
                    for(int i = 0; i < dims; i++){
                        indexString.append("[INDEX").append(i).append("]");
                    }

                    method.addStatement("(($T)content).$L$L = $T.atlas.find($L$L)", type.tname(), field.name(), indexString.toString(), Core.class, parse(an.value(), type.fullName()), fallbackString);

                    for(int i = 0; i < dims; i++){
                        method.endControlFlow();
                    }
                }
            }

            method.endControlFlow();
        }

        regionClass.addMethod(method.build());

        write(regionClass);
    }

    private String parse(String value, String type){
        value = '"' + value + '"';
//        StringBuilder builder = new StringBuilder("\"");
        value = replacePattern(value, accessExpressionPattern, group -> {
            String expression = group.substring(1);//removing "@"
            if (expression.isEmpty()){
                expression="name";
            }
            return "\" + ((" + type + ")content)." + expression + " + \"";
        });
//        value = value.replace("@size", "\" + ((mindustry.world.Block)content).size + \"");
//        value = value.replace("@", "\" + content.name + \"");
//        value = value.replace("#1", "\" + INDEX0 + \"");
//        value = value.replace("#2", "\" + INDEX1 + \"");
        value = replacePattern(value, indexAccessPattern, group -> {
            String number = group.substring(1);//removing "#"
            if (number.isEmpty()){
                number="0";
            }
            return "\" + INDEX" + number + " + \"";
        });
//        value = value.replace("#", "\" + INDEX0 + \"");
        return value;
    }
}
