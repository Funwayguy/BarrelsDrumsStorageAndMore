package funwayguy.bdsandm.network;

import funwayguy.bdsandm.client.color.IBdsmColorBlock;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketBdsm implements IMessage
{
	private NBTTagCompound tags = new NBTTagCompound();
	
	@SuppressWarnings("unused")
	public PacketBdsm()
	{
	}
	
	public PacketBdsm(NBTTagCompound tags)
	{
		this.tags = tags;
	}
	
    @Override
    public void fromBytes(ByteBuf buf)
    {
		tags = ByteBufUtils.readTag(buf);
    }
    
    @Override
    public void toBytes(ByteBuf buf)
    {
		ByteBufUtils.writeTag(buf, tags);
    }
	
	public static class ServerHandler implements IMessageHandler<PacketBdsm,PacketBdsm>
	{
        @Override
        public PacketBdsm onMessage(PacketBdsm message, MessageContext ctx)
        {
            int msgType = message.tags.getInteger("msgType");
            
            if(msgType == 1) // Colour change request
            {
                World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(message.tags.getInteger("dim"));
                BlockPos pos = BlockPos.fromLong(message.tags.getLong("pos"));
                int[] colors = message.tags.getIntArray("color");
    
                IBlockState state = world.getBlockState(pos);
                if(state.getBlock() instanceof IBdsmColorBlock)
                {
                    ((IBdsmColorBlock)state.getBlock()).setColors(world, state, pos, colors);
                }
            }
            
            return null;
        }
    }
	
	@SideOnly(Side.CLIENT)
	public static class ClientHandler implements IMessageHandler<PacketBdsm,PacketBdsm>
	{
        @Override
        public PacketBdsm onMessage(PacketBdsm message, MessageContext ctx)
        {
            return null;
        }
    }
}
