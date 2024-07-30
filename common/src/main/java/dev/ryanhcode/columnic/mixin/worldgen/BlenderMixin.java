package dev.ryanhcode.columnic.mixin.worldgen;

import com.google.common.collect.ImmutableMap;
import dev.ryanhcode.columnic.ColumnicChunkPos;
import dev.ryanhcode.columnic.duck.LevelAccess3D;
import net.minecraft.core.Direction8;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Mixin(Blender.class)
public abstract class BlenderMixin {

    @Shadow @Final private static NormalNoise SHIFT_NOISE;

    @Shadow
    public static Blender.DistanceGetter makeOldChunkDistanceGetter(BlendingData arg, Map<Direction8, BlendingData> map) {
        return null;
    }

    /**
     * @author Ocelot
     * @reason Columnic chunks.
     */
    @Overwrite
    public static void addAroundOldChunksCarvingMaskFilter(WorldGenLevel level, ProtoChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        ImmutableMap.Builder<Direction8, BlendingData> builder = ImmutableMap.builder();

        for (Direction8 direction8 : Direction8.values()) {
            int i = chunkPos.x + direction8.getStepX();
            int j = chunkPos.z + direction8.getStepZ();
            BlendingData blendingData = ((LevelAccess3D) level).getChunk3D(i, ColumnicChunkPos.getY(chunkPos), j).getBlendingData();
            if (blendingData != null) {
                builder.put(direction8, blendingData);
            }
        }

        ImmutableMap<Direction8, BlendingData> immutableMap = builder.build();
        if (chunk.isOldNoiseGeneration() || !immutableMap.isEmpty()) {
            Blender.DistanceGetter distanceGetter = makeOldChunkDistanceGetter(chunk.getBlendingData(), immutableMap);
            CarvingMask.Mask mask = (ix, jx, k) -> {
                double d = (double)ix + 0.5 + SHIFT_NOISE.getValue(ix, jx, k) * 4.0;
                double e = (double)jx + 0.5 + SHIFT_NOISE.getValue(jx, k, ix) * 4.0;
                double f = (double)k + 0.5 + SHIFT_NOISE.getValue(k, ix, jx) * 4.0;
                return distanceGetter.getDistance(d, e, f) < 4.0;
            };
            for (GenerationStep.Carving step : GenerationStep.Carving.values()) {
                chunk.getOrCreateCarvingMask(step).setAdditionalMask(mask);
            }
        }
    }
}
