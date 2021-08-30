package mma.entities.compByAnuke;

import mindustry.entities.*;
import mindustry.entities.EntityCollisions.*;
import mindustry.gen.*;
import mma.annotations.ModAnnotations;


@ModAnnotations.Component
abstract class ElevationMoveComp implements Velc, Posc, Flyingc, Hitboxc {

    @ModAnnotations.Import
    float x, y;

    @ModAnnotations.Replace
    @Override
    public SolidPred solidity() {
        return isFlying() ? null : EntityCollisions::solid;
    }
}