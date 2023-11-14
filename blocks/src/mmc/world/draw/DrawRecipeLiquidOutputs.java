package mmc.world.draw;

import arc.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import mmc.type.*;
import mmc.world.blocks.production.*;
import mmc.world.blocks.production.MultiCrafter.*;

public class DrawRecipeLiquidOutputs extends DrawBlock{
    public TextureRegion[][] liquidOutputRegions;

    @Override
    public void draw(Building b){
        MultiCrafterBuild build = b.as();
        Recipe recipe = build.getCurrentRecipe();
        if(recipe == null || recipe.outputLiquid == null) return;

        int side = recipe.outputLiquidDirection;
        if(side != -1){
            int realRot = (side + build.rotation) % 4;
            Draw.rect(liquidOutputRegions[realRot > 1 ? 1 : 0][build.currentRecipe], build.x, build.y, realRot * 90);
        }
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list){
       /* MultiCrafter crafter = (MultiCrafter)block;
        if(crafter.outputLiquids == null) return;

        for(int i = 0; i < crafter.outputLiquids.length; i++){
            int side = i < crafter.liquidOutputDirections.length ? crafter.liquidOutputDirections[i] : -1;
            if(side != -1){
                int realRot = (side + plan.rotation) % 4;
                Draw.rect(liquidOutputRegions[realRot > 1 ? 1 : 0][i], plan.drawx(), plan.drawy(), realRot * 90);
            }
        }*/
    }

    @Override
    public void load(Block block){
        var crafter = expectMultiCrafter(block);

        if(!Structs.contains(crafter.recipes, recipe -> recipe.outputLiquid != null)) return;

        liquidOutputRegions = new TextureRegion[2][crafter.recipes.length];
        for(int i = 0; i < crafter.recipes.length; i++){
            for(int j = 1; j <= 2; j++){
                liquidOutputRegions[j - 1][i] = Core.atlas.find(block.name + "-" + crafter.recipes[i].outputLiquid.liquid.name + "-output" + j);
            }
        }
    }

    //can't display these properly
    @Override
    public TextureRegion[] icons(Block block){
        return new TextureRegion[]{};
    }

    public MultiCrafter expectMultiCrafter(Block block){
        if(!(block instanceof MultiCrafter crafter)) throw new ClassCastException("This drawer requires the block to be a MultiCrafter. Use a different drawer.");
        return crafter;
    }
}
