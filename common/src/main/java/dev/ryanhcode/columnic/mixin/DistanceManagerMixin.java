package dev.ryanhcode.columnic.mixin;

import dev.ryanhcode.columnic.Columnic;
import dev.ryanhcode.columnic.ColumnicChunkPos;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.TickingTracker;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DistanceManager.class)
public abstract class DistanceManagerMixin {

    @Shadow @Final private Long2ObjectMap<ObjectSet<ServerPlayer>> playersPerChunk;

    @Shadow @Final private DistanceManager.FixedPlayerDistanceChunkTracker naturalSpawnChunkCounter;

    @Shadow @Final private DistanceManager.PlayerTicketTracker playerTicketManager;

    @Shadow @Final private TickingTracker tickingTicketsTracker;

    @Shadow protected abstract int getPlayerTicketLevel();

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public void addPlayer(SectionPos sectionPos, ServerPlayer player) {
        ChunkPos chunkPos = sectionPos.chunk();

        for (int columnY = -Columnic.COLUMN_RENDER_DISTANCE; columnY < Columnic.COLUMN_RENDER_DISTANCE; columnY++) {
            long i = ColumnicChunkPos.of(chunkPos.x, ColumnicChunkPos.getY(chunkPos) + columnY, chunkPos.z).toLong();
            this.playersPerChunk.computeIfAbsent(i, (l) -> {
                return new ObjectOpenHashSet<>();
            }).add(player);
            this.naturalSpawnChunkCounter.update(i, 0, true);
            this.playerTicketManager.update(i, 0, true);
            this.tickingTicketsTracker.addTicket(TicketType.PLAYER, chunkPos, this.getPlayerTicketLevel(), chunkPos);
        }
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public void removePlayer(SectionPos sectionPos, ServerPlayer player) {
        ChunkPos chunkPos = sectionPos.chunk();

        for (int columnY = -Columnic.COLUMN_RENDER_DISTANCE; columnY < Columnic.COLUMN_RENDER_DISTANCE; columnY++) {
            long l = ColumnicChunkPos.of(chunkPos.x, ColumnicChunkPos.getY(chunkPos) + columnY, chunkPos.z).toLong();
            ObjectSet<ServerPlayer> objectSet = this.playersPerChunk.get(l);
            objectSet.remove(player);
            if (objectSet.isEmpty()) {
                this.playersPerChunk.remove(l);
                this.naturalSpawnChunkCounter.update(l, Integer.MAX_VALUE, false);
                this.playerTicketManager.update(l, Integer.MAX_VALUE, false);
                this.tickingTicketsTracker.removeTicket(TicketType.PLAYER, chunkPos, this.getPlayerTicketLevel(), chunkPos);
            }
        }
    }

}
