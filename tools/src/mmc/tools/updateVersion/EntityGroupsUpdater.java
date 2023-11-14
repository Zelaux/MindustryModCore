package mmc.tools.updateVersion;

import arc.files.*;
import arc.struct.*;
import arc.util.*;
import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.Modifier.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class EntityGroupsUpdater extends AnukeCompDownloader{
    public static void run(String mindustryVersion, ClassOrInterfaceDeclaration compData, BlockStmt initializer, String... args){
        CompilationUnit groupDefs = StaticJavaParser.parse(Fi.get("core/src/" + packageName + "/entities/GroupDefs.java").readString());
//        System.out.println(Seq.with(groupDefs.getTypes()).toString("\n"));
        ClassOrInterfaceDeclaration groupsDefs = groupDefs.getClassByName("GroupDefs").get();
        compData.addField(ObjectMap.class.getName() + "<String,String[]>", "indexerDefs", Keyword.PUBLIC, Keyword.STATIC);
        initializer.addStatement("indexerDefs=new " + ObjectMap.class.getName() + "<>();");
        for(FieldDeclaration field : groupsDefs.getFields()){
            AnnotationExpr annotationExpr = field.getAnnotationByName("GroupDef").get();
            ClassExpr classExpr;
            if(annotationExpr instanceof SingleMemberAnnotationExpr){
                classExpr = ((SingleMemberAnnotationExpr)annotationExpr).getMemberValue().asClassExpr();
            }else{
                classExpr = Seq.with(((NormalAnnotationExpr)annotationExpr).getPairs()).find(it -> it.getNameAsString().equals("value")).getValue().asClassExpr();
            }
            String stringType = classExpr.getTypeAsString();
            String groupName = field.getVariables().get(0).getNameAsString();
            initializer.addStatement(Strings.format("indexerDefs.put(\"@\",new String[]{\"@\",\"@\"});",
            stringType.startsWith("mindustry") ? stringType : "mindustry.gen." + stringType,
            "IndexableEntity__" + groupName,
            "setIndex__" + groupName
            ));
        }
        compData.getMembers().sort(Structs.comparing(it -> it instanceof FieldDeclaration ? -1 : 0));
    }
}
