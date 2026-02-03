package cam.pele.sgm.network;

import cam.pele.sgm.client.ClientRuleCache;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundDecayUpdatePacket {

    private final BlockPos pos;
    private final boolean isDecaying;

    public ClientBoundDecayUpdatePacket(BlockPos pos, boolean isDecaying) {
        this.pos = pos;
        this.isDecaying = isDecaying;
    }

    public ClientBoundDecayUpdatePacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.isDecaying = buffer.readBoolean();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeBoolean(isDecaying);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Client-side handling
            ClientRuleCache.setDecaying(pos, isDecaying);
        });
        return true;
    }
}
