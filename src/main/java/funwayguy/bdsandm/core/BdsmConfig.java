package funwayguy.bdsandm.core;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EventBusSubscriber
public class BdsmConfig
{
    private static final List<ResourceLocation> blacklist = new ArrayList<>();
    private static final List<String> fluidBlacklist = new ArrayList<>();
    public static final List<String> oreDictBlacklist = new ArrayList<>();

    public static class Client
    {
        public final BooleanValue alternateControls;
        public final IntValue doubleClickDelay;

        Client(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Client settings")
                    .push("client");

            alternateControls = builder
                    .comment("Use the old sneak method to extract items (may interfere with block harvesting)")
                    .define("alternateControls", false);

            doubleClickDelay = builder
                    .comment("Maximum time in ticks between mouse clicks to be counted as a double click")
                    .defineInRange("doubleClickDelay", 5, 1, 20);

            builder.pop();
        }
    }


    public static class Common
    {
        public final BooleanValue multiPurposeBarrel;
        public final ConfigValue<List<? extends String>> blacklist;
        public final ConfigValue<List<? extends String>> fluidBlacklist;
        public final ConfigValue<List<? extends String>> oreDictBlacklist;

        Common(ForgeConfigSpec.Builder builder)
        {
            builder.comment("General settings")
                    .push("General");

            multiPurposeBarrel = builder
                    .comment("Allows items to be placed in barrels. Disable to only permit fluids")
                    .define("multiPurposeBarrel", true);

            blacklist = builder
                    .comment("Blacklist these items from being stored in barrels and crates")
                    .defineListAllowEmpty(Collections.singletonList("blacklist"), () -> Collections.singletonList(""), o -> (o instanceof String));

            fluidBlacklist = builder
                    .comment("Blacklist these fluids from being stored in barrels")
                    .defineListAllowEmpty(Collections.singletonList("fluidBlacklist"), () -> Collections.singletonList(""), o -> (o instanceof String));

            oreDictBlacklist = builder
                    .comment("Blacklists ore dictionary conversions (REGEX)")
                    .defineListAllowEmpty(Collections.singletonList("oreDictBlacklist"), () -> Collections.singletonList(""), o -> (o instanceof String));

            builder.pop();
        }
    }

    public static final ForgeConfigSpec clientSpec;
    public static final Client CLIENT;
    static
    {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static final ForgeConfigSpec serverSpec;
    public static final Common COMMON;

    static
    {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        serverSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        BDSM.LOGGER.debug("Loaded BDSM's config file {}", configEvent.getConfig().getFileName());
        load();
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.Reloading configEvent) {
        BDSM.LOGGER.debug("BDSM's config just got changed on the file system!");
        load();
    }
    
    protected static void load()
    {
        blacklist.clear();
        for(String s : COMMON.blacklist.get())
        {
            String[] split = s.split(":");
            
            try
            {
                if(split.length == 2)
                {
                    blacklist.add(new ResourceLocation(split[0], split[1]));
                }
            } catch(Exception e)
            {
                BDSM.LOGGER.error("An error occured while parsing blacklist entry", e);
            }
        }
        
        fluidBlacklist.clear();
        COMMON.fluidBlacklist.get().forEach((bl) -> fluidBlacklist.add(bl));

        oreDictBlacklist.clear();
        COMMON.oreDictBlacklist.get().forEach((bl) -> oreDictBlacklist.add(bl));
    }
    
    public static boolean isBlacklisted(FluidStack fluid)
    {
        return fluid != null && fluidBlacklist.contains(fluid.getFluid().getRegistryName());
    }
    
    public static boolean isBlacklisted(ItemStack stack)
    {
        if(stack == null || stack.isEmpty())
        {
            return false;
        }
        
        for(ResourceLocation entry : blacklist)
        {
            if(entry.equals(stack.getItem().getRegistryName()))
            {
                return true;
            }
        }
        
        return false;
    }
}
