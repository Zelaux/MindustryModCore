package mma.entities.compByAnuke;

import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.math.geom.QuadTree.*;
import mindustry.annotations.Annotations.*;
import mindustry.entities.*;
import mindustry.gen.*;
import static mindustry.logic.LAccess.*;

@Component
abstract class HitboxComp implements Posc, Sized, QuadTreeObject {

    @Import
    float x, y;

    transient float lastX, lastY, deltaX, deltaY, hitSize;

    @Override
    public void update() {
    }

    @Override
    public void add() {
        updateLastPosition();
    }

    @Override
    public void afterRead() {
        updateLastPosition();
    }

    @Override
    public float hitSize() {
        return hitSize;
    }

    void getCollisions(Cons<QuadTree> consumer) {
    }

    void updateLastPosition() {
        deltaX = x - lastX;
        deltaY = y - lastY;
        lastX = x;
        lastY = y;
    }

    void collision(Hitboxc other, float x, float y) {
    }

    float deltaLen() {
        return Mathf.len(deltaX, deltaY);
    }

    float deltaAngle() {
        return Mathf.angle(deltaX, deltaY);
    }

    boolean collides(Hitboxc other) {
        return true;
    }

    @Override
    public void hitbox(Rect rect) {
        rect.setCentered(x, y, hitSize, hitSize);
    }

    public void hitboxTile(Rect rect) {
        // tile hitboxes are never bigger than a tile, otherwise units get stuck
        float size = Math.min(hitSize * 0.66f, 7.9f);
        rect.setCentered(x, y, size, size);
    }
}
