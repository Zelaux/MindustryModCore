package coretest.tests.ui.tiledStructures;

import arc.*;
import arc.func.*;
import arc.struct.*;
import mmc.ui.tiledStructures.TiledStructures.*;

public class TestStructures{
    public static final Seq<Prov<TestStructure>> providers = Seq.with(
        TestStructure::new
    );

    public static class TestStructure extends TiledStructure<TestStructure>{

        @Override
        public int outputConnections(){
            return 4;
        }

        @Override
        public int inputConnections(){
            return 2;
        }

        @Override
        public boolean enabledInput(int index){
            return index > 0;
        }

        @Override
        public boolean enabledOutput(int index){
            return index == 0;
        }

        @Override
        public boolean update(){
            return false;
        }

        @Override
        public String typeName(){
            return "TestStructure";
        }

        @Override
        public String name(){
            return Core.bundle.get("TestStructure");
        }
    }
}
