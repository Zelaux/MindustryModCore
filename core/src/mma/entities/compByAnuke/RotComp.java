package mma.entities.compByAnuke;

import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import static mindustry.logic.LAccess.*;

@Component
abstract class RotComp implements Entityc {

    @SyncField(false)
    @SyncLocal
    float rotation;
}
