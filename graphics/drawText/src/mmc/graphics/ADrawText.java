package mmc.graphics;

import arc.graphics.*;
import arc.math.geom.*;
import arclibrary.graphics.*;
import mindustry.ui.*;
/**Shortcut for <a href="https://github.com/Zelaux/ArcLibrary/blob/master/graphics/drawText%2Fsrc%2Farclibrary%2Fgraphics%2FDrawText.java">arclibrary.graphics.DrawText</a>*/
public class ADrawText{

    public static void drawText(float x, float y, float textSize, Color color, String text){
        DrawText.drawText(Fonts.outline, x, y, textSize, color, text);
    }

    public static void drawText(Position pos, float textSize, String text){
        drawText(pos.getX(), pos.getY(), textSize, Color.white, text);
    }

    public static void drawText(Position pos, Color color, String text){
        drawText(pos.getX(), pos.getY(), 0.23f, color, text);
    }

    public static void drawText(Position pos, String text){
        drawText(pos, Color.white, text);
    }

    public static void drawText(float x, float y, float textSize, String text){
        drawText(x, y, textSize, Color.white, text);
    }

    public static void drawText(float x, float y, Color color, String text){
        drawText(x, y, 0.23f, color, text);
    }

    public static void drawText(float x, float y, String text){
        drawText(x, y, Color.white, text);
    }
}
