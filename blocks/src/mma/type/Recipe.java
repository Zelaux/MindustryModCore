package mma.type;

import arc.util.*;
import mindustry.type.*;

public class Recipe{
    public static Recipe empty = with(null, -1);
    public ItemStack outputItem;
    public LiquidStack outputLiquid;
    public int outputLiquidDirection = -1;
    public ItemStack[] consumeItems;
    public LiquidStack[] consumeLiquids;

    public float produceTime;

    public Recipe(){
        consumeItems = ItemStack.empty;
        consumeLiquids = LiquidStack.empty;

    }

    public static Recipe with(ItemStack outputItem, ItemStack[] consumeItems, LiquidStack[] consumeLiquids, float produceTime){
        Recipe recipe = new Recipe();
        recipe.output(outputItem, null);
        recipe.consume(consumeItems, consumeLiquids);
        recipe.produceTime = produceTime;
        recipe.check();
        return recipe;
    }

    public static Recipe with(ItemStack outputItem, LiquidStack[] consumeLiquids, float produceTime){
        return with(outputItem, ItemStack.empty, consumeLiquids, produceTime);
    }

    public static Recipe with(ItemStack outputItem, float produceTime){
        return with(outputItem, LiquidStack.empty, produceTime);
    }

    public static Recipe with(ItemStack outputItem, ItemStack[] consumeItems, float produceTime){
        return with(outputItem, consumeItems, LiquidStack.empty, produceTime);
    }

    public Recipe produceTime(float produceTime){
        this.produceTime = produceTime;
        return this;
    }

    public static Recipe with(){
        return new Recipe();
    }

    @SuppressWarnings("UnusedReturnValue")
    public Recipe consume(@Nullable ItemStack[] items, @Nullable LiquidStack[] liquids){
        if(items != null) consumeItems = items;
        if(liquids != null) consumeLiquids = liquids;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Recipe output(@Nullable ItemStack item, @Nullable LiquidStack liquid){
        if(item != null) outputItem = item;
        if(liquid != null) outputLiquid = liquid;
        return this;
    }

    public Recipe outputLiquidDirection(int outputLiquidDirection){
        this.outputLiquidDirection = outputLiquidDirection;
        return this;
    }

    private void check(){
//            checkItems();
//            checkLiquids();
    }
}