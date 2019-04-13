package funwayguy.bdsandm.inventory.capability;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProviderBarrel implements ICapabilityProvider, ICapabilitySerializable<NBTTagCompound>
{
    private final CapabilityBarrel barrel;
    private ItemStack stack;
    
    public CapabilityProviderBarrel(int initCap, int maxCap)
    {
        barrel = new CapabilityBarrel(initCap, maxCap);
    }
    
    public CapabilityProviderBarrel setParentStack(ItemStack stack)
    {
        this.stack = stack;
        barrel.setFluidContainer(stack);
        
        final ItemStack finStack = stack;
        barrel.setCallback(() -> {
            NBTTagCompound sTag = stack.getTagCompound();
            if(sTag == null)
            {
                sTag = new NBTTagCompound();
                finStack.setTagCompound(sTag);
            }
            
            sTag.setTag("barrelCap", barrel.serializeNBT());
        });
        
        return this;
    }
    
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return BdsmCapabilies.CRATE_CAP == capability || capability == BdsmCapabilies.BARREL_CAP || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;
    }
    
    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        if(capability == BdsmCapabilies.CRATE_CAP)
        {
            return BdsmCapabilies.CRATE_CAP.cast(barrel);
        } else if(capability == BdsmCapabilies.BARREL_CAP)
        {
            return BdsmCapabilies.BARREL_CAP.cast(barrel);
        } else if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(barrel);
        } else if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(barrel);
        } else if(capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
        {
            return CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.cast(barrel);
        }
        
        return null;
    }
    
    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = barrel.serializeNBT();
        if(stack != null) stack.setTagInfo("barrelCap", tag.copy()); // Purely for display purposes client side. Not to be trusted as accurate
        return tag;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        barrel.deserializeNBT(nbt);
        if(stack != null) stack.setTagInfo("barrelCap", nbt.copy()); // Purely for display purposes client side. Not to be trusted as accurate
    }
}
