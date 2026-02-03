package cam.pele.sgm.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TargetSelector {
    /**
     * Checks if a block state matches any of the target strings.
     * Supports:
     * - Wildcard: "*" or "ALL"
     * - Macros:
     * - @solid / @non_solid : Based on occlusion (visual solidity).
     * - @container : Blocks with TileEntities (Chests, Furnaces, etc.).
     * - @gravity : Falling blocks (Sand, Gravel).
     * - @redstone : Signal sources (Levers, Buttons).
     * - Tags: "#namespace:tag_name"
     * - Regex/IDs: "minecraft:.*_log"
     */
    /**
     * Checks if a block state matches the whitelist AND does NOT match the
     * blacklist.
     */
    public static boolean matches(BlockState state, List<String> targets, List<String> blacklist) {
        // 1. Must match target (Whitelist)
        if (!checkList(state, targets)) {
            return false;
        }

        // 2. Must NOT match blacklist (Exclusion)
        if (blacklist != null && !blacklist.isEmpty()) {
            if (checkList(state, blacklist)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Legacy/Client overload (No blacklist support yet).
     */
    public static boolean matches(BlockState state, List<String> targets) {
        return matches(state, targets, null);
    }

    /**
     * Internal logic to check a list of rules against a block state.
     */
    private static boolean checkList(BlockState state, List<String> targets) {
        if (targets == null || targets.isEmpty())
            return false;

        String id = ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();

        for (String target : targets) {
            if (target.startsWith("@")) {
                String macro = target.toLowerCase(java.util.Locale.ROOT);

                // 1. Solidité (Déco vs Structure)
                if (macro.equals("@non_solid")) {
                    if (!state.canOcclude())
                        return true;
                    continue;
                }
                if (macro.equals("@solid")) {
                    if (state.canOcclude())
                        return true;
                    continue;
                }

                // 2. Conteneurs (Stockage & Tile Entities)
                if (macro.equals("@container")) {
                    if (state.hasBlockEntity())
                        return true;
                    continue;
                }

                // 3. Gravité (Physique)
                if (macro.equals("@gravity")) {
                    if (state.getBlock() instanceof net.minecraft.world.level.block.FallingBlock)
                        return true;
                    continue;
                }

                // 4. Redstone (Mécanismes)
                if (macro.equals("@redstone")) {
                    if (state.isSignalSource())
                        return true;
                    continue;
                }

                // Unknown macro
                continue;
            }

            // Wildcard
            if (target.equals("*") || target.equalsIgnoreCase("ALL"))
                return true;

            // Tags
            if (target.startsWith("#")) {
                try {
                    ResourceLocation location = ResourceLocation.tryParse(target.substring(1));
                    TagKey<Block> tagKey = BlockTags.create(location);
                    if (state.is(tagKey))
                        return true;
                } catch (Exception e) {
                    // Ignore invalid resource location
                }
            } else {
                // Regex / ID
                try {
                    if (id.matches(target))
                        return true;
                } catch (PatternSyntaxException e) {
                    // If target is not a valid regex, check for exact match
                    if (id.equals(target))
                        return true;
                }
            }
        }
        return false;
    }
}
