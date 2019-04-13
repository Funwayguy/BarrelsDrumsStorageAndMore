package funwayguy.bdsandm.client.color;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IBdsmColorBlock
{
    int getColorCount(IBlockAccess blockAccess, IBlockState state, BlockPos pos);
    int[] getColors(IBlockAccess blockAccess, IBlockState state, BlockPos pos);
    void setColors(IBlockAccess blockAccess, IBlockState state, BlockPos pos, int[] colors);
}