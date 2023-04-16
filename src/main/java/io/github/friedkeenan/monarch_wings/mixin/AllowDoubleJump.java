package io.github.friedkeenan.monarch_wings.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.friedkeenan.monarch_wings.DoubleJumpPacket;
import io.github.friedkeenan.monarch_wings.DoubleJumper;
import io.github.friedkeenan.monarch_wings.MonarchWingsMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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

    /*
        NOTE: I tried to get whether double jumping was enabled
        to be properly saved, but injecting into
        'readAdditionalSaveData' and 'addAdditionalSaveData'
        did not work for some mysterious reason, and the feature
        is not important enough to me to figure it out.
    */
    private boolean double_jump_enabled = false;

    @Inject(at = @At("HEAD"), method = "aiStep")
    private void enableDoubleJump(CallbackInfo info) {
        if (this.onGround) {
            this.double_jump_enabled = true;
        }
    }

    @Inject(at = @At("HEAD"), method = "jumpFromGround")
    private void disableDoubleJump(CallbackInfo info) {
        /*
            NOTE: Slimes and magma cubes override this method
            without calling the super method, meaning they
            could jump as many times as they want if they
            were wearing an elytra.
        */

        if (!this.onGround) {
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

            this.playSound(MonarchWingsMod.DOUBLE_JUMP);
            this.gameEvent(GameEvent.FLAP);

            this.double_jump_enabled = false;

            this.fallDistance = 0;
        }
    }

    @Override
    public boolean doubleJumped() {
        return !this.double_jump_enabled;
    }

    @Shadow
    public abstract ItemStack getItemBySlot(EquipmentSlot slot);

    private boolean isWearingUsableElytra() {
        final var chest = this.getItemBySlot(EquipmentSlot.CHEST);

        return chest.is(Items.ELYTRA) && ElytraItem.isFlyEnabled(chest);
    }

    private boolean canDoubleJump() {
        return this.double_jump_enabled && this.isWearingUsableElytra();
    }

    @Redirect(
        at = @At(
            value   = "FIELD",
            target  = "Lnet/minecraft/world/entity/LivingEntity;onGround:Z",
            opcode  = Opcodes.GETFIELD,
            ordinal = 2
        ),

        method = "aiStep"
    )
    private boolean allowDoubleJump(LivingEntity entity) {
        return this.onGround || this.canDoubleJump();
    }

    @Shadow
    public abstract float getJumpPower();

    @Redirect(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getJumpPower()F"
        ),

        method = "jumpFromGround"
    )
    private float increaseDoubleJumpPower(LivingEntity entity) {
        if (this.onGround) {
            return this.getJumpPower();
        }

        return DOUBLE_JUMP_POWER_SCALE * this.getJumpPower();
    }

    @Shadow
    protected abstract int calculateFallDamage(float distance, float amplifier);

    @Redirect(
        at = @At(
            value  = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;calculateFallDamage(FF)I"
        ),

        method = "causeFallDamage"
    )
    private int reduceFallDistance(LivingEntity entity, float distance, float amplifier) {
        if (this.isWearingUsableElytra()) {
            return this.calculateFallDamage(distance - ELYTRA_FALL_REDUCTION, amplifier);
        }

        return this.calculateFallDamage(distance, amplifier);
    }
}
