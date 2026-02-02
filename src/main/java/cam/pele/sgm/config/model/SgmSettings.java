package cam.pele.sgm.config.model;

import com.google.gson.annotations.SerializedName;

public class SgmSettings {
    @SerializedName("tick_interval")
    public int tickInterval = 20;

    @SerializedName("debug_mode")
    public boolean debugMode = false;

    @SerializedName("respawn_time")
    /** Default time in ticks before a block respawns (if rule is ALLOW_RESPAWN). */
    public int respawnTime = 600; // 30 seconds

    @SerializedName("decay_time")
    /**
     * Default time in ticks before a temporary block decays (if rule is
     * ALLOW_DECAY).
     */
    public int decayTime = 200; // 10 seconds
}
