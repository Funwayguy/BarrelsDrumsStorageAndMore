package funwayguy.bdsandm.client;

import funwayguy.bdsandm.core.BDSM;
import funwayguy.bdsandm.core.BdsmConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;

public class GuiBdsmConfig extends GuiConfig
{
	public GuiBdsmConfig(GuiScreen parent)
	{
		super(parent, getCategories(BdsmConfig.getConfig()), BDSM.MOD_ID, false, false, BDSM.MOD_NAME);
	}
	
	private static List<IConfigElement> getCategories(Configuration config)
	{
		List<IConfigElement> cats = new ArrayList<>();
		
		for(String s : config.getCategoryNames())
		{
			cats.add(new ConfigElement(config.getCategory(s)));
		}
		
		return cats;
	}
}
