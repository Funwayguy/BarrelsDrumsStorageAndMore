package funwayguy.bdsandm.blocks.tiles;

import funwayguy.bdsandm.inventory.InventoryShipping;
import funwayguy.bdsandm.inventory.capability.ShippingProxyWrapper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class TileEntityShipping extends TileEntity implements IInventoryChangedListener, ITickable
{
    /**If this isn't zero the internal itemhandler will be effectively useless, even for GUIs.
     * If it is then we're directly manipulating this inventory. (DO NOT USE DIRECTLY)
     * Calling getCapability() will redirect to the proxy wrapper. (MACHINES)
     * Calling getHandlerForContainer() will redirect to the tile in charge. (GUI USE ONLY)*/
    private int proxyIdx;
    private final InventoryShipping invo;
    private final ShippingProxyWrapper proxyWrapper;
    
    private static final EnumFacing[][] openSides = new EnumFacing[8][3];
    
    private int[] colors = new int[]{0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF};
    
    @SuppressWarnings("unused")
    public TileEntityShipping()
    {
        this(0);
    }
    
    public TileEntityShipping(int index)
    {
        this.proxyIdx = index & 7; // We need to cut off the turned value because it doesn't actually matter.
        this.invo = new InventoryShipping(this);
        this.proxyWrapper = new ShippingProxyWrapper(this, invo);
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
        if(world.getMinecraftServer() != null) world.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, world.provider.getDimension(), getUpdatePacket());
    }
    
    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }
    
    @Nonnull
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(pos, 0, this.writeToNBT(new NBTTagCompound()));
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
    	this.readFromNBT(pkt.getNbtCompound());
    	this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
    }
    
    @Override
    public void update()
    {
        if(world.isRemote) return;
        
        IEnergyStorage myEnergy = this.getCapability(CapabilityEnergy.ENERGY, null);
        
        if(myEnergy == null || myEnergy.getEnergyStored() <= 0 || !myEnergy.canExtract()) return;
        
        int maxOffer = myEnergy.extractEnergy(Integer.MAX_VALUE, true);
        if(maxOffer <= 0) return;
        int sent = 0;
        
        for(EnumFacing side : openSides[proxyIdx])
        {
            BlockPos offPos = pos.offset(side);
            TileEntity tile = world.getTileEntity(offPos);
            
            if(tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, side.getOpposite()))
            {
                IEnergyStorage target = tile.getCapability(CapabilityEnergy.ENERGY, side.getOpposite());
                
                if(target == null || !target.canReceive())
                {
                    continue;
                }
                
                sent += target.receiveEnergy(maxOffer - sent, false);
            }
        }
        
        myEnergy.extractEnergy(sent, false);
    }
    
    @Override
    public void onInventoryChanged(@Nonnull IInventory invBasic)
    {
        this.markDirty();
    }
    
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        if(getProxyTile() == null) return false;
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY;
    }
    
    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        TileEntityShipping proxyTile = getProxyTile();
        if(proxyTile == null) return null;
        
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(proxyTile.proxyWrapper);
        } else if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(proxyTile.proxyWrapper);
        } else if(capability == CapabilityEnergy.ENERGY)
        {
            return CapabilityEnergy.ENERGY.cast(proxyTile.proxyWrapper);
        }
        
        return null;
    }
    
    // GUI USE ONLY
    public InventoryShipping getContainerInvo()
    {
        TileEntityShipping proxyTile = getProxyTile();
        return proxyTile == null ? null : proxyTile.invo;
    }
    
    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        tag.setInteger("proxyIdx", proxyIdx);
        tag.setIntArray("objColors", colors);
    
        NBTTagList list = new NBTTagList();
        
        for(int i = 0; i < invo.getSizeInventory(); i++)
        {
            ItemStack stack = invo.getStackInSlot(i);
            
            if(stack.isEmpty())
            {
                continue;
            }
            
            NBTTagCompound itemTag = new NBTTagCompound();
            itemTag.setByte("slot", (byte)i);
            itemTag.setTag("item", stack.writeToNBT(new NBTTagCompound()));
            list.appendTag(itemTag);
        }
        
        tag.setTag("invo", list);
        
        return super.writeToNBT(tag);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        
        this.proxyIdx = tag.getInteger("proxyIdx");
        this.colors = Arrays.copyOf(tag.getIntArray("objColors"), colors.length);
        
        invo.clear();
        for(NBTBase nbt : tag.getTagList("invo", 10))
        {
            if(!(nbt instanceof NBTTagCompound))
            {
                continue;
            }
        
            NBTTagCompound itemTag = (NBTTagCompound)nbt;
        
            invo.setSlotWithoutNotice(itemTag.getByte("slot"), new ItemStack(itemTag.getCompoundTag("item")));
        }
        
        invo.markDirty();
    }
    
    @Nullable
    public TileEntityShipping getProxyTile()
    {
        TileEntityShipping proxyTile = this;
        
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
            if(!(tile instanceof TileEntityShipping)) return null;
            proxyTile = (TileEntityShipping)tile;
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
                openSides[i][0] = EnumFacing.DOWN;
            } else
            {
                openSides[i][0] = EnumFacing.UP;
            }
            
            if(i == 0 || i == 2 || i == 4 || i == 6)
            {
                openSides[i][1] = EnumFacing.NORTH;
            } else
            {
                openSides[i][1] = EnumFacing.SOUTH;
            }
            
            if(i < 4)
            {
                openSides[i][2] = EnumFacing.WEST;
            } else
            {
                openSides[i][2] = EnumFacing.EAST;
            }
        }
    }
}
