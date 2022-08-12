package mmat.tests;

import arc.util.*;
import mindustry.ctype.*;
import mma.*;
import mma.annotations.ModAnnotations.*;
import mma.annotations.entities.*;
import mma.gen.*;
import mmat.tests.gen.*;

public class TestMod extends MMAMod {

    public TestMod() {
        int i = 1233;
        // ModGroups.testGroup
//        MindustrySerialization();
        TestVars.load();
//        TmDependencies.enabledHmmm();
        /*Events.run(ClientLoadEvent.class, () -> {
            new BaseDialog("Hello world"){{
                cont.add("Hello, world!");
                addCloseButton();
            }}.show();
        });*/
    }

    @Override
    public void init(){
        super.init();
        TmTex.load();
        System.out.println(TmTex.arrow);
    }

    @Override
    protected void modContent(Content content) {
        super.modContent(content);
        Log.info("test-content: " + content);
    }
}
