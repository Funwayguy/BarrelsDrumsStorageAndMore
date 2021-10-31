package funwayguy.bdsandm.inventory;

import funwayguy.bdsandm.blocks.tiles.ShippingTileEntity;
import funwayguy.bdsandm.core.BDSM;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;

public class InventoryShipping extends Inventory
{
    private final ShippingTileEntity tile;
    
    public InventoryShipping(ShippingTileEntity tile)
    {
        super(27);
        this.tile = tile;
    }
    
    public boolean isUsableByPlayer(PlayerEntity player)
    {
        return player.getDistanceSq(Vector3d.copyCentered(tile.getPos())) < 256;
    }
    
    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }
    
    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).isPresent() ||
                stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).isPresent() ||
                stack.getCapability(CapabilityEnergy.ENERGY, null).isPresent();
    }
    
    @Nonnull
    @Override
    public ItemStack addItem(ItemStack itemstack)
    {
        if(isItemValidForSlot(0, itemstack))
        {
            return itemstack;
        }
        
        for (int i = 0; i < this.getSizeInventory(); ++i)
        {
            ItemStack itemstack1 = this.getStackInSlot(i);

            if (itemstack1.isEmpty())
            {
                this.setInventorySlotContents(i, itemstack);
                this.markDirty();
                return ItemStack.EMPTY;
            }
        }
        
        return itemstack;
    }
    
    private boolean notDirty = false;
    
    @Override
    public void markDirty()
    {
        if(!notDirty)
        {
            super.markDirty();
        }
    }
    
    // Necessary so that the proxy can bulk access and set items before marking the whole thing dirty when its done
    public void setSlotWithoutNotice(int slot, ItemStack stack)
    {
        notDirty = true;
        
        this.setInventorySlotContents(slot, stack);
        
        notDirty = false;
    }
}
