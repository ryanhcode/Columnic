package dev.ryanhcode.columnic.mixin;

import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ProtoChunk.class)
public class ProtoChunkMixin {

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ProtoChunk;isOutsideBuildHeight(I)Z"))
    public boolean columnic$isOutsideBuildHeight(ProtoChunk protoChunk, int y) {
        return  y < protoChunk.getMinBuildHeight() || y >= protoChunk.getMaxBuildHeight();
    }

}
