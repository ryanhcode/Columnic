package dev.ryanhcode.columnic.mixin;

import com.mojang.datafixers.util.Either;
import dev.ryanhcode.columnic.duck.LevelAccess3D;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;

@Mixin(ClientLevel.class)
public class ClientLevelMixin implements LevelAccess3D {
    @Shadow
    @Final
    private ClientChunkCache chunkSource;

    @Override
    public ChunkAccess getChunk3D(int x, int y, int z, ChunkStatus requiredStatus, boolean nonnull) {
        return ((LevelAccess3D) this.chunkSource).getChunk3D(x, y, z, requiredStatus, nonnull);
    }

    @Override
    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture3D(int x, int y, int z, ChunkStatus requiredStatus, boolean nonnull) {
        return ((LevelAccess3D) this.chunkSource).getChunkFuture3D(x, y, z, requiredStatus, nonnull);
    }
}
