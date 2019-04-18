package funwayguy.bdsandm.events;

import funwayguy.bdsandm.blocks.BlockBarrelBase;
import funwayguy.bdsandm.blocks.BlockCrateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class EventHandler
{
    @SubscribeEvent
    public static void onBlockHit(LeftClickBlock event)
    {
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        
        if(!event.getEntityPlayer().isSneaking() && event.getEntityPlayer().capabilities.isCreativeMode && (state.getBlock() instanceof BlockCrateBase || state.getBlock() instanceof BlockBarrelBase))
        {
            event.setCanceled(true);
            state.getBlock().onBlockClicked(event.getWorld(), event.getPos(), event.getEntityPlayer());
        }
    }
}
