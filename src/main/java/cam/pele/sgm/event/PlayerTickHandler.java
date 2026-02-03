package cam.pele.sgm.event;

import cam.pele.sgm.SGM;
import cam.pele.sgm.config.model.RuleAction;
import cam.pele.sgm.config.model.RuleDefinition;
import cam.pele.sgm.config.model.ZoneConfig;
import cam.pele.sgm.logic.ZoneDetectionService;
import cam.pele.sgm.network.ClientBoundZoneSyncPacket;
import cam.pele.sgm.network.SgmNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = SGM.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side.isClient())
            return;

        // Optimization: Run once every second (20 ticks)
        if (event.player.tickCount % 20 != 0)
            return;

        ServerPlayer player = (ServerPlayer) event.player;
        Optional<ZoneConfig> activeZone = ZoneDetectionService.detectZone(player);

        List<ClientBoundZoneSyncPacket.SyncedRule> deniedBreakRules = new ArrayList<>();
        List<ClientBoundZoneSyncPacket.SyncedRule> deniedPlaceRules = new ArrayList<>();

        if (activeZone.isPresent()) {
            ZoneConfig zone = activeZone.get();
            if (zone.rules != null) {
                // Break Rules
                if (zone.rules.breakRules != null) {
                    for (RuleDefinition rule : zone.rules.breakRules) {
                        if (rule.action == RuleAction.DENY) {
                            deniedBreakRules
                                    .add(new ClientBoundZoneSyncPacket.SyncedRule(rule.targets, rule.blacklist));
                        }
                    }
                }
                // Place Rules
                if (zone.rules.placeRules != null) {
                    for (RuleDefinition rule : zone.rules.placeRules) {
                        if (rule.action == RuleAction.DENY) {
                            deniedPlaceRules
                                    .add(new ClientBoundZoneSyncPacket.SyncedRule(rule.targets, rule.blacklist));
                        }
                    }
                }
            }
        }

        // Send update packet to client
        SgmNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new ClientBoundZoneSyncPacket(deniedBreakRules, deniedPlaceRules));
    }
}
