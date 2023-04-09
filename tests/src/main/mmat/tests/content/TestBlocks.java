package mmat.tests.content;

import arc.util.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mma.type.*;
import mma.world.blocks.distribution.*;
import mma.world.blocks.production.*;
import mmat.tests.world.*;

import static mindustry.type.ItemStack.with;

public class TestBlocks{
    public static Block multiCrafter, multiDrill, smartSorter, smartRouter;

    public static void load(){
        new ExampleShapedBlockSprite("rail"){{
            rotate=true;
            requirements(Category.distribution, with(Items.copper, 3));
        }};
        new ExampleShapedBlockSprite("rail-turn"){{
            rotate=true;
            requirements(Category.distribution, with(Items.copper, 3));
        }};
        /*new ItemTurret("test-turret-1"){{
            requirements(Category.turret, with(Items.copper, 100, Items.graphite, 80, Items.titanium, 50));
            ammo(
            Items.copper,  new BasicBulletType(2.5f, 11){{
                width = 7f;
                height = 9f;
                lifetime = 60f;
                ammoMultiplier = 2;
            }}
            );

            size = 2;
            range = 190f;
            reload = 31f;
            ammoEjectBack = 3f;
            recoil = 3f;
            shake = 1f;
            shoot.shots = 4;
            shoot.shotDelay = 3f;

            ammoUseEffect = Fx.casing2;
            scaledHealth = 240;
            shootSound = Sounds.shootBig;

            limitRange();
            coolant = consumeCoolant(0.2f);
        }};
        new ItemTurret("test-turret-2"){{
            shoot=new ShootPattern(){
                @Override
                public void shoot(int totalShots, BulletHandler handler){
                    try{
                        Method shoot = handler.getClass().getMethod("shoot", float.class, float.class, float.class, float.class, float.class, float.class);

                        for(int i = 0; i < shots; i++){
                            shoot.invoke(handler,0, 0,-2*tilesize,0, 0, firstShotDelay + shotDelay * i);
//                            handler.shoot();
                        }
                    }catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
                        throw new RuntimeException(e);
                    }
                }

                {

            }};
            requirements(Category.turret, with(Items.copper, 100, Items.graphite, 80, Items.titanium, 50));
            ammo(
            Items.copper,  new BasicBulletType(2.5f, 11){{
                width = 7f;
                height = 9f;
                lifetime = 60f;
                ammoMultiplier = 2;
            }}
            );

            size = 2;
            range = 190f;
            reload = 31f;
            ammoEjectBack = 3f;
            recoil = 3f;
            shake = 1f;
            shoot.shots = 4;
            shoot.shotDelay = 3f;

            ammoUseEffect = Fx.casing2;
            scaledHealth = 240;
            shootSound = Sounds.shootBig;

            limitRange();
            coolant = consumeCoolant(0.2f);
        }};*/
        smartSorter = new SmartSorter("smart-sorter"){{
            size = 1;
            requirements(Category.crafting, with(Items.copper, 3));
        }};
        smartRouter = new SmartRouter("smart-router"){{
            size = 1;
            requirements(Category.crafting, with(Items.copper, 3));
        }};
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
        new MultiCrafter("multi-crafter-2"){{
            size = 2;
            recipes(
            Recipe.with().produceTime(1f * Time.toSeconds)
            .output(null, new LiquidStack(Liquids.cryofluid, 10))
            .outputLiquidDirection(1)//left
            .consume(ItemStack.with(Items.silicon, 10, Items.titanium, 10), null),

            Recipe.with().produceTime(1f * Time.toSeconds)
            .output(null, new LiquidStack(Liquids.cryofluid, 20))
            .outputLiquidDirection(4 - 1)//right
            .consume(ItemStack.with(Items.silicon, 20, Items.titanium, 20), null)
            );
            requirements(Category.crafting, with(Items.copper, 3));
        }};
        new MultiCrafter("multi-crafter-3"){{
            size = 2;
            recipes(
            Recipe.with().produceTime(1f * Time.toSeconds)
            .output(new ItemStack(TestItems.rawItem, 5), null)
            .consume(ItemStack.with(Items.silicon, 10, Items.titanium, 10), null),

            Recipe.with().produceTime(1f * Time.toSeconds)
            .output(new ItemStack(TestItems.rawItem, 5), null)
            .consume(ItemStack.with(Items.silicon, 20, Items.titanium, 20), null)
            );
            requirements(Category.crafting, with(Items.copper, 3));
        }};
        multiDrill = new MultiRotatorDrill("multi-rotator"){{
            size = 3;
            tier = Items.thorium.hardness;

            drillTime = 280;
            hasPower = true;
            updateEffect = Fx.pulverizeMedium;
            drillEffect = Fx.mineBig;

            rotators(
            Rotator.withWorld(4f, 4f, 8f, "big-rotator").scaleTextureBySize(true),
            Rotator.withWorld(4f, 24f - 4f, 8f, "big-rotator").scaleTextureBySize(true),
            Rotator.withWorld(24f - 3f, 24f / 2f, 6f, "small-rotator").scaleTextureBySize(true)
            );
            requirements(Category.crafting, with(Items.copper, 3));
            consumePower(1.10f);
            consumeLiquid(Liquids.water, 0.08f).boost();
        }};
        new TestBlock("test-block"){{
            requirements(Category.distribution, with(Items.copper, 3));
            size = 2;
            buildCostMultiplier = 4f;
        }};
    }
}
