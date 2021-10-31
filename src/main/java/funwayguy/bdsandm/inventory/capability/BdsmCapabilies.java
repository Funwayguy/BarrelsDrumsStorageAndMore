package funwayguy.bdsandm.inventory.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class BdsmCapabilies
{
    @CapabilityInject(ICrate.class)
    public static Capability<ICrate> CRATE_CAP = null;
    @CapabilityInject(IBarrel.class)
    public static Capability<IBarrel> BARREL_CAP = null;
    
    public static void register()
    {
        CapabilityManager.INSTANCE.register(ICrate.class, new IStorage<ICrate>()
        {
            @Nullable
            @Override
            public INBT writeNBT(Capability<ICrate> capability, ICrate instance, Direction side)
            {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<ICrate> capability, ICrate instance, Direction side, INBT nbt)
            {
                if(nbt instanceof CompoundNBT)
                {
                    instance.deserializeNBT((CompoundNBT)nbt);
                }
            }
        }, () -> new CapabilityCrate(64, 1024));
        
        CapabilityManager.INSTANCE.register(IBarrel.class, new IStorage<IBarrel>()
        {
            @Nullable
            @Override
            public INBT writeNBT(Capability<IBarrel> capability, IBarrel instance, Direction side)
            {
                return instance.serializeNBT();
            }
    
            @Override
            public void readNBT(Capability<IBarrel> capability, IBarrel instance, Direction side, INBT nbt)
            {
                if(nbt instanceof CompoundNBT)
                {
                    instance.deserializeNBT((CompoundNBT)nbt);
                }
            }
        }, () -> new CapabilityBarrel(64, 1024));
    }
}
