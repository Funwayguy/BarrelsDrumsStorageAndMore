package funwayguy.bdsandm.core;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class TabBDSM extends ItemGroup
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
            icon = new ItemStack(BDSMRegistry.WOOD_CRATE.get());
        }
        
        return icon;
    }
}
