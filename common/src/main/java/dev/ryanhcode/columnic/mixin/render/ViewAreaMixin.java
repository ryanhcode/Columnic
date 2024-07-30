package dev.ryanhcode.columnic.mixin.render;

import dev.ryanhcode.columnic.duck.ViewAreaDuck;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ViewArea.class)
public abstract class ViewAreaMixin implements ViewAreaDuck {
//    @Shadow protected int chunkGridSizeY;
//
//    @Shadow @Final protected Level level;
//
//    @Shadow protected int chunkGridSizeZ;
//
//    @Shadow protected int chunkGridSizeX;
//
//    @Shadow public ChunkRenderDispatcher.RenderChunk[] chunks;
//
//    @Shadow protected abstract int getChunkIndex(int x, int y, int z);
//
//    public void repositionCamera(double viewEntityX, double viewEntityZ) {
//        int i = Mth.ceil(viewEntityX);
//        int j = Mth.ceil(viewEntityZ);
//
//        for(int k = 0; k < this.chunkGridSizeX; ++k) {
//            int l = this.chunkGridSizeX * 16;
//            int m = i - 8 - l / 2;
//            int n = m + Math.floorMod(k * 16 - m, l);
//
//            for(int o = 0; o < this.chunkGridSizeZ; ++o) {
//                int p = this.chunkGridSizeZ * 16;
//                int q = j - 8 - p / 2;
//                int r = q + Math.floorMod(o * 16 - q, p);
//
//                for(int s = 0; s < this.chunkGridSizeY; ++s) {
//                    int t = this.level.getMinBuildHeight() + s * 16;
//                    ChunkRenderDispatcher.RenderChunk renderChunk = this.chunks[this.getChunkIndex(k, s, o)];
//                    BlockPos blockPos = renderChunk.getOrigin();
//                    if (n != blockPos.getX() || t != blockPos.getY() || r != blockPos.getZ()) {
//                        renderChunk.setOrigin(n, t, r);
//                    }
//                }
//            }
//        }
//
//    }
//
//    public void setDirty(int sectionX, int sectionY, int sectionZ, boolean reRenderOnMainThread) {
//        int i = Math.floorMod(sectionX, this.chunkGridSizeX);
//        int j = Math.floorMod(sectionY - this.level.getMinSection(), this.chunkGridSizeY);
//        int k = Math.floorMod(sectionZ, this.chunkGridSizeZ);
//        ChunkRenderDispatcher.RenderChunk renderChunk = this.chunks[this.getChunkIndex(i, j, k)];
//        renderChunk.setDirty(reRenderOnMainThread);
//    }
//
//    @Nullable
//    protected ChunkRenderDispatcher.RenderChunk getRenderChunkAt(BlockPos pos) {
//        int i = Mth.floorDiv(pos.getX(), 16);
//        int j = Mth.floorDiv(pos.getY() - this.level.getMinBuildHeight(), 16);
//        int k = Mth.floorDiv(pos.getZ(), 16);
//        if (j >= 0 && j < this.chunkGridSizeY) {
//            i = Mth.positiveModulo(i, this.chunkGridSizeX);
//            k = Mth.positiveModulo(k, this.chunkGridSizeZ);
//            return this.chunks[this.getChunkIndex(i, j, k)];
//        } else {
//            return null;
//        }
//    }


    @Shadow
    protected int chunkGridSizeX;

    @Shadow
    protected int chunkGridSizeZ;

    @Shadow
    protected int chunkGridSizeY;

    @Shadow
    @Final
    protected Level level;

    @Shadow
    public ChunkRenderDispatcher.RenderChunk[] chunks;

    @Shadow
    protected abstract int getChunkIndex(int x, int y, int z);

    @Inject(method = "repositionCamera", at = @At("HEAD"))
    public void repositionCamera(CallbackInfo ci) {
        throw new UnsupportedOperationException("Columnic does not support this operation");
    }

    @Inject(method = "setViewDistance", at = @At("TAIL"))
    public void setSizeY(int renderDistanceChunks, CallbackInfo ci) {
//        this.chunkGridSizeY = renderDistanceChunks * 2 + 1;
    }

    @Override
    public void columnic$repositionCamera3D(double viewEntityX, double viewEntityY, double viewEntityZ) {
        int camX = Mth.ceil(viewEntityX);
        int camY = Mth.ceil(viewEntityY);
        int camZ = Mth.ceil(viewEntityZ);

        int blockX = this.chunkGridSizeX * 16;
        int blockY = this.chunkGridSizeY * 16;
        int blockZ = this.chunkGridSizeZ * 16;
        for (int x = 0; x < this.chunkGridSizeX; x++) {
            int gridX = camX - 8 - blockX / 2;
            int originX = gridX + Math.floorMod(x * 16 - gridX, blockX);

            for (int z = 0; z < this.chunkGridSizeZ; z++) {
                int gridZ = camZ - 8 - blockZ / 2;
                int originZ = gridZ + Math.floorMod(z * 16 - gridZ, blockZ);

                for (int y = 0; y < this.chunkGridSizeY; y++) {
                    int gridY = camY - 8 - blockY / 2;
                    int originY = gridY + Math.floorMod(y * 16 - gridY, blockY);

                    ChunkRenderDispatcher.RenderChunk renderChunk = this.chunks[this.getChunkIndex(x, y, z)];
                    BlockPos blockPos = renderChunk.getOrigin();
                    if (originX != blockPos.getX() || originY != blockPos.getY() || originZ != blockPos.getZ()) {
                        renderChunk.setOrigin(originX, originY, originZ);
                    }
                }
            }
        }
    }

    /**
     * @author Ocelot
     * @reason Columnic supports view outside of the normal y
     */
    @Overwrite
    public void setDirty(int sectionX, int sectionY, int sectionZ, boolean reRenderOnMainThread) {
        int i = Math.floorMod(sectionX, this.chunkGridSizeX);
        int j = Math.floorMod(sectionY, this.chunkGridSizeY);
        int k = Math.floorMod(sectionZ, this.chunkGridSizeZ);
        ChunkRenderDispatcher.RenderChunk renderChunk = this.chunks[this.getChunkIndex(i, j, k)];
        renderChunk.setDirty(reRenderOnMainThread);
    }

    /**
     * @author Ocelot
     * @reason Columnic supports view outside of the normal y
     */
    @Overwrite
    protected @Nullable ChunkRenderDispatcher.RenderChunk getRenderChunkAt(BlockPos pos) {
        int x = Mth.positiveModulo(Mth.floorDiv(pos.getX(), 16), this.chunkGridSizeX);
        int y = Mth.positiveModulo(Mth.floorDiv(pos.getY(), 16), this.chunkGridSizeY);
        int z = Mth.positiveModulo(Mth.floorDiv(pos.getZ(), 16), this.chunkGridSizeZ);
        return this.chunks[this.getChunkIndex(x, y, z)];
    }
}
