package mmat.tests.entities.comp;

import arc.math.geom.*;
import arc.struct.*;
import mindustry.annotations.Annotations.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mma.annotations.ModAnnotations.*;
import mmat.tests.gen.*;

@Component()
@EntityDef(value = {TestBulletc.class}, pooled = true)
 abstract class TestBulletComp implements Bulletc{
}
