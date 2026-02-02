package cam.pele.sgm.network;

import cam.pele.sgm.SGM;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class SgmNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.tryBuild(SGM.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, ClientBoundZoneSyncPacket.class, ClientBoundZoneSyncPacket::encode,
                ClientBoundZoneSyncPacket::decode, ClientBoundZoneSyncPacket::handle);
    }
}
