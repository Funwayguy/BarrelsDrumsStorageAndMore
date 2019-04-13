package funwayguy.bdsandm.core;

import funwayguy.bdsandm.blocks.*;
import funwayguy.bdsandm.core.proxy.CommonProxy;
import funwayguy.bdsandm.core.proxy.TabBDSM;
import funwayguy.bdsandm.events.GuiHandler;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.items.ItemColorTool;
import funwayguy.bdsandm.items.ItemUpgrade;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.Logger;

@Mod(modid = BDSM.MOD_ID, version = "@VERSION@", name = BDSM.MOD_NAME, guiFactory = "funwayguy.bdsandm.client.ConfigGuiFactory")
public class BDSM
{
    public static final String MOD_ID = "bdsandm";
    public static final String MOD_NAME = "BoxesDrumsStorageAndMore";
    
    @Mod.Instance(MOD_ID)
    public static BDSM INSTANCE;
    
    @SidedProxy(clientSide = "funwayguy.bdsandm.core.proxy.ClientProxy", serverSide = "funwayguy.bdsandm.core.proxy.CommonProxy")
    public static CommonProxy proxy;
	public SimpleNetworkWrapper network;
    public static Logger logger;
    
    public static CreativeTabs tabBdsm = new TabBDSM();
    
    public static final Block blockWoodCrate = new BlockWoodCrate();
    public static final Block blockMetalCrate = new BlockMetalCrate();
    
    public static final Block blockWoodBarrel = new BlockWoodBarrel().setHardness(2.0F).setResistance(5.0F);
    public static final Block blockMetalBarrel = new BlockMetalBarrel();
    
    public static final Block blockShippingContainer = new BlockShippingContainer().setHardness(3.0F).setResistance(10.0F);
    
    public static final Item itemUpgrade = new ItemUpgrade();
    public static final Item itemKey = new Item().setTranslationKey(MOD_ID + ".crate_key").setCreativeTab(tabBdsm);
    public static final Item itemColor = new ItemColorTool().setTranslationKey(MOD_ID + ".color_tool").setCreativeTab(tabBdsm);
    
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        network = NetworkRegistry.INSTANCE.newSimpleChannel("BDSM_CHAN");
        
        BdsmConfig.setConfig(event.getSuggestedConfigurationFile());
        BdsmConfig.load();
        
        proxy.registerNetwork();
        proxy.setupObjLoader();
        
        RegEventHandler.initContent();
        
        BdsmCapabilies.register();
    
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
    }
    
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event)
    {
    }
}
