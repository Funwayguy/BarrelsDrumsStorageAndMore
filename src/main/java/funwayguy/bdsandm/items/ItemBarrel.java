package funwayguy.bdsandm.items;

import funwayguy.bdsandm.blocks.tiles.TileEntityBarrel;
import funwayguy.bdsandm.client.color.IBdsmColorItem;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.CapabilityBarrel;
import funwayguy.bdsandm.inventory.capability.CapabilityProviderBarrel;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemBarrel extends ItemBlock implements IBdsmColorItem
{
    private final int stackCap;
    private final int maxCap;
    
    public ItemBarrel(Block block, int initCap, int maxCap)
    {
        super(block);
        
        this.stackCap = initCap;
        this.maxCap = maxCap;
    }
    
    @Override
    public int getColorCount(ItemStack stack)
    {
        if(!stack.hasCapability(BdsmCapabilies.BARREL_CAP, null))
        {
            return 0;
        }
        
        return stack.getCapability(BdsmCapabilies.BARREL_CAP, null).getColorCount();
    }
    
    @Override
    public int[] getColors(ItemStack stack)
    {
        if(!stack.hasCapability(BdsmCapabilies.BARREL_CAP, null))
        {
            return new int[0];
        }
        
        return stack.getCapability(BdsmCapabilies.BARREL_CAP, null).getColors();
    }
    
    @Override
    public void setColors(ItemStack stack, int[] color)
    {
        if(!stack.hasCapability(BdsmCapabilies.BARREL_CAP, null))
        {
            return;
        }
        
        stack.getCapability(BdsmCapabilies.BARREL_CAP, null).setColors(color);
    }
    
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
    {
        CapabilityProviderBarrel capBarrel = new CapabilityProviderBarrel(stackCap, maxCap).setParentStack(stack);
        if(nbt != null) capBarrel.deserializeNBT(nbt);
        return capBarrel;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
    
        CapabilityBarrel barrel = (CapabilityBarrel)stack.getCapability(BdsmCapabilies.BARREL_CAP, null);
        assert barrel != null;
        
        if(!barrel.getRefItem().isEmpty())
        {
            tooltip.add("Item: " + barrel.getRefItem().getDisplayName());
            tooltip.add("Amount: " + barrel.getCount());
        } else if(barrel.getRefFluid() != null)
        {
            tooltip.add("Fluid: " + barrel.getRefFluid().getLocalizedName());
            tooltip.add("Amount: " + barrel.getCount() + "mB");
        } else if(stack.getTagCompound() != null && stack.getTagCompound().hasKey("barrelCap", 10))
        {
            tooltip.add("[EMPTY]");
        }
    }
    
    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack)
    {
        CapabilityBarrel barrel = (CapabilityBarrel)stack.getCapability(BdsmCapabilies.BARREL_CAP, null);
        assert barrel != null;
        
        stack.setTagInfo("barrelCap", barrel.serializeNBT());
        return super.getNBTShareTag(stack);
    }
    
    @Override
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt)
    {
        super.readNBTShareTag(stack, nbt);
        
        CapabilityBarrel barrel = (CapabilityBarrel)stack.getCapability(BdsmCapabilies.BARREL_CAP, null);
        assert barrel != null;
        
        barrel.deserializeNBT(stack.getOrCreateSubCompound("barrelCap"));
    }
    
    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState newState)
    {
        if(!super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) return false;
        
        TileEntity tile = world.getTileEntity(pos);
        
        if(tile instanceof TileEntityBarrel && tile.hasCapability(BdsmCapabilies.BARREL_CAP, null))
        {
            tile.getCapability(BdsmCapabilies.BARREL_CAP, null).copyContainer(stack.getCapability(BdsmCapabilies.BARREL_CAP, null));
            ((TileEntityBarrel)tile).onCrateChanged();
        }
        
        return true;
    }
}
