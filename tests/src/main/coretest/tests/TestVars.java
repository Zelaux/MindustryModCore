package coretest.tests;

import arc.struct.*;
import mindustry.*;
import mmc.*;
import coretest.tests.content.*;

public class TestVars extends ModVars{
    private static final Seq<Runnable> onLoad = new Seq<>();

    static{
        new TestVars();
    }

    @SuppressWarnings({"unused", "RedundantSuppression"})
    public static void create(){

    }

    public static void load(){
        onLoad.each(Runnable::run);
        onLoad.clear();
    }

    @Override
    protected void onLoad(Runnable runnable){
        onLoad.add(runnable);
    }

    @Override
    protected void showException(Throwable ex){
        Vars.ui.showException(ex);
    }

    @Override
    public void loadContent(){
        TestItems.load();
        TestBlocks.load();
    }
}
