package io.github.friedkeenan.monarch_wings.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;

import io.github.friedkeenan.monarch_wings.DoubleJumper;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

@Mixin(ElytraModel.class)
public class FlapElytraModel {
    @Inject(
        at = @At(
            value   = "FIELD",
            target  = "Lnet/minecraft/client/model/ElytraModel;leftWing:Lnet/minecraft/client/model/geom/ModelPart;",
            opcode  = Opcodes.GETFIELD,
            ordinal = 0,
            shift   = At.Shift.BEFORE
        ),

        method = "setupAnim"
    )
    private void flapWings(
        LivingEntity entity, float f, float g, float h, float i, float j,
        CallbackInfo info,

        @Local(ordinal = 5) LocalFloatRef x_rot,
        @Local(ordinal = 6) LocalFloatRef z_rot,
        @Local(ordinal = 7) LocalFloatRef wing_y,
        @Local(ordinal = 8) LocalFloatRef y_rot
    ) {
        if (entity.isFallFlying()) {
            return;
        }

        final var double_jumper = (DoubleJumper) entity;

        if (!double_jumper.canDoubleJump()) {
            return;
        }

        x_rot.set(60.0f  * Mth.DEG_TO_RAD);
        z_rot.set(-60.0f * Mth.DEG_TO_RAD);
        y_rot.set(20.0f  * Mth.DEG_TO_RAD);

        if (entity.isCrouching()) {
            wing_y.set(3.0f);
        } else {
            wing_y.set(0.0f);
        }
    }
}
