package io.github.friedkeenan.monarch_wings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.friedkeenan.monarch_wings.DoubleJumper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;

@Mixin(ClientPacketListener.class)
public abstract class ClientboundAbilitiesDisableDoubleJumpWhenFlying extends ClientCommonPacketListenerImpl {
    protected ClientboundAbilitiesDisableDoubleJumpWhenFlying(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
    }

    @Inject(at = @At("RETURN"), method = "handlePlayerAbilities")
    private void disableDoubleJumpWhenFlying(CallbackInfo info) {
        if (this.minecraft.player.getAbilities().flying) {
            final var double_jumper = (DoubleJumper) this.minecraft.player;

            double_jumper.setDoubleJumpEnabled(false);
        }
    }
}
