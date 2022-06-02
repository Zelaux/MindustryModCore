package mmat.tests;

import arc.util.*;
import mindustry.ctype.*;
import mma.*;
import mma.annotations.ModAnnotations.*;

@MainClass()
@DependenciesAnnotation()
public class TestMod extends MMAMod {

    public TestMod() {
        int i = 1233;
        // ModGroups.testGroup
        TestVars.load();
        /*Events.run(ClientLoadEvent.class, () -> {
            new BaseDialog("Hello world"){{
                cont.add("Hello, world!");
                addCloseButton();
            }}.show();
        });*/
    }

    @Override
    protected void modContent(Content content) {
        super.modContent(content);
        Log.info("test-content: " + content);
    }
}
