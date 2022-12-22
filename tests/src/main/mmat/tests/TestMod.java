package mmat.tests;

import arc.*;
import arc.func.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mma.*;
import mma.ui.tiledStructures.TiledStructures.*;
import mma.ui.tiledStructures.*;
import mmat.tests.gen.*;
import mmat.tests.ui.*;
import mmat.tests.ui.tiledStructures.*;
import mmat.tests.ui.tiledStructures.TestStructures.*;

public class TestMod extends MMAMod{

    public TestMod(){
        int i = 1233;
        // ModGroups.testGroup
//        MindustrySerialization();
        TestVars.load();
//        TmDependencies.enabledHmmm();
        Events.run(ClientLoadEvent.class, () -> {
            new TiledStructuresDialog("tests-structures", TestStructure.class){{
                setGlobalProvider(TestStructure.class, (type, cons) -> new BaseDialog("@add"){{
                    cont.pane(p -> {
                        p.background(Tex.button);
                        p.marginRight(14f);
                        p.defaults().size(195f, 56f);

                        int i = 0;
                        for(Prov<TestStructure> gen : TestStructures.providers){
                            TestStructure obj = gen.get();
                            p.button(obj.typeName(), Styles.flatt, () -> {
                                cons.get(obj);
                                hide();
                            }).with(Table::left).get().getLabelCell().growX().left().padLeft(5f).labelAlign(Align.left);

                            if(++i % 3 == 0) p.row();
                        }
                    }).scrollX(false);

                    addCloseButton();
                    show();
                }});
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
