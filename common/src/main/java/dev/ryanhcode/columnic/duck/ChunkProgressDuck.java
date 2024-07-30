package dev.ryanhcode.columnic.duck;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;

public interface ChunkProgressDuck {
    ChunkStatus getStatus(ChunkPos pos);
}
