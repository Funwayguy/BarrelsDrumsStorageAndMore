package funwayguy.bdsandm.blocks;

import funwayguy.bdsandm.blocks.tiles.BarrelTileEntity;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.IBarrel;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TNTBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

import static net.minecraft.block.FireBlock.AGE;

public class WoodBarrelBlock extends BlockBarrelBase
{
    public WoodBarrelBlock(Properties properties)
    {
        super(properties, 64, 1024);
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand)
    {
        TileEntity tile = worldIn.getTileEntity(pos);

        if(tile instanceof BarrelTileEntity)
        {
            IBarrel barrelCap = tile.getCapability(BdsmCapabilies.BARREL_CAP, null).orElse(null);
            if(barrelCap == null || barrelCap.getCount() <= 0 || barrelCap.getRefFluid() == null) return;
            if(barrelCap.getRefFluid().getFluid().getAttributes().getTemperature() > 500) // Roughly based on the wood ignition temperature in Kelvin
            {
                int j = -50;
                this.tryCatchFire(worldIn, pos.east(), 300 + j, rand, Direction.WEST);
                this.tryCatchFire(worldIn, pos.west(), 300 + j, rand, Direction.EAST);
                this.tryCatchFire(worldIn, pos.down(), 250 + j, rand, Direction.UP);
                this.tryCatchFire(worldIn, pos.up(), 250 + j, rand, Direction.DOWN);
                this.tryCatchFire(worldIn, pos.north(), 300 + j, rand, Direction.SOUTH);
                this.tryCatchFire(worldIn, pos.south(), 300 + j, rand, Direction.NORTH);

                worldIn.getPendingBlockTicks().scheduleTick(pos, this, 20 + rand.nextInt(10), TickPriority.NORMAL);
            }
        }
    }

    private void tryCatchFire(World worldIn, BlockPos pos, int chance, Random random, Direction face)
    {
        BlockState state = worldIn.getBlockState(pos);
        int i = worldIn.getBlockState(pos).getBlock().getFlammability(state, worldIn, pos, face);

        if (random.nextInt(chance) < i)
        {
            BlockState iblockstate = worldIn.getBlockState(pos);

            if (random.nextInt(10) < 5 && !worldIn.isRainingAt(pos))
            {
                int j = random.nextInt(5) / 4;

                if (j > 15)
                {
                    j = 15;
                }

                worldIn.setBlockState(pos, Blocks.FIRE.getDefaultState().with(AGE, j), 3);
            }
            else
            {
                worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
            }

            if (iblockstate.getBlock() == Blocks.TNT)
            {
                Blocks.TNT.onPlayerDestroy(worldIn, pos, iblockstate.with(TNTBlock.UNSTABLE, true));
            }
        }
    }

    @Override
    public boolean ticksRandomly(BlockState state)
    {
        return true;
    }


    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face)
    {
        return 5;
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face)
    {
        return 20;
    }

    @Override
    public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction face)
    {
        return true;
    }
}
