package cam.pele.sgm.network;

import cam.pele.sgm.client.ClientRuleCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientBoundZoneSyncPacket {

    public record SyncedRule(List<String> targets, List<String> blacklist) {
    }

    private final List<SyncedRule> breakRules;
    private final List<SyncedRule> placeRules;

    public ClientBoundZoneSyncPacket(List<SyncedRule> breakRules, List<SyncedRule> placeRules) {
        this.breakRules = breakRules;
        this.placeRules = placeRules;
    }

    public static void encode(ClientBoundZoneSyncPacket msg, FriendlyByteBuf buffer) {
        // Encode Break Rules
        buffer.writeInt(msg.breakRules.size());
        for (SyncedRule rule : msg.breakRules) {
            writeList(buffer, rule.targets);
            writeList(buffer, rule.blacklist);
        }
        // Encode Place Rules
        buffer.writeInt(msg.placeRules.size());
        for (SyncedRule rule : msg.placeRules) {
            writeList(buffer, rule.targets);
            writeList(buffer, rule.blacklist);
        }
    }

    private static void writeList(FriendlyByteBuf buffer, List<String> list) {
        if (list == null) {
            buffer.writeInt(0);
        } else {
            buffer.writeInt(list.size());
            for (String s : list) {
                buffer.writeUtf(s);
            }
        }
    }

    public static ClientBoundZoneSyncPacket decode(FriendlyByteBuf buffer) {
        // Decode Break Rules
        int breakSize = buffer.readInt();
        List<SyncedRule> breakRules = new ArrayList<>(breakSize);
        for (int i = 0; i < breakSize; i++) {
            List<String> targets = readList(buffer);
            List<String> blacklist = readList(buffer);
            breakRules.add(new SyncedRule(targets, blacklist));
        }

        // Decode Place Rules
        int placeSize = buffer.readInt();
        List<SyncedRule> placeRules = new ArrayList<>(placeSize);
        for (int i = 0; i < placeSize; i++) {
            List<String> targets = readList(buffer);
            List<String> blacklist = readList(buffer);
            placeRules.add(new SyncedRule(targets, blacklist));
        }

        return new ClientBoundZoneSyncPacket(breakRules, placeRules);
    }

    private static List<String> readList(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(buffer.readUtf());
        }
        return list;
    }

    public static void handle(ClientBoundZoneSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client Side Execution
            ClientRuleCache.update(msg.breakRules, msg.placeRules);
        });
        ctx.get().setPacketHandled(true);
    }
}
