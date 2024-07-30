package dev.ryanhcode.columnic.mixin.worldgen;

import dev.ryanhcode.columnic.Columnic;
import dev.ryanhcode.columnic.ColumnicChunkPos;
import dev.ryanhcode.columnic.duck.LevelAccess3D;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Locale;

@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionMixin implements LevelAccess3D, WorldGenLevel {

    @Shadow public abstract boolean hasChunk(int chunkX, int chunkZ);

    @Shadow @Final private ChunkPos firstPos;

    @Shadow @Final private List<ChunkAccess> cache;

    @Shadow @Final private int size;

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final private ChunkPos lastPos;

    @Shadow @Final private ServerLevel level;

    @Shadow protected abstract void markPosForPostprocessing(BlockPos pos);

//    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0))
//    public int columnic$size0(List<ChunkAccess> instance) {
//        return instance.size() / Columnic.COLUMN_RENDER_DIAMETER;
//    }
//
//    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 1))
//    public int columnic$size1(List<ChunkAccess> instance) {
//        return instance.size() / Columnic.COLUMN_RENDER_DIAMETER;
//    }

    @Inject(method = { "getChunk(II)Lnet/minecraft/world/level/chunk/ChunkAccess;", "hasChunk" }, at = @At("HEAD"))
    public void disallowNonColumnicChecks(int chunkX, int chunkZ, CallbackInfoReturnable<ChunkAccess> cir) {
        throw new UnsupportedOperationException("Non-columnic chunk queries are not allowed in this context.");
    }

    @Override
    public boolean hasChunk3D(int x, int y, int z) {
        return x >= this.firstPos.x
                && x <= this.lastPos.x
                && y >= ColumnicChunkPos.getY(this.firstPos)
                && y <= ColumnicChunkPos.getY(this.lastPos)
                && z >= this.firstPos.z
                && z <= this.lastPos.z;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int i, int j, int k) {
        ChunkAccess chunkaccess = this.getChunk3D(SectionPos.of(QuartPos.toSection(i), QuartPos.toSection(j), QuartPos.toSection(k)).chunk(), ChunkStatus.BIOMES, false);
        return chunkaccess != null ? chunkaccess.getNoiseBiome(i, j, k) : this.getUncachedNoiseBiome(i, j, k);
    }

    @Override
    public ChunkAccess getChunk3D(int x, int y, int z, ChunkStatus requiredStatus, boolean nonnull) {
        ChunkAccess chunkaccess;
        if (this.hasChunk3D(x, y, z)) {
            int cx = x - this.firstPos.x;
            int k = y - ColumnicChunkPos.getY(this.firstPos);
            int j = z - this.firstPos.z;
            chunkaccess = this.cache.get((cx + j * this.size) /** Columnic.COLUMN_RENDER_DIAMETER + k*/);
            if (chunkaccess.getStatus().isOrAfter(requiredStatus)) {
                return chunkaccess;
            }
        } else {
            chunkaccess = null;
        }

        if (!nonnull) {
            return null;
        } else {
//            LOGGER.error("Requested chunk : {}", ColumnicChunkPos.of(x, y, z));
//            LOGGER.error("Region bounds : {} | {}", this.firstPos, this.lastPos);
//            if (chunkaccess != null) {
//                throw Util.pauseInIde(new RuntimeException(String.format(Locale.ROOT, "Chunk is not of correct status. Expecting %s, got %s | %s", requiredStatus, chunkaccess.getStatus(), ColumnicChunkPos.of(x, y, z))));
//            } else {
//                throw Util.pauseInIde(new RuntimeException(String.format(Locale.ROOT, "We are asking a region for a chunk out of bound | %s", ColumnicChunkPos.of(x, y, z))));
//            }

            EmptyLevelChunk chunk = new EmptyLevelChunk(this.level, ColumnicChunkPos.of(x, y, z), level.getBiomeManager().getBiome(new BlockPos(0, 0, 0)));


            return chunk;
        }
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public boolean addFreshEntity(Entity entity) {
        int cx = SectionPos.blockToSectionCoord(entity.getBlockX());
        int cy = SectionPos.blockToSectionCoord(entity.getBlockY());
        int cz = SectionPos.blockToSectionCoord(entity.getBlockZ());
        this.getChunk3D(cx, Columnic.getColumnYFromSectionY(cy), cz).addEntity(entity);
        return true;
    }

    //FIXME
    @Override
    public int getHeight(Heightmap.Types heightmapType, int x, int z) {
        return this.getChunk3D(SectionPos.blockToSectionCoord(x), 0, SectionPos.blockToSectionCoord(z)).getHeight(heightmapType, x & 15, z & 15) + 1;
    }

    @Override
    public ChunkAccess getChunk3D(ChunkPos pos) {
        return this.getChunk3D(pos.x, ColumnicChunkPos.getY(pos), pos.z, ChunkStatus.EMPTY, true);
    }

    @Override
    public ChunkAccess getChunk3D(int x, int y, int z) {
        return this.getChunk3D(x,y,z, ChunkStatus.EMPTY, true);
    }

    /**
     * @author RyanH
     * @reason Columnic
     */
    @Overwrite
    public BlockState getBlockState(BlockPos pos) {
        return this.getChunk3D(SectionPos.of(pos).chunk()).getBlockState(pos);
    }

    @Override
    public ChunkAccess getChunk(BlockPos pos) {
        return this.getChunk3D(SectionPos.of(pos).chunk());
    }

    /**
     * @author RyanH
     * @reason Columnic
     */
    @Overwrite
    public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        if (!this.ensureCanWrite(pos)) {
            return false;
        } else {
            ChunkAccess chunkAccess = this.getChunk(pos);
            BlockState blockState = chunkAccess.setBlockState(pos, state, false);
            if (blockState != null) {
                this.level.onBlockStateChange(pos, blockState, state);
            }

            if (state.hasBlockEntity()) {
                if (chunkAccess.getStatus().getChunkType() == ChunkStatus.ChunkType.LEVELCHUNK) {
                    BlockEntity blockEntity = ((EntityBlock)state.getBlock()).newBlockEntity(pos, state);
                    if (blockEntity != null) {
                        chunkAccess.setBlockEntity(blockEntity);
                    } else {
                        chunkAccess.removeBlockEntity(pos);
                    }
                } else {
                    CompoundTag compoundTag = new CompoundTag();
                    compoundTag.putInt("x", pos.getX());
                    compoundTag.putInt("y", pos.getY());
                    compoundTag.putInt("z", pos.getZ());
                    compoundTag.putString("id", "DUMMY");
                    chunkAccess.setBlockEntityNbt(compoundTag);
                }
            } else if (blockState != null && blockState.hasBlockEntity()) {
                chunkAccess.removeBlockEntity(pos);
            }

            if (state.hasPostProcess(this, pos)) {
                this.markPosForPostprocessing(pos);
            }

            return true;
        }
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
        if (!this.hasChunk3D(SectionPos.of(pos))) {
            throw new RuntimeException("We are asking a region for a chunk out of bound");
        } else {
//         FIXME
            return new DifficultyInstance(this.level.getDifficulty(), this.level.getDayTime(), 0L, this.level.getMoonBrightness());
        }
    }
}
