package funwayguy.bdsandm.blocks.tiles;

import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.CapabilityCrate;
import funwayguy.bdsandm.inventory.capability.ICrateCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityCrate extends TileEntity implements ICrateCallback
{
    private final CapabilityCrate crateCap;
    
    private boolean creativeBreak = false;
    
    @SuppressWarnings("unused")
    public TileEntityCrate()
    {
        crateCap = new CapabilityCrate(64, 1024).setCallback(this);
    }
    
    public TileEntityCrate(int initCap, int maxCap)
    {
        crateCap = new CapabilityCrate(initCap, maxCap).setCallback(this);
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
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == BdsmCapabilies.CRATE_CAP)
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
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(crateCap);
        } else if(capability == BdsmCapabilies.CRATE_CAP)
        {
            return BdsmCapabilies.CRATE_CAP.cast(crateCap);
        }
        
        return super.getCapability(capability, facing);
    }
    
    @Override
    public void onCrateChanged()
    {
        if(world.isRemote) return;
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
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        
        crateCap.deserializeNBT(nbt.getCompoundTag("crateCap"));
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
    	this.readFromNBT(pkt.getNbtCompound());
    	this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
    }
    
    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setTag("crateCap", crateCap.serializeNBT());
        
        return super.writeToNBT(nbt);
    }
    
    private long lastClick = 0L;
    private int clickCount = 0;
    
    public int getClickCount(long worldTime)
    {
        if(worldTime - lastClick > 5)
        {
            lastClick = worldTime;
            clickCount = 0;
            return 0;
        } else
        {
            lastClick = worldTime;
            return ++clickCount;
        }
    }
}