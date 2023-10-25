package io.github.friedkeenan.monarch_wings.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.friedkeenan.monarch_wings.SetDoubleJumpedInfoPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Mixin(ServerEntity.class)
public class PairDoubleJumpEnabled {
    @Shadow
    @Final
    private Entity entity;

    @Inject(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
            shift  = At.Shift.AFTER
        ),

        method = "addPairing"
    )
    private void pairDoubleJumpEnabled(ServerPlayer player, CallbackInfo info) {
        if (!(this.entity instanceof LivingEntity)) {
            return;
        }

        ServerPlayNetworking.send(player, new SetDoubleJumpedInfoPacket((LivingEntity) this.entity));
    }
}
