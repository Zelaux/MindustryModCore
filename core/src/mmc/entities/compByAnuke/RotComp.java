package mmc.entities.compByAnuke;

import mindustry.annotations.Annotations.*;
import mindustry.gen.*;

@Component
abstract class RotComp implements Entityc {

    @SyncField(false)
    @SyncLocal
    float rotation;
}
