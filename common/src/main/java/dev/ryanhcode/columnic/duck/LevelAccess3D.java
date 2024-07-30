package dev.ryanhcode.columnic.duck;

import com.mojang.datafixers.util.Either;
import dev.ryanhcode.columnic.Columnic;
import dev.ryanhcode.columnic.ColumnicChunkPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface LevelAccess3D {

    default ChunkAccess getChunk3D(SectionPos pos) {
        return this.getChunk3D(pos.x(), Columnic.getColumnYFromSectionY(pos.y()), pos.z(), ChunkStatus.FULL, true);
    }

    default ChunkAccess getChunk3D(SectionPos pos, ChunkStatus requiredStatus) {
        return this.getChunk3D(pos.x(), Columnic.getColumnYFromSectionY(pos.y()), pos.z(), requiredStatus, true);
    }

    default ChunkAccess getChunk3D(SectionPos pos, ChunkStatus requiredStatus, boolean nonnull) {
        return this.getChunk3D(pos.x(), Columnic.getColumnYFromSectionY(pos.y()), pos.z(), requiredStatus, nonnull);
    }

    default ChunkAccess getChunk3D(ChunkPos pos) {
        return this.getChunk3D(pos.x, ColumnicChunkPos.getY(pos), pos.z, ChunkStatus.FULL, true);
    }

    default ChunkAccess getChunk3D(ChunkPos pos, ChunkStatus requiredStatus) {
        return this.getChunk3D(pos.x, ColumnicChunkPos.getY(pos), pos.z, requiredStatus, true);
    }

    default ChunkAccess getChunk3D(ChunkPos pos, ChunkStatus requiredStatus, boolean nonnull) {
        return this.getChunk3D(pos.x, ColumnicChunkPos.getY(pos), pos.z, requiredStatus, nonnull);
    }

    default ChunkAccess getChunk3D(int x, int y, int z) {
        return this.getChunk3D(x, y, z, ChunkStatus.FULL, true);
    }

    default ChunkAccess getChunk3D(int x, int y, int z, ChunkStatus requiredStatus) {
        return this.getChunk3D(x, y, z, requiredStatus, true);
    }

    @Nullable ChunkAccess getChunk3D(int x, int y, int z, ChunkStatus requiredStatus, boolean nonnull);

    default boolean hasChunk3D(ChunkPos pos) {
        return this.getChunk3D(pos, ChunkStatus.FULL, false) != null;
    }

    default boolean hasChunk3D(SectionPos pos) {
        return this.getChunk3D(pos, ChunkStatus.FULL, false) != null;
    }

    default boolean hasChunk3D(int x, int y, int z) {
        return this.getChunk3D(x, y, z, ChunkStatus.FULL, false) != null;
    }

    default CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture3D(SectionPos pos) {
        return this.getChunkFuture3D(pos.x(), Columnic.getColumnYFromSectionY(pos.y()), pos.z(), ChunkStatus.FULL, true);
    }

    default CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture3D(SectionPos pos, ChunkStatus requiredStatus) {
        return this.getChunkFuture3D(pos.x(), Columnic.getColumnYFromSectionY(pos.y()), pos.z(), requiredStatus, true);
    }

    default CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture3D(SectionPos pos, ChunkStatus requiredStatus, boolean nonnull) {
        return this.getChunkFuture3D(pos.x(), Columnic.getColumnYFromSectionY(pos.y()), pos.z(), requiredStatus, nonnull);
    }

    default CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture3D(ChunkPos pos) {
        return this.getChunkFuture3D(pos.x, ColumnicChunkPos.getY(pos), pos.z, ChunkStatus.FULL, true);
    }

    default CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture3D(ChunkPos pos, ChunkStatus requiredStatus) {
        return this.getChunkFuture3D(pos.x, ColumnicChunkPos.getY(pos), pos.z, requiredStatus, true);
    }

    default CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture3D(ChunkPos pos, ChunkStatus requiredStatus, boolean nonnull) {
        return this.getChunkFuture3D(pos.x, ColumnicChunkPos.getY(pos), pos.z, requiredStatus, nonnull);
    }

    default CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture3D(int x, int y, int z) {
        return this.getChunkFuture3D(x, y, z, ChunkStatus.FULL, true);
    }

    default CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture3D(int x, int y, int z, ChunkStatus requiredStatus) {
        return this.getChunkFuture3D(x, y, z, requiredStatus, true);
    }

    CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture3D(int x, int y, int z, ChunkStatus requiredStatus, boolean nonnull);
}
