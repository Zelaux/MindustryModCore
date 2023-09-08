package mma.type;

import arc.util.*;
import mindustry.ctype.*;
import mindustry.type.*;

import static mindustry.Vars.state;

public class Recipe{
    public static Recipe empty = with((ItemStack) null, -1);
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

    /** Empty */
    public static Recipe with(){
        return new Recipe();
    }
    /** Item + Liquid -> Item */
    public static Recipe with(ItemStack outputItem, ItemStack[] consumeItems, LiquidStack[] consumeLiquids, float produceTime) {
        Recipe recipe = new Recipe();
        recipe.output(outputItem, null);
        recipe.consume(consumeItems, consumeLiquids);
        recipe.produceTime = produceTime;
        recipe.check();
        return recipe;
    }
    /** Liquid -> Item */
    public static Recipe with(ItemStack outputItem, LiquidStack[] consumeLiquids, float produceTime) {
        return with(outputItem, ItemStack.empty, consumeLiquids, produceTime);
    }
    /** Item -> Item */
    public static Recipe with(ItemStack outputItem, ItemStack[] consumeItems, float produceTime) {
        return with(outputItem, consumeItems, LiquidStack.empty, produceTime);
    }
    /** Nothing -> Item */
    public static Recipe with(ItemStack outputItem, float produceTime){
        return with(outputItem, LiquidStack.empty, produceTime);
    }
    /** Item + Liquid -> Liquid */
    public static Recipe with(LiquidStack outputLiquid, ItemStack[] consumeItems, LiquidStack[] consumeLiquids, float produceTime) {
        Recipe recipe = new Recipe();
        recipe.output(null, outputLiquid);
        recipe.consume(consumeItems, consumeLiquids);
        recipe.produceTime = produceTime;
        //recipe.check();
        return recipe;
    }
    /** Liquid -> Liquid */
    public static Recipe with(LiquidStack outputLiquid, LiquidStack[] consumeLiquids, float produceTime) {
        return with(outputLiquid, ItemStack.empty, consumeLiquids, produceTime);
    }
    /** Item -> Liquid */
    public static Recipe with(LiquidStack outputLiquid, ItemStack[] consumeItems, float produceTime) {
        return with(outputLiquid, consumeItems, LiquidStack.empty, produceTime);
    }
    /** Nothing -> Liquid */
    public static Recipe with(LiquidStack outputLiquid, float produceTime) {
        return with(outputLiquid, LiquidStack.empty, produceTime);
    }
    /** Item + Liquid -> Item + Liquid */
    public static Recipe with(ItemStack outputItem, LiquidStack outputLiquid, ItemStack[] consumeItems, LiquidStack[] consumeLiquids, float produceTime) {
        Recipe recipe = new Recipe();
        recipe.output(outputItem, outputLiquid);
        recipe.consume(consumeItems, consumeLiquids);
        recipe.produceTime = produceTime;
        return recipe;
    }
    /** Liquid -> Item + Liquid */
    public static Recipe with(ItemStack outputItem, LiquidStack outputLiquid, LiquidStack[] consumeLiquids, float produceTime) {
        return with(outputItem, outputLiquid, ItemStack.empty, consumeLiquids, produceTime);
    }
    /** Item -> Item + Liquid */
    public static Recipe with(ItemStack outputItem, LiquidStack outputLiquid, ItemStack[] consumeItems, float produceTime) {
        return with(outputItem, outputLiquid, consumeItems, LiquidStack.empty, produceTime);
    }
    /** Nothing -> Item + Liquid */
    public static Recipe with(ItemStack outputItem, LiquidStack outputLiquid, float produceTime) {
        return with(outputItem, outputLiquid, ItemStack.empty, LiquidStack.empty, produceTime);
    }


    public UnlockableContent mainContent(){
        return outputItem == null ? outputLiquid == null ? null : outputLiquid.liquid : outputItem.item;
    }

    public Recipe produceTime(float produceTime){
        this.produceTime = produceTime;
        return this;
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

    public boolean unlockedNow(){
        for(ItemStack stack : consumeItems){
            Item item = stack.item;
            if(state.rules.hiddenBuildItems.contains(item) || item.isHidden() || !item.unlockedNow()){
                return false;
            }
        }
        for(LiquidStack stack : consumeLiquids){
            Liquid liquid = stack.liquid;
            if(liquid.isHidden() || !liquid.unlockedNow()){
                return false;
            }
        }
        return true;
    }
}