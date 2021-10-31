package funwayguy.bdsandm.blocks.tiles;

import funwayguy.bdsandm.core.BDSMRegistry;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.CapabilityBarrel;
import funwayguy.bdsandm.inventory.capability.ICrateCallback;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BarrelTileEntity extends TileEntity implements ICrateCallback
{
    private CapabilityBarrel barrelCap = null;
    private LazyOptional<CapabilityBarrel> barrelHolder = LazyOptional.of(() -> barrelCap);
    private boolean creativeBreak = false;

    public BarrelTileEntity(TileEntityType<? extends BarrelTileEntity> typeIn)
    {
        super(typeIn);
    }

    @SuppressWarnings("unused")
    public BarrelTileEntity()
    {
        this(BDSMRegistry.BARREL_TILE.get());
        this.barrelCap = new CapabilityBarrel(64, 1024).setCallback(this);
        this.barrelHolder = LazyOptional.of(() -> barrelCap);
    }
    
    public BarrelTileEntity(int initCap, int maxCap)
    {
        this(BDSMRegistry.BARREL_TILE.get());
        this.barrelCap = new CapabilityBarrel(initCap, maxCap).setCallback(this);
        this.barrelHolder = LazyOptional.of(() -> barrelCap);
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
    public double getMaxRenderDistanceSquared()
    {
        return 1024D;
    }

    @Nullable
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return barrelHolder.cast();
        } else if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            return barrelHolder.cast();
        } else if(capability == BdsmCapabilies.CRATE_CAP)
        {
            return barrelHolder.cast();
        } else if(capability == BdsmCapabilies.BARREL_CAP)
        {
            return barrelHolder.cast();
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
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);
        
        barrelCap.deserializeNBT(nbt.getCompound("barrelCap"));
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        this.read(getBlockState(), pkt.getNbtCompound());
        this.world.markBlockRangeForRenderUpdate(this.pos, this.getBlockState(), this.getBlockState());
    }
    
    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        compound.put("barrelCap", barrelCap.serializeNBT());
        
        return super.write(compound);
    }
    
    private long lastClick = 0L;
    private int clickCount = 0;
    
    public int getClickCount(long worldTime, int delay)
    {
        if(worldTime - lastClick <= 1)
        {
            return -1;
        } else if(worldTime - lastClick > delay)
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

    @Override
    protected void invalidateCaps()
    {
        super.invalidateCaps();
        barrelHolder.invalidate();
    }
}
