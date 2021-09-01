package mma.entities.compByAnuke;

import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import static mindustry.logic.LAccess.*;

@mma.annotations.ModAnnotations.Component
abstract class RotComp implements Entityc {

    @mma.annotations.ModAnnotations.SyncField(false)
    @mma.annotations.ModAnnotations.SyncLocal
    float rotation;
}
