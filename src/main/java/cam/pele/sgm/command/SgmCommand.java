package cam.pele.sgm.command;

import cam.pele.sgm.SGM;
import cam.pele.sgm.config.SgmConfigManager;
import cam.pele.sgm.config.model.RuleDefinition;
import cam.pele.sgm.util.TargetSelector;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = SGM.MODID)
public class SgmCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sgm")
                .requires(source -> source.hasPermission(2)) // OP Level 2
                .then(Commands.literal("reload")
                        .executes(SgmCommand::reload))
                .then(Commands.literal("test")
                        .then(Commands.argument("block_id", ResourceLocationArgument.id())
                                .executes(SgmCommand::test)))
                .then(Commands.literal("info")
                        .executes(SgmCommand::info))
                .then(Commands.literal("memory")
                        .executes(SgmCommand::memory)));
    }

    private static int memory(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer)) {
            context.getSource().sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        var player = (net.minecraft.server.level.ServerPlayer) context.getSource().getEntity();
        var level = player.level();
        var chunk = level.getChunkAt(player.blockPosition());

        var cap = chunk.getCapability(cam.pele.sgm.data.SgmCapEvents.CHUNK_DATA);
        if (cap.isPresent()) {
            cap.ifPresent(data -> {
                int restoringCount = data.getRestoringBlocks().size();
                int decayingCount = data.getDecayingBlocks().size();
                context.getSource().sendSuccess(() -> Component.literal("§d[SGM] Chunk Memory:"), false);
                context.getSource().sendSuccess(() -> Component.literal("§7- Restoring Blocks: " + restoringCount),
                        false);
                context.getSource().sendSuccess(() -> Component.literal("§7- Decaying Blocks: " + decayingCount),
                        false);
            });
        } else {
            context.getSource().sendFailure(Component.literal("§c[SGM] Failed to access Chunk Data capability."));
        }

        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer)) {
            context.getSource().sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        var player = (net.minecraft.server.level.ServerPlayer) context.getSource().getEntity();
        var level = (net.minecraft.server.level.ServerLevel) player.level();
        var pos = player.blockPosition();

        var activeZoneOpt = cam.pele.sgm.logic.ZoneDetectionService.getActiveZone(level, pos);

        if (activeZoneOpt.isPresent()) {
            var zone = activeZoneOpt.get();
            context.getSource().sendSuccess(() -> Component.literal("§a[SGM] Active Zone:"), false);
            context.getSource().sendSuccess(() -> Component.literal("§e- Name: " + zone.name), false);
            context.getSource().sendSuccess(() -> Component.literal("§e- Type: " + zone.type), false);
            context.getSource().sendSuccess(() -> Component.literal("§e- Priority: " + zone.priority), false);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("§7[SGM] No active zone here."), false);
        }

        // Debug: Show actual structures at position
        var structuresAt = level.structureManager().getAllStructuresAt(pos);
        if (!structuresAt.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("§b[Debug] Structures at pos:"), false);
            structuresAt.keySet().forEach(structure -> {
                var id = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                        .getKey(structure);
                context.getSource().sendSuccess(() -> Component.literal("§7- " + (id != null ? id.toString() : "null")),
                        false);
            });
        }

        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        SgmConfigManager.load();
        context.getSource().sendSuccess(() -> Component.literal("SGM Config Reloaded"), true);
        return 1;
    }

    private static int test(CommandContext<CommandSourceStack> context) {
        ResourceLocation blockId = ResourceLocationArgument.getId(context, "block_id");
        Block block = ForgeRegistries.BLOCKS.getValue(blockId);

        if (block == null || blockId.equals(ForgeRegistries.BLOCKS.getKey(null))) { // Check if block exists (registry
                                                                                    // returns AIR for unknown usually?
                                                                                    // Or null?)
            // ForgeRegistries.BLOCKS.getValue returns AIR/default if missing? No, returns
            // value or default. Default is usually AIR.
            // But if specific ID requested and returns AIR, maybe check if ID matches AIR
            // ID.
            // Let's assume AIR is valid but unlikely as target for test if meant to check
            // specific block.
            // But let's check registry containsKey.
            if (!ForgeRegistries.BLOCKS.containsKey(blockId)) {
                context.getSource().sendFailure(Component.literal("Block not found: " + blockId));
                return 0;
            }
        }

        BlockState state = block.defaultBlockState();

        // Create a dummy target list for testing
        List<String> testTargets = Collections.singletonList("#minecraft:base_stone_overworld");
        boolean result = TargetSelector.matches(state, testTargets);

        context.getSource().sendSuccess(
                () -> Component
                        .literal("Test match for " + blockId + " against [#minecraft:base_stone_overworld]: " + result),
                false);

        return 1;
    }
}
