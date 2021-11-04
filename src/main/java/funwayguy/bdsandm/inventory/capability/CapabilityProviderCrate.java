package funwayguy.bdsandm.inventory.capability;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProviderCrate implements ICapabilityProvider, ICapabilitySerializable<CompoundNBT>
{
    private LazyOptional<ICrate> instance;
    private final ICrate crate;
    private ItemStack stack;
    
    public CapabilityProviderCrate(int initCap, int maxCap)
    {
        crate = new CapabilityCrate(initCap, maxCap);
        instance = LazyOptional.of(() -> crate);
    }
    
    public CapabilityProviderCrate setParentStack(ItemStack stack)
    {
        this.stack = stack;
        
        final ItemStack finStack = stack;
        crate.setCallback(() -> {
            CompoundNBT sTag = stack.getTag();
            if(sTag == null)
            {
                sTag = new CompoundNBT();
                finStack.setTag(sTag);
            }
            
            sTag.put("crateCap", crate.serializeNBT()); // Purely for display purposes client side. Not to be trusted as accurate
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
        } else if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return instance.cast();
        }
        
        return LazyOptional.empty();
    }
    
    @Override
    public CompoundNBT serializeNBT()
    {
        CompoundNBT tag = crate.serializeNBT();
        if(stack != null) stack.setTagInfo("crateCap", tag.copy()); // Purely for display purposes client side. Not to be trusted as accurate
        return tag;
    }
    
    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {
        crate.deserializeNBT(nbt);
        if(stack != null) stack.setTagInfo("crateCap", nbt.copy()); // Purely for display purposes client side. Not to be trusted as accurate
    }
}
