package dev.ryanhcode.columnic.forge;

import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkEvent;

public class ColumnicImpl {
    public static void dispatchUnloadChunkEvent(int x, int y, int z, int i, LevelChunk levelchunk) {
        MinecraftForge.EVENT_BUS.post(new ChunkEvent.Unload(levelchunk));

    }
}
