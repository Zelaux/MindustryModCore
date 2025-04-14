package mmc.entities.compByAnuke;

import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import mindustry.world.blocks.power.*;
import static mindustry.logic.LAccess.*;

@mmc.annotations.ModAnnotations.MindustryEntityDef(value = PowerGraphUpdaterc.class, serialize = false, genio = false)
@Component
abstract class PowerGraphUpdaterComp implements Entityc {

    public transient PowerGraph graph;

    @Override
    public void update() {
        graph.update();
    }
}
