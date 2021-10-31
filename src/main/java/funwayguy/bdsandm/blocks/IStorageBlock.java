package funwayguy.bdsandm.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IStorageBlock
{
    void onPlayerInteract(World world, BlockPos pos, BlockState state, Direction side, ServerPlayerEntity player, Hand hand, boolean isHit, boolean altMode, int clickDelay);
}
