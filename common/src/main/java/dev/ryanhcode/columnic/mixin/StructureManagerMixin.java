package dev.ryanhcode.columnic.mixin;

import com.google.common.collect.ImmutableList;
import dev.ryanhcode.columnic.duck.LevelAccess3D;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.StructureAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(StructureManager.class)
public abstract class StructureManagerMixin {

    @Shadow
    @Final
    private LevelAccessor level;

    @Shadow  public abstract StructureStart getStartForStructure(SectionPos sectionPos, Structure structure, StructureAccess structureAccess);

    @Shadow @Final private StructureCheck structureCheck;

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public List<StructureStart> startsForStructure(ChunkPos chunkPos, Predicate<Structure> structurePredicate) {
        Map<Structure, LongSet> map = ((LevelAccess3D) this.level).getChunk3D(chunkPos, ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
        ImmutableList.Builder<StructureStart> builder = ImmutableList.builder();

        for (Map.Entry<Structure, LongSet> structureLongSetEntry : map.entrySet()) {
            Structure structure = structureLongSetEntry.getKey();
            if (structurePredicate.test(structure)) {
                this.fillStartsForStructure(structure, structureLongSetEntry.getValue(), builder::add);
            }
        }

        return builder.build();
    }

    /**
     * @author Ocelot
     * @reason Columnic chunks.
     */
    @Overwrite
    public List<StructureStart> startsForStructure(SectionPos sectionPos, Structure structure) {
        LongSet longSet = ((LevelAccess3D) this.level).getChunk3D(sectionPos.chunk(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForStructure(structure);
        ImmutableList.Builder<StructureStart> builder = ImmutableList.builder();
        Objects.requireNonNull(builder);
        this.fillStartsForStructure(structure, longSet, builder::add);
        return builder.build();
    }

    /**
     * @author RyanH, Ocelot
     * @reason Columnic chunks.
     */
    @Overwrite
    public void fillStartsForStructure(Structure structure, LongSet structureRefs, Consumer<StructureStart> startConsumer) {
        for (long ref : structureRefs) {
            SectionPos sectionPos = SectionPos.of(new ChunkPos(ref), this.level.getMinSection());
            StructureStart structureStart = this.getStartForStructure(sectionPos, structure, ((LevelAccess3D)this.level).getChunk3D(sectionPos.chunk(), ChunkStatus.STRUCTURE_STARTS));
            if (structureStart != null && structureStart.isValid()) {
                startConsumer.accept(structureStart);
            }
        }
    }

    /**
     * @author Ocelot
     * @reason Columnic chunks.
     */
    @Overwrite
    public boolean hasAnyStructureAt(BlockPos pos) {
        return ((LevelAccess3D) this.level).getChunk3D(SectionPos.of(pos).chunk(), ChunkStatus.STRUCTURE_REFERENCES).hasAnyStructureReferences();
    }

    /**
     * @author Ocelot
     * @reason Columnic chunks.
     */
    @Overwrite
    public Map<Structure, LongSet> getAllStructuresAt(BlockPos pos) {
        return ((LevelAccess3D) this.level).getChunk3D(SectionPos.of(pos).chunk(), ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
    }
}