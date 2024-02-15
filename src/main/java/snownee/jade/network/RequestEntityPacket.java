package snownee.jade.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import snownee.jade.api.Identifiers;
import snownee.jade.impl.EntityAccessorImpl;

public record RequestEntityPacket(EntityAccessorImpl.SyncData data) implements CustomPacketPayload {

	public static RequestEntityPacket read(FriendlyByteBuf buffer) {
		return new RequestEntityPacket(new EntityAccessorImpl.SyncData(buffer));
	}

	public static void handle(RequestEntityPacket message, PlayPayloadContext context) {
		if (!(context.player().orElse(null) instanceof ServerPlayer player)) {
			return;
		}
		EntityAccessorImpl.handleRequest(message.data, player, context.workHandler()::execute, tag -> {
			player.connection.send(new ReceiveDataPacket(tag));
		});
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		data.write(buffer);
	}

	@Override
	public ResourceLocation id() {
		return Identifiers.PACKET_REQUEST_ENTITY;
	}
}
