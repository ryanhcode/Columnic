package dev.ryanhcode.columnic.mixin;

import com.mojang.datafixers.DataFixer;
import dev.ryanhcode.columnic.Columnic;
import dev.ryanhcode.columnic.duck.SectionPosHolder;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.Writer;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin extends ChunkStorage {

    @Shadow
    @Final
    private LongSet toDrop;

    @Shadow
    @Final
    private Long2ObjectLinkedOpenHashMap<ChunkHolder> pendingUnloads;

    @Shadow
    @Final
    private Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap;

    @Shadow
    private boolean modified;

    @Shadow
    @Final
    private ChunkTaskPriorityQueueSorter queueSorter;

    @Shadow
    @Final
    private ThreadedLevelLightEngine lightEngine;

    @Shadow
    @Final
    private ServerLevel level;

    @Shadow
    @Final
    private Long2LongMap chunkSaveCooldowns;
    @Shadow
    @Final
    private PoiManager poiManager;
    @Shadow
    @Final
    private Long2ByteMap chunkTypeCache;

    public ChunkMapMixin(Path regionFolder, DataFixer fixerUpper, boolean sync) {
        super(regionFolder, fixerUpper, sync);
    }

    @Shadow
    protected abstract CompletableFuture<Optional<CompoundTag>> readChunk(ChunkPos pos);

    @Redirect(method = "getVisibleChunkIfPresent", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectLinkedOpenHashMap;get(J)Ljava/lang/Object;"))
    public Object get(Long2ObjectLinkedOpenHashMap<ChunkHolder> map, long chunkLong) {
        return map.get(Columnic.chunkToSectionLong(chunkLong, 0));
    }

    @Inject(method = "dumpChunks", at = @At("HEAD"), cancellable = true)
    public void dumpChunks(Writer writer, CallbackInfo ci) {
        Columnic.LOGGER.info("Columnic does not support CSV chunk dump.");
        ci.cancel();
    }

    /**
     * @reason Columnic chunks
     * @author RyanH
     */
    @Overwrite
    ChunkHolder updateChunkScheduling(long sectionPos, int newLevel, @Nullable ChunkHolder holder, int oldLevel) {
        ChunkMap self = (ChunkMap) (Object) this;

        if (!ChunkLevel.isLoaded(oldLevel) && !ChunkLevel.isLoaded(newLevel)) {
            return holder;
        }
        if (holder != null) {
            holder.setTicketLevel(newLevel);
        }
        if (holder != null) {
            if (!ChunkLevel.isLoaded(newLevel)) {
                this.toDrop.add(sectionPos);
            } else {
                this.toDrop.remove(sectionPos);
            }
        }
        if (ChunkLevel.isLoaded(newLevel) && holder == null) {
            holder = this.pendingUnloads.remove(sectionPos);
            if (holder != null) {
                holder.setTicketLevel(newLevel);
            } else {
                holder = new ChunkHolder(new ChunkPos(Columnic.sectionToChunkLong(sectionPos)), newLevel, this.level, this.lightEngine, this.queueSorter, self);
                ((SectionPosHolder) holder).setSectionPos(SectionPos.of(sectionPos));
            }
            this.updatingChunkMap.put(sectionPos, holder);
            this.modified = true;
        }
        return holder;
    }

    /**
     * @reason Columnic chunks
     * @author RyanH
     */
    @Overwrite
    private boolean saveChunkIfNeeded(ChunkHolder holder) {
        ChunkMap self = (ChunkMap) (Object) this;
        if (!holder.wasAccessibleSinceLastSave()) {
            return false;
        }
        ChunkAccess chunkAccess = holder.getChunkToSave().getNow(null);
        if (chunkAccess instanceof ImposterProtoChunk || chunkAccess instanceof LevelChunk) {
            SectionPos section = ((SectionPosHolder) holder).getSectionPos();

            long l = section.asLong();
            long m = this.chunkSaveCooldowns.getOrDefault(l, -1L);
            long n = System.currentTimeMillis();
            if (n < m) {
                return false;
            }
            boolean bl = this.save(chunkAccess);
            holder.refreshAccessibility();
            if (bl) {
                this.chunkSaveCooldowns.put(l, n + 10000L);
            }
            return bl;
        }
        return false;
    }

    /**
     * @author RyanH
     * @reason Columnic chunks
     */
    @Overwrite
    private ChunkAccess createEmptyChunk(ChunkPos chunkPos) {
        this.markPositionReplaceable(chunkPos);
        ProtoChunk chunk = new ProtoChunk(chunkPos, UpgradeData.EMPTY, this.level, this.level.registryAccess().registryOrThrow(Registries.BIOME), null);
        ((SectionPosHolder) chunk).setSectionPos(SectionPos.of(chunkPos, 0));
        return chunk;
    }

    private ChunkAccess createEmptyChunk(SectionPos chunkPos) {
        this.markPositionReplaceable(chunkPos);
        ProtoChunk chunk = new ProtoChunk(chunkPos.chunk(), UpgradeData.EMPTY, this.level, this.level.registryAccess().registryOrThrow(Registries.BIOME), null);
        ((SectionPosHolder) chunk).setSectionPos(chunkPos);
        return chunk;
    }

    /**
     * @author RyanH
     * @reason Columnic chunks
     */
    @Overwrite
    private byte markPosition(ChunkPos chunkPos, ChunkStatus.ChunkType chunkType) {
        return this.chunkTypeCache.put(Columnic.chunkToSectionLong(chunkPos.toLong(), 0), chunkType == ChunkStatus.ChunkType.PROTOCHUNK ? (byte) -1 : 1);
    }

    private byte markPosition(SectionPos chunkPos, ChunkStatus.ChunkType chunkType) {
        return this.chunkTypeCache.put(chunkPos.asLong(), chunkType == ChunkStatus.ChunkType.PROTOCHUNK ? (byte) -1 : 1);
    }

    /**
     * @author RyanH
     * @reason Columnic chunks
     */
    @Overwrite
    private void markPositionReplaceable(ChunkPos chunkPos) {
        this.chunkTypeCache.put(Columnic.chunkToSectionLong(chunkPos.toLong(), 0), (byte) -1);
    }

    private void markPositionReplaceable(SectionPos chunkPos) {
        this.chunkTypeCache.put(chunkPos.asLong(), (byte) -1);
    }


    private boolean isExistingChunkFull(SectionPos sectionPos) {
        CompoundTag compoundTag;
        byte b = this.chunkTypeCache.get(sectionPos.asLong());
        if (b != 0) {
            return b == 1;
        }
        try {
            compoundTag = null;
            // FIXME
//            compoundTag = this.readChunk(sectionPos).join().orElse(null);
            if (compoundTag == null) {
                this.markPositionReplaceable(sectionPos);
                return false;
            }
        } catch (Exception exception) {
            Columnic.LOGGER.error("Failed to read chunk {}", (Object) sectionPos, (Object) exception);
            this.markPositionReplaceable(sectionPos);
            return false;
        }
        ChunkStatus.ChunkType chunkType = ChunkSerializer.getChunkTypeFromTag(compoundTag);
        return this.markPosition(sectionPos, chunkType) == 1;
    }

    /**
     * @reason Columnic chunks
     * @author RyanH
     */
    @Overwrite
    private boolean save(ChunkAccess chunk) {
        this.poiManager.flush(chunk.getPos());
        if (!chunk.isUnsaved()) {
            return false;
        }
        chunk.setUnsaved(false);

        SectionPos chunkPos = ((SectionPosHolder) chunk).getSectionPos();
        try {
            ChunkStatus chunkStatus = chunk.getStatus();
            if (chunkStatus.getChunkType() != ChunkStatus.ChunkType.LEVELCHUNK) {
                if (this.isExistingChunkFull(chunkPos)) {
                    return false;
                }
                if (chunkStatus == ChunkStatus.EMPTY && chunk.getAllStarts().values().stream().noneMatch(StructureStart::isValid)) {
                    return false;
                }
            }
            this.level.getProfiler().incrementCounter("chunkSave");
            CompoundTag compoundTag = ChunkSerializer.write(this.level, chunk);
            this.write(chunkPos.chunk(), compoundTag); // FIXME
            this.markPosition(chunkPos, chunkStatus.getChunkType());
            return true;
        } catch (Exception exception) {
            Columnic.LOGGER.error("Failed to save chunk {}, {}, {}", chunkPos.x(), chunkPos.y(), chunkPos.z(), exception);
            return false;
        }
    }
}
