package dev.ryanhcode.columnic.mixin;

import dev.ryanhcode.columnic.Columnic;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoggerChunkProgressListener.class)
public class LoggerChunkProgressListenerMixin {

    @Mutable
    @Shadow
    @Final
    private int maxCount;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(int radius, CallbackInfo ci) {
        int i = radius * 2 + 1;
        this.maxCount = i * i * Columnic.COLUMN_RENDER_DIAMETER;
    }
}
