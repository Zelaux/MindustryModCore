package mma.tests.entities;

import mindustry.annotations.Annotations.*;
import mma.gen.*;

class ModGroupDefs<G>{
    @GroupDef(value = TestAnnotationsc.class) G testGroup;
    @GroupDef(value = mindustry.gen.Buildingc.class) G testBuilds;
}
