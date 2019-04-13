package funwayguy.bdsandm.blocks.tiles;

import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.CapabilityBarrel;
import funwayguy.bdsandm.inventory.capability.ICrateCallback;
import net.minecraft.block.BlockDirectional;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//@Interface(iface = "mcp.mobius.waila.api.IWailaDataProvider", modid = "waila")
public class TileEntityBarrel extends TileEntity implements ICrateCallback
{
    private final CapabilityBarrel barrelCap;
    private EnumFacing facing = EnumFacing.SOUTH;
    
    private boolean creativeBreak = false;
    
    public TileEntityBarrel()
    {
        barrelCap = new CapabilityBarrel(64, 1024).setCallback(this);
    }
    
    public TileEntityBarrel(int initCap, int maxCap)
    {
        barrelCap = new CapabilityBarrel(initCap, maxCap).setCallback(this);
    }
    
    public EnumFacing getFacing()
    {
        return this.facing;
    }
    
    public void setCreativeBroken(boolean state)
    {
        this.creativeBreak = state;
    }
    
    public boolean isCreativeBroken()
    {
        return this.creativeBreak;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared()
    {
        return 1024D;
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == BdsmCapabilies.CRATE_CAP || capability == BdsmCapabilies.BARREL_CAP)
        {
            return true;
        }
        
        return super.hasCapability(capability, facing);
    }
    
    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(barrelCap);
        } else if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(barrelCap);
        } else if(capability == BdsmCapabilies.CRATE_CAP)
        {
            return BdsmCapabilies.CRATE_CAP.cast(barrelCap);
        } else if(capability == BdsmCapabilies.BARREL_CAP)
        {
            return BdsmCapabilies.BARREL_CAP.cast(barrelCap);
        }
        
        return super.getCapability(capability, facing);
    }
    
    @Override
    public void onCrateChanged()
    {
        if(world.isRemote)
        {
            return;
        }
        
        this.facing = this.world.getBlockState(pos).getValue(BlockDirectional.FACING);
        this.markDirty();
        world.getMinecraftServer().getPlayerList().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 128, world.provider.getDimension(), getUpdatePacket());
    }
    
    @Override
    public NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }
    
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(pos, 0, this.writeToNBT(new NBTTagCompound()));
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        
        barrelCap.deserializeNBT(nbt.getCompoundTag("barrelCap"));
        this.facing = EnumFacing.byIndex(nbt.getInteger("facing"));
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
    	this.readFromNBT(pkt.getNbtCompound());
    	this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setTag("barrelCap", barrelCap.serializeNBT());
        nbt.setInteger("facing", facing.getIndex());
        
        return super.writeToNBT(nbt);
    }
}
