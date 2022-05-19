package mma.tests.content;

import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;
import mma.type.*;
import mma.world.blocks.production.*;

import static mindustry.type.ItemStack.*;

public class TestBlocks{
    public static Block multiCrafter, multiDrill;

    public static void load(){
        multiCrafter = new MultiCrafter("multi-crafter"){{
            size = 2;
            recipes(
            Recipe.with()
            .produceTime(1f * Time.toSeconds)
            .output(new ItemStack(TestItems.rawItem, 5), new LiquidStack(Liquids.cryofluid, 10))
            .outputLiquidDirection(0)//forward
            .consume(ItemStack.with(Items.silicon, 10, Items.titanium, 10), null)
            );
            requirements(Category.crafting, with(Items.copper, 3));
        }};
        multiDrill = new MultiRotatorDrill("multi-rotator"){{
            size = 3;
            tier=Items.thorium.hardness;

            drillTime = 280;
            hasPower = true;
            updateEffect = Fx.pulverizeMedium;
            drillEffect = Fx.mineBig;

            rotators(
            Rotator.withWorld(4f , 4f , 8f,"big-rotator").scaleTextureBySize(true),
             Rotator.withWorld(4f , 24f-4f, 8f,"big-rotator").scaleTextureBySize(true),
             Rotator.withWorld(24f-3f , 24f/2f , 6f,"small-rotator").scaleTextureBySize(true)
            );
            requirements(Category.crafting, with(Items.copper, 3));
            consumePower(1.10f);
            consumeLiquid(Liquids.water, 0.08f).boost();
        }};
    }
}
