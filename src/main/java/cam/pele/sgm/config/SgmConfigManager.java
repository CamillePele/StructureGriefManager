package cam.pele.sgm.config;

import cam.pele.sgm.SGM;
import cam.pele.sgm.config.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class SgmConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("sgm.json");

    public static final String SCHEMA_FILENAME = "sgm.schema.json";
    private static final String SCHEMA_URL = "https://raw.githubusercontent.com/Pele/StructureGriefManager/main/src/generated/resources/"
            + SCHEMA_FILENAME;

    public static SgmConfig CONFIG;

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
                CONFIG = GSON.fromJson(reader, SgmConfig.class);
                if (CONFIG != null) {
                    CONFIG.schema = SCHEMA_URL;
                }
                SGM.LOGGER.info("Structure Grief Manager config loaded successfully.");
            } catch (IOException e) {
                SGM.LOGGER.error("Failed to load Structure Grief Manager config", e);
                CONFIG = createDefaultConfig(); // Fallback
                CONFIG.schema = SCHEMA_URL;
            }
        } else {
            SGM.LOGGER.info("Structure Grief Manager config not found, creating default.");
            CONFIG = createDefaultConfig();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(CONFIG, writer);
                SGM.LOGGER.info("Structure Grief Manager config saved.");
            }
        } catch (IOException e) {
            SGM.LOGGER.error("Failed to save Structure Grief Manager config", e);
        }
    }

    private static SgmConfig createDefaultConfig() {
        SgmConfig config = new SgmConfig();
        config.schema = SCHEMA_URL;

        // Settings default are already set in constructor

        // Add Example Zone
        ZoneConfig zone = new ZoneConfig();
        zone.name = "Example Zone";
        zone.type = ZoneType.STRUCTURE;
        zone.priority = 10;
        zone.structureWhitelist.add("minecraft:.*village.*");
        zone.respawnTime = 1200; // Example override

        // Rules
        RuleDefinition breakRule = new RuleDefinition();
        breakRule.targets = Collections.singletonList("#c:ores");
        breakRule.action = RuleAction.DENY;

        RuleDefinition placeRule = new RuleDefinition();
        placeRule.targets = Collections.singletonList("*");
        placeRule.action = RuleAction.ALLOW_DECAY;
        placeRule.timer = 200;

        zone.rules.breakRules.add(breakRule);
        zone.rules.placeRules.add(placeRule);

        config.zones.add(zone);

        return config;
    }
}
