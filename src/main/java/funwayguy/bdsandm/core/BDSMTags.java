package funwayguy.bdsandm.core;

import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags.IOptionalNamedTag;

public class BDSMTags
{
	public static final IOptionalNamedTag<Item> UPGRADES = tag("upgrades");

	private static IOptionalNamedTag<Item> tag(String name)
	{
		return ItemTags.createOptional(new ResourceLocation(BDSM.MOD_ID, name));
	}
}
