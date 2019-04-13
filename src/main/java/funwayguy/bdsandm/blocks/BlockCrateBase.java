package funwayguy.bdsandm.blocks;

import funwayguy.bdsandm.blocks.tiles.TileEntityCrate;
import funwayguy.bdsandm.core.BDSM;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.ICrate;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class BlockCrateBase extends BlockDirectional implements ITileEntityProvider
{
    private final int initCap;
    private final int maxCap;
    
    public BlockCrateBase(Material material, int initCap, int maxCap)
    {
        super(material);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setCreativeTab(BDSM.tabBdsm);
        
        this.initCap = initCap;
        this.maxCap = maxCap;
    }
    
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if(worldIn.isRemote) return true;
        
        TileEntity tile = worldIn.getTileEntity(pos);
        
        if(tile == null || !tile.hasCapability(BdsmCapabilies.CRATE_CAP, null)) return true;
        
        ICrate crate = tile.getCapability(BdsmCapabilies.CRATE_CAP, null);
        
        if(crate == null)
        {
            return true;
        } else if(!playerIn.isSneaking() && crate.installUpgrade(playerIn, playerIn.getHeldItem(hand)))
        {
            return true;
        } else
        {
            depositItem(crate, playerIn, hand);
        }
        
        return true;
    }
    
    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
        if(worldIn.isRemote) return;
        
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile == null || !tile.hasCapability(BdsmCapabilies.CRATE_CAP, null)) return;
        
        ICrate crate = tile.getCapability(BdsmCapabilies.CRATE_CAP, null);
        if(crate == null) return;
        
        withdrawItem(crate, playerIn);
    }
    
    private void depositItem(ICrate crate, EntityPlayer player, EnumHand hand)
    {
        ItemStack ref = crate.getRefItem();
        ItemStack held = player.getHeldItem(hand);
        
        if(!held.isEmpty() && (ref.isEmpty() || crate.canMergeWith(held)))
        {
            player.setHeldItem(hand, crate.insertItem(crate.getSlots() - 1, held, false));
        } else if(!ref.isEmpty()) // Insert all
        {
            for(int i = 0; i < player.inventory.getSizeInventory(); i++)
            {
                ItemStack invoStack = player.inventory.getStackInSlot(i);
                
                if(crate.canMergeWith(invoStack))
                {
                    invoStack = crate.insertItem(crate.getSlots() - 1, invoStack, false);
                    boolean done = !invoStack.isEmpty();
                    player.inventory.setInventorySlotContents(i, invoStack);
                    if(done) break;
                }
            }
            
        }
    }
    
    private void withdrawItem(ICrate crate, EntityPlayer player)
    {
        if(!crate.getRefItem().isEmpty())
        {
            ItemStack out = crate.extractItem(0, !player.isSneaking() ? 64 : 1, false);
            if(!player.addItemStackToInventory(out)) player.dropItem(out, true, false);
        }
    }
	
    @Override
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }
    
	@Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }
    
    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityCrate(initCap, maxCap);
    }
    
    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer).getOpposite());
    }
    
    @Override
    public int getMetaFromState(IBlockState state)
    {
        return (state.getValue(FACING)).getIndex();
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta & 7));
    }
    
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING);
    }
    
    // =v= DROP MODIFICATIONS =v=
    
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
    }
    
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        TileEntity tile = worldIn.getTileEntity(pos);
        
        if(tile instanceof TileEntityCrate)
        {
            ((TileEntityCrate)tile).setCreativeBroken(player.capabilities.isCreativeMode);
        }
    }
    
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof TileEntityCrate && !((TileEntityCrate)tileentity).isCreativeBroken())
        {
            TileEntityCrate tileBarrel = (TileEntityCrate)tileentity;
            ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
            ICrate tileCap = tileBarrel.getCapability(BdsmCapabilies.CRATE_CAP, null);
            ICrate itemCap = stack.getCapability(BdsmCapabilies.CRATE_CAP, null);
            itemCap.copyContainer(tileCap);
            
            spawnAsEntity(worldIn, pos, stack);
        }

        super.breakBlock(worldIn, pos, state);
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state)
    {
        ItemStack stack = super.getItem(worldIn, pos, state);
        TileEntity tileBarrel = worldIn.getTileEntity(pos);
        
        if(tileBarrel instanceof TileEntityCrate)
        {
            ICrate tileCap = tileBarrel.getCapability(BdsmCapabilies.CRATE_CAP, null);
            ICrate itemCap = stack.getCapability(BdsmCapabilies.CRATE_CAP, null);
    
            itemCap.copyContainer(tileCap);
        }
        
        return stack;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }
    
    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
    {
        if(world.isRemote) return super.rotateBlock(world, pos, axis);
        
        TileEntity tile = world.getTileEntity(pos);
        boolean changed = super.rotateBlock(world, pos, axis);
        TileEntity nTile = world.getTileEntity(pos);
        
        if(changed && tile instanceof TileEntityCrate && nTile instanceof TileEntityCrate)
        {
            nTile.getCapability(BdsmCapabilies.CRATE_CAP, null).copyContainer(tile.getCapability(BdsmCapabilies.CRATE_CAP, null));
            ((TileEntityCrate)nTile).onCrateChanged();
        }
        
        return changed;
    }
}
