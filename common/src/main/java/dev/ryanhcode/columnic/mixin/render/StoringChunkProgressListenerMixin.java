package dev.ryanhcode.columnic.mixin.render;

import dev.ryanhcode.columnic.ColumnicChunkPos;
import dev.ryanhcode.columnic.duck.ChunkProgressDuck;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StoringChunkProgressListener.class)
public class StoringChunkProgressListenerMixin implements ChunkProgressDuck {

    @Shadow @Final private Long2ObjectOpenHashMap<ChunkStatus> statuses;

    @Shadow @Final private int radius;

    @Shadow private ChunkPos spawnPos;

    @Override
    public ChunkStatus getStatus(ChunkPos pos) {
        return this.statuses.get(SectionPos.asLong(pos.x + this.spawnPos.x - this.radius, ColumnicChunkPos.getY(pos) + ColumnicChunkPos.getY(this.spawnPos), pos.z + this.spawnPos.z - this.radius));
    }

}
