package mma.entities.compByAnuke;

import arc.util.*;
import mindustry.gen.*;
import mma.annotations.ModAnnotations;


@ModAnnotations.Component
abstract class ChildComp implements Posc {

    @ModAnnotations.Import
    float x, y;

    @Nullable
    Posc parent;

    float offsetX, offsetY;

    @Override
    public void add() {
        if (parent != null) {
            offsetX = x - parent.getX();
            offsetY = y - parent.getY();
        }
    }

    @Override
    public void update() {
        if (parent != null) {
            x = parent.getX() + offsetX;
            y = parent.getY() + offsetY;
        }
    }
}