package funwayguy.bdsandm.network;

import funwayguy.bdsandm.blocks.IStorageBlock;
import funwayguy.bdsandm.client.color.IBdsmColorBlock;
import funwayguy.bdsandm.core.BdsmConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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
            } else if(msgType == 2) // Control Response
            {
                EntityPlayerMP player = ctx.getServerHandler().player;
                BlockPos pos = BlockPos.fromLong(message.tags.getLong("pos"));
                IBlockState state = player.world.getBlockState(pos);
                EnumFacing face = EnumFacing.byIndex(message.tags.getInteger("face"));
                EnumHand hand = message.tags.getBoolean("offHand") ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
                boolean isHit = message.tags.getBoolean("isHit");
                boolean altMode = message.tags.getBoolean("altMode");
                int delay = message.tags.getInteger("delay");
                
                if(state.getBlock() instanceof IStorageBlock) ((IStorageBlock)state.getBlock()).onPlayerInteract(player.world, pos, state, face, player, hand, isHit, altMode, delay);
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
            int msgType = message.tags.getInteger("msgType");
            
            if(msgType == 2) // Control Query
            {
                BlockPos pos = BlockPos.fromLong(message.tags.getLong("pos"));
                EntityPlayer player = Minecraft.getMinecraft().player;
                IBlockState state = player.world.getBlockState(pos);
                Vec3d start = player.getPositionEyes(1F);
                Vec3d end = player.getLook(1F);
                end = start.add(end.x * 6D, end.y * 6D, end.z * 6D);
                RayTraceResult rtr = state.getSelectedBoundingBox(player.world, pos).calculateIntercept(start, end);
                EnumFacing face = rtr == null ? EnumFacing.DOWN : rtr.sideHit;
                
                NBTTagCompound tagRes = new NBTTagCompound();
                tagRes.setInteger("msgType", 2);
                tagRes.setLong("pos", message.tags.getLong("pos"));
                tagRes.setInteger("face", face.getIndex());
                tagRes.setBoolean("isHit", message.tags.getBoolean("isHit"));
                tagRes.setBoolean("offHand", message.tags.getBoolean("offHand"));
                tagRes.setBoolean("altMode", BdsmConfig.altControls);
                tagRes.setInteger("delay", BdsmConfig.dClickDelay);
                return new PacketBdsm(tagRes);
            }
            
            return null;
        }
    }
}
