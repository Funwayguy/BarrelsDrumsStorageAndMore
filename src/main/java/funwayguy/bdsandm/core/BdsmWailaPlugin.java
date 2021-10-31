package funwayguy.bdsandm.core;

import funwayguy.bdsandm.blocks.tiles.BarrelTileEntity;
import funwayguy.bdsandm.blocks.tiles.CrateTileEntity;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;

@WailaPlugin
public class BdsmWailaPlugin implements IWailaPlugin
{
    @Override
    public void register(IRegistrar registrar)
    {
        registrar.registerComponentProvider(BdsmWailaHandler.INSTANCE, TooltipPosition.BODY, CrateTileEntity.class);
        registrar.registerStackProvider(BdsmWailaHandler.INSTANCE, CrateTileEntity.class);

        registrar.registerComponentProvider(BdsmWailaHandler.INSTANCE, TooltipPosition.BODY, BarrelTileEntity.class);
        registrar.registerStackProvider(BdsmWailaHandler.INSTANCE, BarrelTileEntity.class);
    }
}
