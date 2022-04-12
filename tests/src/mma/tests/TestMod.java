package mma.tests;

import mma.*;
import mma.annotations.*;

@ModAnnotations.MainClass(modInfoPath = "tests/assets/mod.json")
public class TestMod extends MMAMod{
    public TestMod(){
        int i=1233;
        TestVars.load();
    }

}
