package cam.pele.sgm.network;

import cam.pele.sgm.client.ClientRuleCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientBoundZoneSyncPacket {

    private final List<String> deniedTargets;

    public ClientBoundZoneSyncPacket(List<String> deniedTargets) {
        this.deniedTargets = deniedTargets;
    }

    public static void encode(ClientBoundZoneSyncPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.deniedTargets.size());
        for (String target : msg.deniedTargets) {
            buffer.writeUtf(target);
        }
    }

    public static ClientBoundZoneSyncPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<String> targets = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            targets.add(buffer.readUtf());
        }
        return new ClientBoundZoneSyncPacket(targets);
    }

    public static void handle(ClientBoundZoneSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client Side Execution
            // Client Side Execution
            ClientRuleCache.update(msg.deniedTargets);
        });
        ctx.get().setPacketHandled(true);
    }
}
