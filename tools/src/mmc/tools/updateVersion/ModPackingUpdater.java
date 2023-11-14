package mmc.tools.updateVersion;

import arc.files.*;
import arc.func.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.Modifier.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.*;
import com.github.javaparser.ast.nodeTypes.modifiers.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.*;
import mindustry.ctype.*;
import mmc.core.*;
import mmc.tools.*;
import mmc.tools.parsers.*;

import java.util.*;

public class ModPackingUpdater{
    static JavaParser javaParser = new JavaParser();
    static Fi rootDirectory;

    public static void run(String mindustryVersion, String[] args){


        ZipFi sourceZip = LibrariesDownloader.coreZip();
        Fi tools = sourceZip.list()[0].child("tools").child("src").child("mindustry").child("tools");
        rootDirectory = Fi.get("tools/src");

        createModGenerators(javaParser.parse(tools.child("Generators.java").readString()).getResult().get());
        createModImagePacker(javaParser.parse(tools.child("ImagePacker.java").readString()).getResult().get());
//        createMindustrySpritesContainer();

    }

    private static void createMindustrySpritesContainer(){
        CompilationUnit compilationUnit = new CompilationUnit("mmc.tools.gen");
        ClassOrInterfaceDeclaration declaration = compilationUnit.addClass("MindustrySpritesContainer");
        long[] longs = ByteLongMapper.getLongs(Fi.get("core/mindustrySprites.zip").readBytes());
        declaration.addField(StaticJavaParser.parseType("long[]"),"mindustrySpritesLongs",Keyword.PUBLIC,Keyword.STATIC).getVariable(0)
        .setInitializer(Strings.format("new long[]{@}",LongSeq.with(longs).toString(", ")));
        save(compilationUnit,"MindustrySpritesContainer");
    }

    private static void createModImagePacker(CompilationUnit otherCompilationUnit){
        CompilationUnit compilationUnit = otherCompilationUnit.clone();
        compilationUnit.setPackageDeclaration("mmc.tools.gen");

        ClassOrInterfaceDeclaration imagePacker = compilationUnit.getClassByName("ImagePacker").get();
        imagePacker.setName("MindustryImagePacker");
        MethodDeclaration mainMethod = Structs.find(imagePacker.getMethods().toArray(new MethodDeclaration[0]), m -> m.getNameAsString().equals("main"));
        Seq<String> defMethods = Seq.with(imagePacker.getMethods()).map(NodeWithSimpleName::getNameAsString);
        mainMethod.remove();

        imagePacker.addField(PrimitiveType.booleanType(), "disableIconProcessing", Modifier.Keyword.PROTECTED).getVariables().getFirst().get()
        .setInitializer(new BooleanLiteralExpr(false));

        Seq<NodeWithModifiers<? extends Node>> nodeWithModifiers = new Seq<>();
        imagePacker.walk(node -> {
            if(node instanceof NodeWithModifiers){
                nodeWithModifiers.add((NodeWithModifiers<? extends Node>)node);
            }
        });
        for(NodeWithModifiers<?> nodeWithModifier : nodeWithModifiers){
            if(!(nodeWithModifier instanceof NodeWithPublicModifier)) continue;
            if(nodeWithModifier.hasModifier(Modifier.Keyword.PUBLIC) || nodeWithModifier.hasModifier(Modifier.Keyword.PROTECTED))
                continue;
            if(nodeWithModifier.hasModifier(Modifier.Keyword.PRIVATE)){
                nodeWithModifier.removeModifier(Modifier.Keyword.PRIVATE);
            }
            nodeWithModifier.addModifier(Modifier.Keyword.PUBLIC);
        }

        imagePacker.addMethod("preCreatingContent", Modifier.Keyword.PROTECTED);
        imagePacker.addMethod("postCreatingContent", Modifier.Keyword.PROTECTED);

        MethodDeclaration iconProcessingMethod = imagePacker.addMethod("iconProcessing", Modifier.Keyword.PROTECTED);
        iconProcessingMethod.getBody().ifPresent(blockStmt -> {
            blockStmt.addStatement("if (disableIconProcessing){\nreturn;\n}");
        });
        iconProcessingMethod.setThrownExceptions(mainMethod.getThrownExceptions());

        imagePacker.addMethod("runGenerators", Modifier.Keyword.PROTECTED)
        .getBody().get().addStatement("new MindustryGenerators();")
        ;


        MethodDeclaration startMethod = imagePacker.addMethod("start", Modifier.Keyword.PROTECTED);
        startMethod.removeModifier(Modifier.Keyword.PUBLIC);
        BlockStmt blockStmt = new BlockStmt();
        //Log.info("&ly[Generator]&lc Total time to generate: &lg@&lcms", Time.elapsed())
        boolean iconProcessing = false;

        for(Statement statement : mainMethod.getBody().get().getStatements()){
            if(iconProcessing){
                iconProcessingMethod.getBody().get().addStatement(statement);
                continue;
            }
            boolean creatingContent = statement.toString().equals("Vars.content.createBaseContent();");
            if(creatingContent){
                blockStmt.addStatement(javaParser.parseStatement("preCreatingContent();").getResult().get());
            }
            blockStmt.addStatement(statement);
            if(creatingContent){
                blockStmt.addStatement("Vars.content.createModContent();");
            }else if(statement.toString().equals("Draw.scl = 1f / Core.atlas.find(\"scale_marker\").width;")){
                blockStmt.addStatement("load();");
                imagePacker.addMethod("load", Modifier.Keyword.PROTECTED);
            }
            if(creatingContent){
                blockStmt.addStatement(javaParser.parseStatement("postCreatingContent();").getResult().get());
            }
            if(statement.toString().startsWith("Log.info(\"&ly[Generator]&lc Total time to generate: &lg@&lcms\", Time.elapsed())")){
                blockStmt.addStatement(javaParser.parseStatement("iconProcessing();").getResult().get());
                iconProcessing = true;
            }
        }
        startMethod.setBody(blockStmt);
        startMethod.setThrownExceptions(mainMethod.getThrownExceptions());
        startMethod.accept(new ModifierVisitor<Void>(){
            @Override
            public Visitable visit(AssignExpr assignExpr, Void arg){
                if(assignExpr.getTarget().toString().equals("Vars.content")){
                    assignExpr.setValue(javaParser.parseExpression("new " + ModContentLoader.class.getName() + "()").getResult().get());
                }
                return super.visit(assignExpr, arg);
            }

            @Override
            public Visitable visit(ExpressionStmt expressionStmt, Void arg){
                return super.visit(expressionStmt, arg);
            }

            @Override
            public Visitable visit(BlockStmt n, Void arg){
                return super.visit(n, arg);
            }

            @Override
            public Visitable visit(MethodCallExpr methodCallExpr, Void arg){
                Visitable visit = super.visit(methodCallExpr, arg);
                if(methodCallExpr.toString().equals("Generators.run()")){
                    visit = javaParser.parseExpression("runGenerators()").getResult().get();
                }
                return visit;
            }
        }, null);

        imagePacker.accept(new ModifierVisitor<Void>(){
            @Override
            public Visitable visit(NameExpr n, Void arg){
                if(n.getNameAsString().equals("ImagePacker")){
                    n.setName("MindustryImagePacker");
                }
                return super.visit(n, arg);
            }
        }, null);

        sortClasses(compilationUnit);
        imagePacker.getMembers().sort(Structs.comparingInt(o -> {
            boolean b = !(o.isMethodDeclaration() && !defMethods.contains(o.asMethodDeclaration().getNameAsString()));
            boolean b2 = !(o.isMethodDeclaration() && o.asMethodDeclaration().getNameAsString().equals("start"));
            return Mathf.num(b) + Mathf.num(b2);
        }));
        save(compilationUnit);
    }


    private static void createModGenerators(CompilationUnit otherCompilationUnit){
        ClassOrInterfaceDeclaration generators = otherCompilationUnit.getClassByName("Generators").get();

        CompilationUnit compilationUnit = new CompilationUnit();
        compilationUnit.setPackageDeclaration("mmc.tools.gen");
        compilationUnit.getImports().addAll(otherCompilationUnit.getImports());
        for(ImportDeclaration anImport : compilationUnit.getImports()){
            if(anImport.getNameAsString().equals("mindustry.tools.ImagePacker")){
                anImport.setName("mmc.tools.gen.MindustryImagePacker");
            }
        }
//        compilationUnit.addImport("mindustry.tools.Generators.ScorchGenerator", true, false);

        ClassOrInterfaceDeclaration modGenerators = compilationUnit.addClass("MindustryGenerators");
        modGenerators.addModifier(Modifier.Keyword.PUBLIC);
        for(FieldDeclaration field : generators.getFields()){
            modGenerators.addMember(field.clone().addModifier(Modifier.Keyword.PUBLIC));
        }
        for(MethodDeclaration method : generators.getMethods()){
            if(method.hasModifier(Keyword.PRIVATE)){
                MethodDeclaration clone = method.clone();
                NodeList<Modifier> modifiers = clone.getModifiers();
                modifiers.remove(Modifier.privateModifier());
                modifiers.add(Modifier.protectedModifier());
                modifiers.sort(Structs.comparing(i -> i.getKeyword().ordinal()));
                modGenerators.addMember(clone);
            }
        }
//        generators.
        for(BodyDeclaration<?> member : generators.getMembers()){
            if(!member.isClassOrInterfaceDeclaration()) continue;
            modGenerators.addMember(member.asClassOrInterfaceDeclaration().clone().addModifier(Modifier.Keyword.PUBLIC));
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
        for(MethodDeclaration otherRun : generators.getMethods()){
            if(otherRun.getNameAsString().equals("run")){
                otherRun.accept(new VoidVisitorAdapter<Void>(){
                    @Override
                    public void visit(VariableDeclarationExpr variableDeclarationExpr, Void arg){
//                        super.visit(variableDeclarationExpr, arg);
                        FieldDeclaration fieldDeclaration = null;
                        for(VariableDeclarator variable : variableDeclarationExpr.getVariables()){
                            VariableDeclarator current;
                            if(fieldDeclaration == null){
                                fieldDeclaration = modGenerators.addField(variable.getType(), variable.getNameAsString(), Modifier.Keyword.PROTECTED);
                                current = fieldDeclaration.getVariable(0);
                                variable.getInitializer().ifPresent(current::setInitializer);
                            }else{
                                fieldDeclaration.addVariable(current = variable.clone());
                            }
                        }
                    }

                    @Override
                    public void visit(MethodCallExpr methodCall, Void arg){
                        if(methodCall.getNameAsString().equals("generate")){
                            StringLiteralExpr generatorName = methodCall.getArgument(0).asStringLiteralExpr();
                            LambdaExpr lambdaExpr = methodCall.getArgument(1).asLambdaExpr();
                            String methodName = Strings.kebabToCamel(generatorName.getValue());

                            BlockStmt body = null;
                            if(lambdaExpr.getBody().isBlockStmt()){
                                body = lambdaExpr.getBody().asBlockStmt();
                            }else{
                                body = new BlockStmt();
                                body.addStatement(lambdaExpr.getBody());
                            }
                            String boolVarName = "generate" + Strings.capitalize(methodName);
                            modGenerators.addField(PrimitiveType.booleanType(), boolVarName, Modifier.Keyword.PROTECTED);
                            enableMethod.getBody().get()
                            .addStatement(new AssignExpr(new NameExpr(boolVarName), new BooleanLiteralExpr(true), AssignExpr.Operator.ASSIGN));
                            disableMethod.getBody().get()
                            .addStatement(new AssignExpr(new NameExpr(boolVarName), new BooleanLiteralExpr(false), AssignExpr.Operator.ASSIGN));

                            String parseLine = "if (!" + boolVarName + ") return;";
                            body.addStatement(0, javaParser.parseStatement(parseLine).getResult().get());
//                            body.getStatement(0).setComment(new LineComment(parseLine));

                            body.accept(new ModifierVisitor<Void>(){
                                @Override
                                public Visitable visit(MethodCallExpr methodCallExpr, Void arg){
                                    String nameAsString = methodCallExpr.getNameAsString();

                                    if(nameAsString.equals("save")){
                                        Expression argument = methodCallExpr.getArgument(1);
                                        StringLiteralExpr found = argument.accept(new GenericVisitorAdapter<StringLiteralExpr, Void>(){
                                            @Override
                                            public StringLiteralExpr visit(StringLiteralExpr n, Void arg){
                                                StringLiteralExpr visit = super.visit(n, arg);
                                                if(n.getValue().contains("-full")) return n;
                                                return visit;
                                            }
                                        }, null);
                                        if(found != null){
                                            argument.accept(new ModifierVisitor<Void>(){
                                                @Override
                                                public Visitable visit(StringLiteralExpr literalExpr, Void arg){
                                                    for(ContentType type : ContentType.values()){
                                                        String value = literalExpr.getValue();
                                                        String suffix = type.name() + "-";
                                                        if(value.endsWith(suffix)){
                                                            int endIndex = suffix.length() + (value.endsWith("/" + suffix) ? 1 : 0);
                                                            literalExpr.setValue(value.substring(0, value.length() - endIndex));
                                                        }
                                                    }
                                                    return super.visit(literalExpr, arg);
                                                }
                                            }, null);
                                        }
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
                        }
//                        super.visit(methodCall, arg);
                    }
                }, null);
            }
        }

        save(compilationUnit);
    }

    private static void save(CompilationUnit compilationUnit, String className){
        PackageDeclaration packageDeclaration = compilationUnit.getPackageDeclaration().get();
        Fi child = rootDirectory.child(packageDeclaration.getNameAsString().replace(".", "/")).child(className + ".java");
        child.writeString(compilationUnit.toString());
    }

    private static void save(CompilationUnit compilationUnit){
        save(compilationUnit, compilationUnit.findFirst(ClassOrInterfaceDeclaration.class).get().getNameAsString());
    }

    private static void sortClasses(CompilationUnit compilationUnit){
        for(ClassOrInterfaceDeclaration classOrInterfaceDeclaration : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)){
            Comparator<BodyDeclaration<?>> comparator = (o1, o2) -> {
                if(o1.isFieldDeclaration() && o2.isFieldDeclaration()){
                    FieldDeclaration o1f = o1.asFieldDeclaration();
                    FieldDeclaration o2f = o2.asFieldDeclaration();
//                    Comparator<FieldDeclaration> comparator = ;
                    return Structs.<FieldDeclaration>comps(
                    Structs.comparingBool(o -> o.hasModifier(Modifier.Keyword.STATIC)),
                    Structs.comparing(o -> {
                        StringBuilder builder = new StringBuilder();
                        for(VariableDeclarator variable : o.getVariables()){
                            builder.append(variable.getNameAsString()).append(" ");
                        }
                        return builder.toString();
                    })).compare(o1f, o2f);
                }
                Func2<BodyDeclaration<?>, BodyDeclaration<?>, Integer> func = (obj1, obj2) -> {
                    if(obj1.isFieldDeclaration()){
                        return 1;
                    }
                    if(obj1.isMethodDeclaration() && obj2.isClassOrInterfaceDeclaration()){
                        return 1;
                    }
                    if(obj1.isMethodDeclaration() && obj2.isMethodDeclaration()){
                        return obj1.asMethodDeclaration().getNameAsString().compareTo(obj2.asMethodDeclaration().getNameAsString());
                    }
                    if(obj1.isClassOrInterfaceDeclaration() && obj2.isClassOrInterfaceDeclaration()){
                        return obj1.asClassOrInterfaceDeclaration().getNameAsString().compareTo(obj2.asClassOrInterfaceDeclaration().getNameAsString());
                    }
                    return 0;
                };
                int val = func.get(o1, o2);
                if(val == 0){
                    val = -func.get(o2, o1);
                }
                return val;
            };
            classOrInterfaceDeclaration.getMembers().sort((o1, o2) -> -comparator.compare(o1, o2));
        }
    }
}
