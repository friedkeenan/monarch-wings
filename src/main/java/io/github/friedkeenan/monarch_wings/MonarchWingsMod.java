package io.github.friedkeenan.monarch_wings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class MonarchWingsMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("monarch_wings");

    public static final ResourceLocation DOUBLE_JUMP_LOCATION = new ResourceLocation("monarch_wings:entity.monarch_wings.double_jump");
    public static final SoundEvent       DOUBLE_JUMP          = SoundEvent.createVariableRangeEvent(DOUBLE_JUMP_LOCATION);

    public void onInitialize() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, DOUBLE_JUMP_LOCATION, DOUBLE_JUMP);

        ServerPlayNetworking.registerGlobalReceiver(DoubleJumpPacket.TYPE, (packet, player, sender) -> {
            /*
                The player needed to not be on the ground to be able to double jump.

                Normally the server is able to track this on its own
                just fine, but in certain circumstances does not seem
                to know, so we explicitly set it here.
            */
            player.setOnGround(false);

            player.jumpFromGround();
        });

        LOGGER.info("monarch_wings initialized!");
    }
}
