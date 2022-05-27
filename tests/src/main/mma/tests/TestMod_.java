package mma.tests;

import arc.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.ui.dialogs.*;
import mma.*;
import mma.annotations.*;

@ModAnnotations.MainClass(modInfoPath = "tests/assets/mod.hjson")
public class TestMod_ extends MMAMod{
    public TestMod_(){
        int i = 1233;
//        ModGroups.testGroup
        TestVars.load();
        /*Events.run(ClientLoadEvent.class, () -> {
            new BaseDialog("Hello world"){{
                cont.add("Hello, world!");
                addCloseButton();
            }}.show();
        });*/
    }

    @Override
    protected void modContent(Content content){
        super.modContent(content);
        Log.info("test-content: " + content);
    }
}
