package funwayguy.bdsandm.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IStorageBlock
{
    void onPlayerInteract(World world, BlockPos pos, IBlockState state, EnumFacing side, EntityPlayerMP player, EnumHand hand, boolean isHit, boolean altMode, int clickDelay);
}
