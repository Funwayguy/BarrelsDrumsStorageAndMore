package funwayguy.bdsandm.client.color;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public interface IBdsmColorBlock {
    int getColorCount(IBlockReader blockAccess, BlockState state, BlockPos pos);
    int[] getColors(IBlockReader blockAccess, BlockState state, BlockPos pos);
    void setColors(IBlockReader blockAccess, BlockState state, BlockPos pos, int[] colors);
}