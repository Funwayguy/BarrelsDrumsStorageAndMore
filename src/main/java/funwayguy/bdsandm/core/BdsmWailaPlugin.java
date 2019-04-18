package funwayguy.bdsandm.core;

import funwayguy.bdsandm.blocks.tiles.TileEntityBarrel;
import funwayguy.bdsandm.blocks.tiles.TileEntityCrate;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;

@WailaPlugin
public class BdsmWailaPlugin implements IWailaPlugin
{
    @Override
    public void register(IWailaRegistrar registrar)
    {
        registrar.registerBodyProvider(BdsmWailaHandler.INSTANCE, TileEntityCrate.class);
        registrar.registerStackProvider(BdsmWailaHandler.INSTANCE, TileEntityCrate.class);
        
        registrar.registerBodyProvider(BdsmWailaHandler.INSTANCE, TileEntityBarrel.class);
        registrar.registerStackProvider(BdsmWailaHandler.INSTANCE, TileEntityBarrel.class);
    }
}
