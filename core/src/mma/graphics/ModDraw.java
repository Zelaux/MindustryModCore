package mma.graphics;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.FontCache;
import arc.graphics.g2d.GlyphLayout;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Scl;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Tmp;
import mindustry.ui.Fonts;

public class ModDraw extends Draw{
    public static void drawLabel(Position pos, float textSize, Color color, String text){
        Font font = Fonts.outline;
        boolean ints = font.usesIntegerPositions();
        font.getData().setScale(textSize / Scl.scl(1.0f));
        font.setUseIntegerPositions(false);

        font.setColor(color);

        float z = Draw.z();
        Draw.z(z+0.01f);
        FontCache cache = font.getCache();
        cache.clear();
        GlyphLayout layout = cache.addText(text, pos.getX(), pos.getY());

        font.draw(text, pos.getX()- layout.width / 2f, pos.getY()+ layout.height / 2f);
//        font.draw(text+"-", pos.getX(), pos.getY());
        Draw.z(z);

        font.setUseIntegerPositions(ints);
        font.getData().setScale(1);
    }
    public static void drawLabel(Position pos,float textSize,String text){
        drawLabel(pos,textSize,Color.white,text);
    }
    public static void drawLabel(Position pos,Color color,String text){
        drawLabel(pos,0.23f,color,text);
    }
    public static void drawLabel(Position pos,String text){
        drawLabel(pos,Color.white,text);
    }
    public static void drawLabel(float x,float y, float textSize, Color color, String text){
        drawLabel(new Vec2(x,y),textSize,color,text);
    }
    public static void drawLabel(float x    ,float y,float textSize,String text){
        drawLabel(x,y,textSize,Color.white,text);
    }
    public static void drawLabel(float x,float y,Color color,String text){
        drawLabel(x,y,0.23f,color,text);
    }
    public static void drawLabel(float x,float y,String text){
        drawLabel(x,y,Color.white,text);
    }
}
