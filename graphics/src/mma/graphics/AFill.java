package mma.graphics;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.struct.*;

public class AFill extends Fill{
    private static Vec2 vector = new Vec2();
    private static Vec2 u = new Vec2();
    private static Vec2 v = new Vec2();
    private static Vec2 inner = new Vec2();
    private static Vec2 outer = new Vec2();
    private static FloatSeq floats = new FloatSeq(20);
    private static FloatSeq floatBuilder = new FloatSeq(20);
    private static boolean building;
    private static float circlePrecision = 0.4F;

    /**
     * @deprecated use pie
     */
    @Deprecated
    public static void swirl(float x, float y, float radius, float finion, float angle){
        pie(x, y, radius, finion, angle);
    }
    @Deprecated
    public static void circleRect(float x, float y, float radius){
        polyCircle(x, y, radius);
    }

    public static void spikedDonut(float x, float y, float radius1, float length, float finion, float angle){
        spikedDonut(x, y, radius1, length, finion, angle, 0.5f);
    }

    public static void spikedDonut(float x, float y, float radius1, float length, float finion, float angle, float spikeOffset){
        final float sides = 50;
        final float radius2 = radius1 + length;
        int max = (int)(sides * (finion + 0.001F));
        vector.set(0.0F, 0.0F);
        floats.clear();
        Cons2<Float, Float> point = (i, radius) -> {
            vector.set(radius, 0.0F).setAngle(360.0F / sides * i + angle);
            floats.add(vector.x + x, vector.y + y);
        };
        Runnable flush = () -> {
            poly(floats);
            floats.clear();
        };
        Cons<Float> spike = (i) -> {
            point.get(i, radius1);
            point.get(i + 1f, radius1);
            point.get(i + spikeOffset, radius2);
            point.get(i + spikeOffset, radius2);
            flush.run();
        };
        int startI = 0;
        if(max % 2 != 0){
            startI = 1;
            spike.get(0f);
        }

        for(float i = startI; i < max; i++){
            spike.get(i);
        }
    }

    /**
     * @deprecated use donut
     */
    @Deprecated
    public static void doubleSwirl(float x, float y, float radius1, float radius2, float finion, float angle){
        donut(x, y, radius1, radius2, finion, angle);
    }

    //ellipse region
    public static void polyCircle(float x, float y, float radius){
        pie(x, y, radius, 1f, 0f);
    }

    public static void pie(float x, float y, float radius, float finion, float rotation){
        ellipse(x, y, radius * 2f, radius * 2f, finion, 0,rotation);
    }

    public static void ellipse(float x, float y, float width, float height, float finion,  float rotation){
        ellipse(x, y, width, height, finion, 0, rotation);
    }
    public static void ellipse(float x, float y, float width, float height, float finion, float angle, float rotation){
        donutEllipse(x,y,0,0,width,height,finion,angle,rotation);
       /* final float sides = 60;
        finion = Mathf.clamp(finion);
        int max = (int)(sides * (finion + 0.001F));
        vector.set(0.0F, 0.0F);
        floats.clear();

        floats.add(x, y);
        Cons<Float> cons = (i) -> {
            float degrees = 360.0F / sides * i + angle;
            vector.set(1, 0.0F).setAngle(degrees);
            vector.scl(width, height);
//            floats.add(Mathf.cos(degrees) * width + x, Mathf.sin(degrees) * height + y);
            floats.add(vector.x + x, vector.y + y);
        };
        int startI = 0;
        if(max % 2 != 0){
            startI = 1;
            cons.get(0f);
            cons.get(1f);
            cons.get(1f);
//            cons.get(2f);
        }
        for(float i = startI; i < (max); i += 2f){
            cons.get(i);
            cons.get(i + 1f);
            cons.get(i + 2f);
        }
        if(max > 0) poly(floats);
*/
    }

    public static void donut(float x, float y, float radius1, float radius2, float finion, float rotation){
        donutEllipse(x, y, radius1 * 2, radius1 * 2, radius2 * 2, radius2 * 2, finion, 0f, rotation);

    }

    public static void donutEllipse(float x, float y, float width, float height, float width2, float height2, float finion, float rotation){
        donutEllipse(x, y, width, height, width2, height2, finion, 0f, rotation);
    }

    /**
     * @param rotation ellipse rotation
     * @param angleOffset ellipse start offset
     */
    public static void donutEllipse(float x, float y, float width, float height, float width2, float height2, float finion, float angleOffset, float rotation){
        final float sides = 50.0F;
        float max =sides;
        vector.set(0.0F, 0.0F);
        floats.clear();
        Cons<Float> cons = (ix) -> {
            float v = 7.2F * ix;
            v = 360.0F * finion / max * ix;
            vector.set(1, 0.0F).setAngle(v + angleOffset).scl(width, height).rotate(rotation);
            floats.add(vector.x + x, vector.y + y);
            vector.set(1, 0.0F).setAngle(v + angleOffset).scl(width2, height2).rotate(rotation);
            floats.add(vector.x + x, vector.y + y);
        };
        Cons<Float> undoCons = (ix) -> {
            float v = 7.2F * ix;
            v = 360.0F * finion / max * ix;
            vector.set(1, 0.0F).setAngle(v + angleOffset).scl(width2, height2).rotate(rotation);
            floats.add(vector.x + x, vector.y + y);
            vector.set(1, 0.0F).setAngle(v + angleOffset).scl(width, height).rotate(rotation);
            floats.add(vector.x + x, vector.y + y);
        };
        Runnable flush = () -> {
            poly(floats);
            floats.clear();
        };
        int startI = 0;
        if(max % 2.0F != 0.0F){
            startI = 1;
            cons.get(0.0F);
            undoCons.get(1.0F);
            flush.run();
            cons.get(1.0F);
            undoCons.get(1.0F);
            flush.run();
        }

        for(float i = (float)startI; i < max; i += 2.0F){
            cons.get(i);
            undoCons.get(i + 1.0F);
            flush.run();
            cons.get(i + 1.0F);
            undoCons.get(i + 2.0F);
            flush.run();
        }

    }

    //end region
    public static void tri(FloatSeq floats){
        if(floats.size < 6) return;
        float[] items = floats.items;
        tri(items[0], items[1], items[2], items[3], items[4], items[5]);
    }

    public static void quad(FloatSeq floats){
        if(floats.size < 8) return;
        float[] items = floats.items;
        quad(items[0], items[1], items[2], items[3], items[4], items[5], items[6], items[7]);
    }

    public static void crystalLine(float x, float y, float radius1, float radius2, float angle, int count, final float width){
        if(count == 0) return;
        final int sides = 50;
        count = Math.min(sides, count);
        float oneAngle = 360f / count;
        Floatc2 point = (i, radius) -> {
            vector.set(radius, 0.0F).setAngle(oneAngle * i + angle);
            floats.add(vector.x + x, vector.y + y);
        };
        Floatc2 side = (i, i2) -> {
            floats.clear();
            point.get(i2, radius1);
            point.get(i, radius2);
//            Vec2 cpy = vector.trns(oneAngle * i2 + angle, radius2-width, 0.0F).cpy();
//            Vec2 cpy2 = vector.trns(oneAngle * i + angle, radius1-width, 0.0F).cpy();
            point.get(i, radius2 * ((radius1 - width) / radius1));
            point.get(i2, radius1 - width);
            AFill.quad(floats);
        };
        floats.clear();
        for(float i = 0; i < count; i++){
            side.get(i, i - 0.5f);
            side.get(i, i + 0.5f);
        }
    }

    public static void crystal(float x, float y, float radius1, float radius2, float angle, int count){
        if(count == 0) return;
        final float sides = 50;
        float oneAngle = 360f / count;
        float offset = oneAngle / 2f;
        Cons2<Float, Float> point = (i, radius) -> {
            vector.set(radius, 0.0F).setAngle(oneAngle * i + angle);
            floats.add(vector.x + x, vector.y + y);
        };
        floats.clear();
        for(float i = 0; i < count; i++){
            floats.add(x, y);
            point.get(i - 0.5f, radius1);
            point.get(i, radius2);
            point.get(i + 0.5f, radius1);
            quad(floats);
            floats.clear();
        }
    }
}
