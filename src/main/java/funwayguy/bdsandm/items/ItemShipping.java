package funwayguy.bdsandm.items;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemShipping extends ItemBlock
{
    public ItemShipping(Block block)
    {
        super(block);
    }
    
    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack held = player.getHeldItem(hand);
        
        if(player.getHeldItem(hand).isEmpty())
        {
            return EnumActionResult.FAIL;
        }
        
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();
        int myIdx = new int[]{4, 5, 1, 0}[player.getHorizontalFacing().getHorizontalIndex()];
        BlockPos origPos = pos;

        if (!block.isReplaceable(worldIn, pos))
        {
            pos = pos.offset(facing);
        }
        
        switch(myIdx)
        {
            case 4:
                pos = pos.add(-1, 0, 0);
                break;
            case 5:
                pos = pos.add(-1, 0, -1);
                break;
            case 1:
                pos = pos.add(0, 0, -1);
                break;
        }
        
        for(int i = 0; i < 2; i++)
        {
            for(int j = 0; j < 2; j++)
            {
                for(int k = 0; k < 2; k++)
                {
                    BlockPos posOff = pos.add(i, j, k);
                    if(!player.canPlayerEdit(posOff, facing, held) || !worldIn.mayPlace(this.block, posOff, false, facing, null))
                    {
                        return EnumActionResult.FAIL;
                    }
                }
            }
        }
        
        return super.onItemUse(player, worldIn, origPos, hand, facing, hitX, hitY, hitZ);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        
        tooltip.add("Merges other containers of items, fluids and energy within a single block");
    }
}