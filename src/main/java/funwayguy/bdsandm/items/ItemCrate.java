package funwayguy.bdsandm.items;

import funwayguy.bdsandm.blocks.tiles.TileEntityCrate;
import funwayguy.bdsandm.client.color.IBdsmColorItem;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.CapabilityProviderCrate;
import funwayguy.bdsandm.inventory.capability.ICrate;
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

public class ItemCrate extends ItemBlock implements IBdsmColorItem
{
    private final int stackCap;
    private final int maxCap;
    
    public ItemCrate(Block block, int initCap, int maxCap)
    {
        super(block);
        
        this.stackCap = initCap;
        this.maxCap = maxCap;
    }
    
    @Override
    public int getColorCount(ItemStack stack)
    {
        if(!stack.hasCapability(BdsmCapabilies.CRATE_CAP, null))
        {
            return 0;
        }
        
        return stack.getCapability(BdsmCapabilies.CRATE_CAP, null).getColorCount();
    }
    
    @Override
    public int[] getColors(ItemStack stack)
    {
        if(!stack.hasCapability(BdsmCapabilies.CRATE_CAP, null))
        {
            return new int[0];
        }
        
        return stack.getCapability(BdsmCapabilies.CRATE_CAP, null).getColors();
    }
    
    @Override
    public void setColors(ItemStack stack, int[] color)
    {
        if(!stack.hasCapability(BdsmCapabilies.CRATE_CAP, null))
        {
            return;
        }
        
        stack.getCapability(BdsmCapabilies.CRATE_CAP, null).setColors(color);
    }
    
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
    {
        CapabilityProviderCrate capProvCrate = new CapabilityProviderCrate(stackCap, maxCap).setParentStack(stack);
        if(nbt != null) capProvCrate.deserializeNBT(nbt);
        return capProvCrate;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
    
        ICrate crate = stack.getCapability(BdsmCapabilies.CRATE_CAP, null);
        assert crate != null;
        
        if(!crate.getRefItem().isEmpty())
        {
            tooltip.add("Item: " + crate.getRefItem().getDisplayName());
            tooltip.add("Amount: " + crate.getCount());
        } else
        {
            tooltip.add("[EMPTY]");
        }
    }
    
    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack)
    {
        ICrate crate = stack.getCapability(BdsmCapabilies.CRATE_CAP, null);
        assert crate != null;
        
        stack.setTagInfo("crateCap", crate.serializeNBT());
        return super.getNBTShareTag(stack);
    }
    
    @Override
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt)
    {
        super.readNBTShareTag(stack, nbt);
        
        ICrate crate = stack.getCapability(BdsmCapabilies.CRATE_CAP, null);
        assert crate != null;
        
        crate.deserializeNBT(stack.getOrCreateSubCompound("crateCap"));
    }
    
    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState newState)
    {
        if(!super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) return false;
        
        TileEntity tile = world.getTileEntity(pos);
        
        if(tile instanceof TileEntityCrate && tile.hasCapability(BdsmCapabilies.CRATE_CAP, null))
        {
            tile.getCapability(BdsmCapabilies.CRATE_CAP, null).copyContainer(stack.getCapability(BdsmCapabilies.CRATE_CAP, null));
            ((TileEntityCrate)tile).onCrateChanged();
        }
        
        return true;
    }
}
