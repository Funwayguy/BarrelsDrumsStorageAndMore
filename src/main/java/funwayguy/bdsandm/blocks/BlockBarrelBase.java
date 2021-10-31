package funwayguy.bdsandm.blocks;

import funwayguy.bdsandm.blocks.tiles.BarrelTileEntity;
import funwayguy.bdsandm.core.BdsmConfig;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.CapabilityBarrel;
import funwayguy.bdsandm.inventory.capability.IBarrel;
import funwayguy.bdsandm.network.ClientPacketBdsm;
import funwayguy.bdsandm.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockBarrelBase extends DirectionalBlock implements IStorageBlock {
    private final int initCap;
    private final int maxCap;
    
    @SuppressWarnings("WeakerAccess")
    protected BlockBarrelBase(Properties properties, int initCap, int maxCap) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));

        this.initCap = initCap;
        this.maxCap = maxCap;
    }

    @Override
    public void onPlayerInteract(World world, BlockPos pos, BlockState state, Direction side, ServerPlayerEntity player, Hand hand, boolean isHit, boolean altMode, int clickDelay)
    {
        Direction curFace = state.get(FACING);
        if(curFace != side.getOpposite()) return;

        TileEntity tile = world.getTileEntity(pos);
        if(!(tile instanceof BarrelTileEntity)) return;
        BarrelTileEntity tileBarrel = (BarrelTileEntity)tile;

        CapabilityBarrel barrel = (CapabilityBarrel)tile.getCapability(BdsmCapabilies.BARREL_CAP, null).orElse(null);
        if(barrel == null || (!isHit && barrel.installUpgrade(player, player.getHeldItem(hand)))) return;

        if(!isHit)
        {
            if(!player.isSneaking())
            {
                IFluidHandlerItem container = player.getHeldItem(hand).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).orElse(null);
                if(!barrel.getRefFluid().isEmpty() && container != null && container.drain(Integer.MAX_VALUE, FluidAction.SIMULATE).isEmpty())
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
                int curClick = tileBarrel.getClickCount(world.getGameTime(), clickDelay);
                if(curClick >= 0) withdrawItem(barrel, player, curClick);
            }
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        if(worldIn.isRemote || !(player instanceof ServerPlayerEntity)) return ActionResultType.SUCCESS;

        CompoundNBT tag = new CompoundNBT();
        tag.putInt("msgType", 2);
        tag.putLong("pos", pos.toLong());
        tag.putBoolean("isHit", false);
        tag.putBoolean("offHand", handIn == Hand.OFF_HAND);
        PacketHandler.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), new ClientPacketBdsm(tag));

        return ActionResultType.SUCCESS;
    }

    @Override
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player)
    {
        if(worldIn.isRemote || !(player instanceof ServerPlayerEntity)) return;

        CompoundNBT tag = new CompoundNBT();
        tag.putInt("msgType", 2);
        tag.putLong("pos", pos.toLong());
        tag.putBoolean("isHit", true);
        tag.putBoolean("offHand", false);
        PacketHandler.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), new ClientPacketBdsm(tag));
    }

    private void depositItem(CapabilityBarrel barrel, PlayerEntity player, Hand hand)
    {
        ItemStack refItem = barrel.getRefItem();
        FluidStack refFluid = barrel.getRefFluid();
        ItemStack held = player.getHeldItem(hand);
        IFluidHandlerItem container = held.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).orElse(null);
        int maxDrain = barrel.getStackCap() < 0 ? Integer.MAX_VALUE : (barrel.getStackCap() * 1000 - (refFluid.isEmpty() ? 0 : barrel.getCount()));

        if(container != null && refItem.isEmpty() && !held.isEmpty()) // Fill fluid
        {
            FluidStack drainStack;
            
            if(refFluid.isEmpty())
            {
                drainStack = container.drain(maxDrain / held.getCount(), FluidAction.SIMULATE);
            } else
            {
                drainStack = refFluid.copy();
                drainStack.setAmount(maxDrain / held.getCount());
                drainStack = container.drain(drainStack, FluidAction.SIMULATE);
            }
            
            if(!drainStack.isEmpty() && drainStack.getAmount() > 0)
            {
                drainStack.setAmount(drainStack.getAmount() * held.getCount());
                drainStack.setAmount(barrel.fill(drainStack, FluidAction.EXECUTE));
                
                if(!player.abilities.isCreativeMode && drainStack.getAmount() > 0)
                {
                    drainStack.setAmount(drainStack.getAmount() / held.getCount());
                    container.drain(drainStack, FluidAction.EXECUTE);
                    
                    player.setHeldItem(hand, container.getContainer());
                }
                
                return;
            }
        }
        
        if(BdsmConfig.COMMON.multiPurposeBarrel.get())
        {
            if(!held.isEmpty() && (refItem.isEmpty() || barrel.canMergeWith(held) || held.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).isPresent()))
            {
                if(held.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).isPresent())
                {
                    IItemHandler heldCrate = held.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElse(null);
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
    
    private void withdrawItem(CapabilityBarrel barrel, PlayerEntity player, int clickCount)
    {
        ItemStack ref = barrel.getRefItem();
        FluidStack refFluid = barrel.getRefFluid();
        ItemStack held = player.getHeldItem(Hand.MAIN_HAND);
        IFluidHandlerItem container = held.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).orElse(null);
        int maxFill = barrel.getStackCap() < 0 ? Integer.MAX_VALUE : barrel.getCount();
        if(clickCount <= 0) maxFill = Math.min(1000, maxFill);
        
        if(container != null && refFluid != null && !held.isEmpty() && barrel.getCount() >= held.getCount())
        {
            FluidStack fillStack = refFluid.copy();
            fillStack.setAmount(maxFill / held.getCount());
            
            int testFill = container.fill(fillStack, FluidAction.SIMULATE); // Doesn't really matter if we overfill here. Just checking capacity and fluid match
            
            if(testFill > 0)
            {
                fillStack.setAmount(testFill * held.getCount());
                FluidStack drained = barrel.drain(fillStack, FluidAction.EXECUTE);
                if(drained != null)
                {
                    drained.setAmount(drained.getAmount() / held.getCount());
                    container.fill(drained, FluidAction.EXECUTE);
                    player.setHeldItem(Hand.MAIN_HAND, container.getContainer());
                }
            }
        } else if(!ref.isEmpty())
        {
            int count = clickCount <= 0 ? 1 : ref.getMaxStackSize();
            if(clickCount == 1) count--;
            ItemStack out = barrel.extractItem(0, count, false);
            if(player.getHeldItem(Hand.MAIN_HAND).isEmpty())
            {
                player.setHeldItem(Hand.MAIN_HAND, out);
            } else if(!player.addItemStackToInventory(out)) player.dropItem(out, false, false);
        }
    }
	
    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public BlockRenderType getRenderType(BlockState state)
    {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos)
    {
        return true;
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new BarrelTileEntity(initCap, maxCap);
    }
    
    @Nonnull
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }
    
    @Nonnull
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }
    
    // =v= DROP MODIFICATIONS =v=
    
//    @Override
//    public void dropBlockAsItemWithChance(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, float chance, int fortune)
//    {
//    }
    
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)
    {
        TileEntity tile = worldIn.getTileEntity(pos);
        
        if(tile instanceof BarrelTileEntity)
        {
            ((BarrelTileEntity)tile).setCreativeBroken(player.abilities.isCreativeMode);
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof BarrelTileEntity && !((BarrelTileEntity)tileentity).isCreativeBroken())
        {
            BarrelTileEntity tileBarrel = (BarrelTileEntity)tileentity;
            ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
            IBarrel tileCap = tileBarrel.getCapability(BdsmCapabilies.BARREL_CAP, null).orElse(null);
            IBarrel itemCap = stack.getCapability(BdsmCapabilies.BARREL_CAP, null).orElse(null);
            assert itemCap != null;
            itemCap.copyContainer(tileCap);
            
            spawnAsEntity(worldIn, pos, stack);
        }

        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state)
    {
        ItemStack stack = super.getItem(worldIn, pos, state);
        TileEntity tileBarrel = worldIn.getTileEntity(pos);

        if(tileBarrel instanceof BarrelTileEntity)
        {
            IBarrel tileCap = tileBarrel.getCapability(BdsmCapabilies.BARREL_CAP, null).orElse(null);
            IBarrel itemCap = stack.getCapability(BdsmCapabilies.BARREL_CAP, null).orElse(null);
            assert itemCap != null;
            itemCap.copyContainer(tileCap);
        }

        return stack;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(@Nonnull BlockState state, Rotation rot)
    {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }
    
    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(@Nonnull BlockState state, Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }
    
    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public boolean hasComparatorInputOverride(BlockState state)
    {
        return true;
    }
    
    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos)
    {
        TileEntity tileBarrel = worldIn.getTileEntity(pos);
        
        if(tileBarrel instanceof BarrelTileEntity)
        {
            CapabilityBarrel tileCap = (CapabilityBarrel)tileBarrel.getCapability(BdsmCapabilies.BARREL_CAP, null).orElse(null);
            assert tileCap != null;
            long max = tileCap.getStackCap() < 0 ? (1 << 15) : tileCap.getStackCap();
            max *= tileCap.getRefFluid() != null ? 1000L : tileCap.getRefItem().getMaxStackSize();
            double fill = tileCap.getCount() / (double)max;
            return MathHelper.floor(fill * 14D) + (tileCap.getCount() > 0 ? 1 : 0);
        }
        
        return 0;
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, PlacementType type, EntityType<?> entityType)
    {
        return true;
    }
}
