package funwayguy.bdsandm.inventory.capability;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProviderBarrel implements ICapabilityProvider, ICapabilitySerializable<CompoundNBT>
{
    private LazyOptional<CapabilityBarrel> instance;
    private CapabilityBarrel barrel;
    private ItemStack stack;
    
    public CapabilityProviderBarrel(int initCap, int maxCap)
    {
        barrel = new CapabilityBarrel(initCap, maxCap);
        instance = LazyOptional.of(() -> barrel);
    }
    
    public CapabilityProviderBarrel setParentStack(ItemStack stack)
    {
        this.stack = stack;
        barrel.setFluidContainer(stack);
        
        final ItemStack finStack = stack;
        barrel.setCallback(() -> {
            CompoundNBT sTag = stack.getTag();
            if(sTag == null)
            {
                sTag = new CompoundNBT();
                finStack.setTag(sTag);
            }
            
            sTag.put("barrelCap", barrel.serializeNBT());
        });
        
        return this;
    }
    
    @Nullable
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
        if(capability == BdsmCapabilies.CRATE_CAP)
        {
            return instance.cast();
        } else if(capability == BdsmCapabilies.BARREL_CAP)
        {
            return instance.cast();
        } else if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return instance.cast();
        } else if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            return instance.cast();
        } else if(capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
        {
            return instance.cast();
        }
        
        return LazyOptional.empty();
    }
    
    @Override
    public CompoundNBT serializeNBT()
    {
        CompoundNBT tag = barrel.serializeNBT();
        if(stack != null) stack.setTagInfo("barrelCap", tag.copy()); // Purely for display purposes client side. Not to be trusted as accurate
        return tag;
    }
    
    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {
        barrel.deserializeNBT(nbt);
        if(stack != null) stack.setTagInfo("barrelCap", nbt.copy()); // Purely for display purposes client side. Not to be trusted as accurate
    }
}
