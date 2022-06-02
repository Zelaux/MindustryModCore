package mmat.tests.entities;

import mindustry.annotations.Annotations.*;
import mma.gen.*;
import mmat.tests.gen.*;

class ModGroupDefs<G>{
    @GroupDef(value = TestAnnotationsc.class) G testGroup;
    @GroupDef(value = mindustry.gen.Buildingc.class) G testBuilds;
}
