package funwayguy.bdsandm.core;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EventBusSubscriber
public class BdsmConfig
{
    private static final List<BlacklistEntry> blacklist = new ArrayList<>();
    private static final List<String> fluidBlacklist = new ArrayList<>();
    
    public static boolean multiPurposeBarrel = true;
    
    @SubscribeEvent
    public static void onConfigReload(ConfigChangedEvent event)
    {
        if(event.getModID().equals(BDSM.MOD_ID))
        {
            load();
            config.save();
        }
    }
    
    private static Configuration config;
    
    protected static void setConfig(File file)
    {
        config = new Configuration(file, true);
    }
    
    public static Configuration getConfig()
    {
        return config;
    }
    
    protected static void load()
    {
        if(config == null)
        {
            throw new NullPointerException("Attempted to load configuration file before it was initialised");
        }
        
        config.load();
        
        multiPurposeBarrel = config.getBoolean("Multi-Purpose Barrels", Configuration.CATEGORY_GENERAL, true, "Allows items to be placed in barrels. Disable to only permit fluids");
        
        blacklist.clear();
        for(String s : config.getStringList("Item Blacklist", Configuration.CATEGORY_GENERAL, new String[]{} , "Blacklist these items from being stored in barrels and crates"))
        {
            String[] split = s.split(":");
            
            try
            {
                if(split.length == 2)
                {
                    blacklist.add(new BlacklistEntry(new ResourceLocation(split[0], split[1])));
                } else if(split.length == 3)
                {
                    blacklist.add(new BlacklistEntry(new ResourceLocation(split[0], split[1]), Integer.parseInt(split[2])));
                }
            } catch(Exception e)
            {
                BDSM.logger.error("An error occured while parsing blacklist entry", e);
            }
        }
        
        fluidBlacklist.clear();
        Collections.addAll(fluidBlacklist, config.getStringList("Fluid Blacklist", Configuration.CATEGORY_GENERAL, new String[]{} , "Blacklist these fluids from being stored in barrels"));
        
        config.save();
    }
    
    public static boolean isBlacklisted(FluidStack fluid)
    {
        return fluid != null && fluidBlacklist.contains(fluid.getFluid().getName());
    }
    
    public static boolean isBlacklisted(ItemStack stack)
    {
        if(stack == null || stack.isEmpty())
        {
            return false;
        }
        
        for(BlacklistEntry entry : blacklist)
        {
            if(entry.id.equals(stack.getItem().getRegistryName()) && (entry.meta < 0 || entry.meta == stack.getItemDamage()))
            {
                return true;
            }
        }
        
        return false;
    }
    
    private static class BlacklistEntry
    {
        private final ResourceLocation id;
        private final int meta;
        
        private BlacklistEntry(@Nonnull ResourceLocation res)
        {
            this(res, -1);
        }
        
        private BlacklistEntry(@Nonnull ResourceLocation res, int meta)
        {
            this.id = res;
            this.meta = meta;
        }
    }
}
