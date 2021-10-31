package funwayguy.bdsandm.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ShippingItem extends BlockItem
{
    public ShippingItem(Properties properties, Block block)
    {
        super(block, properties);
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        BlockItemUseContext blockContext = new BlockItemUseContext(context);
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        BlockPos pos = context.getPos();
        World worldIn = context.getWorld();
        Direction facing = context.getFace();
        ItemStack held = player.getHeldItem(hand);
        
        if(player.getHeldItem(hand).isEmpty())
        {
            return ActionResultType.FAIL;
        }
        
        BlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();
        int myIdx = new int[]{4, 5, 1, 0}[player.getHorizontalFacing().getHorizontalIndex()];
        BlockPos origPos = pos;

        if (!block.isReplaceable(worldIn.getBlockState(pos), blockContext))
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
                    if(!player.canPlayerEdit(posOff, facing, held) || !blockContext.canPlace())
                    {
                        return ActionResultType.FAIL;
                    }
                }
            }
        }
        
        return super.onItemUse(context);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        tooltip.add(new TranslationTextComponent("Merges other containers of items, fluids and energy within a single block"));
    }
}