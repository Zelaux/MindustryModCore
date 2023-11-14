package mmc.world.draw;

import arc.*;
import arc.graphics.g2d.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import mmc.*;
import mmc.world.blocks.production.*;
import mmc.world.blocks.production.MultiCrafter.*;

public class DrawRecipeTexture extends DrawBlock{
    public TextureRegion[] itemsTexture;
    @Override
    public void draw(Building b){
        MultiCrafterBuild build = b.as();
        Draw.rect(itemsTexture[build.currentRecipe], build.x, build.y, build.block.rotate ? build.rotdeg() : 0.0F);
    }

    @Override
    public void load(Block block){
        MultiCrafter crafter = expectMultiCrafter(block);
        itemsTexture = new TextureRegion[crafter.recipes.length];
        for(int i = 0; i < itemsTexture.length; i++){
            String itemName = crafter.recipes[i].outputItem.item.name;
            if(itemName.startsWith(ModVars.fullName(""))) itemName = itemName.split(ModVars.fullName(""), 2)[1];
//                print("load: @",this.name+"-"+itemName);
            itemsTexture[i] = Core.atlas.find(crafter.name + "-" + itemName,crafter.region);
//            if(!itemsTexture[i].found()) itemsTexture[i] = ;
        }
    }

    public MultiCrafter expectMultiCrafter(Block block){
        if(!(block instanceof MultiCrafter crafter)) throw new ClassCastException("This drawer requires the block to be a MultiCrafter. Use a different drawer.");
        return crafter;
    }
}
