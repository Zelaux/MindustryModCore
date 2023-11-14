package mmc.world.meta;

import arc.*;
import arc.graphics.*;
import arc.scene.style.*;
import arc.scene.ui.Button.*;
import arc.scene.ui.layout.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.meta.*;
import mmc.type.*;

public class RecipeListValue implements StatValue{
    private final Recipe[] recipes;
    private final Drawable style = Core.scene.getStyle(ButtonStyle.class).up;
    public RecipeListValue(Recipe... recipes){
        this.recipes = recipes;
    }

    @Override
    public void display(Table table){
        table.row();
        for(Recipe recipe : recipes){
            table.table(style, tab -> {
                if(recipe.consumeItems.length > 0 || recipe.consumeLiquids.length > 0) tab.add(Stat.input.localized()).color(Pal.accent).expandX().left().row();
                if(recipe.consumeItems.length > 0){
                    tab.table(row -> {
                        for(ItemStack itemIn : recipe.consumeItems){
                            row.add(new ItemDisplay(itemIn.item, itemIn.amount, true)).left();
                            if(recipe.consumeItems.length > 1) row.row();
                        }
                    }).left().row();
                }
                if(recipe.consumeLiquids.length > 0){
                    tab.table(row -> {
                        for(LiquidStack liquidIn : recipe.consumeLiquids){
                            row.add(new LiquidDisplay(liquidIn.liquid, liquidIn.amount, false)).left();
                            if(recipe.consumeLiquids.length > 1) row.row();
                        }
                    }).left().row();
                }
                if(recipe.outputItem != null || recipe.outputLiquid != null) tab.add(Stat.output.localized()).color(Pal.accent).expandX().left().row();
                if(recipe.outputItem != null) tab.table(row -> row.add(new ItemDisplay(recipe.outputItem.item, recipe.outputItem.amount, true))).left().row();
                if(recipe.outputLiquid != null) tab.table(row -> row.add(new LiquidDisplay(recipe.outputLiquid.liquid, recipe.outputLiquid.amount, false))).left().row();
                tab.table(row -> {
                    row.add(Stat.productionTime.localized()).padRight(4f).color(Color.lightGray);
                    StatValues.number(recipe.produceTime / 60f, StatUnit.seconds).display(row);
                }).left().row();
            }).color(Color.lightGray).left().growX();
            table.add().size(18f).row();
        }
    }
}
