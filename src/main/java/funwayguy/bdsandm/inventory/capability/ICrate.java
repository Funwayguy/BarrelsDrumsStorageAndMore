package funwayguy.bdsandm.inventory.capability;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public interface ICrate extends IStackContainer, IItemHandler
{
    @Nonnull
    ItemStack getRefItem();
    boolean canMergeWith(ItemStack stack); // Because for some reason ItemStack doesn't have suitable comparisons
    
    boolean isOreDict();
    void enableOreDict(boolean state);
}
