package cam.pele.sgm.logic;

import cam.pele.sgm.config.model.RuleAction;
import cam.pele.sgm.config.model.RuleDefinition;
import cam.pele.sgm.config.model.ZoneConfig;
import cam.pele.sgm.util.ConfigHelper;
import cam.pele.sgm.util.TargetSelector;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;

public class RuleEvaluationService {

    public static RuleResult evaluateBreak(ServerLevel level, @Nullable ServerPlayer player, BlockPos pos,
            BlockState state) {
        // Admin Bypass (only if player exists)
        if (player != null && canBypass(player)) {
            return RuleResult.ALLOWED;
        }

        // Zone Detection
        Optional<ZoneConfig> activeZone;
        if (player != null) {
            activeZone = ZoneDetectionService.detectZone(player);
        } else {
            activeZone = ZoneDetectionService.getActiveZone(level, pos);
        }

        if (activeZone.isEmpty())
            return RuleResult.ALLOWED;

        ZoneConfig zone = activeZone.get();
        if (zone.rules == null || zone.rules.breakRules == null)
            return RuleResult.ALLOWED;

        for (RuleDefinition rule : zone.rules.breakRules) {
            if (TargetSelector.matches(state, rule.targets)) {
                if (rule.action == RuleAction.DENY)
                    return RuleResult.DENIED;

                if (rule.action == RuleAction.ALLOW_RESPAWN) {
                    int timer = ConfigHelper.getTimer(rule, zone, RuleAction.ALLOW_RESPAWN);
                    return RuleResult.allowRespawn(timer);
                }

                if (rule.action == RuleAction.ALLOW_NO_DROP)
                    return RuleResult.ALLOW_NO_DROP;

                return RuleResult.ALLOWED;
            }
        }

        return RuleResult.ALLOWED;
    }

    public static RuleResult evaluatePlace(ServerLevel level, @Nullable ServerPlayer player, BlockPos pos,
            ItemStack stack) {
        if (player != null && canBypass(player))
            return RuleResult.ALLOWED;

        if (!(stack.getItem() instanceof BlockItem))
            return RuleResult.ALLOWED;
        BlockItem blockItem = (BlockItem) stack.getItem();
        BlockState state = blockItem.getBlock().defaultBlockState();

        // Zone Detection
        Optional<ZoneConfig> activeZone;
        if (player != null) {
            activeZone = ZoneDetectionService.detectZone(player);
        } else {
            activeZone = ZoneDetectionService.getActiveZone(level, pos);
        }

        if (activeZone.isEmpty())
            return RuleResult.ALLOWED;

        ZoneConfig zone = activeZone.get();
        if (zone.rules == null || zone.rules.placeRules == null)
            return RuleResult.ALLOWED;

        for (RuleDefinition rule : zone.rules.placeRules) {
            if (TargetSelector.matches(state, rule.targets)) {
                if (rule.action == RuleAction.DENY)
                    return RuleResult.DENIED;

                if (rule.action == RuleAction.ALLOW_DECAY) {
                    int timer = ConfigHelper.getTimer(rule, zone, RuleAction.ALLOW_DECAY);
                    return RuleResult.allowDecay(timer);
                }

                return RuleResult.ALLOWED;
            }
        }

        return RuleResult.ALLOWED;
    }

    private static boolean canBypass(ServerPlayer player) {
        return player.isCreative();
    }
}
