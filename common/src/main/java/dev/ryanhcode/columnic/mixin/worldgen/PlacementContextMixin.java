package dev.ryanhcode.columnic.mixin.worldgen;

import dev.ryanhcode.columnic.duck.LevelAccess3D;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlacementContext.class)
public class PlacementContextMixin {

    @Shadow
    @Final
    private WorldGenLevel level;

    /**
     * @author RyanH
     * @reason Columnic
     */
    @Overwrite
    public CarvingMask getCarvingMask(ChunkPos chunkPos, GenerationStep.Carving carving) {
        return ((ProtoChunk) ((LevelAccess3D) this.level).getChunk3D(chunkPos)).getOrCreateCarvingMask(carving);
    }

}
