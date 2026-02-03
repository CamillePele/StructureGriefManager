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
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

import java.util.*;

public class ZoneDetectionService {

    // Cache geometric des structures (WeakHashMap pour éviter les fuites mémoire
    // lors du déchargement)
    private static final WeakHashMap<StructureStart, List<BoundingBox>> GEOMETRY_CACHE = new WeakHashMap<>();

    private static boolean isInsideWithPadding(BoundingBox box, BlockPos pos, int padding) {
        return pos.getX() >= box.minX() - padding && pos.getX() <= box.maxX() + padding &&
                pos.getY() >= box.minY() - padding && pos.getY() <= box.maxY() + padding &&
                pos.getZ() >= box.minZ() - padding && pos.getZ() <= box.maxZ() + padding;
    }

    public static Optional<ZoneConfig> detectZone(net.minecraft.server.level.ServerPlayer player) {
        return getActiveZone((ServerLevel) player.level(), player.blockPosition());
    }

    public static Optional<ZoneConfig> getActiveZone(ServerLevel level, BlockPos pos) {
        if (SgmConfigManager.CONFIG == null)
            return Optional.empty();

        List<ZoneConfig> candidateZones = new ArrayList<>();

        // 1. Check all configured zones (Custom Type)
        for (ZoneConfig zone : SgmConfigManager.CONFIG.zones) {
            if (zone.type == ZoneType.CUSTOM) {
                // Phase A: Custom Zones
                if (zone.bounds != null && zone.bounds.contains(pos)) {
                    candidateZones.add(zone);
                }
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

                    if (id == null)
                        continue;
                    String idString = id.toString();

                    // Retrieve StructureStart ONCE (lazy)
                    StructureStart start = null;
                    boolean startRetrieved = false;

                    // Check against all STRUCTURE zones
                    for (ZoneConfig zone : SgmConfigManager.CONFIG.zones) {
                        if (zone.type != ZoneType.STRUCTURE)
                            continue;

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
                            // ID Verified - Now Check Geometry
                            if (!startRetrieved) {
                                start = level.structureManager().getStructureAt(pos, structure);
                                startRetrieved = true;
                            }

                            if (start != null && start.isValid()) {
                                // 1. GLOBAL FAST-FAIL
                                BoundingBox globalBox = start.getBoundingBox();
                                if (!isInsideWithPadding(globalBox, pos, zone.padding)) {
                                    continue;
                                }

                                // 2. PIECE-WISE CHECK WITH CACHE
                                final int currentPadding = zone.padding;
                                // Note: WeakHashMap uses StructureStart as key.
                                List<BoundingBox> paddedPieces = GEOMETRY_CACHE.computeIfAbsent(start, s -> {
                                    List<BoundingBox> boxes = new ArrayList<>();
                                    for (StructurePiece piece : s.getPieces()) {
                                        BoundingBox b = piece.getBoundingBox();
                                        boxes.add(new BoundingBox(
                                                b.minX() - currentPadding, b.minY() - currentPadding,
                                                b.minZ() - currentPadding,
                                                b.maxX() + currentPadding, b.maxY() + currentPadding,
                                                b.maxZ() + currentPadding));
                                    }
                                    return boxes;
                                });

                                // 3. Check pieces
                                boolean insidePiece = false;
                                for (BoundingBox bb : paddedPieces) {
                                    if (bb.isInside(pos)) {
                                        insidePiece = true;
                                        break;
                                    }
                                }

                                if (insidePiece) {
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
