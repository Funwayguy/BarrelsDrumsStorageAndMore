package funwayguy.bdsandm.inventory.capability;

import funwayguy.bdsandm.core.BDSM;
import funwayguy.bdsandm.core.BdsmConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CapabilityBarrel implements ICrate, IBarrel
{
    private FluidStack refFluid = null;
    private ItemStack refStack = ItemStack.EMPTY;
    private final List<OreIngredient> cachedOres = new ArrayList<>();
    private int maxStackCapacity;
    private boolean oreDict = false;
    private boolean lock = false;
    private boolean overflow = false;
    private int[] colors = new int[]{0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF};
    
    private ItemStack containerItem = ItemStack.EMPTY;
    
    private int stackCapacity;
    private int count = 0;
    
    private ICrateCallback callback;
    
    private final IFluidTankProperties[] tankProps;
    
    public CapabilityBarrel(int initCap, int maxStackCap)
    {
        this.stackCapacity = initCap;
        this.maxStackCapacity = maxStackCap;
        
        this.tankProps = new IFluidTankProperties[]{fluidTank};
    }
    
    @Override
    public int getColorCount()
    {
        return colors.length;
    }
    
    @Override
    public int[] getColors()
    {
        return colors;
    }
    
    @Override
    public void setColors(int[] c)
    {
        for(int i = 0; i < c.length && i < colors.length; i++)
        {
            colors[i] = c[i];
        }
    }
    
    /** READ ONLY */
    @Override
    @Nullable
    public FluidStack getRefFluid()
    {
        return refFluid;
    }
    
    @Override
    public boolean isLocked()
    {
        return this.lock;
    }
    
    @Override
    public void setLocked(boolean state)
    {
        this.lock = state;
    }
    
    @Override
    public boolean voidOverflow()
    {
        return overflow;
    }
    
    @Override
    public void setVoidOverflow(boolean state)
    {
        this.overflow = state;
    }
    
    @Override
    public boolean canMergeWith(ItemStack stack)
    {
        if(refFluid == null && ItemStack.areItemStackTagsEqual(refStack, stack))
        {
            if(ItemStack.areItemsEqual(refStack, stack))
            {
                return true;
            } else if(oreDict)
            {
                for(OreIngredient ing : cachedOres)
                {
                    if(ing.apply(stack))
                    {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /** READ ONLY */
    @Nonnull
    @Override
    public ItemStack getRefItem()
    {
        return refStack;
    }
    
    @Override
    public CapabilityBarrel setCallback(ICrateCallback callback)
    {
        this.callback = callback;
        return this;
    }
    
    @Override
    public void setFluidContainer(ItemStack stack)
    {
        this.containerItem = stack;
    }
    
    @Nonnull
    @Override
    public ItemStack getContainer()
    {
        return containerItem;
    }
    
    @Override
    public void syncContainer()
    {
        if(callback != null)
        {
            callback.onCrateChanged();
        }
        
        if(refStack.isEmpty())
        {
            slotRef = ItemStack.EMPTY;
        } else
        {
            if(!slotRef.isEmpty() && canMergeWith(slotRef))
            {
                slotRef.setCount(getCount());
            } else
            {
                slotRef = refStack.copy();
                slotRef.setCount(getCount());
            }
        }
    }
    
    @Override
    public int getStackCap()
    {
        return this.stackCapacity;
    }
    
    @Override
    public void setStackCap(int value)
    {
        this.stackCapacity = value;
    }
    
    @Override
    public int getUpgradeCap()
    {
        return this.maxStackCapacity;
    }
    
    @Override
    public boolean isOreDict()
    {
        return this.oreDict;
    }
    
    @Override
    public void enableOreDict(boolean state)
    {
        this.oreDict = state;
    }
    
    @Override
    public int getCount()
    {
        return stackCapacity < 0 ? ((1 << 15) * refStack.getMaxStackSize()) : count;
    }
    
    @Override
    public void copyContainer(IStackContainer container)
    {
        this.deserializeNBT(container.serializeNBT());
    }
    
    @Override
    public IFluidTankProperties[] getTankProperties()
    {
        return tankProps;
    }
    
    @Override
    public int fill(FluidStack resource, boolean doFill)
    {
        if(!fluidTank.canFillFluidType(resource) || (refFluid == null && lock))
        {
            return 0;
        } else if(stackCapacity < 0)
        {
            if(doFill)
            {
                if(refFluid == null)
                {
                    refFluid = resource.copy();
                    refFluid.amount = 1;
                }
    
                count = 1000;
            }
            
            return resource.amount;
        }
        
        long capacity = stackCapacity * 1000L - count;
        int fill = (int)Math.min(resource.amount, capacity);
        
        if(doFill)
        {
            if(refFluid == null)
            {
                refFluid = resource.copy();
                refFluid.amount = 1;
            }
            
            count += fill;
            
            syncContainer();
        }
        
        return overflow ? resource.amount : fill;
    }
    
    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain)
    {
        if(!fluidTank.canDrainFluidType(resource))
        {
            return null;
        } else if(stackCapacity < 0)
        {
            FluidStack out = refFluid.copy();
            out.amount = resource.amount;
            return out;
        }
        
        FluidStack out = refFluid.copy();
        out.amount = Math.min(resource.amount, count);
        
        if(doDrain)
        {
            count -= out.amount;
            
            if(count <= 0 && !lock)
            {
                refFluid = null;
            }
            
            syncContainer();
        }
        
        return out;
    }
    
    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain)
    {
        if(!fluidTank.canDrain())
        {
            return null;
        } else if(stackCapacity < 0)
        {
            FluidStack out = refFluid.copy();
            out.amount = maxDrain;
            return out;
        } else if(count <= 0)
        {
            return null;
        }
        
        FluidStack out = refFluid.copy();
        out.amount = Math.min(maxDrain, count);
        
        if(doDrain)
        {
            count -= out.amount;
            
            if(count <= 0 && !lock)
            {
                refFluid = null;
            }
            
            syncContainer();
        }
        
        return out;
    }
    
    @Override
    public int getSlots()
    {
        return 2;//(oreDict || overflow) ? 2 : 1;
    }
    
    private ItemStack slotRef = ItemStack.EMPTY;
    
    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot)
    {
        if(slot != 0 || refStack.isEmpty())
        {
            return ItemStack.EMPTY;
        }
        
        return slotRef;
    }
    
    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        if(slot < 0 || slot >= 2 || refFluid != null || stack.isEmpty() || !BdsmConfig.multiPurposeBarrel || BdsmConfig.isBlacklisted(stack))
        {
            return stack;
        } else if(refStack.isEmpty() || (stackCapacity < 0 && !lock))
        {
            if(lock || stack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) || stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) || stack.hasCapability(CapabilityEnergy.ENERGY, null))
            {
                return stack; // BANNED! Nested containers are not permitted!
            } else if(!simulate)
            {
                refStack = stack.copy();
                count = Math.min(stack.getCount(), (stackCapacity < 0 ? (1 << 15) : stackCapacity) * stack.getMaxStackSize());
                refStack.setCount(1);
                
                cachedOres.clear();
                int[] aryIDs = OreDictionary.getOreIDs(refStack);
                topLoop:
                for(int id : aryIDs)
                {
                    String name = OreDictionary.getOreName(id);
                    for(String bl : BdsmConfig.oreDictBlacklist)
                    {
                        if(bl.matches(name)) continue topLoop;
                    }
                    cachedOres.add(new OreIngredient(name));
                }
                
                syncContainer();
            }
            
            int used = Math.min(stack.getCount(), (stackCapacity < 0 ? (1 << 15) : stackCapacity) * stack.getMaxStackSize());
            if(used > stack.getCount()) return ItemStack.EMPTY;
            ItemStack rStack = stack.copy();
            rStack.shrink(used);
            return rStack;
        } else if(!canMergeWith(stack))
        {
            return stack;
        }
        
        long rem = stackCapacity < 0 ? 0 : (long)stackCapacity * (long)refStack.getMaxStackSize() - getCount();
        int add = (int)Math.min(rem, stack.getCount());
        if(add < 0) add = 0;
        
        ItemStack copy = stack.copy();
        copy.setCount(overflow ? 0 : stack.getCount() - add);
        
        if(!simulate)
        {
            count += add;
            syncContainer();
        }
        
        return copy;
    }
    
    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if(slot != 0 || refStack.isEmpty())
        {
            return ItemStack.EMPTY;
        } else if(stackCapacity < 0)
        {
            ItemStack copy = refStack.copy();
            copy.setCount(amount);
            return copy;
        } else if(count <= 0)
        {
            return ItemStack.EMPTY;
        }
        
        int maxExtract;
        
        if(slot < count / refStack.getMaxStackSize())
        {
            maxExtract = refStack.getMaxStackSize();
        } else if(slot == count / refStack.getMaxStackSize())
        {
            maxExtract = (count % refStack.getMaxStackSize());
        } else
        {
            return ItemStack.EMPTY;
        }
        
        ItemStack copy = refStack.copy();
        copy.setCount(Math.min(amount, maxExtract));
        
        if(!simulate)
        {
            count -= copy.getCount();
            
            if(count <= 0 && !lock)
            {
                refStack = ItemStack.EMPTY;
                cachedOres.clear();
            }
            
            syncContainer();
        }
        
        return copy;
    }
    
    @Override
    public int getSlotLimit(int slot)
    {
        return overflow ? Integer.MAX_VALUE : (getRefItem().isEmpty() ? 64 : getRefItem().getMaxStackSize()) * stackCapacity;
    }
    
    @Override
    public boolean installUpgrade(@Nonnull EntityPlayer player, @Nonnull ItemStack stack)
    {
        if(stack.isEmpty()) return false;
        
        if(stack.getItem() == BDSM.itemUpgrade)
        {
            if(stack.getItemDamage() >= 0 && stack.getItemDamage() < 4) // Capacity upgrade
            {
                int value = 64 << (stack.getItemDamage() * 2);
                int remCap = getUpgradeCap() - getStackCap();
                
                if(remCap > 0) // Upgrades are now lossy if not exact
                {
                    setStackCap(getStackCap() + Math.min(value, remCap));
                    syncContainer();
                    
                    if(!player.capabilities.isCreativeMode)
                    {
                        stack.shrink(1);
                        player.inventory.markDirty();
                    }
                    
                    return true;
                }
                
                return false;
                
            } else if(stack.getItemDamage() == 4) // Creative upgrade
            {
                if(getStackCap() >= 0)
                {
                    setStackCap(-1);
                    count = 1 << 15;
                    syncContainer();
                    
                    if(!player.capabilities.isCreativeMode)
                    {
                        stack.shrink(1);
                        player.inventory.markDirty();
                    }
                    
                    return true;
                }
                
                return false;
            } else if(stack.getItemDamage() == 5) // Ore Dict upgrade
            {
                if(!oreDict)
                {
                    oreDict = true;
                    syncContainer();
                    
                    if(!player.capabilities.isCreativeMode)
                    {
                        stack.shrink(1);
                        player.inventory.markDirty();
                    }
                    
                    return true;
                }
                
                return false;
            } else if(stack.getItemDamage() == 6) // Void upgrade
            {
                if(!overflow)
                {
                    overflow = true;
                    syncContainer();
                    
                    if(!player.capabilities.isCreativeMode)
                    {
                        stack.shrink(1);
                        player.inventory.markDirty();
                    }
                    
                    return true;
                }
                
                return false;
            } else if(stack.getItemDamage() == 7) // Upgrade Reset
            {
                if(count <= 0 || stackCapacity < 0)
                {
                    // We're not going to refund creative players. They can just spawn more whenever
                    if(!player.capabilities.isCreativeMode && getStackCap() > 64)
                    {
                        int rem = getStackCap() - 64;
        
                        while(rem >= 64)
                        {
                            ItemStack drop = new ItemStack(BDSM.itemUpgrade, 1, 0);
            
                            if(rem >= 4096)
                            {
                                drop.setItemDamage(3);
                            } else if(rem >= 1024)
                            {
                                drop.setItemDamage(2);
                            } else if(rem >= 256)
                            {
                                drop.setItemDamage(1);
                            }
            
                            rem -= 64 << (drop.getItemDamage() * 2);
            
                            if(!player.addItemStackToInventory(drop)) player.dropItem(drop, true, false);
                        }
                    }
                    
                    if(stackCapacity < 0) count = 0; // Must be reset in the event of a creative upgrade (which modifies the underlying value at times)
                    setStackCap(64); // Also erases creative upgrade (which we're not refunding)
                    if(!lock)
                    {
                        refStack = ItemStack.EMPTY;
                        refFluid = null;
                    }
                }
                
                if(oreDict)
                {
                    if(!player.capabilities.isCreativeMode)
                    {
                        ItemStack drop = new ItemStack(BDSM.itemUpgrade, 1, 5);
                        if(!player.addItemStackToInventory(drop)) player.dropItem(drop, true, false);
                    }
                    oreDict = false;
                }
                
                if(overflow)
                {
                    if(!player.capabilities.isCreativeMode)
                    {
                        ItemStack drop = new ItemStack(BDSM.itemUpgrade, 1, 6);
                        if(!player.addItemStackToInventory(drop)) player.dropItem(drop, true, false);
                    }
                    overflow = false;
                }
                
                syncContainer();
                return true;
            }
        } else if(stack.getItem() == BDSM.itemKey)
        {
            lock = !lock;
            
            if(!lock && getCount() <= 0)
            {
                refStack = ItemStack.EMPTY;
                cachedOres.clear();
                refFluid = null;
            }
            
            syncContainer();
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("refStack", refStack.writeToNBT(new NBTTagCompound()));
        nbt.setTag("refFluid", refFluid == null ? new NBTTagCompound() : refFluid.writeToNBT(new NBTTagCompound()));
        nbt.setInteger("count", count);
        nbt.setInteger("stackCap", stackCapacity);
        nbt.setInteger("maxCap", maxStackCapacity);
        nbt.setBoolean("oreDict", oreDict);
        nbt.setBoolean("overflow", overflow);
        nbt.setBoolean("locked", lock);
        nbt.setIntArray("objColors", colors);
        return nbt;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        refStack = new ItemStack(nbt.getCompoundTag("refStack"));
        refFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("refFluid"));
        count = nbt.getInteger("count");
        stackCapacity = nbt.getInteger("stackCap");
        maxStackCapacity = nbt.getInteger("maxCap");
        oreDict = nbt.getBoolean("oreDict");
        overflow = nbt.getBoolean("overflow");
        lock = nbt.getBoolean("locked");
        colors = Arrays.copyOf(nbt.getIntArray("objColors"), colors.length);
        
        if(!refStack.isEmpty())
        {
            cachedOres.clear();
            int[] aryIDs = OreDictionary.getOreIDs(refStack);
            topLoop:
            for(int id : aryIDs)
            {
                String name = OreDictionary.getOreName(id);
                for(String bl : BdsmConfig.oreDictBlacklist)
                {
                    if(bl.matches(name)) continue topLoop;
                }
                cachedOres.add(new OreIngredient(name));
            }
            
            if(!slotRef.isEmpty() && canMergeWith(slotRef))
            {
                slotRef.setCount(getCount());
            } else
            {
                slotRef = refStack.copy();
                slotRef.setCount(getCount());
            }
        } else
        {
            slotRef = ItemStack.EMPTY;
        }
    }
    
    private final IFluidTankProperties fluidTank = new IFluidTankProperties()
    {
        @Nullable
        @Override
        public FluidStack getContents()
        {
            if(refFluid == null) return null;
            FluidStack tmp = refFluid.copy();
            tmp.amount = getCount();
            return tmp;
        }
        
        @Override
        public int getCapacity()
        {
            return overflow ? Integer.MAX_VALUE : stackCapacity < 0 ? (1 << 15) : (stackCapacity * 1000);
        }
        
        @Override
        public boolean canFill()
        {
            return refStack.isEmpty();
        }

        @Override
        public boolean canDrain()
        {
            return refFluid != null && getCount() > 0;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluidStack)
        {
            return canFill() && !BdsmConfig.isBlacklisted(fluidStack) && (refFluid == null || refFluid.isFluidEqual(fluidStack));
        }

        @Override
        public boolean canDrainFluidType(FluidStack fluidStack)
        {
            return canDrain() && refFluid.isFluidEqual(fluidStack);
        }
    };
}
