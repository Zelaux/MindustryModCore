package mmc.graphics;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.struct.*;

public class AFill extends Fill{
    private static final Vec2 vector = new Vec2();
    private static final Vec2 u = new Vec2();
    private static final Vec2 v = new Vec2();
    private static final Vec2 inner = new Vec2();
    private static final Vec2 outer = new Vec2();
    private static final FloatSeq floats = new FloatSeq(20);
    private static final FloatSeq floatBuilder = new FloatSeq(20);
    private static boolean building;
    private static final float circlePrecision = 0.4F;
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
