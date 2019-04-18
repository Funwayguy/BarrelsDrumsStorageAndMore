package funwayguy.bdsandm.core;

import funwayguy.bdsandm.blocks.tiles.TileEntityBarrel;
import funwayguy.bdsandm.blocks.tiles.TileEntityCrate;
import funwayguy.bdsandm.blocks.tiles.TileEntityShipping;
import funwayguy.bdsandm.client.color.BlockContainerColor;
import funwayguy.bdsandm.client.obj.OBJLoaderColored;
import funwayguy.bdsandm.client.renderer.TileEntityRenderBarrel;
import funwayguy.bdsandm.client.renderer.TileEntityRenderCrate;
import funwayguy.bdsandm.items.ItemBarrel;
import funwayguy.bdsandm.items.ItemCrate;
import funwayguy.bdsandm.items.ItemShipping;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;
import java.util.List;

/*
    All the ugly registry stuff goes here.
 */

@Mod.EventBusSubscriber
public class RegEventHandler
{
    private static final List<Item> All_ITEMS = new ArrayList<>();
    private static final List<Block> ALL_BLOCKS = new ArrayList<>();
    private static final List<IRecipe> ALL_RECIPES = new ArrayList<>();
    
    private static boolean setupRecipes = false;
    
    @SubscribeEvent
    public static void registerBlockEvent(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(ALL_BLOCKS.toArray(new Block[0]));
    }
    
    @SubscribeEvent
    public static void registerItemEvent(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(All_ITEMS.toArray(new Item[0]));
    }
    
    @SubscribeEvent
    public static void registerRecipeEvent(RegistryEvent.Register<IRecipe> event)
    {
        if(!setupRecipes)
        {
            initRecipes();
        }
        
        event.getRegistry().registerAll(ALL_RECIPES.toArray(new IRecipe[0]));
    }
    
    private static void initRecipes()
    {
        setupRecipes = true;
        
        addShapedRecipe("wood_crate", "bdsm", new ItemStack(BDSM.blockWoodCrate), "WWW", "WFW", "WWW", 'W', "plankWood", 'F', new ItemStack(Items.ITEM_FRAME));
        addShapedRecipe("wood_barrel", "bdsm", new ItemStack(BDSM.blockWoodBarrel), "WWW", "WBW", "WWW", 'W', "plankWood", 'B', new ItemStack(Items.BUCKET));
        
        addShapedRecipe("metal_crate", "bdsm", new ItemStack(BDSM.blockMetalBarrel), "III", "ICI", "III", 'I', "ingotIron", 'C', new ItemStack(BDSM.blockWoodCrate));
        addShapedRecipe("wood_barrel", "bdsm", new ItemStack(BDSM.blockMetalBarrel), "III", "IBI", "III", 'I', "ingotIron", 'B', new ItemStack(BDSM.blockWoodBarrel));
        
        addShapedRecipe("shipping_c", "bdsm", new ItemStack(BDSM.blockShippingContainer), "III", "BCB", "III", 'I', "blockIron", 'B', new ItemStack(Blocks.IRON_BARS), 'C', new ItemStack(BDSM.blockMetalCrate));
        addShapedRecipe("shipping_b", "bdsm", new ItemStack(BDSM.blockShippingContainer), "III", "BCB", "III", 'I', "blockIron", 'B', new ItemStack(Blocks.IRON_BARS), 'C', new ItemStack(BDSM.blockMetalBarrel));
        
        addShapedRecipe("crate_key_c", "bdsm", new ItemStack(BDSM.itemKey), "II", "I ", "C ", 'I', "ingotIron", 'C', new ItemStack(BDSM.blockWoodCrate));
        addShapedRecipe("crate_key_b", "bdsm", new ItemStack(BDSM.itemKey), "II", "I ", "C ", 'I', "ingotIron", 'C', new ItemStack(BDSM.blockWoodBarrel));
        
        addShapedRecipe("color_tool_c", "bdsm", new ItemStack(BDSM.itemColor), "DDD", "DCD", "DDD", 'D', "dye", 'C', new ItemStack(BDSM.blockWoodCrate));
        addShapedRecipe("color_tool_b", "bdsm", new ItemStack(BDSM.itemColor), "DDD", "DCD", "DDD", 'D', "dye", 'C', new ItemStack(BDSM.blockWoodBarrel));
        
        addShapelessRecipe("upgrade_uninstall", "bdsm", new ItemStack(BDSM.itemUpgrade, 1, 7), new ItemStack(Items.ITEM_FRAME), new ItemStack(Blocks.CHEST));
        addShapelessRecipe("upgrade_ore_dict", "bdsm", new ItemStack(BDSM.itemUpgrade, 1, 5), new ItemStack(Items.ITEM_FRAME), new ItemStack(Blocks.CHEST), new ItemStack(Blocks.IRON_ORE));
        addShapelessRecipe("upgrade_void", "bdsm", new ItemStack(BDSM.itemUpgrade, 1, 6), new ItemStack(Items.ITEM_FRAME), new ItemStack(Blocks.CHEST), new ItemStack(Items.ENDER_PEARL));
        
        addShapelessRecipe("upgrade_64", "bdsm", new ItemStack(BDSM.itemUpgrade, 4, 0), new ItemStack(Items.ITEM_FRAME), new ItemStack(Blocks.CHEST), new ItemStack(Items.IRON_INGOT));
        addShapelessRecipe("upgrade_64_split", "bdsm", new ItemStack(BDSM.itemUpgrade, 4, 0), new ItemStack(BDSM.itemUpgrade, 1, 1));
        
        addShapelessRecipe("upgrade_256", "bdsm", new ItemStack(BDSM.itemUpgrade, 1, 1), new ItemStack(BDSM.itemUpgrade, 1, 0), new ItemStack(BDSM.itemUpgrade, 1, 0), new ItemStack(BDSM.itemUpgrade, 1, 0), new ItemStack(BDSM.itemUpgrade, 1, 0));
        addShapelessRecipe("upgrade_256_split", "bdsm", new ItemStack(BDSM.itemUpgrade, 4, 1), new ItemStack(BDSM.itemUpgrade, 1, 2));
        
        addShapelessRecipe("upgrade_1024", "bdsm", new ItemStack(BDSM.itemUpgrade, 1, 2), new ItemStack(BDSM.itemUpgrade, 1, 1), new ItemStack(BDSM.itemUpgrade, 1, 1), new ItemStack(BDSM.itemUpgrade, 1, 1), new ItemStack(BDSM.itemUpgrade, 1, 1));
        addShapelessRecipe("upgrade_1024_split", "bdsm", new ItemStack(BDSM.itemUpgrade, 4, 2), new ItemStack(BDSM.itemUpgrade, 1, 3));
        
        addShapelessRecipe("upgrade_4096", "bdsm", new ItemStack(BDSM.itemUpgrade, 1, 3), new ItemStack(BDSM.itemUpgrade, 1, 2), new ItemStack(BDSM.itemUpgrade, 1, 2), new ItemStack(BDSM.itemUpgrade, 1, 2), new ItemStack(BDSM.itemUpgrade, 1, 2));
    }
    
    public static void initContent()
    {
        regItem(BDSM.itemUpgrade, "upgrade");
        regItem(BDSM.itemKey, "crate_key");
        regItem(BDSM.itemColor, "color_tool");
        
        regBlock(BDSM.blockWoodCrate, new ItemCrate(BDSM.blockWoodCrate, 64, 1024), "wood_crate");
        regBlock(BDSM.blockWoodBarrel, new ItemBarrel(BDSM.blockWoodBarrel, 64, 1024), "wood_barrel");
        regBlock(BDSM.blockMetalCrate, new ItemCrate(BDSM.blockMetalCrate, 64, 1 << 15), "metal_crate");
        regBlock(BDSM.blockMetalBarrel, new ItemBarrel(BDSM.blockMetalBarrel, 64, 1 << 15), "metal_barrel");
        regBlock(BDSM.blockShippingContainer, new ItemShipping(BDSM.blockShippingContainer), "shipping_container");
        
        GameRegistry.registerTileEntity(TileEntityCrate.class, new ResourceLocation(BDSM.MOD_ID, "crate"));
        GameRegistry.registerTileEntity(TileEntityBarrel.class, new ResourceLocation(BDSM.MOD_ID, "barrel"));
        GameRegistry.registerTileEntity(TileEntityShipping.class, new ResourceLocation(BDSM.MOD_ID, "shipping"));
        
        Blocks.FIRE.setFireInfo(BDSM.blockWoodCrate, 5, 20);
        Blocks.FIRE.setFireInfo(BDSM.blockWoodBarrel, 5, 20);
    }
    
    private static void regBlock(Block block, String name)
    {
        regBlock(block, new ItemBlock(block), name);
    }
    
    private static void regBlock(Block block, ItemBlock item, String name)
    {
        ResourceLocation res = new ResourceLocation(BDSM.MOD_ID, name);
        ALL_BLOCKS.add(block.setRegistryName(res));
        All_ITEMS.add(item.setRegistryName(res));
    }
    
    private static void regItem(Item item, String name)
    {
        All_ITEMS.add(item.setRegistryName(new ResourceLocation(BDSM.MOD_ID, name)));
    }
    
    private static void addShapelessRecipe(String name, String group, ItemStack output, Object... ingredients)
    {
        addCustomRecipe(new ShapelessOreRecipe(new ResourceLocation(BDSM.MOD_ID, group), output, ingredients), name);
    }
    
    private static void addShapedRecipe(String name, String group, ItemStack output, Object... ingredients)
    {
        addCustomRecipe(new ShapedOreRecipe(new ResourceLocation(BDSM.MOD_ID, group), output, ingredients), name);
    }
    
    private static void addCustomRecipe(IRecipe recipe, String name)
    {
        ALL_RECIPES.add(recipe.setRegistryName(new ResourceLocation(BDSM.MOD_ID, name)));
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void initBlockColors(ColorHandlerEvent.Block event)
    {
        event.getBlockColors().registerBlockColorHandler(BlockContainerColor.INSTANCE, BDSM.blockMetalBarrel, BDSM.blockMetalCrate, BDSM.blockShippingContainer);
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void initItemColors(ColorHandlerEvent.Item event)
    {
        event.getItemColors().registerItemColorHandler(BlockContainerColor.INSTANCE, BDSM.blockMetalBarrel, BDSM.blockMetalCrate, BDSM.blockShippingContainer);
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModelEvent(ModelRegistryEvent event)
    {
        OBJLoaderColored.INSTANCE.addDomain(BDSM.MOD_ID);
        //OBJLoader.INSTANCE.addDomain(BDSM.MOD_ID);
        
        registerItemModel(BDSM.itemUpgrade, 0, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_64", "inventory"));
        registerItemModel(BDSM.itemUpgrade, 1, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_256", "inventory"));
        registerItemModel(BDSM.itemUpgrade, 2, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_1024", "inventory"));
        registerItemModel(BDSM.itemUpgrade, 3, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_4096", "inventory"));
        registerItemModel(BDSM.itemUpgrade, 4, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_creative", "inventory"));
        registerItemModel(BDSM.itemUpgrade, 5, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_ore", "inventory"));
        registerItemModel(BDSM.itemUpgrade, 6, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_void", "inventory"));
        registerItemModel(BDSM.itemUpgrade, 7, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_uninstall", "inventory"));
        
        registerItemModel(BDSM.itemKey);
        registerItemModel(BDSM.itemColor);
        
        registerBlockModel(BDSM.blockWoodCrate);
        registerBlockModel(BDSM.blockWoodBarrel);
        registerBlockModel(BDSM.blockMetalCrate);
        registerBlockModel(BDSM.blockMetalBarrel);
        registerBlockModel(BDSM.blockShippingContainer);
    
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrate.class, new TileEntityRenderCrate());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBarrel.class, new TileEntityRenderBarrel());
    }
    
    @SideOnly(Side.CLIENT)
    private static void registerBlockModel(Block block)
    {
        registerItemModel(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName().toString(), "normal"));
        registerItemModel(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName().toString(), "inventory"));
    }
    
    @SideOnly(Side.CLIENT)
    private static void registerBlockModel(Block block, int meta, ModelResourceLocation model)
    {
        registerItemModel(Item.getItemFromBlock(block), meta, model);
    }
    
    @SideOnly(Side.CLIENT)
    private static void registerItemModel(Item item)
    {
        registerItemModel(item, 0, new ModelResourceLocation(item.getRegistryName().toString(), "inventory"));
    }
    
    @SideOnly(Side.CLIENT)
    private static void registerItemModel(Item item, int meta, ModelResourceLocation model)
    {
        if(!model.getPath().equalsIgnoreCase(item.getRegistryName().getPath()))
        {
            ModelBakery.registerItemVariants(item, model);
        }
        
        ModelLoader.setCustomModelResourceLocation(item, meta, model);
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerBlockColors(IBlockColor blockColor, IItemColor itemColor, Block... blocks)
    {
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(blockColor, blocks);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(itemColor, blocks);
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerItemColors(IItemColor itemColor, Item... items)
    {
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(itemColor, items);
    }
}
