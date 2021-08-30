package mma.entities.compByAnuke;

import mindustry.gen.*;
import mma.annotations.ModAnnotations;


@ModAnnotations.Component
abstract class RotComp implements Entityc {

    @ModAnnotations.SyncField(false)
    @ModAnnotations.SyncLocal
    float rotation;
}