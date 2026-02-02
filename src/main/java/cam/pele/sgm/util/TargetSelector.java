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
    public static boolean matches(BlockState state, List<String> targets) {
        if (targets == null || targets.isEmpty())
            return false;

        String id = ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();

        for (String target : targets) {
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
