package mmc.world;

import mindustry.world.Block;
import mmc.gen.ModContentRegions;

public class ModBlock extends Block {
    public ModBlock(String name) {
        super(name);
    }

    @Override
    public void load() {
        super.load();
        ModContentRegions.loadRegions(this);
    }
}
