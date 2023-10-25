package io.github.friedkeenan.monarch_wings.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.friedkeenan.monarch_wings.DoubleJumpPacket;
import io.github.friedkeenan.monarch_wings.DoubleJumper;
import io.github.friedkeenan.monarch_wings.MonarchWingsMod;
import io.github.friedkeenan.monarch_wings.SetDoubleJumpedInfoPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

@Mixin(LivingEntity.class)
abstract public class AllowDoubleJump extends Entity implements DoubleJumper {
    protected AllowDoubleJump(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    private static float DOUBLE_JUMP_POWER_SCALE = 2.0f;
    private static float ELYTRA_FALL_REDUCTION   = 4.0f;

    private boolean double_jump_enabled = false;

    @Override
    public boolean isDoubleJumpEnabled() {
        return this.double_jump_enabled;
    }

    private void sendSetDoubleJumpEnabled(boolean enabled) {
        final var packet = new SetDoubleJumpedInfoPacket((LivingEntity) (Object) this, enabled);

        for (final var player : PlayerLookup.tracking(this)) {
            if ((Object) this == player) {
                continue;
            }

            ServerPlayNetworking.send(player, packet);
        }
    }

    @Override
    public void setDoubleJumpEnabled(boolean enabled) {
        if (!this.level.isClientSide && enabled != this.double_jump_enabled) {
            this.sendSetDoubleJumpEnabled(enabled);
        }

        this.double_jump_enabled = enabled;
    }

    private boolean isInFluid() {
        return this.isAffectedByFluids() && (this.isInWater() || this.isInLava());
    }

    @Inject(at = @At("TAIL"), method = "aiStep")
    private void enableDoubleJumpOnGround(CallbackInfo info) {
        if (this.onGround) {
            this.setDoubleJumpEnabled(true);
        } else if (this.isInFluid()) {
            this.setDoubleJumpEnabled(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "jumpFromGround")
    private void disableAfterDoubleJump(CallbackInfo info) {
        /*
            NOTE: Slimes and magma cubes override this method
            without calling the super method, meaning they
            could jump as many times as they want if they
            were wearing an elytra.
        */

        if (!this.onGround) {
            this.playSound(MonarchWingsMod.DOUBLE_JUMP);
            this.gameEvent(GameEvent.FLAP);

            this.fallDistance = 0;
            this.setDoubleJumpEnabled(false);

            if (this.level.isClientSide) {
                /*
                    The server normally calls 'jumpFromGround' when
                    the player sends a packet saying that they moved
                    upwards but were previously on the ground. I don't
                    believe that that can be well-adapted to detect a
                    double jump, and so we send an explicit packet
                    in order for the server to recognize the double jump.
                */
                ClientPlayNetworking.send(new DoubleJumpPacket());
            }
        }
    }

    @Shadow
    public abstract ItemStack getItemBySlot(EquipmentSlot slot);

    private boolean isWearingUsableElytra() {
        final var chest = this.getItemBySlot(EquipmentSlot.CHEST);

        return chest.is(Items.ELYTRA) && ElytraItem.isFlyEnabled(chest);
    }

    @Shadow
    public abstract boolean onClimbable();

    @Shadow
    protected abstract boolean isAffectedByFluids();

    @Override
    public boolean canDoubleJump() {
        return (
            !this.onGround               &&
            this.double_jump_enabled     &&
            this.isWearingUsableElytra() &&
            !this.isPassenger()          &&
            !this.onClimbable()          &&
            !this.isInFluid()            &&
            !this.isSwimming()
        );
    }

    @ModifyExpressionValue(
        at = @At(
            value   = "FIELD",
            target  = "Lnet/minecraft/world/entity/LivingEntity;onGround:Z",
            opcode  = Opcodes.GETFIELD,
            ordinal = 2
        ),

        method = "aiStep"
    )
    private boolean allowDoubleJump(boolean original) {
        return original || this.canDoubleJump();
    }

    @Shadow
    public abstract float getJumpPower();

    @ModifyExpressionValue(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getJumpPower()F"
        ),

        method = "jumpFromGround"
    )
    private float increaseDoubleJumpPower(float original) {
        if (this.onGround) {
            return original;
        }

        return DOUBLE_JUMP_POWER_SCALE * original;
    }

    @Shadow
    protected abstract int calculateFallDamage(float distance, float amplifier);

    @WrapOperation(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;calculateFallDamage(FF)I"
        ),

        method = "causeFallDamage"
    )
    private int reduceFallDistance(LivingEntity entity, float distance, float amplifier, Operation<Integer> original) {
        if (this.isWearingUsableElytra()) {
            return original.call(entity, distance - ELYTRA_FALL_REDUCTION, amplifier);
        }

        return original.call(entity, distance, amplifier);
    }
}
