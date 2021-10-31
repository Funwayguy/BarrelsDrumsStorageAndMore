package funwayguy.bdsandm.core;

import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.IBarrel;
import funwayguy.bdsandm.inventory.capability.ICrate;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.text.DecimalFormat;
import java.util.List;

public class BdsmWailaHandler implements IComponentProvider
{
    public static final BdsmWailaHandler INSTANCE = new BdsmWailaHandler();

    @Override
    public ItemStack getStack(IDataAccessor accessor, IPluginConfig config) {
        if(accessor.getTileEntity() == null) return accessor.getStack();

        ICrate crate = accessor.getTileEntity().getCapability(BdsmCapabilies.CRATE_CAP, null).orElse(null);

        if(crate != null && !crate.getRefItem().isEmpty()) return crate.getRefItem();

        return accessor.getStack();
    }


    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config)
    {
        if(accessor.getTileEntity() == null) return;

        ICrate crate = accessor.getTileEntity().getCapability(BdsmCapabilies.CRATE_CAP, null).orElse(null);
        IBarrel barrel = accessor.getTileEntity().getCapability(BdsmCapabilies.BARREL_CAP, null).orElse(null);

        if(barrel != null && barrel.getRefFluid() != null)
        {
            int stackCap = barrel.getStackCap() >= 0 ? barrel.getStackCap() : (1 << 15);
            tooltip.add(new StringTextComponent("Fluid: " + I18n.format(barrel.getRefFluid().getTranslationKey()) + (barrel.isLocked() ? " [LOCKED]" : "")));
            tooltip.add(new StringTextComponent("Amount: " + formatValue(barrel.getCount()) + " mB / " + formatValue(stackCap * 1000) + " mB"));
            if(barrel.voidOverflow()|| barrel.getStackCap() < 0) tooltip.add(new StringTextComponent("Upgrades:"));
            if(barrel.getStackCap() < 0) tooltip.add(new StringTextComponent("- Creative"));
            if(barrel.voidOverflow()) tooltip.add(new StringTextComponent("- Void Overflow"));
        } else if(crate != null)
        {
            int stackCap = crate.getStackCap() >= 0 ? crate.getStackCap() : (1 << 15);
            if(crate.getRefItem().isEmpty())
            {
                tooltip.add(new StringTextComponent("Item: " + "EMPTY" + (crate.isLocked() ? " [LOCKED]" : "")));
                tooltip.add(new StringTextComponent("Amount: " + formatValue(crate.getCount()) + " / " + formatValue(stackCap * 64)));
            } else
            {
                tooltip.add(new StringTextComponent("Item: " + crate.getRefItem().getDisplayName().getString() + (crate.isLocked() ? " [LOCKED]" : "")));
                tooltip.add(new StringTextComponent("Amount: " + formatValue(crate.getCount()) + " / " + formatValue(stackCap * crate.getRefItem().getMaxStackSize())));
            }
            if(crate.voidOverflow() || crate.isOreDict() || crate.getStackCap() < 0) tooltip.add(new StringTextComponent("Upgrades:"));
            if(crate.getStackCap() < 0) tooltip.add(new StringTextComponent("- Creative"));
            if(crate.isOreDict()) tooltip.add(new StringTextComponent("- Ore Dictionary"));
            if(crate.voidOverflow()) tooltip.add(new StringTextComponent("- Void Overflow"));
        }
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
