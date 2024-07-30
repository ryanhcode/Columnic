package dev.ryanhcode.columnic.mixin.worldgen;

import dev.ryanhcode.columnic.ColumnicChunkPos;
import dev.ryanhcode.columnic.duck.LevelAccess3D;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Objects;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin extends ChunkGenerator {

    public NoiseBasedChunkGeneratorMixin(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Shadow protected abstract NoiseChunk createNoiseChunk(ChunkAccess chunk, StructureManager structureManager, Blender blender, RandomState random);

    @Shadow @Final private Holder<NoiseGeneratorSettings> settings;

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
        BiomeManager biomeManager2 = biomeManager.withDifferentSource((ix, jx, kx) -> {
            return this.biomeSource.getNoiseBiome(ix, jx, kx, random.sampler());
        });
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        ChunkPos chunkPos = chunk.getPos();
        NoiseChunk noiseChunk = chunk.getOrCreateNoiseChunk((arg4) -> {
            return this.createNoiseChunk(arg4, structureManager, Blender.of(level), random);
        });
        Aquifer aquifer = noiseChunk.aquifer();
        CarvingContext carvingContext = new CarvingContext((NoiseBasedChunkGenerator) (Object)this, level.registryAccess(), chunk.getHeightAccessorForGeneration(), noiseChunk, random, this.settings.value().surfaceRule());
        CarvingMask carvingMask = ((ProtoChunk)chunk).getOrCreateCarvingMask(step);

        for(int j = -8; j <= 8; ++j) {
            for(int k = -8; k <= 8; ++k) {
                ChunkPos chunkPos2 = new ChunkPos(chunkPos.x + j, chunkPos.z + k);
                ChunkAccess chunkAccess = ((LevelAccess3D)level).getChunk3D(chunkPos2.x, ColumnicChunkPos.getY(chunkPos), chunkPos2.z);
                BiomeGenerationSettings biomeGenerationSettings = chunkAccess.carverBiome(() -> {
                    return this.getBiomeGenerationSettings(this.biomeSource.getNoiseBiome(QuartPos.fromBlock(chunkPos2.getMinBlockX()), 0, QuartPos.fromBlock(chunkPos2.getMinBlockZ()), random.sampler()));
                });
                Iterable<Holder<ConfiguredWorldCarver<?>>> iterable = biomeGenerationSettings.getCarvers(step);
                int l = 0;

                for(Iterator<Holder<ConfiguredWorldCarver<?>>> var24 = iterable.iterator(); var24.hasNext(); ++l) {
                    Holder<ConfiguredWorldCarver<?>> holder = var24.next();
                    ConfiguredWorldCarver<?> configuredWorldCarver = holder.value();
                    worldgenRandom.setLargeFeatureSeed(seed + (long)l, chunkPos2.x, chunkPos2.z);
                    if (configuredWorldCarver.isStartChunk(worldgenRandom)) {
                        Objects.requireNonNull(biomeManager2);
                        configuredWorldCarver.carve(carvingContext, chunk, biomeManager2::getBiome, worldgenRandom, aquifer, chunkPos2, carvingMask);
                    }
                }
            }
        }

    }

}
