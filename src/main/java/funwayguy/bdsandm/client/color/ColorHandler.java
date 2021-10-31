package funwayguy.bdsandm.client.color;

import funwayguy.bdsandm.core.BDSMRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.client.event.ColorHandlerEvent;

public class ColorHandler {
	public static void initBlockColors(final ColorHandlerEvent.Block event)
	{
		BlockColors colors = event.getBlockColors();
		colors.register(ColorHandler::getBlockColor, BDSMRegistry.METAL_BARREL.get(), BDSMRegistry.METAL_CRATE.get(), BDSMRegistry.SHIPPING_CONTAINER_BLOCK.get());
	}

	public static void initItemColors(final ColorHandlerEvent.Item event)
	{
		ItemColors colors = event.getItemColors();
		colors.register(ColorHandler::getItemColor, BDSMRegistry.METAL_BARREL.get(), BDSMRegistry.METAL_CRATE.get(), BDSMRegistry.SHIPPING_CONTAINER_BLOCK.get());
	}

	public static int getBlockColor(BlockState state, IBlockReader worldIn, BlockPos pos, int tintIndex) {
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

	public static int getItemColor(ItemStack stack, int tintIndex)
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
