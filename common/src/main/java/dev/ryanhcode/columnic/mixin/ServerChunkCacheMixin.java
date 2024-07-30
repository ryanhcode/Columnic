package dev.ryanhcode.columnic.mixin;

import com.mojang.datafixers.util.Either;
import dev.ryanhcode.columnic.Columnic;
import dev.ryanhcode.columnic.ColumnicChunkPos;
import dev.ryanhcode.columnic.duck.LevelAccess3D;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin extends ChunkSource implements LevelAccess3D {

    @Shadow
    @Final
    ServerLevel level;
    @Shadow
    @Final
    Thread mainThread;
    @Shadow
    @Final
    private long[] lastChunkPos;

    @Shadow
    @Final
    private ServerChunkCache.MainThreadExecutor mainThreadProcessor;
    @Shadow
    @Final
    private ChunkStatus[] lastChunkStatus;
    @Shadow
    @Final
    private ChunkAccess[] lastChunk;
    @Shadow
    @Final
    private DistanceManager distanceManager;

    @Shadow
    protected abstract void storeInCache(long chunkPos, ChunkAccess chunk, ChunkStatus chunkStatus);

    @Shadow
    @Nullable
    protected abstract ChunkHolder getVisibleChunkIfPresent(long l);

    @Shadow
    protected abstract boolean chunkAbsent(ChunkHolder chunkHolder, int i);

    @Shadow
    abstract boolean runDistanceManagerUpdates();

    @Shadow
    @Final
    public ChunkMap chunkMap;

    /**
     * Columnic fundamentally changes the way chunk positions are stored.
     * This is overwritten to intentionally cause crashes if other mods try and modify the same code,
     * as it is critical to the operation of Columnic.
     *
     * @reason Columnic chunks.
     * @author RyanH
     */
    @Overwrite
    public @Nullable LevelChunk getChunkNow(int chunkX, int chunkZ) {
        return this.getChunkNow(chunkX, 0, chunkZ);
    }

    /**
     * Columnic fundamentally changes the way chunk positions are stored.
     * This is overwritten to intentionally cause crashes if other mods try and modify the same code,
     * as it is critical to the operation of Columnic.
     *
     * @reason Columnic chunks.
     * @author RyanH
     */
    @Overwrite
    public @Nullable ChunkAccess getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load) {
        return this.getChunk(chunkX, 0, chunkZ, requiredStatus, load);
    }

    /**
     * Columnic fundamentally changes the way chunk positions are stored.
     * This is overwritten to intentionally cause crashes if other mods try and modify the same code,
     * as it is critical to the operation of Columnic.
     *
     * @reason Columnic chunks.
     * @author RyanH
     */
    @Overwrite
    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFutureMainThread(int x, int y, ChunkStatus chunkStatus, boolean bl) {
        throw new UnsupportedOperationException("Columnic chunks");
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture(int x, int y, ChunkStatus chunkStatus, boolean bl) {
        boolean bl2 = Thread.currentThread() == this.mainThread;
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture;
        if (bl2) {
            completableFuture = this.getChunkFutureMainThread(x, 0, y, chunkStatus, bl);
            this.mainThreadProcessor.managedBlock(completableFuture::isDone);
        } else {
            completableFuture = CompletableFuture.supplyAsync(() -> this.getChunkFutureMainThread(x, 0, y, chunkStatus, bl), this.mainThreadProcessor).thenCompose((completableFuturex) -> completableFuturex);
        }

        return completableFuture;
    }

    @Unique
    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFutureMainThread(int x, int columnY, int y, ChunkStatus chunkStatus, boolean bl) {
        ChunkPos chunkPos = ColumnicChunkPos.of(x, columnY, y);
        long l = chunkPos.toLong();
        int i = ChunkLevel.byStatus(chunkStatus);
        ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(l);
        if (bl) {
            this.distanceManager.addTicket(TicketType.UNKNOWN, chunkPos, i, chunkPos);
            if (this.chunkAbsent(chunkHolder, i)) {
                ProfilerFiller profilerFiller = this.level.getProfiler();
                profilerFiller.push("chunkLoad");
                this.runDistanceManagerUpdates();
                chunkHolder = this.getVisibleChunkIfPresent(l);
                profilerFiller.pop();
                if (this.chunkAbsent(chunkHolder, i)) {
                    throw Util.pauseInIde(new IllegalStateException("No chunk holder after ticket has been added"));
                }
            }
        }
        if (chunkHolder == null) {
            System.out.println("Chunk was null");
        }
        if (chunkHolder == null) {
            System.out.println("Chunk was not status: " + i);
        }
        return this.chunkAbsent(chunkHolder, i) ? ChunkHolder.UNLOADED_CHUNK_FUTURE : chunkHolder.getOrScheduleFuture(chunkStatus, this.chunkMap);
    }

    @Unique
    @Nullable
    public ChunkAccess getChunk(int chunkX, int columnY, int chunkZ, ChunkStatus requiredStatus, boolean load) {
        ChunkAccess chunkAccess2;
        if (Thread.currentThread() != this.mainThread) {
            return CompletableFuture.supplyAsync(() -> this.getChunk(chunkX, columnY, chunkZ, requiredStatus, load), this.mainThreadProcessor).join();
        }
        ProfilerFiller profilerFiller = this.level.getProfiler();
        profilerFiller.incrementCounter("getChunk");
        long l = SectionPos.asLong(chunkX, columnY, chunkZ);
        for (int i = 0; i < 4; ++i) {
            if (l != this.lastChunkPos[i] || requiredStatus != this.lastChunkStatus[i] || (chunkAccess2 = this.lastChunk[i]) == null && load)
                continue;
            return chunkAccess2;
        }
        profilerFiller.incrementCounter("getChunkCacheMiss");
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = this.getChunkFutureMainThread(chunkX, columnY, chunkZ, requiredStatus, load);
        this.mainThreadProcessor.managedBlock(completableFuture::isDone);
        chunkAccess2 = completableFuture.join().map(chunkAccess -> chunkAccess, chunkLoadingFailure -> {
            if (load) {
                throw Util.pauseInIde(new IllegalStateException("Chunk not there when requested: " + chunkLoadingFailure));
            }
            return null;
        });
        this.storeInCache(l, chunkAccess2, requiredStatus);
        return chunkAccess2;
    }

    @Unique
    @Nullable
    public LevelChunk getChunkNow(int chunkX, int columnY, int chunkZ) {
        if (Thread.currentThread() != this.mainThread) {
            return null;
        }
        this.level.getProfiler().incrementCounter("getChunkNow");
        long l = SectionPos.asLong(chunkX, columnY, chunkZ);
        for (int i = 0; i < 4; ++i) {
            if (l != this.lastChunkPos[i] || this.lastChunkStatus[i] != ChunkStatus.FULL) continue;
            ChunkAccess chunkAccess = this.lastChunk[i];
            return chunkAccess instanceof LevelChunk ? (LevelChunk) chunkAccess : null;
        }
        ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(l);
        if (chunkHolder == null) {
            return null;
        }
        Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = chunkHolder.getFutureIfPresent(ChunkStatus.FULL).getNow(null);
        if (either == null) {
            return null;
        }
        ChunkAccess chunkAccess2 = either.left().orElse(null);
        if (chunkAccess2 != null) {
            this.storeInCache(l, chunkAccess2, ChunkStatus.FULL);
            if (chunkAccess2 instanceof LevelChunk) {
                return (LevelChunk) chunkAccess2;
            }
        }
        return null;
    }

    @Override
    public ChunkAccess getChunk3D(int x, int y, int z, ChunkStatus requiredStatus, boolean nonnull) {
        return this.getChunk(x, y, z, requiredStatus, true);
    }

    @Override
    public boolean hasChunk3D(int x, int y, int z) {
        ChunkHolder chunkHolder = this.getVisibleChunkIfPresent(ColumnicChunkPos.of(x, y, z).toLong());
        return !this.chunkAbsent(chunkHolder, ChunkLevel.byStatus(ChunkStatus.FULL));
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public void blockChanged(BlockPos pos) {
        int i = SectionPos.blockToSectionCoord(pos.getX());
        int y = Columnic.getColumnYFromSectionY(SectionPos.blockToSectionCoord(pos.getY()));
        int j = SectionPos.blockToSectionCoord(pos.getZ());
        ChunkHolder chunkholder = this.getVisibleChunkIfPresent(SectionPos.asLong(i, y, j));
        if (chunkholder != null) {
            chunkholder.blockChanged(pos.offset(0, -y * Columnic.BLOCKS_PER_COLUMN, 0));
        }
    }
}