package dev.ryanhcode.columnic.duck;

import net.minecraft.client.renderer.chunk.RenderChunk;

public interface RenderChunkRegionDuck {
    void columnic$setChunks3D(RenderChunk[] chunks, int sizeX, int sizeY, int minY);
}
