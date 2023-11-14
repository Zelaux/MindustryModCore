package mmc.graphics;

import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.struct.*;


public class ALines extends Lines{
    static FloatSeq floats = new FloatSeq();
    private static final Vec2 vector = new Vec2();

    public static void crystal(float x, float y, float radius1, float radius2, float angle, int count){
        if(count == 0) return;
        final float stroke = getStroke();
        AFill.crystalLine(x, y, radius1, radius2, angle, count, stroke);
    }

}
