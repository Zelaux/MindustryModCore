package mma.entities.compByAnuke;

import arc.math.*;
import arc.util.*;
import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import static mindustry.logic.LAccess.*;

@Component
abstract class ChildComp implements Posc, Rotc {

    @Import
    float x, y, rotation;

    @Nullable
    Posc parent;

    boolean rotWithParent;

    float offsetX, offsetY, offsetPos, offsetRot;

    @Override
    public void add() {
        Rotc r;
        if (parent != null) {
            offsetX = x - parent.getX();
            offsetY = y - parent.getY();
            if (rotWithParent && (parent instanceof Rotc && (r = (Rotc) parent) == parent)) {
                offsetPos = -r.rotation();
                offsetRot = rotation - r.rotation();
            }
        }
    }

    @Override
    public void update() {
        Rotc r;
        if (parent != null) {
            if (rotWithParent && (parent instanceof Rotc && (r = (Rotc) parent) == parent)) {
                x = parent.getX() + Angles.trnsx(r.rotation() + offsetPos, offsetX, offsetY);
                y = parent.getY() + Angles.trnsy(r.rotation() + offsetPos, offsetX, offsetY);
                rotation = r.rotation() + offsetRot;
            } else {
                x = parent.getX() + offsetX;
                y = parent.getY() + offsetY;
            }
        }
    }
}
