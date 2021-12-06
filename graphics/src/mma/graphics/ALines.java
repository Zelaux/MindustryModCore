package mma.graphics;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.struct.*;


public class ALines extends Lines{
    static FloatSeq floats = new FloatSeq();
    private static Vec2 vector = new Vec2();

    public static void crystal(float x, float y, float radius1, float radius2, float angle, int count){
        if(count == 0) return;
        final float stroke = getStroke();
        AFill.crystalLine(x, y, radius1, radius2, angle, count, stroke);
    }

    public static void swirl(float x, float y, float radius, float finion){
        swirl(x, y, radius, finion, 0.0F);
    }

    public static void swirl(float x, float y, float radius, float finion, float angle){
        float stroke = getStroke();
        float halfStroke = stroke / 2.0F;
        AFill.donut(x, y, radius - halfStroke, radius + halfStroke, finion, angle);
    }

    public static void square(float x, float y, float rad){
        rect(x - rad, y - rad, rad * 2.0F, rad * 2.0F);
    }

    public static void rect(float x, float y, float width, float height, float offsetx, float offsety, float rot){
        float stroke = getStroke();
        Vec2[] points1 = ModGeometry.rectPoints(x + offsetx, y + offsety, width, height, rot);
        Vec2[] points2 = ModGeometry.rectPoints(x + stroke + offsetx, y + stroke + offsety, width - stroke * 2f, height - stroke * 2f, rot);
        Cons<Integer> side = (index) -> {
            int index2 = (index + 1) % 4;
            floats.clear();
            floats.add(points1[index].x, points1[index].y);
            floats.add(points1[index2].x, points1[index2].y);
            floats.add(points2[index2].x, points2[index2].y);
            floats.add(points2[index].x, points2[index].y);
            AFill.quad(floats);
        };
        side.get(0);
        side.get(1);
        side.get(2);
        side.get(3);
        floats.clear();
    }

    public static void rect(float x, float y, float width, float height){
        rect(x, y, width, height, 0);
    }

    public static void rect(float x, float y, float width, float height, float rot){
        rect(x, y, width, height, 0, 0, rot);
    }

    public static void rect(Rect rect){
        rect(rect.x, rect.y, rect.width, rect.height, 0);
    }

    public static void rect(float x, float y, float width, float height, int offset){
        rect(x, y, width, height, (float)offset, (float)offset, 0F);
    }
}
