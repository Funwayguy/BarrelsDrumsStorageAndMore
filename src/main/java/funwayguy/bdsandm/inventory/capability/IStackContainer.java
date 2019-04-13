package funwayguy.bdsandm.inventory.capability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

public interface IStackContainer extends INBTSerializable<NBTTagCompound>
{
    IStackContainer setCallback(ICrateCallback callback);
    void syncContainer();
    
    int getStackCap();
    int getUpgradeCap();
    void setStackCap(int value);
    
    int getCount();
    
    boolean isLocked();
    void setLocked(boolean state);
    
    boolean voidOverflow();
    void setVoidOverflow(boolean state);
    
    //boolean oreDict();
    //void setOreDict(boolean state);
    
    boolean installUpgrade(@Nonnull EntityPlayer player, @Nonnull ItemStack stack);
    
    void copyContainer(IStackContainer container);
    
    int getColorCount();
    int[] getColors();
    void setColors(int[] colors);
}
