package funwayguy.bdsandm.client;

import funwayguy.bdsandm.client.renderer.TileEntityRenderBarrel;
import funwayguy.bdsandm.client.renderer.TileEntityRenderCrate;
import funwayguy.bdsandm.core.BDSMRegistry;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientHandler
{
	public static void onClientSetup(final FMLClientSetupEvent event)
	{
		ClientRegistry.bindTileEntityRenderer(BDSMRegistry.BARREL_TILE.get(), TileEntityRenderBarrel::new);
		ClientRegistry.bindTileEntityRenderer(BDSMRegistry.CRATE_TILE.get(), TileEntityRenderCrate::new);

		ScreenManager.registerFactory(BDSMRegistry.SHIPPING_CONTAINER.get(), ShippingScreen::new);

		RenderTypeLookup.setRenderLayer(BDSMRegistry.WOOD_BARREL.get(), RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(BDSMRegistry.WOOD_CRATE.get(), RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(BDSMRegistry.METAL_BARREL.get(), RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(BDSMRegistry.METAL_CRATE.get(), RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(BDSMRegistry.SHIPPING_CONTAINER_BLOCK.get(), RenderType.getCutout());
	}
}
