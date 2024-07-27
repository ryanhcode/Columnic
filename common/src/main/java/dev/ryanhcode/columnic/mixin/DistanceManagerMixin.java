package dev.ryanhcode.columnic.mixin;

import dev.ryanhcode.columnic.Columnic;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DistanceManager.class)
public class DistanceManagerMixin {
    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;toLong()J"))
    public long columnic$defaultToY0(ChunkPos instance) {
        return Columnic.chunkToSectionLong(instance.toLong(), 0);
    }
//    @Redirect(method = "addTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;toLong()J"))
//    public long columnic$addTicker(ChunkPos instance) {
//        return Columnic.chunkToSectionLong(instance.toLong(), 0);
//    }
//
//    @Redirect(method = "removeTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;toLong()J"))
//    public long columnic$removeTicker(ChunkPos instance) {
//        return Columnic.chunkToSectionLong(instance.toLong(), 0);
//    }
//
//    @Redirect(method = "addRegionTicket", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;toLong()J"))
//    public long columnic$addRegionTicket(ChunkPos instance) {
//        return Columnic.chunkToSectionLong(instance.toLong(), 0);
//    }
//
//    @Redirect(method = "removeRegionTicket", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;toLong()J"))
//    public long columnic$removeRegionTicket(ChunkPos instance) {
//        return Columnic.chunkToSectionLong(instance.toLong(), 0);
//    }
//
//    @Redirect(method = "updateChunkForced", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;toLong()J"))
//    public long columnic$updateChunkForced(ChunkPos instance) {
//        return Columnic.chunkToSectionLong(instance.toLong(), 0);
//    }
//
//    @Redirect(method = "addPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;toLong()J"))
//    public long columnic$addPlayer(ChunkPos instance) {
//        return Columnic.chunkToSectionLong(instance.toLong(), 0);
//    }
//
//    @Redirect(method = "removePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;toLong()J"))
//    public long columnic$removePlayer(ChunkPos instance) {
//        return Columnic.chunkToSectionLong(instance.toLong(), 0);
//    }
}
