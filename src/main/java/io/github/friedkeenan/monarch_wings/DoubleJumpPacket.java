package io.github.friedkeenan.monarch_wings;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class DoubleJumpPacket implements FabricPacket {
    public static final PacketType<DoubleJumpPacket> TYPE = PacketType.create(new ResourceLocation("monarch_wings:double_jump"), DoubleJumpPacket::new);

    public DoubleJumpPacket() { }

    public DoubleJumpPacket(FriendlyByteBuf buf) { }

    @Override
    public void write(FriendlyByteBuf buf) { }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
