package dev.ryanhcode.columnic.mixin.net;

import dev.ryanhcode.columnic.ColumnicChunkPos;
import dev.ryanhcode.columnic.duck.PacketYDuck;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;

@Mixin(ClientboundSetChunkCacheCenterPacket.class)
public class ClientboundSetChunkCacheCenterPacketMixin implements PacketYDuck {

    @Unique
    private int columnic$columnY;


    @Inject(method = "Lnet/minecraft/network/protocol/game/ClientboundSetChunkCacheCenterPacket;<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("RETURN"))
    private void columnic$setColumnY(FriendlyByteBuf buf, CallbackInfo ci) {
        this.columnic$columnY = buf.readInt();
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void columnic$writeColumnY(FriendlyByteBuf buf, CallbackInfo ci) {
        buf.writeInt(columnic$columnY);
    }

    @Override
    public int getColumnY() {
        return columnic$columnY;
    }

    @Override
    public void setColumnY(int y) {
        columnic$columnY = y;
    }

}
