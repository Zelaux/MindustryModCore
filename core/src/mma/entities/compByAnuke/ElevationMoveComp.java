package mma.entities.compByAnuke;

import mindustry.annotations.Annotations.*;
import mindustry.entities.*;
import mindustry.entities.EntityCollisions.*;
import mindustry.gen.*;
import static mindustry.logic.LAccess.*;

@mma.annotations.ModAnnotations.Component
abstract class ElevationMoveComp implements Velc, Posc, Flyingc, Hitboxc {

    @mma.annotations.ModAnnotations.Import
    float x, y;

    @mma.annotations.ModAnnotations.Replace
    @Override
    public SolidPred solidity() {
        return isFlying() ? null : EntityCollisions::solid;
    }
}
