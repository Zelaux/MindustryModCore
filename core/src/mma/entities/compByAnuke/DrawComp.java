package mma.entities.compByAnuke;

import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import static mindustry.logic.LAccess.*;

@mma.annotations.ModAnnotations.Component
abstract class DrawComp implements Posc {

    float clipSize() {
        return Float.MAX_VALUE;
    }

    void draw() {
    }
}
