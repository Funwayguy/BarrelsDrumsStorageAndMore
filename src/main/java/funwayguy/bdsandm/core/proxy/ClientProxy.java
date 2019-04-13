package funwayguy.bdsandm.core.proxy;

import funwayguy.bdsandm.client.obj.OBJLoaderColored;
import funwayguy.bdsandm.core.BDSM;
import funwayguy.bdsandm.network.PacketBdsm;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    @Override
    public boolean isClient()
    {
        return true;
    }
    
    @Override
    public void setupObjLoader()
    {
        ModelLoaderRegistry.registerLoader(OBJLoaderColored.INSTANCE);
    }
    
    @Override
    public void registerNetwork()
    {
        super.registerNetwork();
        BDSM.INSTANCE.network.registerMessage(PacketBdsm.ClientHandler.class, PacketBdsm.class, 0, Side.CLIENT);
    }
}
