package mma.tools.updateVersion;

import arc.files.Fi;
import arc.func.Cons;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.visitor.*;
import mma.tools.parsers.LibrariesDownloader;

public class AnnotationsUpdater {
    static JavaParser javaParser = new JavaParser();
    static Fi dir;

    public static void run(String mindustryVersion, String... args) {
        Fi root = LibrariesDownloader.coreZipRoot();
        dir = Fi.get("annotations/src/main/java/mindustry/annotations");
        for (String part : "annotations/src/main/java/mindustry/annotations".split("/")) {
            root = root.child(part);
        }
        root.child("Annotations.java").copyTo(dir.child("Annotations.java"));
        processBaseProcessor(root.child("BaseProcessor.java"), dir.child("BaseProcessor.java"));
        ;
        String prefix = root.absolutePath() + "/";
        root.child("util").walk(fi -> {
            if (fi.isDirectory()) return;
            Fi child = dir.child(fi.absolutePath().substring(prefix.length()));
            String string = fi.readString();
            child.writeString(string);
        });
    }

    private static void processBaseProcessor(Fi file, Fi dest) {
        CompilationUnit compilationUnit = javaParser.parse(file.readString()).getResult().get();
        ClassOrInterfaceDeclaration baseProcessor = compilationUnit.getClassByName("BaseProcessor").get();
        FieldDeclaration lastField = null;
        for (FieldDeclaration field : baseProcessor.getFields()) {
            for (VariableDeclarator variable : field.getVariables()) {
                if (variable.getNameAsString().equals("packageName")) {
//                    pair=new Pair<>(field,variable);
                    field.removeModifier(Modifier.Keyword.FINAL);
                    variable.removeInitializer();
                }
            }
            lastField = field;
        }

        NodeList<BodyDeclaration<?>> members = baseProcessor.getMembers();
        int i = members.indexOf(lastField);
        members.add(i + 1, new MethodDeclaration()
                .setName("getPackageName")
                .setModifiers(Modifier.Keyword.PROTECTED)
                .setType(new ClassOrInterfaceType().setName("String"))
                .setBody(new BlockStmt().addStatement("return \"mindustry.gen\";"))
        );

        for (MethodDeclaration method : baseProcessor.getMethods()) {
            if (method.getNameAsString().equals("process") && method.getType().isPrimitiveType() && method.getType().asPrimitiveType().toDescriptor().equals("Z")) {
//                BlockStmt blockStmt = method.getBody().get();
                method.accept(new ModifierVisitor<Void>(){
                    @Override
                    public Visitable visit(IfStmt ifStmt, Void arg) {
                        if (ifStmt.getCondition().toString().equals("rootDirectory == null")){
                            ifStmt.getThenStmt().asBlockStmt().addStatement(javaParser.parseStatement("packageName=getPackageName();").getResult().get());
                        }
                        return super.visit(ifStmt, arg);
                    }

                    @Override
                    public Visitable visit(TryStmt tryStmt, Void arg) {

                        BlockStmt tryBlock = tryStmt.getTryBlock();
                        ExpressionStmt expressionStmt = tryBlock.accept(new GenericVisitorAdapter<ExpressionStmt, Void>() {
                            @Override
                            public ExpressionStmt visit(ExpressionStmt n, Void arg) {
                                if (n.getExpression().toString().equals("process(roundEnv)")) {
                                    return n;
                                }
                                return super.visit(n, arg);
                            }
                        }, null);
                        if (expressionStmt!=null){
                            tryBlock.addStatement(tryBlock.getStatements().indexOf(expressionStmt),javaParser.parseStatement(
                                    "if (enableTimer) {\n" +
                                    "Log.info(getClass().getSimpleName() + \".work(\" + round + \")\");\n" +
                                    "Time.mark();\n" +
                                    "}\n").getResult().get());
                            tryBlock.addStatement(tryBlock.getStatements().indexOf(expressionStmt)+1,javaParser.parseStatement(
                                    "if (enableTimer) {\n" +
                                    "Log.info(getClass().getSimpleName() + \".work(\" + round + \").time=@ms\", Time.elapsed());\n" +
                                    "}\n").getResult().get());
                            baseProcessor.addField(PrimitiveType.booleanType(),"enableTimer", Modifier.Keyword.PROTECTED);
                        }
                        return super.visit(tryStmt, arg);
                    }
                },null);
            } else if (method.getNameAsString().equals("write") &&
                       method.getParameters().toString().equals("[TypeSpec.Builder builder, Seq<String> imports]")) {
                BlockStmt blockStmt = method.getBody().get();
                BlockStmt clone = blockStmt.clone();
                blockStmt.getStatements().clear();
                blockStmt.addStatement("write(builder, packageName, imports);");

                members.add(members.indexOf(method) + 1, new MethodDeclaration(clone(method.getModifiers()), new VoidType(), "write")
                        .addParameter(method.getParameter(0))
                        .addParameter(javaParser.parseParameter("String packageName").getResult().get())
                        .addParameter(method.getParameter(1))
                        .setBody(clone)
                        .setThrownExceptions(clone(method.getThrownExceptions()))
                );
            }
        }
        dest.writeString(compilationUnit.toString());
    }

    private static <T extends Node> NodeList<T> clone(NodeList<T> nodeList) {
        return new NodeList<>(nodeList);
    }
}
