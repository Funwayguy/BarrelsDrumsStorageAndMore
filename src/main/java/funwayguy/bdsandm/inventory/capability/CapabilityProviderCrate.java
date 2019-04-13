package funwayguy.bdsandm.inventory.capability;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProviderCrate implements ICapabilityProvider, ICapabilitySerializable<NBTTagCompound>
{
    private final ICrate crate;
    private ItemStack stack;
    
    public CapabilityProviderCrate(int initCap, int maxCap)
    {
        crate = new CapabilityCrate(initCap, maxCap);
    }
    
    public CapabilityProviderCrate setParentStack(ItemStack stack)
    {
        this.stack = stack;
        
        final ItemStack finStack = stack;
        crate.setCallback(() -> {
            NBTTagCompound sTag = stack.getTagCompound();
            if(sTag == null)
            {
                sTag = new NBTTagCompound();
                finStack.setTagCompound(sTag);
            }
            
            sTag.setTag("crateCap", crate.serializeNBT()); // Purely for display purposes client side. Not to be trusted as accurate
        });
        
        return this;
    }
    
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == BdsmCapabilies.CRATE_CAP || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }
    
    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        if(capability == BdsmCapabilies.CRATE_CAP)
        {
            return BdsmCapabilies.CRATE_CAP.cast(crate);
        } else if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(crate);
        }
        
        return null;
    }
    
    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = crate.serializeNBT();
        if(stack != null) stack.setTagInfo("crateCap", tag.copy()); // Purely for display purposes client side. Not to be trusted as accurate
        return tag;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        crate.deserializeNBT(nbt);
        if(stack != null) stack.setTagInfo("crateCap", nbt.copy()); // Purely for display purposes client side. Not to be trusted as accurate
    }
}
