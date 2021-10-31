package funwayguy.bdsandm.core;

import funwayguy.bdsandm.client.ClientHandler;
import funwayguy.bdsandm.client.color.ColorHandler;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.network.PacketHandler;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BDSM.MOD_ID)
public class BDSM
{
    public static final String MOD_ID = "bdsandm";
    public static final String MOD_NAME = "BoxesDrumsStorageAndMore";

    public static final Logger LOGGER = LogManager.getLogger();

    public static ItemGroup tabBdsm = new TabBDSM();

    public BDSM()
    {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(Type.CLIENT, BdsmConfig.clientSpec);
        ModLoadingContext.get().registerConfig(Type.COMMON, BdsmConfig.serverSpec);
        eventBus.register(BdsmConfig.class);

        eventBus.addListener(this::setup);

        BDSMRegistry.BLOCKS.register(eventBus);
        BDSMRegistry.TILES.register(eventBus);
        BDSMRegistry.ITEMS.register(eventBus);
        BDSMRegistry.CONTAINERS.register(eventBus);


        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            eventBus.addListener(ClientHandler::onClientSetup);
            eventBus.addListener(ColorHandler::initBlockColors);
            eventBus.addListener(ColorHandler::initItemColors);
        });
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        PacketHandler.init();
        BdsmCapabilies.register();
    }
}
