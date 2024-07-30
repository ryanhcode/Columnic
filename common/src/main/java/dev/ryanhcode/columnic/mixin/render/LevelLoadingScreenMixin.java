package dev.ryanhcode.columnic.mixin.render;

import dev.ryanhcode.columnic.LoadingRenderer3D;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelLoadingScreen.class)
public class LevelLoadingScreenMixin {

    @Shadow @Final private static Object2IntMap<ChunkStatus> COLORS;

    /**
     * @author RyanH
     * @reason 3D columnic chunks
     */
    @Overwrite
    public static void renderChunks(GuiGraphics guiGraphics, StoringChunkProgressListener progressListener, int x, int y, int i, int j) {
        LoadingRenderer3D.renderLoadingScreen3D(guiGraphics, progressListener, x, y, i, j);
    }



}
