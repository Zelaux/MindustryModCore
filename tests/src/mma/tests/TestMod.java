package mma.tests;

import arc.util.*;
import mindustry.ctype.*;
import mma.*;
import mma.annotations.*;
import mma.gen.*;

@ModAnnotations.MainClass(modInfoPath = "tests/assets/mod.json")
public class TestMod extends MMAMod{
    public TestMod(){
        int i=1233;
//        ModGroups.testGroup
        TestVars.load();
    }

    @Override
    protected void modContent(Content content){
        super.modContent(content);
        Log.info("test-content: "+content);
    }
}
