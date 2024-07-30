package dev.ryanhcode.columnic.mixin.render;

import dev.ryanhcode.columnic.Columnic;
import dev.ryanhcode.columnic.duck.RenderChunkRegionDuck;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.*;

@Mixin(RenderChunkRegion.class)
public class RenderChunkRegionMixin implements RenderChunkRegionDuck {

    @Shadow
    @Final
    private int centerZ;
    @Shadow
    @Final
    private int centerX;
    @Unique
    private RenderChunk[] chunks;
    @Unique
    private int sizeX;
    @Unique
    private int sizeY;

    /**
     * Column Y
     */
    @Unique
    private int centerY;

    @Unique
    private int getIndexOf(int x, int y, int z) {
        return (z * this.sizeY + y) * this.sizeX + x;
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public BlockState getBlockState(BlockPos pos) {
        int x = SectionPos.blockToSectionCoord(pos.getX()) - this.centerX;
        int y = Columnic.getColumnYFromSectionY(SectionPos.blockToSectionCoord(pos.getY()));
        int z = SectionPos.blockToSectionCoord(pos.getZ()) - this.centerZ;
        return this.chunks[getIndexOf(x, y - this.centerY, z)].getBlockState(pos.offset(0, -y * Columnic.BLOCKS_PER_COLUMN, 0));
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public BlockEntity getBlockEntity(BlockPos pos) {
        int x = SectionPos.blockToSectionCoord(pos.getX()) - this.centerX;
        int y = Columnic.getColumnYFromSectionY(SectionPos.blockToSectionCoord(pos.getY()));
        int z = SectionPos.blockToSectionCoord(pos.getZ()) - this.centerZ;
        return this.chunks[getIndexOf(x, y - this.centerY, z)].getBlockEntity(pos.offset(0, -y * Columnic.BLOCKS_PER_COLUMN, 0));
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public FluidState getFluidState(BlockPos pos) {
        int x = SectionPos.blockToSectionCoord(pos.getX()) - this.centerX;
        int y = Columnic.getColumnYFromSectionY(SectionPos.blockToSectionCoord(pos.getY()));
        int z = SectionPos.blockToSectionCoord(pos.getZ()) - this.centerZ;
        return this.chunks[getIndexOf(x, y - this.centerY, z)].getBlockState(pos.offset(0,-y * Columnic.BLOCKS_PER_COLUMN,0)).getFluidState();
    }

    @Override
    public void columnic$setChunks3D(RenderChunk[] chunks, int sizeX, int sizeY, int minY) {
        this.chunks = chunks;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.centerY = minY;
    }
}
