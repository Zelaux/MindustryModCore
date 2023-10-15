package mmc.world.consumers;

import arc.func.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

@SuppressWarnings("unchecked")
public class ConsumeLiquidDynamic<T extends Building> extends Consume{
    public final Func<T, LiquidStack[]> liquids;

    public ConsumeLiquidDynamic(Func<T, LiquidStack[]> liquids){
        this.liquids = liquids;
    }

    @Override
    public void apply(Block block){
        block.hasLiquids = true;
    }

    @Override
    public void build(Building tile, Table table){
        LiquidStack[][] current = new LiquidStack[][]{this.liquids.get((T)tile)};
        table.table((cont) -> {
            table.update(() -> {
                if(current[0] != this.liquids.get((T)tile)){
                    this.rebuild(tile, cont);
                    current[0] = this.liquids.get((T)tile);
                }

            });
            this.rebuild(tile, cont);
        });
    }

    private boolean hasLiquid(Building tile, Liquid liquid, float amount){
        return tile.liquids.get(liquid) >= amount;
    }

    private void rebuild(Building tile, Table table){
        table.clear();
        for(LiquidStack stack : liquids.get((T)tile)){
            table.add(new ReqImage(stack.liquid.uiIcon, () -> {
                return tile.items != null && hasLiquid(tile, stack.liquid, stack.amount);
            })).padRight(8.0f);
        }

    }
    @Override
    public void update(Building entity){
    }
    @Override
    public void trigger(Building entity){
        for(LiquidStack stack : this.liquids.get((T)entity)){
            entity.liquids.remove(stack.liquid, stack.amount);
        }

    }

    @Override
    public float efficiency(Building build){
        float min = 1f, delta = build.edelta();
        for(var stack : liquids.get(build.as())){
            min = Math.min(build.liquids.get(stack.liquid) / (stack.amount * delta), min);
        }
        return min;
    }
/*

    @Override
    public boolean valid(Building entity){
        return entity.liquids != null && hasLiquidStacks(entity, this.liquids.get((T)entity));
    }
*/

    @Override
    public void display(Stats stats){
    }

}
