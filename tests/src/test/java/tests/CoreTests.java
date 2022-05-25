package tests;

import mindustry.*;
import mindustry.content.*;
import mindustry.content.TechTree.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.type.*;
import mma.type.*;
import org.junit.jupiter.api.*;
public class CoreTests{
    @BeforeAll
    static void beforeAll(){
        System.out.println("Tests begin");
    }
    @AfterAll

    static void afterAll(){
        System.out.println("Tests end");
    }

    @BeforeEach
    void beforeEach(){
        Vars.content = null;
        Vars.content = new ContentLoader();
        Vars.content.createBaseContent();
        System.gc();
    }


    @Test()
    void techTreeWithUnlockableContent(){
        UnlockableContent content = Blocks.coreNucleus;
        UnlockableContent child = new Item("hello-world-item");
        TechNode node = content.techNode;

        TechTreeContext.contextNode(content, () -> {
            TechTree.node(child);
        });
        Assertions.assertTrue(node.children.contains(child.techNode));
    }
    @Test
     void techTreeWithTechNode(){
        UnlockableContent content = Blocks.coreNucleus;
        UnlockableContent child = new Item("hello-world-item");
        TechNode node = content.techNode;

        TechTreeContext.contextNode(node, () -> {
            TechTree.node(child);
        });
        Assertions.assertTrue(node.children.contains(child.techNode));
    }
}
