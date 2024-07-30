package dev.ryanhcode.columnic.fabric;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.LevelChunk;

public class ColumnicImpl {
    public static void dispatchUnloadChunkEvent(int x, int y, int z, int i, LevelChunk levelchunk) {
        ClientChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload((ClientLevel) levelchunk.getLevel(), levelchunk);
    }
}
