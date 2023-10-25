package io.github.friedkeenan.monarch_wings;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class SetDoubleJumpedInfoPacket implements FabricPacket {
    public static final PacketType<SetDoubleJumpedInfoPacket> TYPE = PacketType.create(new ResourceLocation("monarch_wings:set_double_jumped_info"), SetDoubleJumpedInfoPacket::new);

    private final int entity_id;
    private final boolean enabled;
    private final boolean on_ground;

    public SetDoubleJumpedInfoPacket(LivingEntity entity, boolean enabled) {
        this.entity_id = entity.getId();
        this.enabled   = enabled;
        this.on_ground = entity.isOnGround();
    }

    public SetDoubleJumpedInfoPacket(LivingEntity entity) {
        this(entity, ((DoubleJumper) entity).isDoubleJumpEnabled());
    }

    @Nullable
    public Entity getEntity(Level level) {
        return level.getEntity(this.entity_id);
    }

    public boolean enabled() {
        return this.enabled;
    }

    public boolean isOnGround() {
        return this.on_ground;
    }

    public SetDoubleJumpedInfoPacket(FriendlyByteBuf buf) {
        this.entity_id = buf.readVarInt();
        this.enabled   = buf.readBoolean();
        this.on_ground = buf.readBoolean();
    }

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(this.entity_id);
        buf.writeBoolean(this.enabled);
        buf.writeBoolean(this.on_ground);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

}
