package io.github.friedkeenan.monarch_wings.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import io.github.friedkeenan.monarch_wings.DoubleJumper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(Player.class)
public abstract class DisableGliding extends LivingEntity {
    protected DisableGliding(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/player/Player;onGround:Z",
            opcode = Opcodes.GETFIELD
        ),

        method = "tryToStartFallFlying"
    )
    private boolean disableGliding(boolean original) {
        final var double_jumper = (DoubleJumper) this;

        if (this.level.dimension() == Level.END && double_jumper.isDoubleJumpDisabled()) {
            return original;
        }

        return true;
    }
}
