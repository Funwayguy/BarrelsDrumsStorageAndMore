package funwayguy.bdsandm.blocks;

import funwayguy.bdsandm.blocks.tiles.TileEntityShipping;
import funwayguy.bdsandm.client.color.IBdsmColorBlock;
import funwayguy.bdsandm.core.BDSM;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockShippingContainer extends Block implements ITileEntityProvider, IBdsmColorBlock
{
    private static final PropertyInteger PROXY_IDX = PropertyInteger.create("index", 0, 7);
    private static final PropertyBool TURNED = PropertyBool.create("turned");
    private static boolean multiBreak = false; // Keeps the multiblock breaking from triggering more attempts cascading break attempts
    
    public BlockShippingContainer()
    {
        super(Material.IRON);
        
        this.setDefaultState(this.blockState.getBaseState().withProperty(PROXY_IDX, 0).withProperty(TURNED, false));
        this.setTranslationKey(BDSM.MOD_ID + ".shipping_container");
        this.setCreativeTab(BDSM.tabBdsm);
    }
    
    @Override
    public int getColorCount(IBlockAccess blockAccess, IBlockState state, BlockPos pos)
    {
        TileEntity tile = blockAccess.getTileEntity(pos);
        
        if(tile instanceof TileEntityShipping)
        {
            TileEntityShipping proxy = ((TileEntityShipping)tile).getProxyTile();
            if(proxy != null) return proxy.getColorCount();
        }
        
        return 0;
    }
    
    @Override
    public int[] getColors(IBlockAccess blockAccess, IBlockState state, BlockPos pos)
    {
        TileEntity tile = blockAccess.getTileEntity(pos);
        
        if(tile instanceof TileEntityShipping)
        {
            TileEntityShipping proxy = ((TileEntityShipping)tile).getProxyTile();
            if(proxy != null) return proxy.getColors();
        }
        
        return new int[0];
    }
    
    @Override
    public void setColors(IBlockAccess blockAccess, IBlockState state, BlockPos pos, int[] colors)
    {
        TileEntity tile = blockAccess.getTileEntity(pos);
        
        if(tile instanceof TileEntityShipping)
        {
            TileEntityShipping proxy = ((TileEntityShipping)tile).getProxyTile();
            if(proxy == null) return;
            
            proxy.setColors(colors);
            tile.markDirty();
            tile.getWorld().markBlockRangeForRenderUpdate(pos, pos);
        }
    }
    
	@Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing heldItem, float side, float hitX, float hitY)
    {
    	if(!world.isRemote)
    	{
    		player.openGui(BDSM.INSTANCE, 0, world, pos.getX(), pos.getY(), pos.getZ());
    	}
    	
        return true;
    }
    
    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state)
    {
        if(multiBreak)
        {
            super.breakBlock(worldIn, pos, state);
            return;
        }
        
        multiBreak = true;
        
        TileEntity tile = worldIn.getTileEntity(pos);
        
        if(tile instanceof TileEntityShipping)
        {
            InventoryHelper.dropInventoryItems(worldIn, pos, ((TileEntityShipping)tile).getContainerInvo());
        }
        
        int myIdx = state.getValue(PROXY_IDX);
        BlockPos startPos;
        
        switch(myIdx)
        {
            case 4:
                startPos = pos.add(-1, 0, 0);
                break;
            case 5:
                startPos = pos.add(-1, 0, -1);
                break;
            case 1:
                startPos = pos.add(0, 0, -1);
                break;
            case 0:
                startPos = pos;
                break;
            case 6:
                startPos = pos.add(-1, -1, 0);
                break;
            case 7:
                startPos = pos.add(-1, -1, -1);
                break;
            case 3:
                startPos = pos.add(0, -1, -1);
                break;
            case 2:
                startPos = pos.add(0, -1, 0);
                break;
            default:
                startPos = pos;
        }
        
        for(int i = 0; i < 2; i++)
        {
            for(int j = 0; j < 2; j++)
            {
                for(int k = 0; k < 2; k++)
                {
                    int idx = (i * 4) + (j * 2) + k;
                    
                    if(idx == myIdx)
                    {
                        continue;
                    }
                    
                    worldIn.setBlockToAir(startPos.add(i, j, k));
                }
            }
        }
        
        super.breakBlock(worldIn, pos, state);
        
        multiBreak = false;
    }
    
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        boolean turnIt = placer.getHorizontalFacing().getHorizontalIndex() % 2 == 0; // This really only matters index 0 and 4 but we set them all for consistency
        int myIdx = new int[]{4, 5, 1, 0}[placer.getHorizontalFacing().getHorizontalIndex()];
        BlockPos startPos;
        
        switch(myIdx)
        {
            case 4:
                startPos = pos.add(-1, 0, 0);
                break;
            case 5:
                startPos = pos.add(-1, 0, -1);
                break;
            case 1:
                startPos = pos.add(0, 0, -1);
                break;
            default:
                startPos = pos;
        }
        
        for(int i = 0; i < 2; i++)
        {
            for(int j = 0; j < 2; j++)
            {
                for(int k = 0; k < 2; k++)
                {
                    int idx = (i * 4) + (j * 2) + k;
                    
                    if(idx == myIdx)
                    {
                        continue;
                    }
                    
                    worldIn.setBlockState(startPos.add(i, j, k), this.getDefaultState().withProperty(PROXY_IDX, idx).withProperty(TURNED, turnIt));
                }
            }
        }
    }
	
    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        if(state.getValue(TURNED))
        {
            return state.getValue(PROXY_IDX) == 4 ? EnumBlockRenderType.MODEL : EnumBlockRenderType.INVISIBLE;
        } else
        {
            return state.getValue(PROXY_IDX) == 0 ? EnumBlockRenderType.MODEL : EnumBlockRenderType.INVISIBLE;
        }
    }
    
    @Nonnull
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
    
    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(PROXY_IDX) | (state.getValue(TURNED) ? 8 : 0);
    }
    
    @Nonnull
    @Override
    public IBlockState getStateForPlacement(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer, EnumHand hand)
    {
        return this.getDefaultState().withProperty(PROXY_IDX, new int[]{4, 5, 1, 0}[placer.getHorizontalFacing().getHorizontalIndex()]).withProperty(TURNED, placer.getHorizontalFacing().getHorizontalIndex() % 2 == 0);
    }
    
    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(PROXY_IDX, meta & 7).withProperty(TURNED, (meta & 8) == 8);
    }
    
    @Nonnull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, PROXY_IDX, TURNED);
    }
    
    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta)
    {
        return new TileEntityShipping(meta);
    }
}
