package funwayguy.bdsandm.blocks;

import funwayguy.bdsandm.blocks.tiles.TileEntityBarrel;
import funwayguy.bdsandm.core.BDSM;
import funwayguy.bdsandm.core.BdsmConfig;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.CapabilityBarrel;
import funwayguy.bdsandm.inventory.capability.IBarrel;
import funwayguy.bdsandm.network.PacketBdsm;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockBarrelBase extends BlockDirectional implements ITileEntityProvider, IStorageBlock
{
    private final int initCap;
    private final int maxCap;
    
    @SuppressWarnings("WeakerAccess")
    protected BlockBarrelBase(Material materialIn, int initCap, int maxCap)
    {
        super(materialIn);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setCreativeTab(BDSM.tabBdsm);
        
        this.initCap = initCap;
        this.maxCap = maxCap;
    }
    
    @Override
    public void onPlayerInteract(World world, BlockPos pos, IBlockState state, EnumFacing side, EntityPlayerMP player, EnumHand hand, boolean isHit, boolean altMode, int clickDelay)
    {
        EnumFacing curFace = state.getValue(FACING);
        if(curFace != side.getOpposite()) return;
        
        TileEntity tile = world.getTileEntity(pos);
        if(!(tile instanceof TileEntityBarrel)) return;
        TileEntityBarrel tileBarrel = (TileEntityBarrel)tile;
        
        CapabilityBarrel barrel = (CapabilityBarrel)tile.getCapability(BdsmCapabilies.BARREL_CAP, null);
        if(barrel == null || (!isHit && barrel.installUpgrade(player, player.getHeldItem(hand)))) return;
        
        if(!isHit)
        {
            if(!player.isSneaking())
            {
                 IFluidHandlerItem container = player.getHeldItem(hand).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if(barrel.getRefFluid() != null && container != null && container.drain(Integer.MAX_VALUE, false) == null)
                {
                    withdrawItem(barrel, player, 0);
                } else
                {
                    depositItem(barrel, player, hand);
                }
            }
        } else
        {
            if(altMode)
            {
                withdrawItem(barrel, player, player.isSneaking() ? 0 : 2);
            } else if(!player.isSneaking())
            {
                int curClick = tileBarrel.getClickCount(world.getTotalWorldTime(), clickDelay);
                if(curClick >= 0) withdrawItem(barrel, player, curClick);
            }
        }
    }
    
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if(worldIn.isRemote || !(playerIn instanceof EntityPlayerMP)) return true;
    
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("msgType", 2);
        tag.setLong("pos", pos.toLong());
        tag.setBoolean("isHit", false);
        tag.setBoolean("offHand", hand == EnumHand.OFF_HAND);
        BDSM.INSTANCE.network.sendTo(new PacketBdsm(tag), (EntityPlayerMP)playerIn);
        
        return true;
    }
    
    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
        if(worldIn.isRemote || !(playerIn instanceof EntityPlayerMP)) return;
    
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("msgType", 2);
        tag.setLong("pos", pos.toLong());
        tag.setBoolean("isHit", true);
        tag.setBoolean("offHand", false);
        BDSM.INSTANCE.network.sendTo(new PacketBdsm(tag), (EntityPlayerMP)playerIn);
    }
    
    private void depositItem(CapabilityBarrel barrel, EntityPlayer player, EnumHand hand)
    {
        ItemStack refItem = barrel.getRefItem();
        FluidStack refFluid = barrel.getRefFluid();
        ItemStack held = player.getHeldItem(hand);
        IFluidHandlerItem container = held.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        int maxDrain = barrel.getStackCap() < 0 ? Integer.MAX_VALUE : (barrel.getStackCap() * 1000 - (refFluid == null ? 0 : barrel.getCount()));
        
        if(container != null && refItem.isEmpty() && !held.isEmpty()) // Fill fluid
        {
            FluidStack drainStack;
            
            if(refFluid == null)
            {
                drainStack = container.drain(maxDrain / held.getCount(), false);
            } else
            {
                drainStack = refFluid.copy();
                drainStack.amount = maxDrain / held.getCount();
                drainStack = container.drain(drainStack, false);
            }
            
            if(drainStack != null && drainStack.amount > 0)
            {
                drainStack.amount *= held.getCount();
                drainStack.amount = barrel.fill(drainStack, true);
                
                if(!player.capabilities.isCreativeMode && drainStack.amount > 0)
                {
                    drainStack.amount /= held.getCount();
                    container.drain(drainStack, true);
                    
                    player.setHeldItem(hand, container.getContainer());
                }
                
                return;
            }
        }
        
        if(BdsmConfig.multiPurposeBarrel)
        {
            if(!held.isEmpty() && (refItem.isEmpty() || barrel.canMergeWith(held) || held.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)))
            {
                if(held.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
                {
                    IItemHandler heldCrate = held.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                    assert heldCrate != null;
                    
                    for(int s = 0; s < heldCrate.getSlots(); s++)
                    {
                        ItemStack transfer = heldCrate.extractItem(s, Integer.MAX_VALUE, true);
                    
                        ItemStack transStack = transfer.copy();
                        transStack.setCount(transStack.getCount() * held.getCount());
                        int prev = transStack.getCount();
                        
                        if(prev > 0 && (refItem.isEmpty() || barrel.canMergeWith(transStack)))
                        {
                            transStack = barrel.insertItem(barrel.getSlots() - 1, transStack, false);
                            if(transStack.getCount() != prev) heldCrate.extractItem(s, (prev - transStack.getCount()) / held.getCount(), false);
                        }
                    }
                } else
                {
                    player.setHeldItem(hand, barrel.insertItem(barrel.getSlots() - 1, held, false));
                }
            } else if(!refItem.isEmpty()) // Insert all
            {
                for(int i = 0; i < player.inventory.getSizeInventory(); i++)
                {
                    ItemStack invoStack = player.inventory.getStackInSlot(i);
            
                    if(barrel.canMergeWith(invoStack))
                    {
                        invoStack = barrel.insertItem(barrel.getSlots() - 1, invoStack, false);
                        boolean done = !invoStack.isEmpty();
                        player.inventory.setInventorySlotContents(i, invoStack);
                
                        if(done)
                        {
                            break;
                        }
                    }
                }
        
            }
        }
    }
    
    private void withdrawItem(CapabilityBarrel barrel, EntityPlayer player, int clickCount)
    {
        ItemStack ref = barrel.getRefItem();
        FluidStack refFluid = barrel.getRefFluid();
        ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);
        IFluidHandlerItem container = held.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        int maxFill = barrel.getStackCap() < 0 ? Integer.MAX_VALUE : barrel.getCount();
        if(clickCount <= 0) maxFill = Math.min(1000, maxFill);
        
        if(container != null && refFluid != null && !held.isEmpty() && barrel.getCount() >= held.getCount())
        {
            FluidStack fillStack = refFluid.copy();
            fillStack.amount = maxFill / held.getCount();
            
            int testFill = container.fill(fillStack, false); // Doesn't really matter if we overfill here. Just checking capacity and fluid match
            
            if(testFill > 0)
            {
                fillStack.amount = testFill * held.getCount();
                FluidStack drained = barrel.drain(fillStack, true);
                if(drained != null)
                {
                    drained.amount /= held.getCount();
                    container.fill(drained, true);
                    player.setHeldItem(EnumHand.MAIN_HAND, container.getContainer());
                }
            }
        } else if(!ref.isEmpty())
        {
            int count = clickCount <= 0 ? 1 : ref.getMaxStackSize();
            if(clickCount == 1) count--;
            ItemStack out = barrel.extractItem(0, count, false);
            if(player.getHeldItem(EnumHand.MAIN_HAND).isEmpty())
            {
                player.setHeldItem(EnumHand.MAIN_HAND, out);
            } else if(!player.addItemStackToInventory(out)) player.dropItem(out, false, false);
        }
    }
	
    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
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
    
    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta)
    {
        return new TileEntityBarrel(initCap, maxCap);
    }
    
    @Nonnull
    @Override
    public IBlockState getStateForPlacement(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer, EnumHand hand)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer).getOpposite());
    }
    
    @Override
    public int getMetaFromState(IBlockState state)
    {
        return (state.getValue(FACING)).getIndex();
    }
    
    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta & 7));
    }
    
    @Nonnull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING);
    }
    
    // =v= DROP MODIFICATIONS =v=
    
    @Override
    public void dropBlockAsItemWithChance(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, float chance, int fortune)
    {
    }
    
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        TileEntity tile = worldIn.getTileEntity(pos);
        
        if(tile instanceof TileEntityBarrel)
        {
            ((TileEntityBarrel)tile).setCreativeBroken(player.capabilities.isCreativeMode);
        }
    }
    
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof TileEntityBarrel && !((TileEntityBarrel)tileentity).isCreativeBroken())
        {
            TileEntityBarrel tileBarrel = (TileEntityBarrel)tileentity;
            ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
            IBarrel tileCap = tileBarrel.getCapability(BdsmCapabilies.BARREL_CAP, null);
            IBarrel itemCap = stack.getCapability(BdsmCapabilies.BARREL_CAP, null);
            assert itemCap != null;
            itemCap.copyContainer(tileCap);
            
            spawnAsEntity(worldIn, pos, stack);
        }

        super.breakBlock(worldIn, pos, state);
    }
    
    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public ItemStack getItem(World worldIn, BlockPos pos, @Nonnull IBlockState state)
    {
        ItemStack stack = super.getItem(worldIn, pos, state);
        TileEntity tileBarrel = worldIn.getTileEntity(pos);
        
        if(tileBarrel instanceof TileEntityBarrel)
        {
            IBarrel tileCap = tileBarrel.getCapability(BdsmCapabilies.BARREL_CAP, null);
            IBarrel itemCap = stack.getCapability(BdsmCapabilies.BARREL_CAP, null);
            assert itemCap != null;
            itemCap.copyContainer(tileCap);
        }
        
        return stack;
    }
    
    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState withRotation(@Nonnull IBlockState state, Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }
    
    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState withMirror(@Nonnull IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }
    
    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public boolean hasComparatorInputOverride(IBlockState state)
    {
        return true;
    }
    
    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos)
    {
        TileEntity tileBarrel = worldIn.getTileEntity(pos);
        
        if(tileBarrel instanceof TileEntityBarrel)
        {
            CapabilityBarrel tileCap = (CapabilityBarrel)tileBarrel.getCapability(BdsmCapabilies.BARREL_CAP, null);
            assert tileCap != null;
            long max = tileCap.getStackCap() < 0 ? (1 << 15) : tileCap.getStackCap();
            max *= tileCap.getRefFluid() != null ? 1000L : tileCap.getRefItem().getMaxStackSize();
            double fill = tileCap.getCount() / (double)max;
            return MathHelper.floor(fill * 14D) + (tileCap.getCount() > 0 ? 1 : 0);
        }
        
        return 0;
    }
    
    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, net.minecraft.entity.EntityLiving.SpawnPlacementType type)
    {
        return true;
    }
}
