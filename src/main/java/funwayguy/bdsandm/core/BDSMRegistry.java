package funwayguy.bdsandm.core;

import funwayguy.bdsandm.blocks.BlockShippingContainer;
import funwayguy.bdsandm.blocks.MetalBarrelBlock;
import funwayguy.bdsandm.blocks.MetalCrateBlock;
import funwayguy.bdsandm.blocks.WoodBarrelBlock;
import funwayguy.bdsandm.blocks.WoodCrateBlock;
import funwayguy.bdsandm.blocks.tiles.BarrelTileEntity;
import funwayguy.bdsandm.blocks.tiles.CrateTileEntity;
import funwayguy.bdsandm.blocks.tiles.ShippingTileEntity;
import funwayguy.bdsandm.inventory.ShippingContainer;
import funwayguy.bdsandm.items.BarrelItem;
import funwayguy.bdsandm.items.ColorToolItem;
import funwayguy.bdsandm.items.CrateItem;
import funwayguy.bdsandm.items.ShippingItem;
import funwayguy.bdsandm.items.UpgradeItem;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/*
    All the ugly registry stuff goes here.
 */

public class BDSMRegistry
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BDSM.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BDSM.MOD_ID);
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, BDSM.MOD_ID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, BDSM.MOD_ID);

    public static final RegistryObject<Block> WOOD_CRATE = BLOCKS.register("wood_crate", () ->
            new WoodCrateBlock(AbstractBlock.Properties.create(Material.WOOD).hardnessAndResistance(2.0F, 5.0F).notSolid()));
    public static final RegistryObject<Block> WOOD_BARREL = BLOCKS.register("wood_barrel", () ->
            new WoodBarrelBlock(AbstractBlock.Properties.create(Material.WOOD).hardnessAndResistance(2.0F, 5.0F).notSolid()));
    public static final RegistryObject<Block> METAL_CRATE = BLOCKS.register("metal_crate", () ->
            new MetalCrateBlock(AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(3.0F, 10.0F).notSolid()));
    public static final RegistryObject<Block> METAL_BARREL = BLOCKS.register("metal_barrel", () ->
            new MetalBarrelBlock(AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(3.0F, 10.0F).notSolid()));
    public static final RegistryObject<Block> SHIPPING_CONTAINER_BLOCK = BLOCKS.register("shipping_container", () ->
            new BlockShippingContainer(AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(3.0F, 10.0F).notSolid()));

    public static final RegistryObject<Item> UPGRADE_64 = ITEMS.register("upgrade_64", () -> new UpgradeItem(new Item.Properties().group(BDSM.tabBdsm), 64));
    public static final RegistryObject<Item> UPGRADE_256 = ITEMS.register("upgrade_256", () -> new UpgradeItem(new Item.Properties().group(BDSM.tabBdsm), 256));
    public static final RegistryObject<Item> UPGRADE_1024 = ITEMS.register("upgrade_1024", () -> new UpgradeItem(new Item.Properties().group(BDSM.tabBdsm), 1024));
    public static final RegistryObject<Item> UPGRADE_4096 = ITEMS.register("upgrade_4096", () -> new UpgradeItem(new Item.Properties().group(BDSM.tabBdsm), 4096));
    public static final RegistryObject<Item> UPGRADE_CREATIVE = ITEMS.register("upgrade_creative", () -> new UpgradeItem(new Item.Properties().group(BDSM.tabBdsm), Integer.MAX_VALUE));
    public static final RegistryObject<Item> UPGRADE_ORE = ITEMS.register("upgrade_ore", () -> new Item(new Item.Properties().group(BDSM.tabBdsm)));
    public static final RegistryObject<Item> UPGRADE_VOID = ITEMS.register("upgrade_void", () -> new Item(new Item.Properties().group(BDSM.tabBdsm)));
    public static final RegistryObject<Item> UPGRADE_UNINSTALL = ITEMS.register("upgrade_uninstall", () -> new Item(new Item.Properties().group(BDSM.tabBdsm)));

    public static final RegistryObject<Item> CRATE_KEY = ITEMS.register("crate_key", () -> new Item(new Item.Properties().group(BDSM.tabBdsm)));
    public static final RegistryObject<Item> COLOR_TOOL = ITEMS.register("color_tool", () -> new ColorToolItem(new Item.Properties().group(BDSM.tabBdsm)));

    public static final RegistryObject<Item> WOOD_CRATE_ITEM = ITEMS.register("wood_crate", () -> new CrateItem(new Item.Properties().group(BDSM.tabBdsm), WOOD_CRATE.get(), 64, 1024));
    public static final RegistryObject<Item> WOOD_BARREL_ITEM = ITEMS.register("wood_barrel", () -> new BarrelItem(new Item.Properties().group(BDSM.tabBdsm), WOOD_BARREL.get(), 64, 1024));
    public static final RegistryObject<Item> METAL_CRATE_ITEM = ITEMS.register("metal_crate", () -> new CrateItem(new Item.Properties().group(BDSM.tabBdsm), METAL_CRATE.get(), 64, 1 << 15));
    public static final RegistryObject<Item> METAL_BARREL_ITEM = ITEMS.register("metal_barrel", () -> new BarrelItem(new Item.Properties().group(BDSM.tabBdsm), METAL_BARREL.get(), 64, 1 << 15));
    public static final RegistryObject<Item> SHIPPING_CONTAINER_ITEM = ITEMS.register("shipping_container", () -> new ShippingItem(new Item.Properties().group(BDSM.tabBdsm), SHIPPING_CONTAINER_BLOCK.get()));


    public static final RegistryObject<TileEntityType<CrateTileEntity>> CRATE_TILE = TILES.register("crate", () -> TileEntityType.Builder.create(() ->
            new CrateTileEntity(), WOOD_CRATE.get(), METAL_CRATE.get()).build(null));
    public static final RegistryObject<TileEntityType<BarrelTileEntity>> BARREL_TILE = TILES.register("barrel", () -> TileEntityType.Builder.create(() ->
            new BarrelTileEntity(), WOOD_BARREL.get(), METAL_BARREL.get()).build(null));
    public static final RegistryObject<TileEntityType<ShippingTileEntity>> SHIPPING_TILE = TILES.register("shipping", () -> TileEntityType.Builder.create(() ->
            new ShippingTileEntity(), SHIPPING_CONTAINER_BLOCK.get()).build(null));

    public static final RegistryObject<ContainerType<ShippingContainer>> SHIPPING_CONTAINER = CONTAINERS.register("shipping", () ->
            IForgeContainerType.create((windowId, inv, data) -> new ShippingContainer(windowId, inv, data)));

    private static void initRecipes()
    {
//        addShapedRecipe("wood_crate", "bdsm", new ItemStack(BDSM.blockWoodCrate), "WWW", "WFW", "WWW", 'W', "plankWood", 'F', new ItemStack(Items.ITEM_FRAME));
//        addShapedRecipe("wood_barrel", "bdsm", new ItemStack(BDSM.blockWoodBarrel), "WWW", "WBW", "WWW", 'W', "plankWood", 'B', new ItemStack(Items.BUCKET));
//
//        addShapedRecipe("metal_crate", "bdsm", new ItemStack(BDSM.blockMetalCrate), "III", "ICI", "III", 'I', "ingotIron", 'C', new ItemStack(BDSM.blockWoodCrate, 1, OreDictionary.WILDCARD_VALUE));
//        addShapedRecipe("metal_barrel", "bdsm", new ItemStack(BDSM.blockMetalBarrel), "III", "IBI", "III", 'I', "ingotIron", 'B', new ItemStack(BDSM.blockWoodBarrel, 1, OreDictionary.WILDCARD_VALUE));
//
//        addShapedRecipe("shipping_c", "bdsm", new ItemStack(BDSM.blockShippingContainer), "III", "BCB", "III", 'I', "blockIron", 'B', new ItemStack(Blocks.IRON_BARS), 'C', new ItemStack(BDSM.blockMetalCrate, 1, OreDictionary.WILDCARD_VALUE));
//        addShapedRecipe("shipping_b", "bdsm", new ItemStack(BDSM.blockShippingContainer), "III", "BCB", "III", 'I', "blockIron", 'B', new ItemStack(Blocks.IRON_BARS), 'C', new ItemStack(BDSM.blockMetalBarrel, 1, OreDictionary.WILDCARD_VALUE));
//
//        addShapedRecipe("crate_key_c", "bdsm", new ItemStack(BDSM.itemKey), "II", "I ", "C ", 'I', "ingotIron", 'C', new ItemStack(BDSM.blockWoodCrate, 1, OreDictionary.WILDCARD_VALUE));
//        addShapedRecipe("crate_key_b", "bdsm", new ItemStack(BDSM.itemKey), "II", "I ", "C ", 'I', "ingotIron", 'C', new ItemStack(BDSM.blockWoodBarrel, 1, OreDictionary.WILDCARD_VALUE));
//
//        addShapedRecipe("color_tool_c", "bdsm", new ItemStack(BDSM.itemColor), "DDD", "DCD", "DDD", 'D', "dye", 'C', new ItemStack(BDSM.blockWoodCrate, 1, OreDictionary.WILDCARD_VALUE));
//        addShapedRecipe("color_tool_b", "bdsm", new ItemStack(BDSM.itemColor), "DDD", "DCD", "DDD", 'D', "dye", 'C', new ItemStack(BDSM.blockWoodBarrel, 1, OreDictionary.WILDCARD_VALUE));
//
//        addShapelessRecipe("upgrade_uninstall", "bdsm", new ItemStack(BDSM.itemUpgrade, 1, 7), new ItemStack(Items.ITEM_FRAME), new ItemStack(Blocks.CHEST));
//        addShapelessRecipe("upgrade_ore_dict", "bdsm", new ItemStack(BDSM.itemUpgrade, 1, 5), new ItemStack(Items.ITEM_FRAME), new ItemStack(Blocks.CHEST), new ItemStack(Blocks.IRON_ORE));
//        addShapelessRecipe("upgrade_void", "bdsm", new ItemStack(BDSM.itemUpgrade, 1, 6), new ItemStack(Items.ITEM_FRAME), new ItemStack(Blocks.CHEST), new ItemStack(Items.ENDER_PEARL));
//
//        addShapelessRecipe("upgrade_64", "bdsm", new ItemStack(BDSM.itemUpgrade, 4, 0), new ItemStack(Items.ITEM_FRAME), new ItemStack(Blocks.CHEST), new ItemStack(Items.IRON_INGOT));
//        addShapelessRecipe("upgrade_64_split", "bdsm", new ItemStack(BDSM.itemUpgrade, 4, 0), new ItemStack(BDSM.itemUpgrade, 1, 1));
//
//        addShapelessRecipe("upgrade_256", "bdsm", new ItemStack(BDSM.itemUpgrade, 1, 1), new ItemStack(BDSM.itemUpgrade, 1, 0), new ItemStack(BDSM.itemUpgrade, 1, 0), new ItemStack(BDSM.itemUpgrade, 1, 0), new ItemStack(BDSM.itemUpgrade, 1, 0));
//        addShapelessRecipe("upgrade_256_split", "bdsm", new ItemStack(BDSM.itemUpgrade, 4, 1), new ItemStack(BDSM.itemUpgrade, 1, 2));
//
//        addShapelessRecipe("upgrade_1024", "bdsm", new ItemStack(BDSM.itemUpgrade, 1, 2), new ItemStack(BDSM.itemUpgrade, 1, 1), new ItemStack(BDSM.itemUpgrade, 1, 1), new ItemStack(BDSM.itemUpgrade, 1, 1), new ItemStack(BDSM.itemUpgrade, 1, 1));
//        addShapelessRecipe("upgrade_1024_split", "bdsm", new ItemStack(BDSM.itemUpgrade, 4, 2), new ItemStack(BDSM.itemUpgrade, 1, 3));
//
//        addShapelessRecipe("upgrade_4096", "bdsm", new ItemStack(BDSM.itemUpgrade, 1, 3), new ItemStack(BDSM.itemUpgrade, 1, 2), new ItemStack(BDSM.itemUpgrade, 1, 2), new ItemStack(BDSM.itemUpgrade, 1, 2), new ItemStack(BDSM.itemUpgrade, 1, 2));
    }

//    private static void regBlock(Block block, String name)
//    {
//        regBlock(block, new BlockItem(block), name);
//    }
    
//    private static void regBlock(Block block, BlockItem item, String name)
//    {
//        ResourceLocation res = new ResourceLocation(BDSM.MOD_ID, name);
//        ALL_BLOCKS.add(block.setRegistryName(res));
//        All_ITEMS.add(item.setRegistryName(res));
//    }

//    private static void regItem(Item item, String name)
//    {
//        All_ITEMS.add(item.setRegistryName(new ResourceLocation(BDSM.MOD_ID, name)));
//    }
    
//    private static void addShapelessRecipe(String name, String group, ItemStack output, Object... ingredients)
//    {
//        addCustomRecipe(new ShapelessOreRecipe(new ResourceLocation(BDSM.MOD_ID, group), output, ingredients), name);
//    }
//
//    private static void addShapedRecipe(String name, String group, ItemStack output, Object... ingredients)
//    {
//        addCustomRecipe(new ShapedOreRecipe(new ResourceLocation(BDSM.MOD_ID, group), output, ingredients), name);
//    }
//
//    private static void addCustomRecipe(IRecipe recipe, String name)
//    {
//        ALL_RECIPES.add(recipe.setRegistryName(new ResourceLocation(BDSM.MOD_ID, name)));
//    }
//
//    @SubscribeEvent
//    @SideOnly(Side.CLIENT)
//    public static void initBlockColors(ColorHandlerEvent.Block event)
//    {
//        event.getBlockColors().registerBlockColorHandler(BlockContainerColor.INSTANCE, BDSM.blockMetalBarrel, BDSM.blockMetalCrate, BDSM.blockShippingContainer);
//    }
//
//    @SubscribeEvent
//    @SideOnly(Side.CLIENT)
//    public static void initItemColors(ColorHandlerEvent.Item event)
//    {
//        event.getItemColors().registerItemColorHandler(BlockContainerColor.INSTANCE, BDSM.blockMetalBarrel, BDSM.blockMetalCrate, BDSM.blockShippingContainer);
//    }
    
//    @SubscribeEvent
//    @SideOnly(Side.CLIENT)
//    public static void registerModelEvent(ModelRegistryEvent event)
//    {
//        OBJLoaderColored.INSTANCE.addDomain(BDSM.MOD_ID);
//        //OBJLoader.INSTANCE.addDomain(BDSM.MOD_ID);
//
//        registerItemModel(BDSM.itemUpgrade, 0, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_64", "inventory"));
//        registerItemModel(BDSM.itemUpgrade, 1, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_256", "inventory"));
//        registerItemModel(BDSM.itemUpgrade, 2, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_1024", "inventory"));
//        registerItemModel(BDSM.itemUpgrade, 3, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_4096", "inventory"));
//        registerItemModel(BDSM.itemUpgrade, 4, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_creative", "inventory"));
//        registerItemModel(BDSM.itemUpgrade, 5, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_ore", "inventory"));
//        registerItemModel(BDSM.itemUpgrade, 6, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_void", "inventory"));
//        registerItemModel(BDSM.itemUpgrade, 7, new ModelResourceLocation(BDSM.MOD_ID + ":upgrade_uninstall", "inventory"));
//
//        registerItemModel(BDSM.itemKey);
//        registerItemModel(BDSM.itemColor);
//
//        registerBlockModel(BDSM.blockWoodCrate);
//        registerBlockModel(BDSM.blockWoodBarrel);
//        registerBlockModel(BDSM.blockMetalCrate);
//        registerBlockModel(BDSM.blockMetalBarrel);
//        registerBlockModel(BDSM.blockShippingContainer);
//
//        ClientRegistry.bindTileEntitySpecialRenderer(CrateTileEntity.class, new TileEntityRenderCrate());
//        ClientRegistry.bindTileEntitySpecialRenderer(BarrelTileEntity.class, new TileEntityRenderBarrel());
//    }
//
//    @SideOnly(Side.CLIENT)
//    private static void registerBlockModel(Block block)
//    {
//        registerItemModel(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName().toString(), "normal"));
//        registerItemModel(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName().toString(), "inventory"));
//    }
//
//    @SideOnly(Side.CLIENT)
//    private static void registerBlockModel(Block block, int meta, ModelResourceLocation model)
//    {
//        registerItemModel(Item.getItemFromBlock(block), meta, model);
//    }
//
//    @SideOnly(Side.CLIENT)
//    private static void registerItemModel(Item item)
//    {
//        registerItemModel(item, 0, new ModelResourceLocation(item.getRegistryName().toString(), "inventory"));
//    }
//
//    @SideOnly(Side.CLIENT)
//    private static void registerItemModel(Item item, int meta, ModelResourceLocation model)
//    {
//        if(!model.getPath().equalsIgnoreCase(item.getRegistryName().getPath()))
//        {
//            ModelBakery.registerItemVariants(item, model);
//        }
//
//        ModelLoader.setCustomModelResourceLocation(item, meta, model);
//    }
//
//    @SideOnly(Side.CLIENT)
//    public static void registerBlockColors(IBlockColor blockColor, IItemColor itemColor, Block... blocks)
//    {
//        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(blockColor, blocks);
//        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(itemColor, blocks);
//    }
//
//    @SideOnly(Side.CLIENT)
//    public static void registerItemColors(IItemColor itemColor, Item... items)
//    {
//        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(itemColor, items);
//    }
}
