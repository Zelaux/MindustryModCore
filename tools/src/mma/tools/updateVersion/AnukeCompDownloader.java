package mma.tools.updateVersion;

import arc.files.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.JsonWriter.*;
import arc.util.serialization.Jval.*;
import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.Modifier.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.*;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration.ConfigOption;
import com.github.javaparser.printer.configuration.Indentation;
import mma.tools.parsers.*;

import java.util.*;

public class AnukeCompDownloader{
    protected static final String annotationsClassName = "ModAnnotations";
    protected static final JavaCodeConverter codeConverter = new JavaCodeConverter(false);
    protected static final Seq<String> mindustryAnnotations = new Seq<>();
    protected static String packageName = null;
    protected static String selectedClassName = "";

    public static void run(String mindustryVersion, String[] args){
        Seq<String> argsSeq = Seq.with(args);
        Fi folder = new Fi("debug");
        try{
            if(packageName == null){
                packageName = argsSeq.select(a -> a.startsWith("package=")).map(s -> s.substring("package=".length())).find(f -> true);
                if(packageName == null){
                    packageName = Fi.get("core/src").list()[0].nameWithoutExtension();
                }
            }
            Fi outDirectory = new Fi("core/src/" + packageName + "/entities/compByAnuke");

            if(outDirectory.exists()) outDirectory.delete();
            folder.mkdirs();
            Fi compJava = folder.child("compJava");
            Fi finalComp = folder.child("finalComp");


            ZipFi sourceZip = LibrariesDownloader.coreZip();

            Fi child = sourceZip.list()[0].child("core").child("src").child("mindustry").child("entities").child("comp");
            Fi annotation = sourceZip.list()[0].child("annotations").child("src").child("main").child("java")
            .child("mindustry").child("annotations").child("Annotations.java");
            loadAnnotations(annotation);
            for(Fi fi : child.list()){
                fi.copyTo(compJava.child(fi.name()));
            }
            boolean useBlackList = false;
            for(Fi fi : compJava.list()){
                if(fi.isDirectory()) continue;
                String className = fi.nameWithoutExtension();
                selectedClassName = className;
                if(useBlackList){
                    if(Seq.with("BuildingComp", "BulletComp", "DecalComp", "EffectStateComp", "FireComp", "LaunchCoreComp", "PlayerComp", "PuddleComp").contains(className)){
                        compJava.child(fi.name()).delete();
                        finalComp.child(fi.name()).delete();
                        continue;
                    }
                    if(className.equals("BuildingComp") ||
                       className.equals("BulletComp") ||
                       className.equals("DecalComp") ||
                       className.equals("EffectStateComp") ||
                       className.equals("FireComp") ||
                       className.equals("LaunchCoreComp") ||
                       className.equals("PlayerComp") ||
                       className.equals("PuddleComp") ||
                       className.equals("PosTeamDef")
                    ){
                        Log.info("@ skipped", className);
                        continue;
                    }
                }
                String file = fi.readString();
                String convert = fixCode(codeConverter.convert(file, className));
                finalComp.child(fi.name()).writeString(convert);
            }
            Seq<String> names = new Seq<>();
            for(Fi fi : finalComp.list()){
                fi.copyTo(outDirectory.child(fi.name()));
                names.add(fi.nameWithoutExtension());
            }

            createAnnotationsConfigClass(outDirectory, names);

            CompilationUnit compilationUnit = new CompilationUnit();
            ClassOrInterfaceDeclaration compData = compilationUnit.addClass("CompData", new Keyword[0]);
            Fi compDataFile = Fi.get("annotations/src/main/java/" + packageName + "/annotations/entities/CompData.java");
            compilationUnit.addImport(ObjectMap.class);
            compData.addField("ObjectMap<String,String>", "compMap", Keyword.STATIC, Keyword.FINAL);
            compData.addField("String", "groupDefs", Keyword.STATIC, Keyword.FINAL);
            BlockStmt initializer = compData.addStaticInitializer();
            initializer.addAndGetStatement("compMap = new ObjectMap<>()");
            for(Fi file : outDirectory.list()){
                String compName = file.nameWithoutExtension();
                CompilationUnit unit = codeConverter.javaParser.parse(file.readString()).getResult().get();
                DefaultPrinterConfiguration configuration = new DefaultPrinterConfiguration();
                configuration.addOption(new DefaultConfigurationOption(ConfigOption.INDENTATION,new Indentation(Indentation.IndentType.SPACES, 2)));
//                configuration.addOption(new DefaultConfigurationOption(ConfigOption.INDENTATION,new Indentation(Indentation.IndentType.SPACES, 0)));
                String code =unit.toString(configuration); //Strings.format("@", file.readString()
                /*.replace("\"", "\\\"").replace("'", "\\'").replace("\\n", "\\\\n")*/
//                );
//                file.sibling(file.nameWithoutExtension()+"__.java").writeString(code);
                NodeList<Expression> list = new NodeList<>();
                for(String line : code.split("\n")){
                    list.add(StaticJavaParser.parseExpression(Jval.valueOf(line+"\n").toString(Jformat.formatted)));
                }

                initializer.addAndGetStatement(Strings.format("compMap.put(\"@\",String.join(\"\",@))", compName, new ArrayCreationExpr(StaticJavaParser.parseType("String"),
                NodeList.nodeList(new ArrayCreationLevel()),
                new ArrayInitializerExpr(list)
                )));
//                initializer.addAndGetStatement(Strings.format("compMap.put(\"@\",@)", compName, StaticJavaParser.parseExpression(Jval.valueOf(code).toString(Jformat.formatted))));
            }
            initializer.addAndGetStatement("groupDefs=" + new StringLiteralExpr(
            Fi.get("core/src/" + packageName + "/entities/GroupDefs.java").readString()
            .replace("\"", "\\\"").replace("\'", "\\\'")
            ));
            EntityGroupsUpdater.run(mindustryVersion,compData,initializer,args);
            compilationUnit.setPackageDeclaration(packageName + ".annotations.entities");
            compDataFile.writeString(compilationUnit.toString());
        }catch(Exception e){
            e.printStackTrace();
        }
        folder.deleteDirectory();
        folder.walk(f -> f.delete());
        folder.delete();
    }

    protected static void loadAnnotations(Fi annotations){
        ParseResult<CompilationUnit> result = codeConverter.javaParser.parse(annotations.readString());
        CompilationUnit compilationUnit = result.getResult().get();
        mindustryAnnotations.clear();
        compilationUnit.accept(new VoidVisitorAdapter<Void>(){
            @Override
            public void visit(AnnotationDeclaration n, Void arg){

                mindustryAnnotations.add((String)n.getNameAsString());
            }
        }, null);
    }

    protected static String fixCode(String convert){
        CompilationUnit compilationUnit = codeConverter.javaParser.parse(convert).getResult().get();
        compilationUnit.setPackageDeclaration(packageName + ".entities.compByAnuke");
        compilationUnit.addImport("mindustry.logic.LAccess", true, true);
        Seq<AnnotationExpr> annotationExprs = new Seq<>();
        String className = compilationUnit.findFirst(ClassOrInterfaceDeclaration.class).get().getNameAsString();
        compilationUnit.walk(node -> {
            if(node instanceof AnnotationExpr){
                annotationExprs.add((AnnotationExpr)node);
            }
        });
        for(AnnotationExpr annotationExpr : annotationExprs){
            String identifier = annotationExpr.getName().getIdentifier();
            if(mindustryAnnotations.contains(identifier)){
//                annotationExpr.getName().setQualifier(new Name(packageName + ".annotations." + annotationsClassName));
            }
            if(identifier.equals("EntityDef")){
                if(true){
                    annotationExpr.setName(packageName + ".annotations." + annotationsClassName + ".MindustryEntityDef");
                }else{
                    Node parentNode = annotationExpr.getParentNode().get();
                    NodeWithAnnotations<?> parent = (NodeWithAnnotations<?>)parentNode;
                    int i = parent.getAnnotations().indexOf(annotationExpr);
                    Comment comment = new LineComment(annotationExpr.toString());
                    if(i == parent.getAnnotations().size() - 1){
                        addComment(parentNode, comment, true);
                    }else{
                        AnnotationExpr next = parent.getAnnotations().get(i + 1);
                        addComment(next, comment, true);
                    }

//                parent.addOrphanComment();
                    annotationExpr.remove();
                }
            }
        }
        return compilationUnit.toString();
    }

    protected static void addComment(Node parent, Comment comment, boolean replace){
        Optional<Comment> optional = parent.getComment();
        if(optional.isPresent()){
            if(replace){
                Comment other = optional.get();
                parent.setComment(comment);
                comment.setRange(other.getRange().orElse(null));
                addComment(parent, other, false);
                return;
            }
            Range other = optional.get().getRange().get();
            String string = comment.toString();

            String[] split = string.split("\n");
            Range range = Range.range(other.begin.line - (int)split.length, 1, other.begin.line, split[split.length - 1].length());
            comment.setRange(range);
            parent.findCompilationUnit().get().addOrphanComment(comment);
        }else{
            parent.setComment(comment);
        }
    }

    protected static void createAnnotationsConfigClass(Fi dir, Seq<String> names){

        CompilationUnit compilationUnit = new CompilationUnit();
        compilationUnit.setPackageDeclaration(packageName + ".entities.compByAnuke");
        compilationUnit.addImport(packageName + ".annotations." + annotationsClassName, false, false);
//        compilationUnit.addImport("mindustry.gen", false, true);
        ClassOrInterfaceDeclaration annotationConfig = compilationUnit.addClass("AnnotationConfigComponents");

        for(String name : names){
            if(!name.endsWith("Comp")) continue;
            String interfaceName = interfaceName(name);
            ClassOrInterfaceDeclaration comp = new ClassOrInterfaceDeclaration();
            annotationConfig.addMember(comp);
            comp
//            .addModifier(Modifier.Keyword.PUBLIC)
            .setInterface(true)
            .setName(interfaceName)
            .getExtendedTypes().add(codeConverter.javaParser.parseClassOrInterfaceType("mindustry.gen." + interfaceName).getResult().get());
            comp.addAnnotation(annotationsClassName + ".EntitySuperClass");
        }
        dir.child("AnnotationConfigComponents.java").writeString(compilationUnit.toString());
    }

    static String interfaceName(String comp){
        String suffix = "Comp";

        //example: BlockComp -> IBlock
        return comp.substring(0, comp.length() - suffix.length()) + "c";
    }

    protected static Seq<String> transform(String line, Seq<String> out){
        out = new Seq<>();
        boolean debug = selectedClassName.equals("BuilderComp") && false;
        if(line.contains("return switch")){
            String[] split = line.split("\n");
            StringBuilder newLine = new StringBuilder();
            boolean lineReturn = false;
            int opened = 0;
            for(String s : split){
                String l = s
                .replace("return switch", "switch")
                .replace(" -> ", ":\nreturn ");
                if(l.contains(":\nreturn ")){
                    lineReturn = true;
//                    newLine.append(l, 0, l.indexOf("\nreturn")).append("\nreturn");
                }
                if(l.contains(";")) lineReturn = false;
                newLine.append(l);
                if(!lineReturn) newLine.append("\n");
            }
            String s1 = newLine.toString();
            StringBuilder g = new StringBuilder(s1.split("\n", 2)[0]);
            for(String s2 : s1.split("\n", 2)[1].split("\n")){
                String tran = transform(s2);
//                Log.info("[@]->[@]", s2, tran);
                g.append(tran).append("\n");
            }
            out.add(g.toString());
            return out;
        }
        try{
            if(line.contains("instanceof ")){
                line = line.replace("\n\n", "\n");
                if(line.contains("->")){
                    int brackets = 0;
                    for(int i = 0; i < line.length(); i++){
                        char c = line.charAt(i);
                        if(c == '(') brackets++;
                        if(c == ')') brackets--;
                    }
                    boolean monoLine = brackets == 0;
                    String preCenter = line.substring(0, line.indexOf('('));
                    String center = line.substring(line.indexOf('('), line.lastIndexOf(')') + 1);
                    String postCenter = line.substring(line.lastIndexOf(')'));
                    if(monoLine){
                        boolean curlyBraces = center.substring(center.indexOf("->")).replace(" ", "").startsWith("{");
                        if(curlyBraces){
                            String part1 = center.substring(0, center.indexOf("{") + 1);
                            String partCode = center.substring(center.indexOf("{") + 1, center.lastIndexOf("}"));
                            String part2 = center.substring(center.lastIndexOf("}"));
                            line = preCenter + part1 + transform(partCode) + part2 + postCenter;
                        }else{
                            String part1 = center.substring(0, center.indexOf("->") + 2) + "{";
                            String partCode = "return " + center.substring(center.indexOf("->") + 2, center.lastIndexOf(")")) + ";";
                            String part2 = "}";
                            line = preCenter + part1 + transform(partCode) + part2 + postCenter;
                        }
                    }else{
                        //            controlling.removeAll(u -> u.dead || !(u.controller() instanceof FormationAI ai && ai.leader == self()));
                        throw new RuntimeException("It's not monoline, I don't know what to do!!!");
                    }
                }else{
                    String deb = line.split(" instanceof", 2)[0];
                    if(debug){
                        Log.info("line: @", line);
                    }
                    int opened = 0;
                    String instanceName = "";
                    String[] g17 = deb.split("");
                    if(deb.endsWith(" self()")) instanceName = "self()";
                    String returnCheck = deb.contains("return") ? deb.substring(deb.indexOf("return ")) : "";
                    if(returnCheck.split(" ").length == 2 && returnCheck.startsWith("return "))
                        instanceName = returnCheck.substring(returnCheck.indexOf(" ") + 1);
                    if(deb.substring(0, deb.lastIndexOf(" ")).replace(" ", "").equals("")) instanceName = deb;
                    for(int i = g17.length - 1; i >= 0; i--){
                        String symbol = g17[i];
                        if(symbol.equals(")")){
                            opened++;
                        }
                        if(opened > 0){
                            if(symbol.equals("(")) opened--;
                        }else if(symbol.equals("(") || symbol.equals("&") || symbol.equals("|") || symbol.equals("?") || symbol.equals(":")){
                            instanceName = deb.substring(i + 1);
                            break;
                        }

                    }

                    if(debug){
                        Log.info("instanceName: @", instanceName);
                    }
                    if(instanceName.equals("")){
                        out.clear();
                        out.add(line);
                        return out;
                    }
//                Log.info(instanceName);
                    String[] split = line.split("instanceof ", 2)[1].split(" ", 2);
                    String[] strings = split[1].split("");
                    StringBuilder variableBuild = new StringBuilder();
                    Seq<String> with = Seq.with(")", "&", "|", "?");
                    for(int i = 0; i < strings.length; i++){
                        String string = strings[i];
                        if(with.contains(string)) break;
                        variableBuild.append(string);
                    }
                    String variableName = variableBuild.toString();
                    if(debug){
                        Log.info("variableName: @", variableName);
                    }
                    if(variableName.replace(" ", "").equals("")){
                        out.clear();
                        out.add(line);
                        return out;
                    }
                    int index = split[1].indexOf(variableName);
                    String part1 = line.substring(0, line.indexOf("instanceof ") + "instanceof ".length()) + split[0] + split[1].substring(0, index);
                    String part2 = split[1].substring(index + variableName.length());
                    String l = " && (" + variableName + " = (" + split[0] + ")" + instanceName + ")==" + instanceName + "\n", nl = split[0] + " " + variableName.replace(" ", "") + ";";
//            Log.info("===@",part1);
                    out.add(nl);
                    line =/* nl + "\n" +*/ part1 + l + part2;
//            Log.info("==@",Seq.with(split).toString(","));
                    if(split.length > 3 && !split[2].equals("&&") && !split[2].equals("||") && !split[2].equals(")") && !split[2].equals("}")){
                    }
                }
            }
        }catch(Exception e){
            if(debug){
                Log.err("cannot transform line: [" + line + "] reason: @", e);
            }else{
                Log.err("cannot transform line: [@]", line);
            }
        }
//       line= line.replace(" : "," \n: ");
        line = line.replace("};\n" +
                            "    }", "}\n}");
        if(line.contains("\n")){
            StringBuilder nl = new StringBuilder(), prel = new StringBuilder();
            for(String s : line.split("\n")){
                Seq<String> transform = transform(s, null);
                if(transform.size > 1){
                    out.set(0, out.get(0) + "\n" + transform.get(0));
                    if(out.size == 2){
                        out.set(1, out.get(1) + "\n" + transform.get(1));
                    }else{
                        out.add(transform.get(1));
                    }
                }else{
                    if(out.size == 2){
                        out.set(1, out.get(1) + "\n" + transform.get(0));
                    }else{
                        out.add(transform.get(0));
                    }

                }
            }
            line = prel.toString() + nl.toString();
        }


        out.add(line);
//        return line;
        return out;
    }

    protected static String transform(String line){
        Seq<String> transform = transform(line, null);
        return transform.toString("\n");
    }

    protected static String strip(String str){
        while(str.startsWith(" ")){
            str = str.substring(1);
        }
        while(str.endsWith(" ")){
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    protected static boolean isMethod(String mline){
        return mline.contains("(") && mline.contains(")") && mline.contains("{") && mline.startsWith("    ") && !mline.substring(4).startsWith("    ");
    }
}