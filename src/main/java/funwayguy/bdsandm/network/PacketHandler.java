package funwayguy.bdsandm.network;

import funwayguy.bdsandm.core.BDSM;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;

public class PacketHandler
{
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(BDSM.MOD_ID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);


	private static int id = 0;

	public static void init(){
		NETWORK.registerMessage(id++, ClientPacketBdsm.class, ClientPacketBdsm::encode, ClientPacketBdsm::decode, ClientPacketBdsm::handleClient);
		NETWORK.registerMessage(id++, ServerPacketBdsm.class, ServerPacketBdsm::encode, ServerPacketBdsm::decode, ServerPacketBdsm::handleServer);
	}
}
