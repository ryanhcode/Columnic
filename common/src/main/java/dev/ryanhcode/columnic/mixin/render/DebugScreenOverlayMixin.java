package dev.ryanhcode.columnic.mixin.render;

import dev.ryanhcode.columnic.duck.LevelAccess3D;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {

    @Shadow
    @Nullable
    private ChunkPos lastPos;

    @Redirect(method = "getClientChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getChunk(II)Lnet/minecraft/world/level/chunk/LevelChunk;"))
    public LevelChunk getChunk(ClientLevel instance, int x, int z) {
        return (LevelChunk) ((LevelAccess3D) instance).getChunk3D(this.lastPos);
    }
}
