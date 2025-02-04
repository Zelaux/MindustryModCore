package coretest.tests;

import arc.struct.*;
import coretest.tests.content.*;
import mindustry.*;
import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import mmc.*;

public class TestVars extends ModVars{
    private static final Seq<Runnable> onLoad = new Seq<>();

    static{
        new TestVars();
    }

    @Remote(targets = Loc.client, called = Loc.both)
    public static void remote1(Player player, int a){

    }
    @Remote(targets = Loc.client, called = Loc.client)
    public static void remote3(Player player, int a){

    }

    @Remote(targets = Loc.server, called = Loc.client)
    public static void remote2(int a){

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
