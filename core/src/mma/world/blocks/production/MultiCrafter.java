package mma.world.blocks.production;

import arc.Core;
import arc.func.Func;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.Structs;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.Liquids;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import mma.type.Recipe;
import mma.ui.MultiBar;
import mma.world.ModBlock;
import mma.world.consumers.ConsumeLiquidDynamic;
import mma.world.meta.RecipeListValue;

import static mma.ModVars.fullName;


public class MultiCrafter extends ModBlock {
    public final int timerDump;
    public final int timerReBuildBars;
    public Recipe[] recipes = {};
    public Effect craftEffect;
    public Effect updateEffect;
    public Effect changeCraft;
    public float updateEffectChance;
    public TextureRegion[] itemsTexture;
    public boolean changeTexture = false;
    public boolean dynamicItem = true;
    public boolean dynamicLiquid = true;
    public float extraStorageLiquid = 1;
    public float extraStorageItem = 1;
    public int[] itemsCapacities = {};
    public float[] liquidsCapacities = {};

    public MultiCrafter(String name) {
        super(name);
        timerDump = timers++;
        timerReBuildBars = timers++;
        this.craftEffect = Fx.none;
        this.updateEffect = Fx.none;
        this.updateEffectChance = 0.04F;
        this.update = true;
        this.solid = true;
        this.hasItems = true;
        this.ambientSound = Sounds.machine;
        this.sync = true;
        this.ambientSoundVolume = 0.03F;
        this.flags = EnumSet.of(BlockFlag.factory);
        this.configurable = true;
        this.itemCapacity = 10;
        this.destructible = true;
        this.group = BlockGroup.none;
        this.config(Integer.class, (build, i) -> {
            MultiCrafterBuild tile = (MultiCrafterBuild) build;
            tile.currentRecipe = i >= 0 && i < recipes.length ? i : -1;
            Color color;
            boolean spawnEffect = changeTexture;
            if (tile.currentRecipe != -1) {
                color = new Color(Color.white).lerp(Color.yellow, 1.0f);
                if (itemsTexture != null && itemsTexture[i] == region) spawnEffect = false;
            } else {
                color = new Color(Color.black).lerp(Color.white, 0.0f);
            }
            if (spawnEffect && changeCraft!=null){
                changeCraft.at(tile.x, tile.y, tile.block.size * 1.1f, color, new Color(Color.black).lerp(Color.white, 8.0f));
            }
            tile.progress = 0.0F;
            tile.rebuildInfo();

        });
        if (dynamicItem) {
            this.consumes.add(new ConsumeItemDynamic(e -> {
                return ((MultiCrafterBuild) e).getCurrentRecipe().consumeItems;
            }));
        }
        if (dynamicLiquid) {
            this.consumes.add(new ConsumeLiquidDynamic<MultiCrafterBuild>(e -> {
                return e.getCurrentRecipe().consumeLiquids;
            }));
        }
    }

    public void recipes(Recipe... recipes) {
        this.recipes = recipes;
    }

    @Override
    public void load() {
        super.load();
        if (changeTexture) {
            itemsTexture = new TextureRegion[this.recipes.length];
            for (int i = 0; i < itemsTexture.length; i++) {
                String itemName = this.recipes[i].outputItem.item.name;
                if (itemName.startsWith(fullName(""))) itemName = itemName.split(fullName(""), 2)[1];
//                print("load: @",this.name+"-"+itemName);
                itemsTexture[i] = Core.atlas.find(this.name + "-" + itemName);
                if (!itemsTexture[i].found()) itemsTexture[i] = region;
            }
        }
    }

    @Override
    public void setStats() {
        stats.add(Stat.size, "@x@", size, size);
        stats.add(Stat.health, (float) health, StatUnit.none);
        if (canBeBuilt()) {
            stats.add(Stat.buildTime, buildCost / 60.0F, StatUnit.seconds);
            stats.add(Stat.buildCost, StatValues.items(false, requirements));
        }

        if (instantTransfer) {
            stats.add(Stat.maxConsecutive, 2.0F, StatUnit.none);
        }

        consumes.display(stats);
        if (hasLiquids) {
            stats.add(Stat.liquidCapacity, liquidCapacity, StatUnit.liquidUnits);
        }

        if (hasItems && itemCapacity > 0) {
            stats.add(Stat.itemCapacity, (float) itemCapacity, StatUnit.items);
        }
        stats.add(Stat.output, new RecipeListValue(recipes));


    }

    public void init() {
        this.itemCapacity = 0;
        itemsCapacities = new int[Vars.content.items().size];
        liquidsCapacities = new float[Vars.content.liquids().size];
        for (Recipe recipe : recipes) {
            for (ItemStack stack : recipe.consumeItems) {
                itemsCapacities[stack.item.id] = Math.max(itemsCapacities[stack.item.id], stack.amount * 2);
                this.itemCapacity = Math.max(this.itemCapacity, stack.amount);
            }
            for (LiquidStack stack : recipe.consumeLiquids) {
                liquidsCapacities[stack.liquid.id] = Math.max(liquidsCapacities[stack.liquid.id], stack.amount * 2);
                this.liquidCapacity = Math.max(this.liquidCapacity, stack.amount);
            }
            if (recipe.outputLiquid != null)
                this.liquidCapacity = Math.max(this.liquidCapacity, recipe.outputLiquid.amount);
            if (recipe.outputItem != null) this.itemCapacity = Math.max(this.itemCapacity, recipe.outputItem.amount);
        }
        this.liquidCapacity *= extraStorageLiquid;
        this.itemCapacity *= extraStorageItem;
        super.init();


        this.config(Item.class, (obj, item) -> {
            MultiCrafterBuild tile = (MultiCrafterBuild) obj;
            tile.currentRecipe = Structs.indexOf(recipes, recipe -> recipe.outputItem.item == item);
            tile.resetProgress();
        });
    }

    @Override
    public void setBars() {
        bars.add("health", (entity) -> {
            return (new Bar("stat.health", Pal.health, entity::healthf)).blink(Color.white);
        });
        if (this.hasLiquids) {
            Func<Building, Liquid> current;
            if (this.consumes.has(ConsumeType.liquid) && this.consumes.get(ConsumeType.liquid) instanceof ConsumeLiquidDynamic) {
                bars.add("liquids", (t) -> {
                    MultiCrafterBuild build = (MultiCrafterBuild) t;
                    if (build == null) return new Bar("0", Color.black.cpy(), () -> 0f);
                    Seq<MultiBar.BarPart> barParts = new Seq<>();

                    LiquidStack[] stacks = build.getNeedLiquids();
                    for (LiquidStack stack : stacks) {
                        barParts.add(new MultiBar.BarPart(stack.liquid.color, () -> {
                            if (build.liquids == null) return 0.0f;
                            float amounts = build.liquids.get(stack.liquid);
                            float need = stack.amount;
                            if (need == 0 && build.currentRecipe != -1) return 0;
                            return Math.max(amounts / need, 0);
                        }));
                    }
                    return new MultiBar(() -> {
                        String text = Core.bundle.get("bar.liquids");
                        if (build.liquids == null)
                            return text;
                        return text + " " + Mathf.round((build.countNowLiquid() / build.countNeedLiquid() * 100f), 0.1f) + "%";
                    }, barParts);
                });

            } else {
                if (this.consumes.has(ConsumeType.liquid) && this.consumes.get(ConsumeType.liquid) instanceof ConsumeLiquid) {
                    Liquid liquid = ((ConsumeLiquid) this.consumes.get(ConsumeType.liquid)).liquid;
                    current = (entity) -> {
                        return liquid;
                    };
                } else {
                    current = (entity) -> {
                        return entity.liquids == null ? Liquids.water : entity.liquids.current();
                    };
                }
                bars.add("liquid", (entity) -> {
                    return new Bar(() -> {
                        return entity.liquids.get((Liquid) current.get(entity)) <= 0.001F ? Core.bundle.get("bar.liquid") : ((Liquid) current.get(entity)).localizedName;
                    }, () -> {
                        return ((Liquid) current.get(entity)).barColor();
                    }, () -> {
                        return entity != null && entity.liquids != null ? entity.liquids.get((Liquid) current.get(entity)) / this.liquidCapacity : 0.0F;
                    });
                });
            }
        }

        if (this.hasPower && this.consumes.hasPower()) {
            ConsumePower cons = this.consumes.getPower();
            boolean buffered = cons.buffered;
            float capacity = cons.capacity;
            bars.add("power", (entity) -> {
                return new Bar(() -> {
                    return buffered ? Core.bundle.format("bar.poweramount", Float.isNaN(entity.power.status * capacity) ? "<ERROR>" : (int) (entity.power.status * capacity)) : Core.bundle.get("bar.power");
                }, () -> {
                    return Pal.powerBar;
                }, () -> {
                    return Mathf.zero(cons.requestedPower(entity)) && entity.power.graph.getPowerProduced() + entity.power.graph.getBatteryStored() > 0.0F ? 1.0F : entity.power.status;
                });
            });
        }

        if (this.hasItems && this.configurable) {

            bars.add("items", (entity) -> {
                return new Bar(() -> {
                    return Core.bundle.format("bar.items", entity.items.total());
                }, () -> {
                    return Pal.items;
                }, () -> {
                    return (float) entity.items.total() / (float) this.itemCapacity;
                });
            });
        }

    }


    public class MultiCrafterBuild extends Building {
        public int currentRecipe = -1;
        public MultiCrafter block;
        public float progress;
        public float totalProgress;
        protected Runnable rebuildBars = () -> {
        };
        protected Runnable rebuildCons = () -> {
        };

        public MultiCrafterBuild() {
            this.block = MultiCrafter.this;
        }

        public Building init(Tile tile, Team team, boolean shouldAdd, int rotation) {
            return super.init(tile, team, shouldAdd, rotation);
        }

        @Override
        public void update() {
            super.update();
        }

        public void displayConsumption(Table table) {
            rebuildCons = () -> {
                table.clearChildren();
                table.clear();
                table.left();

                for (Consume cons : consumes.all()) {
                    if (!cons.isOptional() || !cons.isBoost()) {
                        cons.build(this, table);
                    }
                }
            };
            rebuildCons.run();

        }

        public void displayBars(Table table) {

            rebuildBars = () -> {
                table.clearChildren();
                for (Func<Building, Bar> bar : bars.list()) {
                    try {
                        table.add(bar.get(this)).growX();
                        table.row();
                    } catch (ClassCastException e) {
                        break;
                    }
                }
            };
            rebuildBars.run();
        }

        public void drawStatus() {
            if (this.currentRecipe == -1) return;
            if (!MultiCrafter.this.changeTexture || (MultiCrafter.this.size >= 2)) {
                super.drawStatus();
            } else {
                float brcx = this.tile.drawx() + (float) (this.block.size * 8) / 2.0F - 2.0F;
                float brcy = this.tile.drawy() - (float) (this.block.size * 8) / 2.0F + 2.0F;
                Draw.z(71.0F);
                Draw.color(Pal.gray);
                Fill.square(brcx, brcy, 1.25f, 45.0F);
                Draw.color(this.cons.status().color);
                Fill.square(brcx, brcy, 0.75f, 45.0F);
                Draw.color();
            }

        }

        public int getMaximumAccepted(Item item) {
            return itemsCapacities[item.id];
        }

        public void draw() {
            if (!MultiCrafter.this.changeTexture || this.currentRecipe == -1) {
                Draw.rect(this.block.region, this.x, this.y, this.block.rotate ? this.rotdeg() : 0.0F);
            } else {
                Draw.rect(MultiCrafter.this.itemsTexture[currentRecipe], this.x, this.y, this.block.rotate ? this.rotdeg() : 0.0F);
            }
        }

        public void buildConfiguration(Table table) {
            Seq<Item> recipes = Seq.with(MultiCrafter.this.recipes).map((u) -> {
                return u.outputItem.item;
            }).filter((u) -> {
                return u.unlockedNow();
            });
            if (recipes.any()) {
                ItemSelection.buildTable(table, recipes, () -> {
                    return currentRecipe == -1 ? null : MultiCrafter.this.recipes[currentRecipe].outputItem.item;
                }, (item) -> {
                    this.configure(Structs.indexOf(MultiCrafter.this.recipes, (u) -> {
                        return u.outputItem.item == item;
                    }));
                });
            } else {
                table.table(Styles.black3, (t) -> {
                    t.add("@none").color(Color.lightGray);
                });
            }
        }

        public float countNowLiquid() {
            float amounts = 0;
            for (LiquidStack stack : getNeedLiquids()) {
                amounts += Math.min(this.liquids.get(stack.liquid), stack.amount);
            }
            return amounts;
        }

        public float countNeedLiquid() {
            float need = 0;
            for (LiquidStack stack : getNeedLiquids()) {
                need += stack.amount;
            }
            return need;
        }

        public LiquidStack[] getNeedLiquids() {
            return getCurrentRecipe().consumeLiquids;
        }

        public void handleLiquid(Building source, Liquid liquid, float amount) {
            LiquidStack[] needLiquids = getNeedLiquids();
            LiquidStack found = Structs.find(needLiquids, (l) -> l.liquid == liquid);
            if (found == null) {
                return;
            }
            float need = Math.max(0, found.amount - liquids.get(liquid));
            this.liquids.add(liquid, Math.min(amount, need));
        }

        @Override
        public String toString() {
            return "MultiCrafterBuild#" + id;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            LiquidStack found = Structs.find(getNeedLiquids(), (l) -> l.liquid.name.equals(liquid.name));
            return found != null && this.liquids.get(liquid) <= liquidsCapacities[liquid.id];
        }


        public boolean acceptItem(Building source, Item item) {
            int[] count = {-1};
            for (ItemStack stack : getCurrentRecipe().consumeItems) {
                if (stack.item == item) count[0] += stack.amount;
            }
            return count[0] > 0 && this.items.get(item) < count[0];
        }

        public boolean shouldConsume() {
            return getCurrentRecipe().outputItem == null || this.items.get(getCurrentRecipe().outputItem.item) < MultiCrafter.this.itemCapacity;
        }

        protected Recipe getCurrentRecipe() {
            if (currentRecipe == -1) return Recipe.empty;
            return recipes[currentRecipe];
        }

        public boolean canCraft() {
            ItemStack[] requirements = getCurrentRecipe().consumeItems;
            int req = 0;
            for (ItemStack i : requirements) {
                req += (i.amount + i.item.id);
            }
            int[] counter={0};
            items.each((item, c) -> {
                counter[0] += (item.id + c);
            });
            int now = counter[0];

            return this.consValid() && req <= now;
        }

        public void updateTile() {
            if (timer.get(timerReBuildBars, 5)) {
                setBars();
            }
            if (currentRecipe < 0 || currentRecipe >= recipes.length) {
                currentRecipe = -1;
                progress = 0;
            }

            if (canCraft() && currentRecipe != -1) {
                progress += getProgressIncrease(recipes[currentRecipe].produceTime);
                totalProgress += delta();
            }

            if (progress >= 1.0F && currentRecipe != -1) {
                consume();
                if (getCurrentRecipe().outputItem != null) {
                    for (int i = 0; i < getCurrentRecipe().outputItem.amount; ++i) {
                        offload(getCurrentRecipe().outputItem.item);
                    }
                }

                craftEffect.at(x, y);
                progress %= 1f;
            }
            if (getCurrentRecipe().outputItem != null && timer(timerDump, 5.0F)) {
                dump(getCurrentRecipe().outputItem.item);
            }

            if (currentRecipe == -1) sleep();

        }

        public Object config() {
            return currentRecipe;
        }

        public void write(Writes write) {
            super.write(write);
            write.i(currentRecipe);
        }

        public void playerPlaced(Object config) {
            if (lastConfig == null) lastConfig = -1;
            if (config == null) {
                if (!lastConfig.equals(-1)) configure(lastConfig);
            } else {
                configure(config);
            }
        }

        public void read(Reads read, byte revision) {
            super.read(read, revision);
            currentRecipe = read.i();
            if (currentRecipe < 0 || currentRecipe >= recipes.length) {
                currentRecipe = -1;
                progress = 0;
            }
        }

        public void resetProgress() {
            progress = 0f;
            totalProgress = 0f;
        }

        public void rebuildInfo() {
            rebuildBars.run();
            rebuildCons.run();
        }
    }
}