package tests;

import arc.*;
import arc.mock.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.type.*;
import mindustry.world.*;
import mmc.*;
import mmc.core.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModContentLoaderTests{
    @BeforeAll
    static void beforeAll(){
        System.out.println("Tests ModContentLoaderTests begin");
    }

    @AfterAll
    static void afterAll(){
        System.out.println("Tests ModContentLoaderTests end");
    }

    @BeforeEach
    void beforeEach(){
        Vars.content = null;
        Vars.content = new ContentLoader();
        Vars.content.createBaseContent();
        Core.settings=new MockSettings();
        System.gc();
    }

    @Test
    void blockFilters(){
        int defaultItemsAmount = Vars.content.items().size;
        Vars.content = null;
        System.gc();
        TestVars vars = new TestVars();

        Vars.content = new ModContentLoader();
        Vars.content.createBaseContent();
        Vars.content.createModContent();

        Vars.content.init();

        assertEquals(defaultItemsAmount + 1,vars.testBlock.itemFilter.length);
        assertEquals(1,Vars.content.items().size);
        assertEquals(1,Vars.content.blocks().size);
    }

}

class TestVars extends ModVars{
    Block testBlock;
    Item testItem;

    @Override
    protected void onLoad(Runnable runnable){
//        runnable.run();
    }

    @Override
    protected void showException(Throwable ex){

    }

    @Override
    public void loadContent(){
        testItem = new Item("test-item");
        testBlock = new Block("test-block");
    }
}