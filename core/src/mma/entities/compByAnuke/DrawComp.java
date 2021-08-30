package mma.entities.compByAnuke;

import mindustry.gen.*;
import mma.annotations.ModAnnotations;


@ModAnnotations.Component
abstract class DrawComp implements Posc {

    float clipSize() {
        return Float.MAX_VALUE;
    }

    void draw() {
    }
}