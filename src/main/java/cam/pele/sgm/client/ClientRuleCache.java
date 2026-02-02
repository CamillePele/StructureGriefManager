package cam.pele.sgm.client;

import cam.pele.sgm.util.TargetSelector;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class ClientRuleCache {

    private static List<String> currentDeniedTargets = new ArrayList<>();

    public static void update(List<String> targets) {
        currentDeniedTargets = new ArrayList<>(targets);
    }

    public static boolean isDenied(BlockState state) {
        if (currentDeniedTargets.isEmpty())
            return false;
        return TargetSelector.matches(state, currentDeniedTargets);
    }
}
