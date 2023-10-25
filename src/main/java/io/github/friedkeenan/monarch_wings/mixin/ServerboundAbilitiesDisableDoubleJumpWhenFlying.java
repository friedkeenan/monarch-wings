package io.github.friedkeenan.monarch_wings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.friedkeenan.monarch_wings.DoubleJumper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerboundAbilitiesDisableDoubleJumpWhenFlying {
    @Shadow
    private ServerPlayer player;

    @Inject(at = @At("RETURN"), method = "handlePlayerAbilities")
    private void disableDoubleJumpWhenFlying(CallbackInfo info) {
        if (this.player.getAbilities().flying) {
            final var double_jumper = (DoubleJumper) this.player;

            double_jumper.setDoubleJumpEnabled(false);
        }
    }
}
