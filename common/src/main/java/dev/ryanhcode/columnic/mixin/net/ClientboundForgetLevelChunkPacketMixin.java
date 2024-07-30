package dev.ryanhcode.columnic.mixin.net;

import dev.ryanhcode.columnic.duck.PacketYDuck;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundForgetLevelChunkPacket.class)
public class ClientboundForgetLevelChunkPacketMixin implements PacketYDuck {

    @Unique
    private int columnic$columnY;


    @Inject(method = "Lnet/minecraft/network/protocol/game/ClientboundForgetLevelChunkPacket;<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("RETURN"))
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
