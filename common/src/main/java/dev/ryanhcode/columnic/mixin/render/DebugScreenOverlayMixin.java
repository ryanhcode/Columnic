package dev.ryanhcode.columnic.mixin.render;

import dev.ryanhcode.columnic.ColumnicChunkPos;
import dev.ryanhcode.columnic.duck.LevelAccess3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;

@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenOverlayMixin {

    @Shadow
    @Nullable
    private ChunkPos lastPos;

    @Shadow
    @Nullable
    private CompletableFuture<LevelChunk> serverChunk;

    @Shadow
    @Nullable
    protected abstract ServerLevel getServerLevel();

    @Shadow
    @Nullable
    private LevelChunk clientChunk;

    @Shadow
    @Final
    private Minecraft minecraft;

    /**
     * @author Ocelot
     * @reason Use async 3D load
     */
    @Overwrite
    private @Nullable LevelChunk getServerChunk() {
        if (this.serverChunk == null) {
            ServerLevel serverlevel = this.getServerLevel();
            if (serverlevel != null) {
                this.serverChunk = ((LevelAccess3D) serverlevel)
                        .getChunkFuture3D(this.lastPos.x, ColumnicChunkPos.getY(this.lastPos), this.lastPos.z, ChunkStatus.FULL, false)
                        .thenApply(either -> either.map(arg -> (LevelChunk) arg, arg -> null));
            }

            if (this.serverChunk == null) {
                this.serverChunk = CompletableFuture.completedFuture(this.getClientChunk());
            }
        }

        return this.serverChunk.getNow(null);
    }

    /**
     * @author Ocelot
     * @reason Use async 3D load
     */
    @Overwrite
    private LevelChunk getClientChunk() {
        if (this.clientChunk == null || this.clientChunk.isEmpty()) {
            this.clientChunk = (LevelChunk) ((LevelAccess3D) this.minecraft.level).getChunk3D(this.lastPos);
        }

        return this.clientChunk;
    }
}
