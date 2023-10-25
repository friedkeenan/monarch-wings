package io.github.friedkeenan.monarch_wings;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.LivingEntity;

public class MonarchWingsClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SetDoubleJumpedInfoPacket.TYPE, (packet, player, sender) -> {
            @Nullable final var entity = packet.getEntity(player.level());
            if (entity == null) {
                return;
            }

            if (!(entity instanceof LivingEntity)) {
                return;
            }

            entity.setOnGround(packet.onGround());

            final var double_jumper = (DoubleJumper) entity;

            double_jumper.setDoubleJumpEnabled(packet.enabled());
        });
    }
}
