package mmc.graphics;

import arc.math.*;
import arc.math.geom.*;

public class ModGeometry{
    private static final Vec2 tmp1 = new Vec2(), tmp2 = new Vec2();

    private static Vec2 vec2(float x, float y){
        return new Vec2(x, y);
    }

    public static Vec2[] rectPoints(float x, float y, float width, float height, float rot){
        float vw = width / 2f;
        float vh = height / 2f;
        Vec2[] points = new Vec2[]{vec2(-vw, -vh), vec2(vw, -vh), vec2(vw, vh), vec2(-vw, vh), vec2(-vw, -vh)};
//        Seq<Vec2> cords=new Seq<>();
        Vec2[] cords = new Vec2[points.length];
        for(int i = 0; i < points.length; i++){

            cords[i] = (points[i].cpy().rotate(rot).add(x + vw, y + vh));
        }
        return cords;
    }

    public static Vec2[] rectPoints(Rect rect, float rot){
        return rectPoints(rect.x, rect.y, rect.width, rect.height, rot);
    }

    /**
     * @deprecated use pointOnSqrtByAngle
     */
    @Deprecated()
    public static Vec2 sqrtByAngle(float radius, float angle, Vec2 vec2){
        return pointOnSqrtByAngle(radius, angle, vec2);
    }

    /**
     * @return point on square from radius and angle
     */
    public static Vec2 pointOnSqrtByAngle(float radius, float angle, Vec2 vector){
        return vector.trns(angle, sqrtDstByAngle(radius, angle));
    }

    /**
     * @return distance from square center to edge by angle
     */
    public static float sqrtDstByAngle(float radius, float angle){
        return radius / Math.max(Mathf.sinDeg(angle % 90), Mathf.cosDeg(angle % 90));
    }
}
