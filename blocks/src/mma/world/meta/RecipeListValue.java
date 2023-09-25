package mma.world.meta;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.Button.ButtonStyle;
import arc.scene.ui.layout.Table;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.ui.ItemDisplay;
import mindustry.ui.LiquidDisplay;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValue;
import mindustry.world.meta.StatValues;
import mma.type.Recipe;

public class RecipeListValue implements StatValue {
    private final Recipe[] recipes;
    private final ButtonStyle infoStyle = Core.scene.getStyle(ButtonStyle.class);
    public RecipeListValue(Recipe... recipes) {
        this.recipes = recipes;
    }

    @Override
    public void display(Table table) {
        table.row();
        for (Recipe recipe : recipes) {
            table.table(infoStyle.up, tab -> {
                if (recipe.consumeItems.length > 0 || recipe.consumeLiquids.length > 0) tab.add("[accent]" + Stat.input.localized()).expandX().left().row();
                if (recipe.consumeItems.length > 0) {
                    tab.table(row -> {
                        for (ItemStack itemIn : recipe.consumeItems) {
                            row.add(new ItemDisplay(itemIn.item, itemIn.amount, true)).left();
                            if (recipe.consumeItems.length > 1) row.row();
                        }
                    }).left().row();
                }
                if (recipe.consumeLiquids.length > 0) {
                    tab.table(row -> {
                        for (LiquidStack liquidIn : recipe.consumeLiquids) {
                            row.add(new LiquidDisplay(liquidIn.liquid, liquidIn.amount, false)).left();
                            if (recipe.consumeLiquids.length > 1) row.row();
                        }
                    }).left().row();
                }
                if (recipe.outputItem != null || recipe.outputLiquid != null) tab.add("[accent]" + Stat.output.localized()).expandX().left().row();
                if (recipe.outputItem != null) tab.table(row -> row.add(new ItemDisplay(recipe.outputItem.item, recipe.outputItem.amount, true))).left().row();
                if (recipe.outputLiquid != null) tab.table(row -> row.add(new LiquidDisplay(recipe.outputLiquid.liquid, recipe.outputLiquid.amount, false))).left().row();
                tab.table(row -> {
                    row.add("[lightgray]" + Stat.productionTime.localized() + ":[]").padRight(4f);
                    StatValues.number(recipe.produceTime / 60f, StatUnit.seconds).display(row);
                }).left().row();
            }).color(Color.lightGray).left().growX();
            table.add().size(18f).row();
        }
    }
}
