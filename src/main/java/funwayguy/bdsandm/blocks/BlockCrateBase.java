package funwayguy.bdsandm.blocks;

import funwayguy.bdsandm.blocks.tiles.CrateTileEntity;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.ICrate;
import funwayguy.bdsandm.network.ServerPacketBdsm;
import funwayguy.bdsandm.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockCrateBase extends DirectionalBlock implements IStorageBlock
{
    private final int initCap;
    private final int maxCap;

    @SuppressWarnings("WeakerAccess")
    public BlockCrateBase(Properties properties, int initCap, int maxCap)
    {
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
        if(!(tile instanceof CrateTileEntity)) return;
        CrateTileEntity tileCrate = (CrateTileEntity)tile;

        ICrate crate = tile.getCapability(BdsmCapabilies.CRATE_CAP, null).orElse(null);
        if(crate == null || (!isHit && crate.installUpgrade(player, player.getHeldItem(hand)))) return;

        if(!isHit)
        {
            if(!player.isSneaking()) depositItem(crate, player, hand);
        } else
        {
            if(altMode)
            {
                withdrawItem(crate, player, player.isSneaking() ? 0 : 2);
            } else if(!player.isSneaking())
            {
                int curClick = tileCrate.getClickCount(world.getGameTime(), clickDelay);
                if(curClick >= 0) withdrawItem(crate, player, curClick);
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
        PacketHandler.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), new ServerPacketBdsm(tag));

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
        PacketHandler.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), new ServerPacketBdsm(tag));
    }

    private void depositItem(ICrate crate, PlayerEntity player, Hand hand)
    {
        ItemStack ref = crate.getRefItem();
        ItemStack held = player.getHeldItem(hand);

        if(!held.isEmpty() && (ref.isEmpty() || crate.canMergeWith(held) || held.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).isPresent()))
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

                    if(prev > 0 && (ref.isEmpty() || crate.canMergeWith(transStack)))
                    {
                        transStack = crate.insertItem(crate.getSlots() - 1, transStack, false);
                        if(transStack.getCount() != prev) heldCrate.extractItem(s, (prev - transStack.getCount()) / held.getCount(), false);
                    }
                }
            } else
            {
                player.setHeldItem(hand, crate.insertItem(crate.getSlots() - 1, held, false));
            }
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

    private void withdrawItem(ICrate crate, PlayerEntity player, int clickCount)
    {
        if(!crate.getRefItem().isEmpty())
        {
            int count = clickCount <= 0 ? 1 : crate.getRefItem().getMaxStackSize();
            if(clickCount == 1) count--;
            ItemStack out = crate.extractItem(0, count, false);
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
        return new CrateTileEntity(initCap, maxCap);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if(worldIn.isRemote) return;
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof CrateTileEntity) ((CrateTileEntity)tile).onCrateChanged();
    }

    @Nullable
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

    public void dropBlockAsItemWithChance(World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, float chance, int fortune)
    {
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)
    {
        TileEntity tile = worldIn.getTileEntity(pos);

        if(tile instanceof CrateTileEntity)
        {
            ((CrateTileEntity)tile).setCreativeBroken(player.abilities.isCreativeMode);
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof CrateTileEntity && !((CrateTileEntity)tileentity).isCreativeBroken())
        {
            CrateTileEntity tileBarrel = (CrateTileEntity)tileentity;
            ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
            ICrate tileCap = tileBarrel.getCapability(BdsmCapabilies.CRATE_CAP, null).orElse(null);
            ICrate itemCap = stack.getCapability(BdsmCapabilies.CRATE_CAP, null).orElse(null);
            assert tileCap != null;
            assert itemCap != null;
            itemCap.copyContainer(tileCap);

            spawnAsEntity(worldIn, pos, stack);
        }

        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, @Nonnull BlockState state)
    {
        ItemStack stack = super.getItem(worldIn, pos, state);
        TileEntity tileBarrel = worldIn.getTileEntity(pos);

        if(tileBarrel instanceof CrateTileEntity)
        {
            ICrate tileCap = tileBarrel.getCapability(BdsmCapabilies.CRATE_CAP, null).orElse(null);
            ICrate itemCap = stack.getCapability(BdsmCapabilies.CRATE_CAP, null).orElse(null);
            assert tileCap != null;
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

//    @Override
//    public boolean rotateBlock(World world, @Nonnull BlockPos pos, @Nonnull Direction axis)
//    {
//        if(world.isRemote) return super.rotate(world, pos, axis);
//
//        TileEntity tile = world.getTileEntity(pos);
//        boolean changed = super.rotate(world, pos, axis);
//        TileEntity nTile = world.getTileEntity(pos);
//
//        if(changed && tile instanceof CrateTileEntity && nTile instanceof CrateTileEntity)
//        {
//            //noinspection ConstantConditions
//            ICrate tileCap = tile.getCapability(BdsmCapabilies.CRATE_CAP, null).orElse(null);
//            ICrate nCap = nTile.getCapability(BdsmCapabilies.CRATE_CAP, null).orElse(null);
//            assert tileCap != null;
//            assert nCap != null;
//
//            nCap.copyContainer(tileCap);
//            ((CrateTileEntity)nTile).onCrateChanged();
//        }
//
//        return changed;
//    }

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

        if(tileBarrel instanceof CrateTileEntity)
        {
            ICrate tileCap = tileBarrel.getCapability(BdsmCapabilies.CRATE_CAP, null).orElse(null);
            assert tileCap != null;
            long max = tileCap.getStackCap() < 0 ? (1 << 15) : tileCap.getStackCap();
            max *= tileCap.getRefItem().getMaxStackSize();
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
