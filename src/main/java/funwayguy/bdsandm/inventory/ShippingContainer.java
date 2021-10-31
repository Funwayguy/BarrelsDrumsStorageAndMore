package funwayguy.bdsandm.inventory;

import funwayguy.bdsandm.blocks.tiles.ShippingTileEntity;
import funwayguy.bdsandm.core.BDSMRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

import java.util.Objects;

public class ShippingContainer extends Container
{
    private final InventoryShipping shipInvo;

    public ShippingContainer(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data)
    {
        this(windowId, playerInventory, getTileEntity(playerInventory, data));
    }

    private static ShippingTileEntity getTileEntity(final PlayerInventory playerInventory, final PacketBuffer data)
    {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null!");
        Objects.requireNonNull(data, "data cannot be null!");
        final TileEntity tileAtPos = playerInventory.player.world.getTileEntity(data.readBlockPos());

        if (tileAtPos instanceof ShippingTileEntity) {
            return (ShippingTileEntity) tileAtPos;
        }

        throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
    }

    public ShippingContainer(int id, PlayerInventory playerInvo, ShippingTileEntity te)
    {
        super(BDSMRegistry.SHIPPING_CONTAINER.get(), id);
        
        this.shipInvo = te.getContainerInvo();
        if(shipInvo != null) {
            int i = -18;

            for (int j = 0; j < 3; ++j)
            {
                for (int k = 0; k < 9; ++k)
                {
                    this.addSlot(new Slot(shipInvo, k + j * 9, 8 + k * 18, 18 + j * 18)
                    {
                        @Override
                        public boolean isItemValid(ItemStack stack)
                        {
                            return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
                        }
                    });
                }
            }

            for (int l = 0; l < 3; ++l)
            {
                for (int j1 = 0; j1 < 9; ++j1)
                {
                    this.addSlot(new Slot(playerInvo, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i));
                }
            }

            for (int i1 = 0; i1 < 9; ++i1)
            {
                this.addSlot(new Slot(playerInvo, i1, 8 + i1 * 18, 161 + i));
            }
        } else {
            onContainerClosed(playerInvo.player);
        }
    }
    
    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < 27)
            {
                if (!this.mergeItemStack(itemstack1, 27, this.inventorySlots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 0, 27, false))
            {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty())
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }
    
    @Override
    public boolean canInteractWith(PlayerEntity playerIn)
    {
        return shipInvo.isUsableByPlayer(playerIn);
    }
    
    @Override
    public void onContainerClosed(PlayerEntity playerIn)
    {
        super.onContainerClosed(playerIn);
    }
}
