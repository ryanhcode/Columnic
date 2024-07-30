package dev.ryanhcode.columnic.mixin;

import dev.ryanhcode.columnic.Columnic;
import dev.ryanhcode.columnic.ColumnicChunkPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionPos.class)
public abstract class SectionPosMixin {

    @Shadow public abstract int x();

    @Shadow public abstract int z();

    @Shadow public abstract int y();

    @Inject(method = "chunk", at = @At("HEAD"), cancellable = true)
    private void chunk(CallbackInfoReturnable<ChunkPos> cir) {
        cir.setReturnValue(ColumnicChunkPos.of(x(), Columnic.getColumnYFromSectionY(y()), z()));
    }

    @Inject(method = "of(Lnet/minecraft/world/level/ChunkPos;I)Lnet/minecraft/core/SectionPos;", at = @At("HEAD"), cancellable = true)
    private static void of(ChunkPos chunkPos, int y, CallbackInfoReturnable<SectionPos> cir) {
        int resY = y + ColumnicChunkPos.getMinSectionY(chunkPos) - Columnic.BASE_MIN_SECTION;
        cir.setReturnValue(SectionPos.of(chunkPos.x, resY, chunkPos.z));
    }

}
