package cam.pele.sgm.config.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class RuleSet {
    @SerializedName("break")
    public List<RuleDefinition> breakRules = new ArrayList<>();

    @SerializedName("place")
    public List<RuleDefinition> placeRules = new ArrayList<>();
}
