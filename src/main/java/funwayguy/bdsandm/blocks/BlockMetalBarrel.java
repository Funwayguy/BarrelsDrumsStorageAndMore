package funwayguy.bdsandm.blocks;

import funwayguy.bdsandm.client.color.IBdsmColorBlock;
import funwayguy.bdsandm.core.BDSM;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.IBarrel;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockMetalBarrel extends BlockBarrelBase implements IBdsmColorBlock
{
    public BlockMetalBarrel()
    {
        super(Material.IRON, 64, 1 << 15);
        this.setHardness(3.0F).setResistance(10.0F);
        this.setTranslationKey(BDSM.MOD_ID + ".metal_barrel");
    }
    
    @Override
    public int getColorCount(IBlockAccess blockAccess, IBlockState state, BlockPos pos)
    {
        TileEntity tile = blockAccess.getTileEntity(pos);
        
        if(tile == null || !tile.hasCapability(BdsmCapabilies.BARREL_CAP, null))
        {
            return 0;
        }
        
        return tile.getCapability(BdsmCapabilies.BARREL_CAP, null).getColorCount();
    }
    
    @Override
    public int[] getColors(IBlockAccess blockAccess, IBlockState state, BlockPos pos)
    {
        TileEntity tile = blockAccess.getTileEntity(pos);
        
        if(tile == null || !tile.hasCapability(BdsmCapabilies.BARREL_CAP, null))
        {
            return new int[0];
        }
        
        return tile.getCapability(BdsmCapabilies.BARREL_CAP, null).getColors();
    }
    
    @Override
    public void setColors(IBlockAccess blockAccess, IBlockState state, BlockPos pos, int[] color)
    {
        TileEntity tile = blockAccess.getTileEntity(pos);
        
        if(tile == null || !tile.hasCapability(BdsmCapabilies.BARREL_CAP, null))
        {
            return;
        }
    
        IBarrel capBarrel = tile.getCapability(BdsmCapabilies.BARREL_CAP, null);
        capBarrel.setColors(color);
        capBarrel.syncContainer();
    }
}