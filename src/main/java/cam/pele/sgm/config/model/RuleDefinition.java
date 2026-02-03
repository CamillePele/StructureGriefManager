package cam.pele.sgm.config.model;

import java.util.Collections;
import java.util.List;

public class RuleDefinition {
    public List<String> targets = Collections.emptyList();
    public List<String> blacklist = new java.util.ArrayList<>();
    public RuleAction action = RuleAction.DENY;
    public Integer timer;
}
