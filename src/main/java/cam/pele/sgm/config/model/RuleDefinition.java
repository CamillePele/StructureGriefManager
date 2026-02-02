package cam.pele.sgm.config.model;

import java.util.Collections;
import java.util.List;

public class RuleDefinition {
    public List<String> targets = Collections.emptyList();
    public RuleAction action = RuleAction.DENY;
    public Integer timer;
}
