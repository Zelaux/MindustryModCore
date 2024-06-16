package tests;

import mindustry.*;
import mindustry.content.*;
import mindustry.content.TechTree.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.type.*;
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
    void techTreeWithNull(){
        UnlockableContent content = Blocks.coreNucleus;
        UnlockableContent child = new Item("hello-world-item");
        TechNode node = content.techNode;
        Assertions.assertEquals("contextContent cannot be null",
        Assertions.assertThrows(NullPointerException.class, () -> {
            TechTreeContext.contextNode((UnlockableContent)null, () -> {
                TechTree.node(child);
            });
        }, "TechTreeContext.contextNode should throw NPE when contextContent is null").getMessage(),
        "TechTreeContext.contextNode should throw NPE with message \"contextContent cannot be null\" when contextContent is null");
        Assertions.assertEquals("contextContent.techNode cannot be null",
        Assertions.assertThrows(NullPointerException.class, () -> {
            content.techNode = null;
            TechTreeContext.contextNode(content, () -> {
                TechTree.node(child);
            });
        }, "TechTreeContext.contextNode should throw NPE when contextContent.techNode  is null").getMessage(),
        "TechTreeContext.contextNode should throw NPE with message \"contextContent.techNode cannot be null\" when contextContent.techNode is null");
        Assertions.assertEquals("techNode cannot be null",
        Assertions.assertThrows(NullPointerException.class, () -> {
            TechTreeContext.contextNode((TechNode)null, () -> {
                TechTree.node(child);
            });
        }, "TechTreeContext.contextNode should throw NPE when techNode is null").getMessage(),
        "TechTreeContext.contextNode should throw NPE with message \"techNode cannot be null\" when techNode is null");
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
