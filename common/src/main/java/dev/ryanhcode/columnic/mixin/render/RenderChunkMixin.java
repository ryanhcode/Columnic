package dev.ryanhcode.columnic.mixin.render;

import dev.ryanhcode.columnic.duck.LevelAccess3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkRenderDispatcher.RenderChunk.class)
public class RenderChunkMixin {

    /**
     * @author Ocelot
     * @reason Allow chunks to exist outside of normal range
     */
    @Overwrite
    private boolean doesChunkExistAt(BlockPos pos) {
        return ((LevelAccess3D)Minecraft.getInstance().level).hasChunk3D(SectionPos.of(pos));
    }
}
