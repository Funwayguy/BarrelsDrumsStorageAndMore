package funwayguy.bdsandm.items;

import funwayguy.bdsandm.client.color.IBdsmColorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;

public class ColorToolItem extends Item
{
    public ColorToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        BlockState state = context.getWorld().getBlockState(context.getPos());

        if(state.getBlock() instanceof IBdsmColorBlock && context.getWorld().isRemote)
        {
            funwayguy.bdsandm.client.ColourScreen.openScreen(state, context.getWorld(), context.getPos());
        }

        return ActionResultType.SUCCESS;
    }
}
