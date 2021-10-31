package funwayguy.bdsandm.items;

import net.minecraft.item.Item;

public class UpgradeItem extends Item
{
	public final int value;
	public UpgradeItem(Properties properties, int value)
	{
		super(properties);
		this.value = value;
	}
}
