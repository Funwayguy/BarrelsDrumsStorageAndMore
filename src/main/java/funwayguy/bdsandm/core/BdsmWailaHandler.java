package funwayguy.bdsandm.core;

import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.IBarrel;
import funwayguy.bdsandm.inventory.capability.ICrate;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.List;

public class BdsmWailaHandler implements IWailaDataProvider
{
    public static final BdsmWailaHandler INSTANCE = new BdsmWailaHandler();
    
    @Nonnull
    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if(accessor.getTileEntity() == null) return accessor.getStack();
        
        ICrate crate = accessor.getTileEntity().getCapability(BdsmCapabilies.CRATE_CAP, null);
        
        if(crate != null && !crate.getRefItem().isEmpty()) return crate.getRefItem();
        
        return accessor.getStack();
    }
    
    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        if(accessor.getTileEntity() == null) return tooltip;
        
        ICrate crate = accessor.getTileEntity().getCapability(BdsmCapabilies.CRATE_CAP, null);
        IBarrel barrel = accessor.getTileEntity().getCapability(BdsmCapabilies.BARREL_CAP, null);
        
        if(barrel != null && barrel.getRefFluid() != null)
        {
            int stackCap = barrel.getStackCap() >= 0 ? barrel.getStackCap() : (1 << 15);
            tooltip.add("Fluid: " + barrel.getRefFluid().getLocalizedName() + (barrel.isLocked() ? " [LOCKED]" : ""));
            tooltip.add("Amount: " + formatValue(barrel.getCount()) + " mB / " + formatValue(stackCap * 1000) + " mB");
            if(barrel.voidOverflow()|| barrel.getStackCap() < 0) tooltip.add("Upgrades:");
            if(barrel.getStackCap() < 0) tooltip.add("- Creative");
            if(barrel.voidOverflow()) tooltip.add("- Void Overflow");
        } else if(crate != null)
        {
            int stackCap = crate.getStackCap() >= 0 ? crate.getStackCap() : (1 << 15);
            if(crate.getRefItem().isEmpty())
            {
                tooltip.add("Item: " + "EMPTY" + (crate.isLocked() ? " [LOCKED]" : ""));
                tooltip.add("Amount: " + formatValue(crate.getCount()) + " / " + formatValue(stackCap * 64));
            } else
            {
                tooltip.add("Item: " + crate.getRefItem().getDisplayName() + (crate.isLocked() ? " [LOCKED]" : ""));
                tooltip.add("Amount: " + formatValue(crate.getCount()) + " / " + formatValue(stackCap * crate.getRefItem().getMaxStackSize()));
            }
            if(crate.voidOverflow() || crate.isOreDict() || crate.getStackCap() < 0) tooltip.add("Upgrades:");
            if(crate.getStackCap() < 0) tooltip.add("- Creative");
            if(crate.isOreDict()) tooltip.add("- Ore Dictionary");
            if(crate.voidOverflow()) tooltip.add("- Void Overflow");
        }
        return tooltip;
    }
    
    private static final DecimalFormat df = new DecimalFormat("0.##");
    private static final String[] suffixes = new String[]{"","K","M","B","T"};
    
    private static String formatValue(long value)
    {
        String s = "";
        double n = 1;
        
        for(int i = suffixes.length - 1; i >= 0; i--)
        {
            n = Math.pow(1000D, i);
            if(Math.abs(value) >= n)
            {
                s = suffixes[i];
                break;
            }
        }
        
        return df.format(value / n) + s;
    }
}
