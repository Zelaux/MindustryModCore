package mmat.tests;

import arc.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mma.*;
import mma.ui.tiledStructures.TiledStructures.*;
import mmat.tests.gen.*;
import mmat.tests.ui.*;
import mmat.tests.ui.tiledStructures.TestStructures.*;
import mmat.tests.ui.tiledStructures.*;

public class TestMod extends MMAMod{

    public TestMod(){
        int i = 1233;
        // ModGroups.testGroup
//        MindustrySerialization();
        TestVars.load();
//        TmDependencies.enabledHmmm();
        Events.run(ClientLoadEvent.class, () -> {
            new TestTiledStructuresDialog("tests-structures", TestStructure.class){{

                Seq<TiledStructure> tiledStructures = new Seq<>();
                show(() -> tiledStructures, tiledStructures::set);
            }};
            /*new BaseDialog("Hello world"){{
                cont.add("Hello, world!");
                addCloseButton();
            }}.show();*/
        });
    }

    @Override
    public void init(){
        super.init();

        TmTex.load();
        TestStyles.load();
        TmTex.loadStyles();
        System.out.println(TmTex.arrow);
    }

    @Override
    protected void modContent(Content content){
        super.modContent(content);
        Log.info("test-content: " + content);
    }
}
