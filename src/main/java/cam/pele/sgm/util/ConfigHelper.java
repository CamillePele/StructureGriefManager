package cam.pele.sgm.util;

import cam.pele.sgm.config.SgmConfigManager;
import cam.pele.sgm.config.model.RuleAction;
import cam.pele.sgm.config.model.RuleDefinition;
import cam.pele.sgm.config.model.ZoneConfig;

public class ConfigHelper {

    public static int getTimer(RuleDefinition rule, ZoneConfig zone, RuleAction action) {
        // 1. Level 1: Specific Rule
        if (rule.timer != null) {
            return rule.timer;
        }

        // 2. Level 2 & 3: Zone Override or Global Default
        if (action == RuleAction.ALLOW_RESPAWN) {
            if (zone.respawnTime != null) {
                return zone.respawnTime;
            }
            if (SgmConfigManager.CONFIG != null && SgmConfigManager.CONFIG.settings != null) {
                return SgmConfigManager.CONFIG.settings.respawnTime;
            }
        } else if (action == RuleAction.ALLOW_DECAY) {
            if (zone.decayTime != null) {
                return zone.decayTime;
            }
            if (SgmConfigManager.CONFIG != null && SgmConfigManager.CONFIG.settings != null) {
                return SgmConfigManager.CONFIG.settings.decayTime;
            }
        }

        // 3. Safety Default
        return 200;
    }
}
