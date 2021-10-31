package funwayguy.bdsandm.inventory.capability;

import funwayguy.bdsandm.core.BDSMTags;
import funwayguy.bdsandm.core.BdsmConfig;
import funwayguy.bdsandm.core.BDSMRegistry;
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
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CapabilityCrate implements ICrate
{
    private ItemStack refStack = ItemStack.EMPTY;
    private final List<ITag<Item>> cachedTags = new ArrayList<>();
    private int maxStackCapacity;
    private boolean oreDict = false;
    private boolean lock = false;
    private boolean overflow = false;
    private int[] colors = new int[]{0xFFFFFFFF, 0xFFFFFFFF};
    
    private int stackCapacity;
    private int count = 0;
    
    private ICrateCallback callback;
    
    public CapabilityCrate(int initCap, int maxStackCap)
    {
        this.stackCapacity = initCap;
        this.maxStackCapacity = maxStackCap;
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
    @Nonnull
    @Override
    public ItemStack getRefItem()
    {
        return this.refStack;
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
        if(ItemStack.areItemStackTagsEqual(refStack, stack))
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
    
    @Override
    public CapabilityCrate setCallback(ICrateCallback callback)
    {
        this.callback = callback;
        return this;
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
    public void setStackCap(int value)
    {
        this.stackCapacity = value;
    }
    
    @Override
    public int getCount()
    {
        return stackCapacity < 0 ? ((1 << 15) * refStack.getMaxStackSize()) : this.count;
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
        if(slot < 0 || slot >= 2 || stack.isEmpty() || BdsmConfig.isBlacklisted(stack))
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
        
        if(!simulate && add != 0)
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
        }
        
        ItemStack copy = refStack.copy();
        copy.setCount(Math.min(amount, getCount()));
        
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
        return overflow ? Integer.MAX_VALUE : (refStack.isEmpty() ? 64 : refStack.getMaxStackSize()) * stackCapacity;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack)
    {
        return true;
    }

    @Override
    public void copyContainer(IStackContainer crate)
    {
        this.deserializeNBT(crate.serializeNBT());
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
                    if(!lock) refStack = ItemStack.EMPTY;
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
}
