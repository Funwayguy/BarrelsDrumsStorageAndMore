package funwayguy.bdsandm.inventory.capability;

import funwayguy.bdsandm.blocks.tiles.ShippingTileEntity;
import funwayguy.bdsandm.inventory.InventoryShipping;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.TreeSet;

public class ShippingProxyWrapper implements IItemHandler, IFluidHandler, IEnergyStorage, IInventoryChangedListener
{
    private final InventoryShipping shipInvo;
    
    private final TreeSet<ProxyEntry<IItemHandler>> invoItems = new TreeSet<>();
    
    private final TreeSet<ProxyEntry<IFluidHandlerItem>> fluidItems = new TreeSet<>();
    
    private final TreeSet<ProxyEntry<IEnergyStorage>> energyItems = new TreeSet<>();
    
    public ShippingProxyWrapper(ShippingTileEntity tileShip, InventoryShipping invo)
    {
        this.shipInvo = invo;
        this.shipInvo.addListener(this);
        this.shipInvo.addListener(tileShip);
    }
    
    private boolean skipRefresh = false;
    
    private void markDirty()
    {
        skipRefresh = true;
        shipInvo.markDirty();
        skipRefresh = false;
    }
    
    private void refreshProxyEntries()
    {
        if(skipRefresh) return;
        
        invoItems.clear();
        fluidItems.clear();
        energyItems.clear();
        
        for(int i = 0; i < shipInvo.getSizeInventory(); i++)
        {
            ItemStack stack = shipInvo.getStackInSlot(i);
            
            if(stack.isEmpty())
            {
                continue;
            }
            
            if(stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).isPresent())
            {
                IItemHandler itemHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElse(null);
                assert itemHandler != null;
                invoItems.add(new ProxyEntry<>(i, itemHandler));
            }
            
            if(stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).isPresent())
            {
                IFluidHandlerItem fluidHandlerItem = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).orElse(null);
                assert fluidHandlerItem != null;
                fluidItems.add(new ProxyEntry<>(i, fluidHandlerItem));
            }
            
            if(stack.getCapability(CapabilityEnergy.ENERGY, null).isPresent())
            {
                IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null).orElse(null);
                assert energyStorage != null;
                energyItems.add(new ProxyEntry<>(i, energyStorage));
            }
        }
    }
    
    @Override
    public void onInventoryChanged(@Nonnull IInventory invBasic)
    {
        this.refreshProxyEntries();
    }
    
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate)
    {
        int pushed = 0;
    
        Iterator<ProxyEntry<IEnergyStorage>> eIter = energyItems.iterator();
        
        while(eIter.hasNext() && pushed < maxReceive)
        {
            ProxyEntry<IEnergyStorage> eStore = eIter.next();
            
            if(eStore.handler.canReceive())
            {
                pushed += eStore.handler.receiveEnergy(maxReceive - pushed, simulate);
            }
        }
        
        if(pushed != 0 && !simulate) this.markDirty();
        return pushed;
    }
    
    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        int pulled = 0;
    
        Iterator<ProxyEntry<IEnergyStorage>> eIter = energyItems.iterator();
        
        while(eIter.hasNext() && pulled < maxExtract)
        {
            ProxyEntry<IEnergyStorage> eStore = eIter.next();
            
            if(eStore.handler.canExtract())
            {
                pulled += eStore.handler.extractEnergy(maxExtract - pulled, simulate);
            }
        }
        
        if(pulled != 0 && !simulate) this.markDirty();
        return pulled;
    }
    
    @Override
    public int getEnergyStored()
    {
        int total = 0;
        
        for(ProxyEntry<IEnergyStorage> eStore : energyItems)
        {
            total += eStore.handler.getEnergyStored();
        }
        
        return total;
    }
    
    @Override
    public int getMaxEnergyStored()
    {
        int total = 0;
        
        for(ProxyEntry<IEnergyStorage> eStore : energyItems)
        {
            total += eStore.handler.getMaxEnergyStored();
        }
        
        return total;
    }
    
    @Override
    public boolean canExtract()
    {
        for(ProxyEntry<IEnergyStorage> eStore : energyItems)
        {
            if(eStore.handler.canExtract())
            {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean canReceive()
    {
        for(ProxyEntry<IEnergyStorage> eStore : energyItems)
        {
            if(eStore.handler.canReceive())
            {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public int getTanks() {
        return 0;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return null;
    }

    @Override
    public int getTankCapacity(int tank) {
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction doFill)
    {
        FluidStack remaining = resource.copy();
        
        Iterator<ProxyEntry<IFluidHandlerItem>> eIter = fluidItems.iterator();
        
        while(eIter.hasNext() && remaining.getAmount() > 0)
        {
            ProxyEntry<IFluidHandlerItem> eStore = eIter.next();
            
            int tmp = eStore.handler.fill(remaining, doFill);
            remaining.setAmount(remaining.getAmount() - tmp);
            
            if(doFill.execute() && tmp > 0)
            {
                this.shipInvo.setSlotWithoutNotice(eStore.slot, eStore.handler.getContainer());
            }
        }
        
        this.markDirty();
        
        return resource.getAmount() - remaining.getAmount();
    }
    
    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, FluidAction doDrain)
    {
        FluidStack requesting = resource.copy();
        FluidStack pulled = null;
        
        Iterator<ProxyEntry<IFluidHandlerItem>> eIter = fluidItems.iterator();
        
        while(eIter.hasNext() && requesting.getAmount() > 0)
        {
            ProxyEntry<IFluidHandlerItem> eStore = eIter.next();
            
            FluidStack tmp = eStore.handler.drain(requesting, FluidAction.SIMULATE);
            
            if(tmp != null && tmp.isFluidEqual(requesting))
            {
                eStore.handler.drain(requesting, doDrain);
                
                if(pulled == null)
                {
                    pulled = tmp;
                } else
                {
                    pulled.setAmount(pulled.getAmount() + tmp.getAmount());
                }

                requesting.setAmount(requesting.getAmount() - tmp.getAmount());

                if(doDrain.execute())
                {
                    this.shipInvo.setSlotWithoutNotice(eStore.slot, eStore.handler.getContainer());
                }
            }
        }
        
        this.markDirty();
        
        return pulled;
    }
    
    @Nullable
    @Override
    public FluidStack drain(int maxDrain, FluidAction doDrain)
    {
        int requesting = maxDrain;
        FluidStack pulled = null;
        
        Iterator<ProxyEntry<IFluidHandlerItem>> eIter = fluidItems.iterator();
        
        while(eIter.hasNext() && requesting > 0)
        {
            ProxyEntry<IFluidHandlerItem> eStore = eIter.next();
            
            FluidStack tmp = eStore.handler.drain(requesting, FluidAction.SIMULATE);
            
            if(tmp != null && (pulled == null || tmp.isFluidEqual(pulled)))
            {
                eStore.handler.drain(requesting, doDrain);
                
                if(pulled == null)
                {
                    pulled = tmp;
                } else
                {
                    pulled.setAmount(pulled.getAmount() + tmp.getAmount());
                }
                
                requesting -= tmp.getAmount();

                if(doDrain.execute())
                {
                    this.shipInvo.setSlotWithoutNotice(eStore.slot, eStore.handler.getContainer());
                }
            }
        }
        
        this.markDirty();
        
        return pulled;
    }
    
    @Override
    public int getSlots()
    {
        int total = 0;
        
        for(ProxyEntry<IItemHandler> eStore : invoItems)
        {
            total += eStore.handler.getSlots();
        }
        
        return total;
    }
    
    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot)
    {
        int index = 0;
        
        for(ProxyEntry<IItemHandler> eStore : invoItems)
        {
            if(slot >= index && slot < index + eStore.handler.getSlots())
            {
                return eStore.handler.getStackInSlot(slot - index);
            }
            
            index += eStore.handler.getSlots();
        }
        
        return ItemStack.EMPTY;
    }
    
    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        int index = 0;
        
        for(ProxyEntry<IItemHandler> eStore : invoItems)
        {
            if(slot >= index && slot < index + eStore.handler.getSlots())
            {
                ItemStack result = eStore.handler.insertItem(slot - index, stack, simulate);
                if(!simulate) this.markDirty();
                return result;
            }
            
            index += eStore.handler.getSlots();
        }
        
        return stack;
    }
    
    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        int index = 0;
        
        for(ProxyEntry<IItemHandler> eStore : invoItems)
        {
            if(slot >= index && slot < index + eStore.handler.getSlots())
            {
                ItemStack result = eStore.handler.extractItem(slot - index, amount, simulate);
                if(!simulate) this.markDirty();
                return result;
            }
            
            index += eStore.handler.getSlots();
        }
        
        return ItemStack.EMPTY;
    }
    
    @Override
    public int getSlotLimit(int slot)
    {
        int index = 0;
        
        for(ProxyEntry<IItemHandler> eStore : invoItems)
        {
            if(slot >= index && slot < index + eStore.handler.getSlots())
            {
                return eStore.handler.getSlotLimit(slot - index);
            }
            
            index += eStore.handler.getSlots();
        }
        
        return 0;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return false;
    }

    private class ProxyEntry<T> implements Comparable<ProxyEntry<T>>
    {
        private final int slot;
        private final T handler;
        
        private ProxyEntry(int slot, @Nonnull T handler)
        {
            this.slot = slot;
            this.handler = handler;
        }
        
        @Override
        public int compareTo(@Nonnull ProxyEntry<T> o) // Helps keeps these in slot order
        {
            return Integer.compare(this.slot, o.slot);
        }
    }
}
