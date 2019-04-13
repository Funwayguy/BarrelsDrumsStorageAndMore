package funwayguy.bdsandm.events;

import funwayguy.bdsandm.blocks.tiles.TileEntityShipping;
import funwayguy.bdsandm.client.GuiColour;
import funwayguy.bdsandm.client.GuiShipping;
import funwayguy.bdsandm.client.color.IBdsmColorBlock;
import funwayguy.bdsandm.inventory.ContainerShipping;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler
{
    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        
        if(ID == 0 && tile instanceof TileEntityShipping)
        {
            return new ContainerShipping(player.inventory, ((TileEntityShipping)tile).getContainerInvo());
        }
        
        return null;
    }
    
    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity tile = world.getTileEntity(pos);
        IBlockState state = world.getBlockState(pos);
        
        if(ID == 0 && tile instanceof TileEntityShipping)
        {
            return new GuiShipping(player.inventory, ((TileEntityShipping)tile).getContainerInvo());
        } else if(ID == 1 && state.getBlock() instanceof IBdsmColorBlock)
        {
            return new GuiColour((IBdsmColorBlock)state.getBlock(), world, pos);
        }
        
        return null;
    }
}
