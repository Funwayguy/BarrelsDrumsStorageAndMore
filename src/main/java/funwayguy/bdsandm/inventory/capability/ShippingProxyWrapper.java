package funwayguy.bdsandm.inventory.capability;

import funwayguy.bdsandm.blocks.tiles.TileEntityShipping;
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
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ShippingProxyWrapper implements IItemHandler, IFluidHandler, IEnergyStorage, IInventoryChangedListener
{
    private final InventoryShipping shipInvo;
    
    private final TreeSet<ProxyEntry<IItemHandler>> invoItems = new TreeSet<>();
    
    private final TreeSet<ProxyEntry<IFluidHandlerItem>> fluidItems = new TreeSet<>();
    
    private final TreeSet<ProxyEntry<IEnergyStorage>> energyItems = new TreeSet<>();
    
    public ShippingProxyWrapper(TileEntityShipping tileShip, InventoryShipping invo)
    {
        this.shipInvo = invo;
        this.shipInvo.addInventoryChangeListener(this);
        this.shipInvo.addInventoryChangeListener(tileShip);
    }
    
    private void refreshProxyEntries()
    {
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
            
            if(stack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
            {
                IItemHandler itemHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                assert itemHandler != null;
                invoItems.add(new ProxyEntry<>(i, itemHandler));
            }
            
            if(stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
            {
                IFluidHandlerItem fluidHandlerItem = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                assert fluidHandlerItem != null;
                fluidItems.add(new ProxyEntry<>(i, fluidHandlerItem));
            }
            
            if(stack.hasCapability(CapabilityEnergy.ENERGY, null))
            {
                IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
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
    public IFluidTankProperties[] getTankProperties()
    {
        List<IFluidTankProperties> tanks = new ArrayList<>();
        
        for(ProxyEntry<IFluidHandlerItem> eStore : fluidItems)
        {
            Collections.addAll(tanks, eStore.handler.getTankProperties());
        }
        
        return tanks.toArray(new IFluidTankProperties[0]);
    }
    
    @Override
    public int fill(FluidStack resource, boolean doFill)
    {
        FluidStack remaining = resource.copy();
        
        Iterator<ProxyEntry<IFluidHandlerItem>> eIter = fluidItems.iterator();
        
        while(eIter.hasNext() && remaining.amount > 0)
        {
            ProxyEntry<IFluidHandlerItem> eStore = eIter.next();
            
            int tmp = eStore.handler.fill(remaining, doFill);
            remaining.amount -= tmp;
            
            if(doFill && tmp > 0)
            {
                this.shipInvo.setSlotWithoutNotice(eStore.slot, eStore.handler.getContainer());
            }
        }
        
        shipInvo.markDirty();
        
        return resource.amount - remaining.amount;
    }
    
    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain)
    {
        FluidStack requesting = resource.copy();
        FluidStack pulled = null;
        
        Iterator<ProxyEntry<IFluidHandlerItem>> eIter = fluidItems.iterator();
        
        while(eIter.hasNext() && requesting.amount > 0)
        {
            ProxyEntry<IFluidHandlerItem> eStore = eIter.next();
            
            FluidStack tmp = eStore.handler.drain(requesting, false);
            
            if(tmp != null && tmp.isFluidEqual(requesting))
            {
                eStore.handler.drain(requesting, doDrain);
                
                if(pulled == null)
                {
                    pulled = tmp;
                } else
                {
                    pulled.amount += tmp.amount;
                }
                
                requesting.amount -= tmp.amount;
                
                if(doDrain)
                {
                    this.shipInvo.setSlotWithoutNotice(eStore.slot, eStore.handler.getContainer());
                }
            }
        }
        
        shipInvo.markDirty();
        
        return pulled;
    }
    
    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain)
    {
        int requesting = maxDrain;
        FluidStack pulled = null;
        
        Iterator<ProxyEntry<IFluidHandlerItem>> eIter = fluidItems.iterator();
        
        while(eIter.hasNext() && requesting > 0)
        {
            ProxyEntry<IFluidHandlerItem> eStore = eIter.next();
            
            FluidStack tmp = eStore.handler.drain(requesting, false);
            
            if(tmp != null && (pulled == null || tmp.isFluidEqual(pulled)))
            {
                eStore.handler.drain(requesting, doDrain);
                
                if(pulled == null)
                {
                    pulled = tmp;
                } else
                {
                    pulled.amount += tmp.amount;
                }
                
                requesting -= tmp.amount;
                
                if(doDrain)
                {
                    this.shipInvo.setSlotWithoutNotice(eStore.slot, eStore.handler.getContainer());
                }
            }
        }
        
        shipInvo.markDirty();
        
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
                if(!simulate) this.shipInvo.markDirty();
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
                if(!simulate) this.shipInvo.markDirty();
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
