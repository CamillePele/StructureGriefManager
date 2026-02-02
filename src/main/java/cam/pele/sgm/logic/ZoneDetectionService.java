package cam.pele.sgm.logic;

import cam.pele.sgm.config.SgmConfigManager;
import cam.pele.sgm.config.model.ZoneConfig;
import cam.pele.sgm.config.model.ZoneType;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.*;

public class ZoneDetectionService {

    public static Optional<ZoneConfig> detectZone(net.minecraft.server.level.ServerPlayer player) {
        return getActiveZone((ServerLevel) player.level(), player.blockPosition());
    }

    public static Optional<ZoneConfig> getActiveZone(ServerLevel level, BlockPos pos) {
        if (SgmConfigManager.CONFIG == null)
            return Optional.empty();

        List<ZoneConfig> candidateZones = new ArrayList<>();

        // 1. Check all configured zones
        for (ZoneConfig zone : SgmConfigManager.CONFIG.zones) {
            if (zone.type == ZoneType.CUSTOM) {
                // Phase A: Custom Zones
                if (zone.bounds != null && zone.bounds.contains(pos)) {
                    candidateZones.add(zone);
                }
            } else if (zone.type == ZoneType.STRUCTURE) {
                // Phase B: Structure Zones logic will be handled below to avoid repeated
                // structure lookups
                // We'll filter them later or check them now?
                // Efficient approach: If we have ANY structure zones, we query the structure
                // manager ONCE.
            }
        }

        // Optimization: Do we need to check structures?
        boolean hasStructureZones = SgmConfigManager.CONFIG.zones.stream()
                .anyMatch(z -> z.type == ZoneType.STRUCTURE);

        if (hasStructureZones) {
            Map<Structure, LongSet> structuresAt = level.structureManager().getAllStructuresAt(pos);
            if (structuresAt != null && !structuresAt.isEmpty()) {
                for (Map.Entry<Structure, LongSet> entry : structuresAt.entrySet()) {
                    Structure structure = entry.getKey();
                    ResourceLocation id = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                            .getKey(structure);

                    if (id != null) {
                        String idString = id.toString();

                        // Check against all STRUCTURE zones
                        for (ZoneConfig zone : SgmConfigManager.CONFIG.zones) {
                            if (zone.type == ZoneType.STRUCTURE) {
                                boolean matchesWhitelist = false;
                                if (zone.structureWhitelist != null && !zone.structureWhitelist.isEmpty()) {
                                    for (String regex : zone.structureWhitelist) {
                                        try {
                                            if (idString.matches(regex)) {
                                                matchesWhitelist = true;
                                                break;
                                            }
                                        } catch (java.util.regex.PatternSyntaxException e) {
                                            // Invalid regex, ignored
                                        }
                                    }
                                }

                                boolean matchesBlacklist = false;
                                if (zone.structureBlacklist != null) {
                                    for (String regex : zone.structureBlacklist) {
                                        try {
                                            if (idString.matches(regex)) {
                                                matchesBlacklist = true;
                                                break;
                                            }
                                        } catch (java.util.regex.PatternSyntaxException e) {
                                            // Invalid regex, ignored
                                        }
                                    }
                                }

                                if (matchesWhitelist && !matchesBlacklist) {
                                    candidateZones.add(zone);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Phase C: Resolution
        // Sort by Priority DESC
        candidateZones.sort((z1, z2) -> Integer.compare(z2.priority, z1.priority));

        if (candidateZones.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(candidateZones.get(0));
    }
}
