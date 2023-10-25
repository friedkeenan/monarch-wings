package io.github.friedkeenan.monarch_wings.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.friedkeenan.monarch_wings.DoubleJumper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

@Mixin(ClientPacketListener.class)
public class ClientboundAbilitiesDisableDoubleJumpWhenFlying {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(at = @At("RETURN"), method = "handlePlayerAbilities")
    private void disableDoubleJumpWhenFlying(CallbackInfo info) {
        if (this.minecraft.player.getAbilities().flying) {
            final var double_jumper = (DoubleJumper) this.minecraft.player;

            double_jumper.setDoubleJumpEnabled(false);
        }
    }
}
