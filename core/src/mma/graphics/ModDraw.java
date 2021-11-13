package mma.graphics;

import arc.*;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Scl;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Tmp;
import mindustry.ui.Fonts;

import static arc.Core.atlas;

public class ModDraw extends Draw{
    private static float[] vertices = new float[24];

    public static void quad(TextureRegion region,float x1, float y1, float c1, float x2, float y2, float c2, float x3, float y3, float c3, float x4, float y4, float c4){
        float mcolor = Draw.getMixColor().toFloatBits();
        float u = region.u;
        float v = region.v;
        vertices[0] = x1;
        vertices[1] = y1;
        vertices[2] = c1;
        vertices[3] = u;
        vertices[4] = v;
        vertices[5] = mcolor;

        vertices[6] = x2;
        vertices[7] = y2;
        vertices[8] = c2;
        vertices[9] = u;
        vertices[10] = v;
        vertices[11] = mcolor;

        vertices[12] = x3;
        vertices[13] = y3;
        vertices[14] = c3;
        vertices[15] = u;
        vertices[16] = v;
        vertices[17] = mcolor;

        vertices[18] = x4;
        vertices[19] = y4;
        vertices[20] = c4;
        vertices[21] = u;
        vertices[22] = v;
        vertices[23] = mcolor;

        Draw.vert(region.texture, vertices, 0, vertices.length);
    }


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
