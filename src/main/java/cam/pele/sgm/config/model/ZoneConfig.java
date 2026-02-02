package cam.pele.sgm.config.model;

import com.google.gson.annotations.SerializedName;

public class ZoneConfig {
    public String name = "Default Zone";
    public ZoneType type = ZoneType.STRUCTURE;

    /** Zone priority. Higher values are checked first. */
    public int priority = 0;

    @SerializedName("structure_whitelist")
    /**
     * List of structure ID regexes to include (e.g. "minecraft:.*village.*"). Match
     * ANY.
     */
    public java.util.List<String> structureWhitelist = new java.util.ArrayList<>();

    @SerializedName("structure_blacklist")
    /** List of structure ID regexes to exclude. Match ANY excludes the zone. */
    public java.util.List<String> structureBlacklist = new java.util.ArrayList<>();

    public Bounds bounds = new Bounds();
    public RuleSet rules = new RuleSet();

    @SerializedName("respawn_time")
    /** Override global respawn time for this zone in ticks. */
    public Integer respawnTime = null;

    @SerializedName("decay_time")
    /** Override global decay time for this zone in ticks. */
    public Integer decayTime = null;
}
