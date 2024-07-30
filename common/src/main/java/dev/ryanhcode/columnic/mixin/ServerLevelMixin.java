package dev.ryanhcode.columnic.mixin;

import dev.ryanhcode.columnic.duck.LevelAccess3D;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements LevelAccess3D {
    @Shadow @Final private ServerChunkCache chunkSource;

    @Override
    public ChunkAccess getChunk3D(int x, int y, int z, ChunkStatus requiredStatus, boolean nonnull) {
        return ((LevelAccess3D) this.chunkSource).getChunk3D(x, y, z, requiredStatus, nonnull);
    }
}
