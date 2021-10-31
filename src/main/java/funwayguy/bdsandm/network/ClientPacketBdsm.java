package funwayguy.bdsandm.network;

import funwayguy.bdsandm.core.BdsmConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class ClientPacketBdsm
{
	protected CompoundNBT tags = new CompoundNBT();

	@SuppressWarnings("unused")
	public ClientPacketBdsm()
	{
	}

	public ClientPacketBdsm(CompoundNBT tags)
	{
		this.tags = tags;
	}

	public void encode(PacketBuffer buf) {
		buf.writeCompoundTag(tags);
	}

	public static ClientPacketBdsm decode(final PacketBuffer packetBuffer) {
		return new ClientPacketBdsm(packetBuffer.readCompoundTag());
	}

	public void handleClient(Supplier<Context> context) {
		Context ctx = context.get();
		ctx.enqueueWork(() -> {
			if (ctx.getDirection().getReceptionSide().isClient()) {
				int msgType = tags.getInt("msgType");

				if(msgType == 2) // Control Query
				{
					BlockPos pos = BlockPos.fromLong(tags.getLong("pos"));
					PlayerEntity player = Minecraft.getInstance().player;
					BlockState state = player.world.getBlockState(pos);
					Vector3d start = player.getEyePosition(1F);
					Vector3d end = player.getLook(1F);
					end = start.add(end.x * 6D, end.y * 6D, end.z * 6D);

					BlockRayTraceResult rtr = state.getRenderShape(player.world, pos).rayTrace(start, end, pos);
					Direction face = rtr == null ? Direction.DOWN : rtr.getFace();

					CompoundNBT tagRes = new CompoundNBT();
					tagRes.putInt("msgType", 2);
					tagRes.putLong("pos", tags.getLong("pos"));
					tagRes.putInt("face", face.getIndex());
					tagRes.putBoolean("isHit", tags.getBoolean("isHit"));
					tagRes.putBoolean("offHand", tags.getBoolean("offHand"));
					tagRes.putBoolean("altMode", BdsmConfig.CLIENT.alternateControls.get());
					tagRes.putInt("delay", BdsmConfig.CLIENT.doubleClickDelay.get());
					PacketHandler.NETWORK.sendToServer(new ServerPacketBdsm(tagRes));
				}
			}
		});
		ctx.setPacketHandled(true);
	}
}
