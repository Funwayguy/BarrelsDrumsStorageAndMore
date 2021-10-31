package funwayguy.bdsandm.network;

import funwayguy.bdsandm.blocks.IStorageBlock;
import funwayguy.bdsandm.client.color.IBdsmColorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class ServerPacketBdsm
{
	protected CompoundNBT tags = new CompoundNBT();
	
	@SuppressWarnings("unused")
	public ServerPacketBdsm()
	{
	}
	
	public ServerPacketBdsm(CompoundNBT tags)
	{
		this.tags = tags;
	}

	public void encode(PacketBuffer buf) {
		buf.writeCompoundTag(tags);
	}

	public static ServerPacketBdsm decode(final PacketBuffer packetBuffer) {
		return new ServerPacketBdsm(packetBuffer.readCompoundTag());
	}

	public void handleServer(Supplier<Context> context) {
		NetworkEvent.Context ctx = context.get();
		ctx.enqueueWork(() -> {
			if (ctx.getDirection().getReceptionSide().isServer() && ctx.getSender() != null) {
				ServerPlayerEntity player = ctx.getSender();
				int msgType = tags.getInt("msgType");
				BlockPos pos = BlockPos.fromLong(tags.getLong("pos"));

				if(msgType == 1) // Colour change request
				{
					ResourceLocation dimLocation = ResourceLocation.tryCreate(tags.getString("dim"));
					if(dimLocation != null) {
						RegistryKey<World> worldKey = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, dimLocation);
						World world = player.getServer().getWorld(worldKey);
						int[] colors = tags.getIntArray("color");

						BlockState state = world.getBlockState(pos);
						if(state.getBlock() instanceof IBdsmColorBlock)
						{
							((IBdsmColorBlock)state.getBlock()).setColors(world, state, pos, colors);
						}
					}

				} else if(msgType == 2) // Control Response
				{
					BlockState state = player.world.getBlockState(pos);
					Direction face = Direction.byIndex(tags.getInt("face"));
					Hand hand = tags.getBoolean("offHand") ? Hand.OFF_HAND : Hand.MAIN_HAND;
					boolean isHit = tags.getBoolean("isHit");
					boolean altMode = tags.getBoolean("altMode");
					int delay = tags.getInt("delay");

					if(state.getBlock() instanceof IStorageBlock) ((IStorageBlock)state.getBlock()).onPlayerInteract(player.world, pos, state, face, player, hand, isHit, altMode, delay);
				}
			}
		});
		ctx.setPacketHandled(true);
	}
}
