package dev.ryanhcode.columnic.mixin;

import dev.ryanhcode.columnic.Columnic;
import dev.ryanhcode.columnic.ColumnicChunkPos;
import dev.ryanhcode.columnic.duck.LevelAccess3D;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BulkSectionAccess.class)
public class BulkSectionAccessMixin {

    @Shadow
    @Final
    private LevelAccessor level;

    @Shadow
    private LevelChunkSection lastSection;

    @Shadow
    private long lastSectionKey;

    @Shadow
    @Final
    private Long2ObjectMap<LevelChunkSection> acquiredSections;

    /**
     * @author RyanH, Ocelot
     * @reason Columnic chunks.
     */
    @Overwrite
    public LevelChunkSection getSection(BlockPos pos) {
        long key = SectionPos.asLong(pos);

        int columnY = Columnic.getColumnY(pos);
        int minSectionY = ColumnicChunkPos.getMinSectionY(columnY);
        int newIndex = SectionPos.blockToSectionCoord(pos.getY()) - minSectionY;

        if (this.lastSection == null || this.lastSectionKey != key) {
            this.lastSection = this.acquiredSections.computeIfAbsent(key, packed -> {
                ChunkAccess chunkAccess = ((LevelAccess3D) this.level).getChunk3D(SectionPos.of(packed).chunk());
                LevelChunkSection levelChunkSection = chunkAccess.getSection(newIndex);
                levelChunkSection.acquire();
                return levelChunkSection;
            });
            this.lastSectionKey = key;
        }

        return this.lastSection;
    }
}
