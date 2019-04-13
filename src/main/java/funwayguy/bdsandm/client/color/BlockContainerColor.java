package funwayguy.bdsandm.client.color;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class BlockContainerColor implements IBlockColor, IItemColor
{
    public static final BlockContainerColor INSTANCE = new BlockContainerColor();
    
    @Override
    public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex)
    {
        if(state.getBlock() instanceof IBdsmColorBlock)
        {
            int[] colors = ((IBdsmColorBlock)state.getBlock()).getColors(worldIn, state, pos);
            
            if(tintIndex >= 0 && tintIndex < colors.length)
            {
                return colors[tintIndex];
            }
        }
        
        return 0xFFFFFFFF;
    }
    
    @Override
    public int colorMultiplier(ItemStack stack, int tintIndex)
    {
        if(stack.getItem() instanceof IBdsmColorBlock)
        {
            int[] colors = ((IBdsmColorItem)stack.getItem()).getColors(stack);
            
            if(tintIndex >= 0 && tintIndex < colors.length)
            {
                return colors[tintIndex];
            }
        }
        
        return 0xFFFFFFFF;
    }
}