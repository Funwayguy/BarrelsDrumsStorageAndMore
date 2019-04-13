package funwayguy.bdsandm.core.proxy;

import funwayguy.bdsandm.core.BDSM;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class TabBDSM extends CreativeTabs
{
    private ItemStack icon;
    
    public TabBDSM()
    {
        super(BDSM.MOD_ID);
    }
    
    @Override
    public ItemStack createIcon()
    {
        if(icon == null)
        {
            icon = new ItemStack(BDSM.blockWoodCrate);
        }
        
        return icon;
    }
}
