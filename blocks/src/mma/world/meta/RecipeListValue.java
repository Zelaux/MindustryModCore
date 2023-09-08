package mma.world.meta;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Tex;
import mindustry.ui.ItemDisplay;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValue;
import mindustry.world.meta.StatValues;
import mma.type.Recipe;
import mma.world.meta.values.LiquidListValue;

public class RecipeListValue implements StatValue {
    private final Recipe[] recipes;

    public RecipeListValue(Recipe... recipes) {
        this.recipes = recipes;
    }

    @Override
    public void display(Table table) {
        table.row();
        for (Recipe recipe : recipes) {
            if (recipe.outputItem != null) {
                table.add(new ItemDisplay(recipe.outputItem.item, recipe.outputItem.amount)).padRight(5).left();
            }
            if (recipe.outputLiquid != null) {
                table.row();
                StatValues.liquid(recipe.outputLiquid.liquid,recipe.outputLiquid.amount,true).display(table);
            }

            (table.table((bt) -> {
                bt.left().defaults().padRight(3.0F).left();
                if (recipe.consumeItems.length > 0) {
                    StatValues.items(true, recipe.consumeItems).display(bt.table().get());
                    if (recipe.consumeLiquids.length > 0) bt.row();
                }

                if (recipe.consumeLiquids.length > 0) {
                    new LiquidListValue(true, recipe.consumeLiquids).display(bt.table().get());
                    bt.row();
                }
                bt.add(Strings.format("[lightgray]@: [white]", Core.bundle.get("stat.productiontime"), recipe.produceTime));
                StatValues.number(recipe.produceTime/ 60.0F, StatUnit.seconds).display(bt);
            }).padTop(-9.0F).left().get()).background(Tex.underline);
            table.row();
        }

    }

    void sep(Table table, String text) {
        table.row();
        table.add(text);
    }

    <T extends UnlockableContent> TextureRegion icon(T t) {
        return t.uiIcon;
    }
}
