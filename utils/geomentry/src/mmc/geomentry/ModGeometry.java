package mmc.geomentry;

import arc.math.*;
import arc.math.geom.*;

public class ModGeometry{

    /**
     * @param radius half side size
     * @return point on square from radius and angle
     */
    public static Vec2 pointOnSqrtByAngle(float radius, float angle, Vec2 vector){
        return vector.trns(angle, sqrtDstByAngle(radius, angle));
    }

    /**
     * @param radius half side size
     * @return distance from square center to edge by angle
     */
    public static float sqrtDstByAngle(float radius, float angle){
        return radius / Math.max(Mathf.sinDeg(angle % 90), Mathf.cosDeg(angle % 90));
    }
}
