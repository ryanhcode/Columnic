package dev.ryanhcode.columnic.mixin;

import dev.ryanhcode.columnic.ColumnicChunkPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkTracker;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkTracker.class)
public abstract class ChunkTrackerMixin extends DynamicGraphMinFixedPoint {

    protected ChunkTrackerMixin(int firstQueuedLevel, int width, int height) {
        super(firstQueuedLevel, width, height);
    }

    @Shadow
    protected abstract int computeLevelFromNeighbor(long startPos, long endPos, int startLevel);

    /**
     * @reason Columnic chunks.
     * @author RyanH
     */
    @Overwrite
    protected void checkNeighborsAfterUpdate(long pos, int level, boolean isDecreasing) {
        if (!isDecreasing || level < this.levelCount - 2) {
            ChunkPos chunkPos = new ChunkPos(pos);
            int x = chunkPos.x;
            int y = ColumnicChunkPos.getY(chunkPos);
            int z = chunkPos.z;

            for (int ox = -1; ox <= 1; ++ox) {
                for (int oz = -1; oz <= 1; ++oz) {
//                    for (int oy = -1; oy <= 1; ++oy) {
                    int oy = 0;
                        long m = SectionPos.asLong(x + ox, y + oy, z + oz);
                        if (m != pos) {
                            this.checkNeighbor(pos, m, level, isDecreasing);
                        }
//                    }
                }
            }

        }
    }

    /**
     * @reason Columnic chunks.
     * @author RyanH
     */
    @Overwrite
    protected int getComputedLevel(long pos, long excludedSourcePos, int level) {
        int i = level;
        ChunkPos chunkPos = new ChunkPos(pos);
        int x = chunkPos.x;
        int y = ColumnicChunkPos.getY(chunkPos);
        int z = chunkPos.z;

        for (int ox = -1; ox <= 1; ++ox) {
            for (int oz = -1; oz <= 1; ++oz) {
                int oy = 0;
//                for (int oy = -1; oy <= 1; oy++) {
                    long n = SectionPos.asLong(x + ox, y + oy, z + oz);
                    if (n == pos) {
                        n = ChunkPos.INVALID_CHUNK_POS;
                    }

                    if (n != excludedSourcePos) {
                        int o = this.computeLevelFromNeighbor(n, pos, this.getLevel(n));

                        if (i > o) {
                            i = o;
                        }

                        if (i == 0) {
                            return i;
                        }
                    }
//                }
            }
        }

        return i;
    }


}
