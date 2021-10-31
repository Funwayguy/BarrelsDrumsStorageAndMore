package funwayguy.bdsandm.blocks;

import funwayguy.bdsandm.client.color.IBdsmColorBlock;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.ICrate;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class MetalCrateBlock extends BlockCrateBase implements IBdsmColorBlock {
    public MetalCrateBlock(Properties properties)
    {
        super(properties, 64, 1 << 15);
    }
    
    @Override
    public int getColorCount(IBlockReader blockAccess, BlockState state, BlockPos pos)
    {
        TileEntity tile = blockAccess.getTileEntity(pos);
        
        if(tile == null || !tile.getCapability(BdsmCapabilies.CRATE_CAP, null).isPresent())
        {
            return 0;
        }
        
        return tile.getCapability(BdsmCapabilies.CRATE_CAP, null).orElse(null).getColorCount();
    }
    
    @Override
    public int[] getColors(IBlockReader blockAccess, BlockState state, BlockPos pos)
    {
        TileEntity tile = blockAccess.getTileEntity(pos);
        
        if(tile == null || !tile.getCapability(BdsmCapabilies.CRATE_CAP, null).isPresent())
        {
            return new int[0];
        }
        
        return tile.getCapability(BdsmCapabilies.CRATE_CAP, null).orElse(null).getColors();
    }
    
    @Override
    public void setColors(IBlockReader blockAccess, BlockState state, BlockPos pos, int[] color)
    {
        TileEntity tile = blockAccess.getTileEntity(pos);
        
        if(tile == null || !tile.getCapability(BdsmCapabilies.CRATE_CAP, null).isPresent())
        {
            return;
        }
        
        ICrate capCrate = tile.getCapability(BdsmCapabilies.CRATE_CAP, null).orElse(null);
        capCrate.setColors(color);
        tile.markDirty();
        capCrate.syncContainer();
    }
}
