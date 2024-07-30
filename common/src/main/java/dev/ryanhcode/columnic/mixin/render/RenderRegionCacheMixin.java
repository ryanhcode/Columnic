package dev.ryanhcode.columnic.mixin.render;

import dev.ryanhcode.columnic.Columnic;
import dev.ryanhcode.columnic.ColumnicChunkPos;
import dev.ryanhcode.columnic.duck.LevelAccess3D;
import dev.ryanhcode.columnic.duck.RenderChunkRegionDuck;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

@Mixin(RenderRegionCache.class)
public class RenderRegionCacheMixin {

    @Shadow
    @Final
    private Long2ObjectMap<RenderRegionCache.ChunkInfo> chunkInfoCache;

    /**
     * @author Ocelot
     * @reason Columnic chunks.
     */
    @Overwrite
    public @Nullable RenderChunkRegion createRegion(Level level, BlockPos start, BlockPos end, int padding) {
        int minX = SectionPos.blockToSectionCoord(start.getX() - padding);
        int minY = Columnic.getColumnY(start.getY() - padding);
        int minZ = SectionPos.blockToSectionCoord(start.getZ() - padding);
        int maxX = SectionPos.blockToSectionCoord(end.getX() + padding);
        int maxY = Columnic.getColumnY(end.getY() + padding);
        int maxZ = SectionPos.blockToSectionCoord(end.getZ() + padding);

        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;

        RenderRegionCache.ChunkInfo[] chunkInfos = new RenderRegionCache.ChunkInfo[sizeX * sizeY * sizeZ];
        LevelAccess3D levelAccess3D = (LevelAccess3D) level;

        boolean isEmpty = true;

        for (int z = 0; z < sizeZ; z++) {
            for (int y = 0; y < sizeY; y++) {
                for (int x = 0; x < sizeX; x++) {
                    RenderRegionCache.ChunkInfo info = this.chunkInfoCache.computeIfAbsent(ColumnicChunkPos.of(minX + x, minY + y, minZ + z).toLong(), (packed) -> new RenderRegionCache.ChunkInfo((LevelChunk) levelAccess3D.getChunk3D(new ChunkPos(packed))));
                    chunkInfos[(z * sizeY + y) * sizeX + x] = info;

                    if (!isEmpty) { // Skip checking if sections are empty
                        continue;
                    }

                    int offset = (minY + y) * Columnic.BLOCKS_PER_COLUMN;
                    if (!info.chunk().isYSpaceEmpty(start.getY() - offset, end.getY() - offset)) {
                        isEmpty = false;
                    }
                }
            }
        }

        if (isEmpty) {
            return null;
        }

        RenderChunk[] renderChunks = new RenderChunk[(maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1)];
        for (int i = 0; i < chunkInfos.length; i++) {
            renderChunks[i] = chunkInfos[i].renderChunk();
        }
        RenderChunkRegion region = new RenderChunkRegion(level, minX, minZ, null);
        ((RenderChunkRegionDuck) region).columnic$setChunks3D(renderChunks, sizeX, sizeY, minY);
        return region;
    }
}
