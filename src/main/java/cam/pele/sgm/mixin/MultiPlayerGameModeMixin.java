package cam.pele.sgm.mixin;

import cam.pele.sgm.client.ClientRuleCache;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    public void onUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult result,
            CallbackInfoReturnable<InteractionResult> cir) {
        // 1. Get held item
        ItemStack stack = player.getItemInHand(hand);

        // 2. Check if it's a BlockItem (we only filter block placement)
        if (stack.getItem() instanceof BlockItem blockItem) {
            // 3. Simulate the state that would be placed
            BlockState stateToCheck = blockItem.getBlock().defaultBlockState();

            // 4. Check SGM Cache (Bypass for Creative)
            if (ClientRuleCache.isPlaceDenied(stateToCheck, player)) {
                // 5. Block action purely
                // FAIL prevents packet sending to server
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }
}
