package funwayguy.bdsandm.blocks.tiles;

import funwayguy.bdsandm.core.BDSM;
import funwayguy.bdsandm.core.BDSMRegistry;
import funwayguy.bdsandm.inventory.InventoryShipping;
import funwayguy.bdsandm.inventory.ShippingContainer;
import funwayguy.bdsandm.inventory.capability.ShippingProxyWrapper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class ShippingTileEntity extends TileEntity implements IInventoryChangedListener, ITickableTileEntity, INamedContainerProvider
{
    /**If this isn't zero the internal itemhandler will be effectively useless, even for GUIs.
     * If it is then we're directly manipulating this inventory. (DO NOT USE DIRECTLY)
     * Calling getCapability() will redirect to the proxy wrapper. (MACHINES)
     * Calling getHandlerForContainer() will redirect to the tile in charge. (GUI USE ONLY)*/
    private int proxyIdx;
    private final InventoryShipping invo;
    private ShippingProxyWrapper proxyWrapper = null;
    private LazyOptional<ShippingProxyWrapper> proxyHolder = LazyOptional.of(() -> proxyWrapper);
    
    private static final Direction[][] openSides = new Direction[8][3];
    
    private int[] colors = new int[]{0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF};

    @SuppressWarnings("unused")
    public ShippingTileEntity()
    {
        this(0);
    }

    public ShippingTileEntity(int index)
    {
        super(BDSMRegistry.SHIPPING_TILE.get());
        this.proxyIdx = index;
        this.invo = new InventoryShipping(this);
        this.proxyWrapper = new ShippingProxyWrapper(this, invo);
        this.proxyHolder = LazyOptional.of(() -> proxyWrapper);
    }
    
    public int getColorCount()
    {
        return colors.length;
    }
    
    public int[] getColors()
    {
        return colors;
    }
    
    public void setColors(int[] c)
    {
        for(int i = 0; i < c.length && i < colors.length; i++)
        {
            colors[i] = c[i];
        }
        
        this.markDirty();
        if(world.getServer() != null) world.getServer().getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, world.getDimensionKey(), getUpdatePacket());
    }
    
    @Nonnull
    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.write(new CompoundNBT());
    }
    
    @Nonnull
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(pos, 0, this.write(new CompoundNBT()));
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
    	this.read(getBlockState(), pkt.getNbtCompound());
        this.world.markBlockRangeForRenderUpdate(this.pos, this.getBlockState(), this.getBlockState());
    }
    
    @Override
    public void tick()
    {
        if(world.isRemote) return;

        IEnergyStorage myEnergy = getCapability(CapabilityEnergy.ENERGY, null).orElse(null);
        
        if(myEnergy == null || myEnergy.getEnergyStored() <= 0 || !myEnergy.canExtract()) return;
        
        int maxOffer = myEnergy.extractEnergy(Integer.MAX_VALUE, true);
        if(maxOffer <= 0) return;
        int sent = 0;
        
        for(Direction side : openSides[proxyIdx])
        {
            BlockPos offPos = pos.offset(side);
            TileEntity tile = world.getTileEntity(offPos);
            
            if(tile != null && tile.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).isPresent())
            {
                IEnergyStorage target = tile.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).orElse(null);
                if(target == null || !target.canReceive()) continue;
                
                sent += target.receiveEnergy(maxOffer - sent, false);
            }
        }
        
        myEnergy.extractEnergy(sent, false);
    }
    
    @Override
    public void onInventoryChanged(@Nonnull IInventory invBasic) {
        this.markDirty();
    }

    
    @Nullable
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
        ShippingTileEntity proxyTile = getProxyTile();
        if(proxyTile == null) return super.getCapability(capability, facing);
        
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return proxyHolder.cast();
        } else if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            return proxyHolder.cast();
        } else if(capability == CapabilityEnergy.ENERGY)
        {
            return proxyHolder.cast();
        }

        return super.getCapability(capability, facing);
    }
    
    @Nullable
    public InventoryShipping getContainerInvo()
    {
        ShippingTileEntity proxyTile = getProxyTile();
        return proxyTile == null ? null : proxyTile.invo;
    }
    
    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT tag)
    {
        tag.putInt("proxyIdx", proxyIdx);
        tag.putIntArray("objColors", colors);
    
        ListNBT list = new ListNBT();
        
        for(int i = 0; i < invo.getSizeInventory(); i++)
        {
            ItemStack stack = invo.getStackInSlot(i);
            
            if(stack.isEmpty())
            {
                continue;
            }
            
            CompoundNBT itemTag = new CompoundNBT();
            itemTag.putByte("slot", (byte)i);
            itemTag.put("item", stack.write(new CompoundNBT()));
            list.add(itemTag);
        }
        
        tag.put("invo", list);
        
        return super.write(tag);
    }
    
    @Override
    public void read(BlockState state, CompoundNBT tag)
    {
        super.read(state, tag);
        
        this.proxyIdx = tag.getInt("proxyIdx");
        this.colors = Arrays.copyOf(tag.getIntArray("objColors"), colors.length);
        
        invo.clear();
        for(INBT nbt : tag.getList("invo", 10))
        {
            if(!(nbt instanceof CompoundNBT))
            {
                continue;
            }
        
            CompoundNBT itemTag = (CompoundNBT)nbt;
        
            invo.setSlotWithoutNotice(itemTag.getByte("slot"), ItemStack.read(itemTag.getCompound("item")));
        }
        
        invo.markDirty();
    }
    
    @Nullable
    public ShippingTileEntity getProxyTile()
    {
        ShippingTileEntity proxyTile = this;
        
        if(proxyIdx != 0)
        {
            BlockPos startPos;
    
            switch(proxyIdx)
            {
                case 4:
                    startPos = pos.add(-1, 0, 0);
                    break;
                case 5:
                    startPos = pos.add(-1, 0, -1);
                    break;
                case 1:
                    startPos = pos.add(0, 0, -1);
                    break;
                case 6:
                    startPos = pos.add(-1, -1, 0);
                    break;
                case 7:
                    startPos = pos.add(-1, -1, -1);
                    break;
                case 3:
                    startPos = pos.add(0, -1, -1);
                    break;
                case 2:
                    startPos = pos.add(0, -1, 0);
                    break;
                default:
                    startPos = pos;
            }
            
            if(!world.isBlockLoaded(startPos)) return null;
            TileEntity tile = world.getTileEntity(startPos);
            if(!(tile instanceof ShippingTileEntity)) return null;
            proxyTile = (ShippingTileEntity)tile;
        }
        
        return proxyTile;
    }
    
    static
    {
        // I could manually hardcode this in but I'm lazy and there are a lot of orientations to cover
        
        for(int i = 0; i < 8; i++)
        {
            if(i == 0 || i == 1 || i == 4 || i == 5)
            {
                openSides[i][0] = Direction.DOWN;
            } else
            {
                openSides[i][0] = Direction.UP;
            }
            
            if(i == 0 || i == 2 || i == 4 || i == 6)
            {
                openSides[i][1] = Direction.NORTH;
            } else
            {
                openSides[i][1] = Direction.SOUTH;
            }
            
            if(i < 4)
            {
                openSides[i][2] = Direction.WEST;
            } else
            {
                openSides[i][2] = Direction.EAST;
            }
        }
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return new TranslationTextComponent(BDSM.MOD_ID + ".shipping.gui");
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory playerInv, PlayerEntity player)
    {
        return new ShippingContainer(id, playerInv, this);
    }

    @Override
    protected void invalidateCaps()
    {
        super.invalidateCaps();
        proxyHolder.invalidate();
    }
}
