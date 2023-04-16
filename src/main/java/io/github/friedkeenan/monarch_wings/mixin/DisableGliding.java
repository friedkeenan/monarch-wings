package io.github.friedkeenan.monarch_wings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(at = @At("HEAD"), method = "tryToStartFallFlying", cancellable = true)
    private void disableGliding(CallbackInfoReturnable<Boolean> info) {
        final var double_jumper = (DoubleJumper) this;
        if (this.level.dimension() != Level.END || !double_jumper.doubleJumped()) {
            info.setReturnValue(false);
        }
    }
}
