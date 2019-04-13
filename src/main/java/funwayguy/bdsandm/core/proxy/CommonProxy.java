package funwayguy.bdsandm.core.proxy;

import funwayguy.bdsandm.core.BDSM;
import funwayguy.bdsandm.network.PacketBdsm;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy
{
    public boolean isClient()
    {
        return false;
    }
    
    public void setupObjLoader()
    {
    }
    
    public void registerNetwork()
    {
        BDSM.INSTANCE.network.registerMessage(PacketBdsm.ServerHandler.class, PacketBdsm.class, 0, Side.SERVER);
    }
}
