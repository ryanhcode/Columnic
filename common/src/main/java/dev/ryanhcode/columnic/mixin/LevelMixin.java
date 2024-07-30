package dev.ryanhcode.columnic.mixin;

import dev.ryanhcode.columnic.Columnic;
import dev.ryanhcode.columnic.duck.LevelAccess3D;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelReader {

    /**
     * @author RyanH
     * @reason Columnic
     */
    @Inject(method = "getChunkAt", at = @At("HEAD"), cancellable = true)
    public void getChunkAt(BlockPos pos, CallbackInfoReturnable<LevelChunk> cir) {
        int columnY = Columnic.getColumnY(pos);

        if (columnY != 0) {
            ChunkAccess chunk = ((LevelAccess3D) this).getChunk3D(new ChunkPos(pos));

            if (chunk != null) {
                cir.setReturnValue((LevelChunk) chunk);
            }
        }
    }

    @Override
    public Holder<Biome> getNoiseBiome(int i, int j, int k) {
        ChunkAccess chunkaccess = ((LevelAccess3D) this).getChunk3D(SectionPos.of(QuartPos.toSection(i), QuartPos.toSection(j), QuartPos.toSection(k)).chunk(), ChunkStatus.BIOMES, false);
        return chunkaccess != null ? chunkaccess.getNoiseBiome(i, j, k) : this.getUncachedNoiseBiome(i, j, k);
    }
}
