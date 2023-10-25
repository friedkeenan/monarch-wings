package io.github.friedkeenan.monarch_wings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import io.github.friedkeenan.monarch_wings.DoubleJumper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(ServerPlayer.class)
public abstract class ServerDisableDoubleJumpWhenFlying extends Player implements DoubleJumper {
    public ServerDisableDoubleJumpWhenFlying(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(at = @At("HEAD"), method = "onUpdateAbilities")
    private void disableDoubleJumpWhenFlying(CallbackInfo info) {
        if (this.getAbilities().flying) {
            this.setDoubleJumpEnabled(false);
        }
    }
}
