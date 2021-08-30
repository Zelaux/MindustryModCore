package mma.type;

import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;

public class Recipe {
    public static Recipe empty = with(null, -1);
    public ItemStack outputItem;
    public LiquidStack outputLiquid;
    public ItemStack[] consumeItems;
    public LiquidStack[] consumeLiquids;

    public float produceTime;

    public Recipe() {
        consumeItems = ItemStack.empty;
        consumeLiquids = ModLiquidStack.empty;

    }

    public static Recipe with(ItemStack outputItem, ItemStack[] consumeItems, LiquidStack[] consumeLiquids, float produceTime) {
        Recipe recipe = new Recipe();
        if (outputItem != null) recipe.outputItem = outputItem;
        if (consumeItems != null) recipe.consumeItems = consumeItems;
        if (consumeLiquids != null) recipe.consumeLiquids = consumeLiquids;
        recipe.produceTime = produceTime;
        recipe.check();
        return recipe;
    }

    public static Recipe with(ItemStack outputItem, LiquidStack[] consumeLiquids, float produceTime) {
        return with(outputItem, ItemStack.empty, consumeLiquids, produceTime);
    }

    public static Recipe with(ItemStack outputItem, float produceTime) {
        return with(outputItem, ModLiquidStack.empty, produceTime);
    }

    public static Recipe with(ItemStack outputItem, ItemStack[] consumeItems, float produceTime) {
        return with(outputItem, consumeItems, ModLiquidStack.empty, produceTime);
    }

    private void check() {
//            checkItems();
//            checkLiquids();
    }
}