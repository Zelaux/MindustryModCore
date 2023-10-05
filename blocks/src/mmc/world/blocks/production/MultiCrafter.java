package mmc.world.blocks.production;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.EnumSet;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import mmc.type.*;
import mmc.ui.*;
import mmc.world.*;
import mmc.world.consumers.*;
import mmc.world.meta.*;

import static mindustry.Vars.*;


public class MultiCrafter extends ModBlock{
    public final int timerDump;
    public final int timerReBuildBars;
    public Recipe[] recipes = {};

    /** if true, crafters with multiple liquid outputs will dump excess when there's still space for at least one liquid type */
    public boolean dumpExtraLiquid = true;
    public boolean ignoreLiquidFullness = false;

    //TODO should be seconds?
    public float craftTime = 80;
    public Effect craftEffect = Fx.none;
    public Effect updateEffect = Fx.none;
    public Effect changeCraftEffect = Fx.none;
    public float updateEffectChance = 0.04f;
    public float warmupSpeed = 0.019f;
    /** Only used for legacy cultivator blocks. */
    public boolean legacyReadWarmup = false;

    public DrawBlock drawer = new DrawDefault();
    public boolean changeTexture = false;
    public boolean dynamicItem = true;
    public boolean dynamicLiquid = true;
    public float extraStorageLiquid = 1;
    public float extraStorageItem = 1;
    public int[] itemsCapacities = {};
    public float[] liquidsCapacities = {};

    public MultiCrafter(String name){
        super(name);
        timerDump = timers++;
        timerReBuildBars = timers++;
        this.update = true;
        this.solid = true;
        this.hasItems = true;
        this.ambientSound = Sounds.machine;
        this.sync = true;
        this.ambientSoundVolume = 0.03F;
        this.flags = EnumSet.of(BlockFlag.factory);
        this.configurable = true;
        this.destructible = true;
        config(Integer.class, (MultiCrafterBuild tile, Integer i) -> {
            tile.currentRecipe = i >= 0 && i < recipes.length ? i : -1;
            Color color;
            boolean spawnEffect = changeTexture;
            if(tile.currentRecipe != -1){
                color = new Color(Color.white).lerp(Color.yellow, 1.0f);
//                if(itemsTexture != null && itemsTexture[i] == region) spawnEffect = false;
            }else{
                color = new Color(Color.black).lerp(Color.white, 0.0f);
            }
            if(spawnEffect && changeCraftEffect != null){
                changeCraftEffect.at(tile.x, tile.y, tile.block.size * 1.1f, color, new Color(Color.black).lerp(Color.white, 8.0f));
            }
            tile.progress = 0.0F;
            tile.rebuildInfo();

        });
        if(dynamicItem){
            this.consumeBuilder.add(new ConsumeItemDynamic(e -> {
                return ((MultiCrafterBuild)e).getCurrentRecipe().consumeItems;
            }));
        }
        if(dynamicLiquid){
            this.consumeBuilder.add(new ConsumeLiquidDynamic<MultiCrafterBuild>(e -> {
                return e.getCurrentRecipe().consumeLiquids;
            }));
        }
    }

    public void recipes(Recipe... recipes){
        this.recipes = recipes;
    }

    @Override
    public void load(){
        super.load();
        drawer.load(this);
    }

    @Override
    public void setStats(){
        stats.add(Stat.size, "@x@", size, size);
        stats.add(Stat.health, (float)health, StatUnit.none);
        if(canBeBuilt()){
            stats.add(Stat.buildTime, buildCost / 60.0F, StatUnit.seconds);
            stats.add(Stat.buildCost, StatValues.items(false, requirements));
        }

        if(instantTransfer){
            stats.add(Stat.maxConsecutive, 2.0F, StatUnit.none);
        }


        for(var c : consumers){
            c.display(stats);
        }
        if(hasLiquids){
            stats.add(Stat.liquidCapacity, liquidCapacity, StatUnit.liquidUnits);
        }

        if(hasItems && itemCapacity > 0){
            stats.add(Stat.itemCapacity, (float)itemCapacity, StatUnit.items);
        }
        stats.add(Stat.output, new RecipeListValue(recipes));


    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons(){
        return drawer.finalIcons(this);
    }

    @Override
    public boolean outputsItems(){
        return Structs.contains(recipes, r -> r.outputItem != null && r.outputItem.amount > 0);
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out){
        drawer.getRegionsToOutline(this, out);
    }

    @Override
    public void init(){
        rotate |= Structs.contains(recipes, recipe -> recipe.outputLiquid != null && recipe.outputLiquidDirection != -1);
        initCapacities();
        super.init();


        this.config(Item.class, (obj, item) -> {
            MultiCrafterBuild tile = (MultiCrafterBuild)obj;
            tile.currentRecipe = Structs.indexOf(recipes, recipe -> recipe.outputItem.item == item);
            tile.resetProgress();
        });
    }

    private void initCapacities(){
        this.itemCapacity = 0;
        itemsCapacities = new int[Vars.content.items().size];
        liquidsCapacities = new float[Vars.content.liquids().size];
        var maxConsumeItems = itemCapacity;
        var maxConsumeLiquids = liquidCapacity;
        for(Recipe recipe : recipes){
            int consumeItems = 0;
            int consumeLiquids = 0;
            for(ItemStack stack : recipe.consumeItems){
                consumeItems += stack.amount;
                itemsCapacities[stack.item.id] = Math.max(itemsCapacities[stack.item.id], stack.amount * 2);
                this.itemCapacity = Math.max(this.itemCapacity, stack.amount);
            }
            maxConsumeItems = Math.max(maxConsumeItems, consumeItems);
            for(LiquidStack stack : recipe.consumeLiquids){
                consumeLiquids += stack.amount;
                liquidsCapacities[stack.liquid.id] = Math.max(liquidsCapacities[stack.liquid.id], stack.amount * 2);
                this.liquidCapacity = Math.max(this.liquidCapacity, stack.amount);
            }
            maxConsumeLiquids = Math.max(maxConsumeLiquids, consumeLiquids);
            if(recipe.outputLiquid != null){

                LiquidStack stack = recipe.outputLiquid;
                liquidsCapacities[stack.liquid.id] = Math.max(liquidsCapacities[stack.liquid.id], stack.amount * 2);
                this.liquidCapacity = Math.max(this.liquidCapacity, stack.amount);
            }
            if(recipe.outputItem != null){
                ItemStack stack = recipe.outputItem;
                itemsCapacities[stack.item.id] = Math.max(itemsCapacities[stack.item.id], stack.amount * 2);
                this.itemCapacity = Math.max(this.itemCapacity, stack.amount);
            }
        }
//        Log.info("MultiCrafter.init.itemsCapacities=@", Arrays.toString(itemsCapacities));
//        Log.info("MultiCrafter.init.liquidsCapacities=@", Arrays.toString(liquidsCapacities));
        liquidCapacity = maxConsumeLiquids;
        itemCapacity = maxConsumeItems;
        this.liquidCapacity *= extraStorageLiquid;
        this.itemCapacity *= extraStorageItem;
    }

    @Override
    public void setBars(){
        addBar("health", (entity) -> {
            return (new Bar("stat.health", Pal.health, entity::healthf)).blink(Color.white);
        });

        if(consPower != null){
            boolean buffered = consPower.buffered;
            float capacity = consPower.capacity;

            addBar("power", entity -> new Bar(
            () -> buffered ? Core.bundle.format("bar.poweramount", Float.isNaN(entity.power.status * capacity) ? "<ERROR>" : UI.formatAmount((int)(entity.power.status * capacity))) :
            Core.bundle.get("bar.power"),
            () -> Pal.powerBar,
            () -> Mathf.zero(consPower.requestedPower(entity)) && entity.power.graph.getPowerProduced() + entity.power.graph.getBatteryStored() > 0f ? 1f : entity.power.status)
            );
        }

        if(hasItems && configurable){
            addBar("items", entity -> new Bar(
            () -> Core.bundle.format("bar.items", entity.items.total()),
            () -> Pal.items,
            () -> (float)entity.items.total() / itemCapacity)
            );
        }

        if(unitCapModifier != 0){
            stats.add(Stat.maxUnits, (unitCapModifier < 0 ? "-" : "+") + Math.abs(unitCapModifier));
        }
        //liquids added last
        if(hasLiquids){
            ConsumeLiquidDynamic<?> consumeLiquidDynamic = (ConsumeLiquidDynamic<?>)Structs.find(consumers, c -> c instanceof ConsumeLiquidDynamic);
            if(consumeLiquidDynamic != null){
                addBar("liquids", (MultiCrafterBuild build) -> {
                    if(build == null) return new Bar("0", Color.black.cpy(), () -> 0f);
                    Seq<MultiBar.BarPart> barParts = new Seq<>();

                    LiquidStack[] stacks = build.getNeedLiquids();
                    for(LiquidStack stack : stacks){
                        barParts.add(new MultiBar.BarPart(stack.liquid.color, () -> {
                            if(build.liquids == null) return 0.0f;
                            float amounts = build.liquids.get(stack.liquid);
                            float need = stack.amount;
                            if(need == 0 && build.currentRecipe != -1) return 0;
                            return Math.max(amounts / need, 0);
                        }));
                    }
                    return new MultiBar(() -> {
                        String text = Core.bundle.get("bar.liquids");
                        if(build.liquids == null)
                            return text;
                        return text + " " + Mathf.round((build.countNowLiquid() / build.countRequiredLiquid() * 100f), 0.1f) + "%";
                    }, barParts);
                });
            }
            //TODO liquids need to be handled VERY carefully. there are several potential possibilities:
            //1. no consumption or output (conduit/tank)
            // - display current(), 1 bar
            //2. static set of inputs and outputs
            // - create bars for each input/output, straightforward
            //3. TODO dynamic input/output combo???
            // - confusion

            boolean added = false;

            //TODO handle in consumer
            //add bars for *specific* consumed liquids
            for(var consl : consumers){
                if(consl instanceof ConsumeLiquid liq){
                    added = true;
                    addLiquidBar(liq.liquid);
                }else if(consl instanceof ConsumeLiquids multi){
                    added = true;
                    for(var stack : multi.liquids){
                        addLiquidBar(stack.liquid);
                    }
                }
            }

            //nothing was added, so it's safe to add a dynamic liquid bar (probably?)
            if(!added){
                addLiquidBar(build -> build.liquids.current());
            }
        }
    }


    public class MultiCrafterBuild extends Building{
        public int currentRecipe = -1;
        public MultiCrafter block = MultiCrafter.this;
        public float progress;
        public float totalProgress;
        public float warmup;
        protected Runnable rebuildBars = () -> {
        };
        protected Runnable rebuildCons = () -> {
        };

        @Override
        public void draw(){
            drawer.draw(this);
        }

        @Override
        public void drawLight(){
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public boolean shouldConsume(){
            if(getCurrentRecipe().outputItem != null){
                var output = getCurrentRecipe().outputItem;
                if(items.get(output.item) + output.amount > itemCapacity){
                    return false;
                }
            }
            if(getCurrentRecipe().outputLiquid != null && !ignoreLiquidFullness){
                boolean allFull = true;
                var output = getCurrentRecipe().outputLiquid;
                if(liquids.get(output.liquid) >= liquidCapacity - 0.001f){
                    if(!dumpExtraLiquid){
                        return false;
                    }
                }else{
                    //if there's still space left, it's not full for all liquids
                    allFull = false;
                }

                //if there is no space left for any liquid, it can't reproduce
                if(allFull){
                    return false;
                }
            }

            return enabled;
//            return getCurrentRecipe().outputItem == null || this.items.get(getCurrentRecipe().outputItem.item) < MultiCrafter.this.itemCapacity;
        }

        public void updateTile(){
            if(timer.get(timerReBuildBars, 5)){
                setBars();
            }

            if(currentRecipe < 0 || currentRecipe >= recipes.length){
                currentRecipe = -1;
                progress = 0;
            }

            if(efficiency > 0 && currentRecipe != -1){

                progress += getProgressIncrease(getCurrentRecipe().produceTime);
                warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed);

                //continuously output based on efficiency
                Recipe recipe = getCurrentRecipe();
                if(recipe.outputLiquid != null){
                    float inc = getProgressIncrease(1f);
                    var output = recipe.outputLiquid;
                    handleLiquid(this, output.liquid, Math.min(output.amount * inc, liquidsCapacities[output.liquid.id] - liquids.get(output.liquid)));
                }

                if(wasVisible && Mathf.chanceDelta(updateEffectChance)){
                    updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4));
                }
            }else{
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }

            //TODO may look bad, revert to edelta() if so
            totalProgress += warmup * Time.delta;

            if(progress >= 1f){
                craft();
            }

            dumpOutputs();
            if(getCurrentRecipe().outputItem != null && timer(timerDump, 5.0F)){
                dump(getCurrentRecipe().outputItem.item);
            }
        }


        public float warmupTarget(){
            return 1f;
        }

        @Override
        public float warmup(){
            return warmup;
        }

        @Override
        public float totalProgress(){
            return totalProgress;
        }

        private void craft(){
            consume();
            if(getCurrentRecipe().outputItem != null){
                for(int i = 0; i < getCurrentRecipe().outputItem.amount; ++i){
                    offload(getCurrentRecipe().outputItem.item);
                }
            }

            craftEffect.at(x, y);
            progress %= 1f;
        }

        public void dumpOutputs(){
            if(currentRecipe == -1) return;
            Recipe recipe = getCurrentRecipe();
            if(recipe.outputItem != null && timer(timerDump, dumpTime / timeScale)){
                var output = recipe.outputItem;
                dump(output.item);
            }

            if(recipe.outputLiquid != null){
                var output = recipe.outputLiquid;
                dumpLiquid(output.liquid, 2f, recipe.outputLiquidDirection);
            }
        }

        @Override
        public double sense(LAccess sensor){
            if(sensor == LAccess.progress) return progress();
            //attempt to prevent wild total liquid fluctuation, at least for crafters
            Recipe recipe = getCurrentRecipe();
            if(sensor == LAccess.totalLiquids && recipe != null && recipe.outputLiquid != null) return liquids.get(recipe.outputLiquid.liquid);
            return super.sense(sensor);
        }

        @Override
        public float progress(){
            return Mathf.clamp(progress);
        }

        @Override
        public int getMaximumAccepted(Item item){
            return itemsCapacities[item.id];
        }

        @Override
        public boolean shouldAmbientSound(){
            return efficiency > 0;
        }

        //custom part

        @Override
        public void drawSelect(){
            super.drawSelect();
            Recipe recipe = getCurrentRecipe();
            if(recipe != null && recipe.outputLiquid != null){
                int dir = recipe.outputLiquidDirection;

                if(dir != -1){
                    Draw.rect(
                    recipe.outputLiquid.liquid.fullIcon,
                    x + Geometry.d4x(dir + rotation) * (size * tilesize / 2f + 4),
                    y + Geometry.d4y(dir + rotation) * (size * tilesize / 2f + 4),
                    8f, 8f
                    );
                }
            }
        }

        @Override
        public void displayConsumption(Table table){
            rebuildCons = () -> {
                table.clearChildren();
                table.clear();

                table.left();
                for(Consume cons : block.consumers){
                    if(cons.optional && cons.booster) continue;
                    cons.build(self(), table);
                }
            };
            rebuildCons.run();

        }

        @Override
        public void displayBars(Table table){

            rebuildBars = () -> {
                table.clearChildren();
                for(Func<Building, Bar> bar : block.listBars()){
                    var result = bar.get(self());
                    if(result == null) continue;
                    table.add(result).growX();
                    table.row();
                }
            };
            rebuildBars.run();
        }

        @Override
        public void drawStatus(){

            if(this.currentRecipe == -1) return;
            if(!MultiCrafter.this.changeTexture || (MultiCrafter.this.size >= 2)){
                super.drawStatus();
            }else{
                float brcx = tile.drawx() + (float)(block.size * 8) / 2.0F - 2.0F;
                float brcy = tile.drawy() - (float)(block.size * 8) / 2.0F + 2.0F;
                Draw.z(71.0F);
                Draw.color(Pal.gray);
                Fill.square(brcx, brcy, 1.25f, 45.0F);
                Draw.color(status().color);
                Fill.square(brcx, brcy, 0.75f, 45.0F);
                Draw.color();
            }

        }

        public <T> void buildTable(Table table, Seq<T> items, Func<T, UnlockableContent> itemToContent, Prov<T> holder, Cons<T> consumer){

            ButtonGroup<ImageButton> group = new ButtonGroup<>();
            group.setMinCheckCount(0);
            Table cont = new Table();
            cont.defaults().size(40);

            int i = 0;

            for(T item : items){
//                if(!content.unlockedNow() || () || content.isHidden()) continue;
                UnlockableContent content = itemToContent.get(item);
                ImageButton button = cont.button(Tex.whiteui, Styles.clearTogglei, 24, () -> control.input.config.hideConfig()).group(group).tooltip(content.localizedName).get();
                button.changed(() -> consumer.get(button.isChecked() ? item : null));
                button.getStyle().imageUp = new TextureRegionDrawable(content.uiIcon);
                button.update(() -> button.setChecked(holder.get() == item));

                if(i++ % 4 == 3){
                    cont.row();
                }
            }

            //add extra blank spaces so it looks nice
            if(i % 4 != 0){
                int remaining = 4 - (i % 4);
                for(int j = 0; j < remaining; j++){
                    cont.image(Styles.black6);
                }
            }

            ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
            pane.setScrollingDisabled(true, false);

            pane.setScrollYForce(block.selectScroll);
            pane.update(() -> {
                block.selectScroll = pane.getScrollY();
            });

            pane.setOverscroll(false, false);
            table.add(pane).maxHeight(Scl.scl(40 * 5));
        }

        @Override
        public void buildConfiguration(Table table){
            Seq<Recipe> recipes = Seq.with(MultiCrafter.this.recipes).retainAll(Recipe::unlockedNow);
            if(recipes.any()){
                this.buildTable(table, recipes, (Recipe it) -> it.mainContent(), () -> {
                    return currentRecipe == -1 ? null : MultiCrafter.this.recipes[currentRecipe];
                }, (item) -> {
                    this.configure(Structs.indexOf(MultiCrafter.this.recipes, item));
                });
            }else{
                table.table(Styles.black3, (t) -> {
                    t.add("@none").color(Color.lightGray);
                });
            }
        }

        public float countNowLiquid(){
            float amounts = 0;
            for(LiquidStack stack : getNeedLiquids()){
                amounts += Math.min(this.liquids.get(stack.liquid), stack.amount);
            }
            return amounts;
        }

        public float countRequiredLiquid(){
            float need = 0;
            for(LiquidStack stack : getNeedLiquids()){
                need += stack.amount;
            }
            return need;
        }

        public LiquidStack[] getNeedLiquids(){
            return getCurrentRecipe().consumeLiquids;
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount){
            Recipe recipe = getCurrentRecipe();
            if (recipe==null)return;
            if(recipe.outputLiquid != null && liquid == recipe.outputLiquid.liquid){
                float need = Math.max(0, liquidsCapacities[liquid.id] - liquids.get(liquid));
                this.liquids.add(liquid, Math.min(amount, need));
                return;
            }
            LiquidStack[] needLiquids = getNeedLiquids();
            LiquidStack found = Structs.find(needLiquids, (l) -> l.liquid == liquid);
            if(found == null){
                return;
            }
            float need = Math.max(0, found.amount - liquids.get(liquid));
            this.liquids.add(liquid, Math.min(amount, need));
        }

        @Override
        public String toString(){
            return "MultiCrafterBuild#" + id;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            LiquidStack found = Structs.find(getNeedLiquids(), (l) -> l.liquid.name.equals(liquid.name));
            return found != null && this.liquids.get(liquid) <= liquidsCapacities[liquid.id];
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            /*int[] count = {-1};
            for(ItemStack stack : getCurrentRecipe().consumeItems){
                if(stack.item == item) count[0] += stack.amount;
            }
            return count[0] > 0 && this.items.get(item) < count[0];*/
            int maximumAccepted = getMaximumAccepted(item);
            return maximumAccepted > 0 && items.get(item) < maximumAccepted;
        }

        public Recipe getCurrentRecipe(){
            if(currentRecipe == -1) return Recipe.empty;
            return recipes[currentRecipe];
        }

        public boolean canCraft(){
            ItemStack[] requirements = getCurrentRecipe().consumeItems;
            int req = 0;
            for(ItemStack i : requirements){
                req += (i.amount + i.item.id);
            }
            int[] counter = {0};
            items.each((item, c) -> {
                counter[0] += (item.id + c);
            });
            int now = counter[0];

            return this.consumeTriggerValid() && req <= now;
        }

        public Object config(){
            return currentRecipe;
        }

        public void write(Writes write){
            super.write(write);
            write.i(currentRecipe);
        }

        public void playerPlaced(Object config){
            if(lastConfig == null) lastConfig = -1;
            if(config == null){
                if(!lastConfig.equals(-1)) configure(lastConfig);
            }else{
                configure(config);
            }
        }

        public void read(Reads read, byte revision){
            super.read(read, revision);
            currentRecipe = read.i();
            if(currentRecipe < 0 || currentRecipe >= recipes.length){
                currentRecipe = -1;
                progress = 0;
            }
        }

        public void resetProgress(){
            progress = 0f;
            totalProgress = 0f;
        }

        public void rebuildInfo(){
            rebuildBars.run();
            rebuildCons.run();
        }
    }
}