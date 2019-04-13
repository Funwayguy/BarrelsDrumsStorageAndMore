package funwayguy.bdsandm.items;

import funwayguy.bdsandm.core.BDSM;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Collections;

public class ItemUpgrade extends Item
{
    public ItemUpgrade()
    {
        this.setTranslationKey(BDSM.MOD_ID + ".upgrade");
        this.setHasSubtypes(true);
        this.setCreativeTab(BDSM.tabBdsm);
    }
    
    @Nonnull
    @Override
    public String getTranslationKey(ItemStack stack)
    {
        switch(stack.getItemDamage()%8)
        {
            case 0:
                return this.getTranslationKey() + "_64";
            case 1:
                return this.getTranslationKey() + "_256";
            case 2:
                return this.getTranslationKey() + "_1024";
            case 3:
                return this.getTranslationKey() + "_4096";
            case 4:
                return this.getTranslationKey() + "_creative";
            case 5:
                return this.getTranslationKey() + "_ore";
            case 6:
                return this.getTranslationKey() + "_void";
            case 7:
                return this.getTranslationKey() + "_uninstall";
        }
        
        return this.getTranslationKey();
    }
    
    private ItemStack[] subItems;
    
    @Override
	@SideOnly(Side.CLIENT)
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list)
    {
        if(this.isInCreativeTab(tab))
        {
            if(subItems == null)
            {
                subItems = new ItemStack[8];
                
                for(int i = 0; i < 8; i++)
                {
                    subItems[i] = new ItemStack(this, 1, i);
                }
            }
            
            Collections.addAll(list, subItems);
        }
    }
}
