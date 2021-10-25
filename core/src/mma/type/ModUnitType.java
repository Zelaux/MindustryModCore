package mma.type;

import arc.*;
import mindustry.gen.*;
import mindustry.type.*;
import mma.*;

public class ModUnitType extends UnitType{
    public ModUnitType(String name){
        super(name);
        outlines= ModVars.packSprites;
        if (constructor == null) {
            constructor = EntityMapping.map(name);
        }
    }
    @Override
    public void load() {
        super.load();
        shadowRegion = Core.atlas.find(name + "-shadow", shadowRegion);
    }
}
