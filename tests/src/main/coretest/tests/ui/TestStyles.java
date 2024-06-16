package coretest.tests.ui;

import arc.scene.ui.*;
import arc.scene.ui.Button.*;
import arc.scene.ui.TextButton.*;
import mindustry.annotations.Annotations.*;

@StyleDefaults
public class TestStyles{
    public static ButtonStyle defaultCustomElementStyle;
    public static void load(){
        defaultCustomElementStyle=new TextButtonStyle();
    }
    public static class CustomElement extends Button{}
}
