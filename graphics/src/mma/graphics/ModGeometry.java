package mma.graphics;

import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.util.Tmp;

public class ModGeometry {private static final Vec2 tmp1=new Vec2(),tmp2=new Vec2();
    private static Vec2 vec2(float x,float y){
        return new Vec2(x,y);
    }
    public static Vec2[] rectPoints(float x, float y, float width, float height, float rot){
        float vw = width / 2f;
        float vh = height / 2f;
        Vec2[] points=new Vec2[]{vec2(-vw,-vh),vec2(vw,-vh),vec2(vw,vh),vec2(-vw,vh),vec2(-vw,-vh)};
//        Seq<Vec2> cords=new Seq<>();
        Vec2[] cords=new Vec2[points.length];
        for (int i = 0; i < points.length; i++) {

            cords[i]=(points[i].cpy().rotate(rot).add(x+vw,y+vh));
        }
        return cords;
    }
    public static Vec2[] rectPoints(Rect rect, float rot){
        return rectPoints(rect.x,rect.y,rect.width,rect.height,rot);
    }

    public static Vec2 sqrtByAngle(float radius, float angle,Vec2 vec2) {
        int angleOffset = (int) (angle / 90);
        tmp1.trns(angle, 100f);
        float na = angle % 90;
        tmp2.set(tmp1).rotate(-angleOffset * 90);
        float sx = tmp2.x, sy = tmp2.y;
        if (na <= 45) {
            tmp2.scl(radius / sx, radius / sx);
        } else {
            tmp2.scl(radius / sy, radius / sy);
        }
        tmp2.rotate(angleOffset * 90);
        vec2.set(tmp2);
        return vec2;
    }
}
