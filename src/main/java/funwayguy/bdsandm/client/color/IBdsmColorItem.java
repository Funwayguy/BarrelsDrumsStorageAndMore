package funwayguy.bdsandm.client.color;

import net.minecraft.item.ItemStack;

public interface IBdsmColorItem {
    int getColorCount(ItemStack stack);
    int[] getColors(ItemStack stack);
    void setColors(ItemStack stack, int[] colors);
}