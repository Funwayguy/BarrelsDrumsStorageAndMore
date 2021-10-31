package funwayguy.bdsandm.items;

import funwayguy.bdsandm.blocks.tiles.BarrelTileEntity;
import funwayguy.bdsandm.client.color.IBdsmColorItem;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.CapabilityBarrel;
import funwayguy.bdsandm.inventory.capability.CapabilityProviderBarrel;
import funwayguy.bdsandm.inventory.capability.IBarrel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;

public class BarrelItem extends BlockItem implements IBdsmColorItem
{
    private final int stackCap;
    private final int maxCap;
    
    public BarrelItem(Properties properties, Block block, int initCap, int maxCap)
    {
        super(block, properties);
        
        this.stackCap = initCap;
        this.maxCap = maxCap;
    }
    
    @Override
    public int getColorCount(ItemStack stack)
    {
        if(!stack.getCapability(BdsmCapabilies.BARREL_CAP, null).isPresent())
        {
            return 0;
        }
        
        return stack.getCapability(BdsmCapabilies.BARREL_CAP, null).orElse(null).getColorCount();
    }
    
    @Override
    public int[] getColors(ItemStack stack)
    {
        if(!stack.getCapability(BdsmCapabilies.BARREL_CAP, null).isPresent())
        {
            return new int[0];
        }
        
        return stack.getCapability(BdsmCapabilies.BARREL_CAP, null).orElse(null).getColors();
    }
    
    @Override
    public void setColors(ItemStack stack, int[] color)
    {
        if(!stack.getCapability(BdsmCapabilies.BARREL_CAP, null).isPresent())
        {
            return;
        }
        
        stack.getCapability(BdsmCapabilies.BARREL_CAP, null).orElse(null).setColors(color);
    }
    
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt)
    {
        CapabilityProviderBarrel capBarrel = new CapabilityProviderBarrel(stackCap, maxCap).setParentStack(stack);
        if(nbt != null) capBarrel.deserializeNBT(nbt);
        return capBarrel;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        CapabilityBarrel barrel = (CapabilityBarrel)stack.getCapability(BdsmCapabilies.BARREL_CAP, null).orElse(null);
        assert barrel != null;

        if(!barrel.getRefItem().isEmpty())
        {
            tooltip.add(new StringTextComponent("Item: " + barrel.getRefItem().getDisplayName().getString()));
            tooltip.add(new StringTextComponent("Amount: " + formatValue(barrel.getCount())));
        } else if(barrel.getRefFluid() != null)
        {
            tooltip.add(new StringTextComponent("Fluid: " + I18n.format(barrel.getRefFluid().getTranslationKey())));
            tooltip.add(new StringTextComponent("Amount: " + formatValue(barrel.getCount()) + " mB"));
        } else if(stack.getTag() != null && stack.getTag().contains("barrelCap", 10))
        {
            tooltip.add(new StringTextComponent("[EMPTY]"));
        }
    }

    @Nullable
    @Override
    public CompoundNBT getShareTag(ItemStack stack)
    {
        CapabilityBarrel barrel = (CapabilityBarrel)stack.getCapability(BdsmCapabilies.BARREL_CAP, null).orElse(null);
        assert barrel != null;

        stack.setTagInfo("barrelCap", barrel.serializeNBT());
        return super.getShareTag(stack);
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundNBT nbt)
    {
        super.readShareTag(stack, nbt);

        CapabilityBarrel barrel = (CapabilityBarrel)stack.getCapability(BdsmCapabilies.BARREL_CAP, null).orElse(null);
        assert barrel != null;

        barrel.deserializeNBT(stack.getOrCreateChildTag("barrelCap"));
    }
    
    @Override
    protected boolean placeBlock(BlockItemUseContext context, BlockState state)
    {
        if(!super.placeBlock(context, state)) return false;

        TileEntity tile = context.getWorld().getTileEntity(context.getPos());
        
        if(tile instanceof BarrelTileEntity && tile.getCapability(BdsmCapabilies.BARREL_CAP, null).isPresent())
        {
            IBarrel stackCap = context.getItem().getCapability(BdsmCapabilies.BARREL_CAP, null).orElse(null);
            assert stackCap != null;
            tile.getCapability(BdsmCapabilies.BARREL_CAP, null).orElse(null).copyContainer(stackCap);

            ((BarrelTileEntity)tile).onCrateChanged();
        }
        
        return true;
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
