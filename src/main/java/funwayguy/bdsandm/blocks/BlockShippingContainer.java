package funwayguy.bdsandm.blocks;

import funwayguy.bdsandm.blocks.tiles.ShippingTileEntity;
import funwayguy.bdsandm.client.color.IBdsmColorBlock;
import funwayguy.bdsandm.inventory.InventoryShipping;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockShippingContainer extends Block implements IBdsmColorBlock {
    private static final IntegerProperty PROXY_IDX = IntegerProperty.create("index", 0, 7);
    private static final BooleanProperty TURNED = BooleanProperty.create("turned");
    private static boolean multiBreak = false; // Keeps the multiblock breaking from triggering more attempts cascading break attempts

    public BlockShippingContainer(Properties properties) {
        super(properties);

        this.setDefaultState(this.stateContainer.getBaseState().with(PROXY_IDX, 0).with(TURNED, false));
    }

    @Override
    public int getColorCount(IBlockReader blockAccess, BlockState state, BlockPos pos) {
        TileEntity tile = blockAccess.getTileEntity(pos);

        if (tile instanceof ShippingTileEntity) {
            ShippingTileEntity proxy = ((ShippingTileEntity) tile).getProxyTile();
            if (proxy != null) return proxy.getColorCount();
        }

        return 0;
    }

    @Override
    public int[] getColors(IBlockReader blockAccess, BlockState state, BlockPos pos) {
        TileEntity tile = blockAccess.getTileEntity(pos);

        if (tile instanceof ShippingTileEntity) {
            ShippingTileEntity proxy = ((ShippingTileEntity) tile).getProxyTile();
            if (proxy != null) return proxy.getColors();
        }

        return new int[0];
    }

    @Override
    public void setColors(IBlockReader blockAccess, BlockState state, BlockPos pos, int[] colors) {
        TileEntity tile = blockAccess.getTileEntity(pos);

        if (tile instanceof ShippingTileEntity) {
            ShippingTileEntity proxy = ((ShippingTileEntity) tile).getProxyTile();
            if (proxy == null) return;

            proxy.setColors(colors);
            tile.markDirty();
            tile.getWorld().markBlockRangeForRenderUpdate(pos, state, state);
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (!worldIn.isRemote && tileentity instanceof ShippingTileEntity) {
            NetworkHooks.openGui((ServerPlayerEntity) player, (ShippingTileEntity) tileentity, pos);
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (multiBreak) {
            super.onReplaced(state, worldIn, pos, newState, isMoving);
            return;
        }

        multiBreak = true;

        TileEntity tile = worldIn.getTileEntity(pos);

        if (tile instanceof ShippingTileEntity) {
            InventoryShipping invo = ((ShippingTileEntity) tile).getContainerInvo();
            if (invo != null) InventoryHelper.dropInventoryItems(worldIn, pos, invo);
        }

        int myIdx = state.get(PROXY_IDX);
        BlockPos startPos;

        switch (myIdx) {
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

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 2; k++) {
                    int idx = (i * 4) + (j * 2) + k;

                    if (idx == myIdx) {
                        continue;
                    }

                    worldIn.setBlockState(startPos.add(i, j, k), Blocks.AIR.getDefaultState());
                }
            }
        }

        super.onReplaced(state, worldIn, pos, newState, isMoving);

        multiBreak = false;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        boolean turnIt = placer.getHorizontalFacing().getHorizontalIndex() % 2 == 0; // This really only matters index 0 and 4 but we set them all for consistency
        int myIdx = new int[]{4, 5, 1, 0}[placer.getHorizontalFacing().getHorizontalIndex()];
        BlockPos startPos;

        switch (myIdx) {
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

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 2; k++) {
                    int idx = (i * 4) + (j * 2) + k;

                    if (idx == myIdx) {
                        continue;
                    }

                    worldIn.setBlockState(startPos.add(i, j, k), this.getDefaultState().with(PROXY_IDX, idx).with(TURNED, turnIt));
                }
            }
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public BlockRenderType getRenderType(BlockState state) {
        if (state.get(TURNED)) {
            return state.get(PROXY_IDX) == 4 ? BlockRenderType.MODEL : BlockRenderType.INVISIBLE;
        } else {
            return state.get(PROXY_IDX) == 0 ? BlockRenderType.MODEL : BlockRenderType.INVISIBLE;
        }
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos)
    {
        return true;
    }

    @Nonnull
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(PROXY_IDX, new int[]{4, 5, 1, 0}[context.getPlacementHorizontalFacing().getHorizontalIndex()]).with(TURNED, context.getPlacementHorizontalFacing().getHorizontalIndex() % 2 == 0);
    }

    @Nonnull
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(PROXY_IDX, TURNED);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ShippingTileEntity(state.get(PROXY_IDX));
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, PlacementType type, EntityType<?> entityType) {
        return true;
    }
}
