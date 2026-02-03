package cam.pele.sgm.client;

import cam.pele.sgm.network.ClientBoundZoneSyncPacket;
import cam.pele.sgm.util.TargetSelector;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class ClientRuleCache {

    private static List<ClientBoundZoneSyncPacket.SyncedRule> deniedBreakRules = new ArrayList<>();
    private static List<ClientBoundZoneSyncPacket.SyncedRule> deniedPlaceRules = new ArrayList<>();

    public static void update(List<ClientBoundZoneSyncPacket.SyncedRule> breakList,
            List<ClientBoundZoneSyncPacket.SyncedRule> placeList) {
        deniedBreakRules = new ArrayList<>(breakList);
        deniedPlaceRules = new ArrayList<>(placeList);
    }

    public static boolean isBreakDenied(BlockState state, net.minecraft.world.entity.player.Player player) {
        if (player.isCreative())
            return false;

        for (ClientBoundZoneSyncPacket.SyncedRule rule : deniedBreakRules) {
            if (TargetSelector.matches(state, rule.targets(), rule.blacklist())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPlaceDenied(BlockState state, net.minecraft.world.entity.player.Player player) {
        if (player.isCreative())
            return false;

        for (ClientBoundZoneSyncPacket.SyncedRule rule : deniedPlaceRules) {
            if (TargetSelector.matches(state, rule.targets(), rule.blacklist())) {
                return true;
            }
        }
        return false;
    }
}
