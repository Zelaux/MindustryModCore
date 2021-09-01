package mma.tools.updateVersion;

import arc.files.Fi;
import arc.files.ZipFi;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Structs;
import arc.util.Time;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithPublicModifier;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import mindustry.ctype.ContentType;
import mma.tools.parsers.LibrariesDownloader;

import java.util.Locale;

public class ModPackingUpdater {
    static JavaParser javaParser = new JavaParser();
    static Fi rootDirectory;

    public static void run(String mindustryVersion, String[] args) {

        long nanos = System.nanoTime();
        Log.info("ModPacking update");

        ZipFi sourceZip = LibrariesDownloader.coreZip();
        Fi tools = sourceZip.list()[0].child("tools").child("src").child("mindustry").child("tools");
        rootDirectory = Fi.get("tools/src");

        createModGenerators(javaParser.parse(tools.child("Generators.java").readString()).getResult().get());
        createModImagePacker(javaParser.parse(tools.child("ImagePacker.java").readString()).getResult().get());

        System.out.println(Strings.format("Time taken: @s", Time.nanosToMillis(Time.timeSinceNanos(nanos)) / 1000f));
    }

    private static void createModImagePacker(CompilationUnit otherCompilationUnit) {
        CompilationUnit compilationUnit = otherCompilationUnit.clone();
        compilationUnit.setPackageDeclaration("mma.tools.gen");

        ClassOrInterfaceDeclaration imagePacker = compilationUnit.getClassByName("ImagePacker").get();
        imagePacker.setName("MindustryImagePacker");
        MethodDeclaration mainMethod = Structs.find(imagePacker.getMethods().toArray(new MethodDeclaration[0]), m -> m.getNameAsString().equals("main"));
        mainMethod.remove();
        Seq<NodeWithModifiers<? extends Node>> nodeWithModifiers = new Seq<>();
        imagePacker.walk(node -> {
            if (node instanceof NodeWithModifiers) {
                nodeWithModifiers.add((NodeWithModifiers<? extends Node>) node);
            }
        });
        for (NodeWithModifiers<?> nodeWithModifier : nodeWithModifiers) {
            if (!(nodeWithModifier instanceof NodeWithPublicModifier)) continue;
            if (nodeWithModifier.hasModifier(Modifier.Keyword.PUBLIC)) continue;
            if (nodeWithModifier.hasModifier(Modifier.Keyword.PRIVATE)) {
                nodeWithModifier.removeModifier(Modifier.Keyword.PRIVATE);
            }
            nodeWithModifier.addModifier(Modifier.Keyword.PUBLIC);
        }
        imagePacker.accept(new ModifierVisitor<Void>() {
            @Override
            public Visitable visit(NameExpr n, Void arg) {
                if (n.getNameAsString().equals("ImagePacker")) {
                    n.setName("MindustryImagePacker");
                }
                return super.visit(n, arg);
            }
        }, null);
        save(compilationUnit);
    }


    private static void createModGenerators(CompilationUnit otherCompilationUnit) {
        ClassOrInterfaceDeclaration generators = otherCompilationUnit.getClassByName("Generators").get();

        CompilationUnit compilationUnit = new CompilationUnit();
        compilationUnit.setPackageDeclaration("mma.tools.gen");
        compilationUnit.getImports().addAll(otherCompilationUnit.getImports());
        for (ImportDeclaration anImport : compilationUnit.getImports()) {
            if (anImport.getNameAsString().equals("mindustry.tools.ImagePacker")) {
                anImport.setName("mma.tools.gen.MindustryImagePacker");
            }
        }
        compilationUnit.addImport("mindustry.tools.Generators.ScorchGenerator", true, false);

        ClassOrInterfaceDeclaration modGenerators = compilationUnit.addClass("MindustryGenerators");
        modGenerators.addModifier(Modifier.Keyword.PUBLIC);
        for (FieldDeclaration field : generators.getFields()) {
            modGenerators.addMember(field.clone().addModifier(Modifier.Keyword.PUBLIC));
        }

        ConstructorDeclaration constructor = modGenerators.addConstructor(Modifier.Keyword.PUBLIC);
        constructor.getBody()
                .addStatement("setup();")
                .addStatement("run();");

        MethodDeclaration runMethod = modGenerators.addMethod("run", Modifier.Keyword.PROTECTED);
        MethodDeclaration disableMethod = modGenerators.addMethod("disable", Modifier.Keyword.PROTECTED);
        MethodDeclaration enableMethod = modGenerators.addMethod("enable", Modifier.Keyword.PROTECTED);
        modGenerators.addMethod("setup", Modifier.Keyword.PROTECTED)
        .getBody().get().addStatement("enable();")
        ;
        for (MethodDeclaration otherRun : generators.getMethods()) {
            if (otherRun.getNameAsString().equals("run")) {
                otherRun.accept(new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(VariableDeclarationExpr variableDeclarationExpr, Void arg) {
//                        super.visit(variableDeclarationExpr, arg);
                        FieldDeclaration fieldDeclaration = null;
                        for (VariableDeclarator variable : variableDeclarationExpr.getVariables()) {
                            VariableDeclarator current;
                            if (fieldDeclaration == null) {
                                fieldDeclaration = modGenerators.addField(variable.getType(), variable.getNameAsString(), Modifier.Keyword.PROTECTED);
                                current = fieldDeclaration.getVariable(0);
                                variable.getInitializer().ifPresent(expression -> {
                                    current.setInitializer(expression);
                                });
                            } else {
                                fieldDeclaration.addVariable(current = variable.clone());
                            }
                        }
                    }

                    @Override
                    public void visit(MethodCallExpr methodCall, Void arg) {
                        if (methodCall.getNameAsString().equals("generate")) {
                            StringLiteralExpr generatorName = methodCall.getArgument(0).asStringLiteralExpr();
                            LambdaExpr lambdaExpr = methodCall.getArgument(1).asLambdaExpr();
                            String methodName = Strings.kebabToCamel(generatorName.getValue());

                            BlockStmt body = null;
                            if (lambdaExpr.getBody().isBlockStmt()) {
                                body = lambdaExpr.getBody().asBlockStmt();
                            } else {
                                body = new BlockStmt();
                                body.addStatement(lambdaExpr.getBody());
                            }
                            String boolVarName = "generate" + Strings.capitalize(methodName);
                            modGenerators.addField(PrimitiveType.booleanType(), boolVarName, Modifier.Keyword.PROTECTED);
                            enableMethod.getBody().get()
                                    .addStatement(new AssignExpr(new NameExpr(boolVarName),new BooleanLiteralExpr(true), AssignExpr.Operator.ASSIGN));
                            disableMethod.getBody().get()
                                    .addStatement(new AssignExpr(new NameExpr(boolVarName),new BooleanLiteralExpr(false), AssignExpr.Operator.ASSIGN));

                            String parseLine = "if (!" + boolVarName + ") return;";
                            body.addStatement(0, javaParser.parseStatement(parseLine).getResult().get());
//                            body.getStatement(0).setComment(new LineComment(parseLine));

                            body.accept(new ModifierVisitor<Void>() {
                                @Override
                                public Visitable visit(MethodCallExpr methodCallExpr, Void arg) {
                                    String nameAsString = methodCallExpr.getNameAsString();

                                    if (nameAsString.equals("save")) {
                                        Expression argument = methodCallExpr.getArgument(1);
                                        StringLiteralExpr found = argument.accept(new GenericVisitorAdapter<StringLiteralExpr, Void>() {
                                            @Override
                                            public StringLiteralExpr visit(StringLiteralExpr n, Void arg) {
                                                StringLiteralExpr visit = super.visit(n, arg);
                                                if (n.getValue().contains("-full")) return n;
                                                return visit;
                                            }
                                        }, null);
                                        if (found != null) {
                                            argument.accept(new ModifierVisitor<Void>() {
                                                @Override
                                                public Visitable visit(StringLiteralExpr literalExpr, Void arg) {
                                                    Log.info("literalExpr: @",literalExpr);
                                                    for (ContentType type : ContentType.values()) {
                                                        String value = literalExpr.getValue();
                                                        String suffix = type.name() + "-";
                                                        if (value.endsWith(suffix)) {
                                                            Log.info("suffix: @",suffix);
                                                            int endIndex = suffix.length() + (value.endsWith("/" + suffix) ? 1 : 0);
                                                            literalExpr.setValue(value.substring(0, value.length()-endIndex));
                                                        }
                                                    }
                                                    return super.visit(literalExpr, arg);
                                                }
                                            }, null);
                                        }
                                        if (argument.isBinaryExpr()) {
                                        }
                                    } else if (nameAsString.equals("saveScaled")) {

                                    }
                                    return super.visit(methodCallExpr, arg);
                                }
                            }, null);
                            modGenerators.addMethod(methodName, Modifier.Keyword.PROTECTED).setBody(body);
                            runMethod.getBody().get().addStatement(new MethodCallExpr("generate").
                                    addArgument(generatorName).
                                    addArgument(new MethodReferenceExpr()
                                            .setScope(new ThisExpr())
                                            .setIdentifier(methodName)
                                    )
                            );
                            Log.info("generateName: @", methodName);
                        }
//                        super.visit(methodCall, arg);
                    }
                }, null);
            }
        }

        save(compilationUnit);
    }

    private static void save(CompilationUnit compilationUnit, String className) {
        PackageDeclaration packageDeclaration = compilationUnit.getPackageDeclaration().get();
        rootDirectory.child(packageDeclaration.getNameAsString().replace(".", "/")).child(className + ".java").writeString(compilationUnit.toString());
    }

    private static void save(CompilationUnit compilationUnit) {
        save(compilationUnit, compilationUnit.findFirst(ClassOrInterfaceDeclaration.class).get().getNameAsString());
    }
}
