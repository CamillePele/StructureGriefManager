package cam.pele.sgm.config.model;

import java.util.ArrayList;
import java.util.List;

public class SgmConfig {
    @com.google.gson.annotations.SerializedName("$schema")
    public String schema;

    public SgmSettings settings = new SgmSettings();
    public List<ZoneConfig> zones = new ArrayList<>();
}
