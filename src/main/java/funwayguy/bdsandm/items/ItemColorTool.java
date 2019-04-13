package funwayguy.bdsandm.items;

import funwayguy.bdsandm.client.color.IBdsmColorBlock;
import funwayguy.bdsandm.core.BDSM;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemColorTool extends Item
{
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        IBlockState state = worldIn.getBlockState(pos);
        
        if(state.getBlock() instanceof IBdsmColorBlock)
        {
            player.openGui(BDSM.INSTANCE, 1, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        
        return EnumActionResult.SUCCESS;
    }
}
