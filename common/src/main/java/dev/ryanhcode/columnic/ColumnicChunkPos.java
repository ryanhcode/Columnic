package dev.ryanhcode.columnic;

import dev.ryanhcode.columnic.duck.ChunkPosDuck;
import net.minecraft.world.level.ChunkPos;

public class ColumnicChunkPos {

    public static ChunkPos of(int x, int y, int z) {
        ChunkPos chunkPos = new ChunkPos(x, z);
        ((ChunkPosDuck) chunkPos).setY(y);

        return chunkPos;
    }

    public static int getY(ChunkPos chunkPos) {
        return ((ChunkPosDuck) chunkPos).getY();
    }

    public static int getMinSectionY(ChunkPos chunkPos) {
        return getMinSectionY(getY(chunkPos));
    }

    public static int getMinSectionY(int columnY) {
        int minSection = Columnic.BASE_MIN_SECTION;
        minSection += columnY * (Columnic.BASE_MAX_SECTION - Columnic.BASE_MIN_SECTION);

        return minSection;
    }
}
