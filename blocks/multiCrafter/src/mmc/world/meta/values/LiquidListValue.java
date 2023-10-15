package mmc.world.meta.values;

import arc.scene.ui.layout.Table;
import mindustry.type.LiquidStack;
import mindustry.ui.*;
import mindustry.world.meta.*;

public class LiquidListValue implements StatValue {
    private final LiquidStack[] stacks;

    public LiquidListValue(LiquidStack... stacks) {
        this.stacks=stacks;
    }

    public void display(Table table) {
        for (LiquidStack stack : stacks) {
            table.add(new LiquidDisplay(stack.liquid, stack.amount, false)).padRight(5.0F);
        }

    }
}
