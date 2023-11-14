package mmc.annotations.util;

import arc.struct.*;
import org.intellij.lang.annotations.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class StringTemplate{

    private final Seq<Object> parts = new Seq<>();

    public static StringTemplate compile(String text){
        StringTemplate template = new StringTemplate();
        int prevIndex = 0;
        @ParseState
        int state = ParseState.noState;
        int lineIndex = 0;
        for(int i = 0; i < text.length(); i++){
            char c = text.charAt(i);
            if(c == '\n') lineIndex = i;
            if(state == ParseState.noState){
                if(c == '$'){
                    template.push(text, prevIndex, i);
                    prevIndex = i;
                    state = ParseState.variableStart;
                    continue;
                }
            }else if(state == ParseState.variableStart){
                if(c != '{'){
                    parseException(text, lineIndex, i, "Expected '{'");
                }
                state = ParseState.openBraceFound;
                continue;
            }else if(state == ParseState.openBraceFound){
                if(c == '\n') parseException(text, lineIndex, i, "New Lines is not allowed in variable names");
                if(c != '}') continue;
                String substring = text.substring(prevIndex + 2, i).trim();
                if(substring.isEmpty()) parseException(text, lineIndex, i, "Empty variable name not allowed/");
                template.parts.add(new VariableRegion(substring));
                i++;
                prevIndex = i;
                state = ParseState.noState;
            }
            if(c == '\\'){
                state = ParseState.escaped;
                continue;
            }
            state = ParseState.noState;
        }
        if(prevIndex < text.length()){
            template.push(text, prevIndex, text.length());
        }
        return template;
    }

    private static void parseException(String text, int lineIndex, int charIndex, String error){
        int lineEndIndex = text.indexOf('\n', lineIndex + 1);
        String line = text.substring(lineIndex, lineEndIndex);
        int charInLine = charIndex - lineIndex;
        StringBuilder builder = new StringBuilder();
        builder.append(line).append('\n');
        //noinspection StringRepeatCanBeUsed
        for(int i = 0; i < charInLine - 1; i++){
            builder.append(' ');
        }
        builder.append('^').append(' ').append(error).append('[').append(lineIndex).append(':').append(charInLine).append(']');
        throw new RuntimeException(builder.toString());
    }

    private void push(String text, int prevIndex, int i){
        parts.add(text.substring(prevIndex, i));
    }

    public String toString(String... variableMap){
        ObjectMap<String, Object> map = new ObjectMap<>();
        for(int i = 0; i < variableMap.length / 2; i++){
            map.put(variableMap[i * 2], variableMap[i * 2 + 1]);
        }
        return toString(map);
    }

    public String toString(@Nullable ObjectMap<String, ?> variableMap){
        return toString(variableMap, true);
    }

    public String toString(@Nullable ObjectMap<String, ?> variableMap, boolean canThrown){
        String[] strings = new String[parts.size];
        for(int i = 0; i < parts.size; i++){
            Object o = parts.get(i);
            if(o instanceof VariableRegion){
                VariableRegion region = (VariableRegion)o;
                if(canThrown && variableMap!=null && !variableMap.containsKey(region.name)){
                    throw new RuntimeException("No such variable: " + region.name);
                }
                strings[i] = region.replaceByVariable(variableMap);
            }else{
                strings[i] = String.valueOf(o);
            }
        }
        return String.join("", strings);
    }

    public ObjectSet<String> variables(){
        return parts.select(it -> it instanceof VariableRegion).<VariableRegion>as().map(it -> it.name).asSet();
    }

    @MagicConstant(valuesFromClass = ParseState.class)
    private @interface ParseState{
        int noState = 0;
        int escaped = 1;
        int variableStart = 2;
        int openBraceFound = 3;
    }

    private static final class VariableRegion{
        public final String name;
        private final String toString;

        public VariableRegion(String name){
            this.name = name;
            toString = "${" + Objects.requireNonNull(this.name) + "}";
        }

        @Override
        public String toString(){
            return toString;
        }

        public String replaceByVariable(@Nullable ObjectMap<String, ?> variableMap){
            if(variableMap == null) return toString;
            //noinspection unchecked,rawtypes
            return String.valueOf(((ObjectMap)variableMap).get(name, toString));
        }
    }
}
