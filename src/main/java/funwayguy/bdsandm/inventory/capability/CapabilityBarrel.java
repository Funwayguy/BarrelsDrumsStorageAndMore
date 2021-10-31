package funwayguy.bdsandm.inventory.capability;

import funwayguy.bdsandm.core.BDSMRegistry;
import funwayguy.bdsandm.core.BDSMTags;
import funwayguy.bdsandm.core.BdsmConfig;
import funwayguy.bdsandm.items.UpgradeItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CapabilityBarrel implements ICrate, IBarrel
{
    private FluidStack refFluid = FluidStack.EMPTY;
    private ItemStack refStack = ItemStack.EMPTY;
    private final List<ITag<Item>> cachedTags = new ArrayList<>();
    private int maxStackCapacity;
    private boolean oreDict = false;
    private boolean lock = false;
    private boolean overflow = false;
    private int[] colors = new int[]{0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF};
    
    private ItemStack containerItem = ItemStack.EMPTY;
    
    private int stackCapacity;
    private int count = 0;
    
    private ICrateCallback callback;
    
    private final FluidTank[] tankProps;
    
    public CapabilityBarrel(int initCap, int maxStackCap)
    {
        this.stackCapacity = initCap;
        this.maxStackCapacity = maxStackCap;
        
        this.tankProps = new FluidTank[]{fluidTank};
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
        if(refFluid.isEmpty() && ItemStack.areItemStackTagsEqual(refStack, stack))
        {
            if(ItemStack.areItemsEqual(refStack, stack))
            {
                return true;
            } else if(oreDict)
            {
                for(ITag<Item> ing : cachedTags)
                {
                    if(stack.getItem().isIn(ing))
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
    
//    @Override
//    public IFluidTankProperties[] getTankProperties()
//    {
//        return tankProps;
//    }

    @Override
    public int getTanks() {
        return tankProps.length;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return tankProps[tank].getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return tankProps[tank].getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction doFill)
    {
        if(!fluidTank.isFluidValid(resource) || (refFluid.isEmpty() && lock))
        {
            return 0;
        } else if(stackCapacity < 0)
        {
            if(doFill.execute())
            {
                if(refFluid.isEmpty())
                {
                    refFluid = resource.copy();
                    refFluid.setAmount(1);
                }
    
                count = 1000;
            }
            
            return resource.getAmount();
        }
        
        long capacity = stackCapacity * 1000L - count;
        int fill = (int)Math.min(resource.getAmount(), capacity);
        
        if(doFill.execute())
        {
            if(refFluid.isEmpty())
            {
                refFluid = resource.copy();
                refFluid.setAmount(1);
            }
            
            count += fill;
            
            syncContainer();
        }
        
        return overflow ? resource.getAmount() : fill;
    }
    
    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, FluidAction doDrain)
    {
        if(!refFluid.isFluidEqual(resource))
        {
            return null;
        } else if(stackCapacity < 0)
        {
            FluidStack out = refFluid.copy();
            out.setAmount(resource.getAmount());
            return out;
        }
        
        FluidStack out = refFluid.copy();
        out.setAmount(Math.min(resource.getAmount(), count));

        if(doDrain.execute())
        {
            count -= out.getAmount();
            
            if(count <= 0 && !lock)
            {
                refFluid = FluidStack.EMPTY;
            }
            
            syncContainer();
        }
        
        return out;
    }
    
    @Nullable
    @Override
    public FluidStack drain(int maxDrain, FluidAction doDrain)
    {
        if(!(refFluid != null && fluidTank.getFluidAmount() > 0))
        {
            return null;
        } else if(stackCapacity < 0)
        {
            FluidStack out = refFluid.copy();
            out.setAmount(maxDrain);
            return out;
        } else if(count <= 0)
        {
            return null;
        }
        
        FluidStack out = refFluid.copy();
        out.setAmount(Math.min(maxDrain, count));
        
        if(doDrain.execute())
        {
            count -= out.getAmount();
            
            if(count <= 0 && !lock)
            {
                refFluid = FluidStack.EMPTY;
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
        if(slot < 0 || slot >= 2 || refFluid != null || stack.isEmpty() || !BdsmConfig.COMMON.multiPurposeBarrel.get() || BdsmConfig.isBlacklisted(stack))
        {
            return stack;
        } else if(refStack.isEmpty() || (stackCapacity < 0 && !lock))
        {
            if(lock || stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).isPresent() ||
                    stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).isPresent() ||
                    stack.getCapability(CapabilityEnergy.ENERGY, null).isPresent())
            {
                return stack; // BANNED! Nested containers are not permitted!
            } else if(!simulate)
            {
                refStack = stack.copy();
                count = Math.min(stack.getCount(), (stackCapacity < 0 ? (1 << 15) : stackCapacity) * stack.getMaxStackSize());
                refStack.setCount(1);
                
                cachedTags.clear();
                ITagCollectionSupplier tagCollection = TagCollectionManager.getManager();
                Set<ResourceLocation> aryIDs = refStack.getItem().getTags();
                topLoop:
                for(ResourceLocation id : aryIDs)
                {
                    String name = id.toString();
                    for(String bl : BdsmConfig.oreDictBlacklist)
                    {
                        if(name.matches(bl)) continue topLoop;
                    }
                    cachedTags.add((Tag<Item>) tagCollection.getItemTags().get(id));
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
                cachedTags.clear();
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
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public boolean installUpgrade(@Nonnull PlayerEntity player, @Nonnull ItemStack stack)
    {
        if(stack.isEmpty()) return false;
        
        if(stack.getItem().isIn(BDSMTags.UPGRADES))
        {
            if(stack.getItem() instanceof UpgradeItem) // Capacity upgrade
            {
                int value = ((UpgradeItem)stack.getItem()).value;
                int remCap = getUpgradeCap() - getStackCap();
                
                if(remCap > 0) // Upgrades are now lossy if not exact
                {
                    setStackCap(getStackCap() + Math.min(value, remCap));
                    syncContainer();
                    
                    if(!player.abilities.isCreativeMode)
                    {
                        stack.shrink(1);
                        player.inventory.markDirty();
                    }
                    
                    return true;
                }
                
                return false;

            } else if(stack.getItem() == BDSMRegistry.UPGRADE_CREATIVE.get()) // Creative upgrade
            {
                if(getStackCap() >= 0)
                {
                    setStackCap(-1);
                    count = 1 << 15;
                    syncContainer();
                    
                    if(!player.abilities.isCreativeMode)
                    {
                        stack.shrink(1);
                        player.inventory.markDirty();
                    }
                    
                    return true;
                }
                
                return false;
            } else if(stack.getItem() == BDSMRegistry.UPGRADE_ORE.get()) // Ore Dict upgrade
            {
                if(!oreDict)
                {
                    oreDict = true;
                    syncContainer();
                    
                    if(!player.abilities.isCreativeMode)
                    {
                        stack.shrink(1);
                        player.inventory.markDirty();
                    }
                    
                    return true;
                }
                
                return false;
            } else if(stack.getItem() == BDSMRegistry.UPGRADE_VOID.get()) // Void upgrade
            {
                if(!overflow)
                {
                    overflow = true;
                    syncContainer();
                    
                    if(!player.abilities.isCreativeMode)
                    {
                        stack.shrink(1);
                        player.inventory.markDirty();
                    }
                    
                    return true;
                }
                
                return false;
            } else if(stack.getItem() == BDSMRegistry.UPGRADE_UNINSTALL.get()) // Upgrade Reset
            {
                if(count <= 0 || stackCapacity < 0)
                {
                    // We're not going to refund creative players. They can just spawn more whenever
                    if(!player.abilities.isCreativeMode && getStackCap() > 64)
                    {
                        int rem = getStackCap() - 64;
        
                        while(rem >= 64)
                        {
                            ItemStack drop = new ItemStack(BDSMRegistry.UPGRADE_64.get());

                            if(rem >= 4096)
                            {
                                drop = new ItemStack(BDSMRegistry.UPGRADE_4096.get());
                            } else if(rem >= 1024)
                            {
                                drop = new ItemStack(BDSMRegistry.UPGRADE_1024.get());
                            } else if(rem >= 256)
                            {
                                drop = new ItemStack(BDSMRegistry.UPGRADE_256.get());
                            }

                            rem = 0;
            
                            if(!player.addItemStackToInventory(drop)) player.dropItem(drop, true, false);
                        }
                    }
                    
                    if(stackCapacity < 0) count = 0; // Must be reset in the event of a creative upgrade (which modifies the underlying value at times)
                    setStackCap(64); // Also erases creative upgrade (which we're not refunding)
                    if(!lock)
                    {
                        refStack = ItemStack.EMPTY;
                        refFluid = FluidStack.EMPTY;
                    }
                }
                
                if(oreDict)
                {
                    if(!player.abilities.isCreativeMode)
                    {
                        ItemStack drop = new ItemStack(BDSMRegistry.UPGRADE_ORE.get(), 1);
                        if(!player.addItemStackToInventory(drop)) player.dropItem(drop, true, false);
                    }
                    oreDict = false;
                }
                
                if(overflow)
                {
                    if(!player.abilities.isCreativeMode)
                    {
                        ItemStack drop = new ItemStack(BDSMRegistry.UPGRADE_VOID.get(), 1);
                        if(!player.addItemStackToInventory(drop)) player.dropItem(drop, true, false);
                    }
                    overflow = false;
                }
                
                syncContainer();
                return true;
            }
        } else if(stack.getItem() == BDSMRegistry.CRATE_KEY.get())
        {
            lock = !lock;
            
            if(!lock && getCount() <= 0)
            {
                refStack = ItemStack.EMPTY;
                cachedTags.clear();
                refFluid = FluidStack.EMPTY;
            }
            
            syncContainer();
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public CompoundNBT serializeNBT()
    {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("refStack", refStack.write(new CompoundNBT()));
        nbt.put("refFluid", refFluid.isEmpty() ? new CompoundNBT() : refFluid.writeToNBT(new CompoundNBT()));
        nbt.putInt("count", count);
        nbt.putInt("stackCap", stackCapacity);
        nbt.putInt("maxCap", maxStackCapacity);
        nbt.putBoolean("oreDict", oreDict);
        nbt.putBoolean("overflow", overflow);
        nbt.putBoolean("locked", lock);
        nbt.putIntArray("objColors", colors);
        return nbt;
    }
    
    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {
        refStack = ItemStack.read(nbt.getCompound("refStack"));
        refFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("refFluid"));
        count = nbt.getInt("count");
        stackCapacity = nbt.getInt("stackCap");
        maxStackCapacity = nbt.getInt("maxCap");
        oreDict = nbt.getBoolean("oreDict");
        overflow = nbt.getBoolean("overflow");
        lock = nbt.getBoolean("locked");
        colors = Arrays.copyOf(nbt.getIntArray("objColors"), colors.length);
        
        if(!refStack.isEmpty())
        {
            cachedTags.clear();
            ITagCollectionSupplier tagCollection = TagCollectionManager.getManager();
            Set<ResourceLocation> aryIDs = refStack.getItem().getTags();
            topLoop:
            for(ResourceLocation id : aryIDs)
            {
                String name = id.toString();
                for(String bl : BdsmConfig.oreDictBlacklist)
                {
                    if(name.matches(bl)) continue topLoop;
                }
                cachedTags.add((Tag<Item>) tagCollection.getItemTags().get(id));
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

    private final FluidTank fluidTank = new FluidTank(overflow ? Integer.MAX_VALUE : stackCapacity < 0 ? (1 << 15) : (stackCapacity * 1000))
    {
        @Nullable
        @Override
        public FluidStack getFluid()
        {
            if(refFluid.isEmpty()) return FluidStack.EMPTY;
            FluidStack tmp = refFluid.copy();
            tmp.setAmount(getCount());
            return tmp;
        }
        
        @Override
        public int getCapacity()
        {
            return overflow ? Integer.MAX_VALUE : stackCapacity < 0 ? (1 << 15) : (stackCapacity * 1000);
        }

        @Override
        public boolean isFluidValid(FluidStack fluidStack) {
            return super.isFluidValid(fluidStack) && !BdsmConfig.isBlacklisted(fluidStack) && (refFluid.isEmpty() || refFluid.isFluidEqual(fluidStack));
        }
    };
}
