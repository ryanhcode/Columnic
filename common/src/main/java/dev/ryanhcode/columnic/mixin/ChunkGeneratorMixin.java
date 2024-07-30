package dev.ryanhcode.columnic.mixin;

import dev.ryanhcode.columnic.ColumnicChunkPos;
import dev.ryanhcode.columnic.duck.LevelAccess3D;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.Set;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {

    @Inject(method = "method_39787", at = @At("HEAD"), cancellable = true)
    private static void collectBiomes(WorldGenLevel level, Set<Holder<Biome>> set, ChunkPos chunkPos, CallbackInfo ci) {
        ci.cancel();
        ChunkAccess chunkAccess = ((LevelAccess3D) level).getChunk3D(chunkPos);
        for (LevelChunkSection levelChunkSection : chunkAccess.getSections()) {
            levelChunkSection.getBiomes().getAll(set::add);
        }
    }

    /**
     * @author RyanH
     * @reason Columnic
     */
    @Overwrite
    public void createReferences(WorldGenLevel level, StructureManager structureManager, ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int cx = chunkPos.x;
        int cy = ColumnicChunkPos.getY(chunkPos);
        int cz = chunkPos.z;
        int minx = chunkPos.getMinBlockX();
        int minz = chunkPos.getMinBlockZ();
        SectionPos sectionPos = SectionPos.of(chunkPos.x, ColumnicChunkPos.getMinSectionY(chunk.getPos()), chunkPos.z);

        // TODO vertical
        for (int x = cx - 8; x <= cx + 8; ++x) {
            for (int z = cz - 8; z <= cz + 8; ++z) {
//                for (int y = cy - Columnic.COLUMN_RENDER_DISTANCE; y <= cy + Columnic.COLUMN_RENDER_DISTANCE; ++y) {
                long p = SectionPos.asLong(x, cy, z);

                for (StructureStart structureStart : ((LevelAccess3D) level).getChunk3D(x, cy, z).getAllStarts().values()) {
                    try {
                        if (structureStart.isValid() && structureStart.getBoundingBox().intersects(minx, minz, minx + 15, minz + 15)) {
                            structureManager.addReferenceForStructure(sectionPos, structureStart.getStructure(), p, chunk);
                            DebugPackets.sendStructurePacket(level, structureStart);
                        }
                    } catch (Exception var21) {
                        CrashReport crashReport = CrashReport.forThrowable(var21, "Generating structure reference");
                        CrashReportCategory crashReportCategory = crashReport.addCategory("Structure");
                        Optional<? extends Registry<Structure>> optional = level.registryAccess().registry(Registries.STRUCTURE);
                        crashReportCategory.setDetail("Id", () -> optional.map((arg2) -> arg2.getKey(structureStart.getStructure()).toString()).orElse("UNKNOWN"));
                        crashReportCategory.setDetail("Name", () -> BuiltInRegistries.STRUCTURE_TYPE.getKey(structureStart.getStructure().type()).toString());
                        crashReportCategory.setDetail("Class", () -> structureStart.getStructure().getClass().getCanonicalName());
                        throw new ReportedException(crashReport);
                    }
                }
//                }
            }
        }
    }
}

