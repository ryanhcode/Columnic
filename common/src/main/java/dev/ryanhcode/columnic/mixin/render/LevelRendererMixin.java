package dev.ryanhcode.columnic.mixin.render;

import dev.ryanhcode.columnic.duck.ViewAreaDuck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow
    @Nullable
    private ViewArea viewArea;

    @Shadow
    private double lastCameraY;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getMaxBuildHeight()I"))
    public int columnic$getMaxBuildHeight(ClientLevel instance) {
        return Integer.MAX_VALUE - 16; // buffer incase added to
    }

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getMinBuildHeight()I"))
    public int columnic$getMinBuildHeight(LevelReader instance) {
        return -Integer.MAX_VALUE + 16; // buffer incase added to
    }

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getMinBuildHeight()I"))
    public int columnic$getMinBuildHeightRelative(ClientLevel instance) {
        return -Integer.MAX_VALUE + 16; // buffer incase added to
    }

    @Redirect(method = "allChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ViewArea;repositionCamera(DD)V"))
    public void setupReloadViewArea(ViewArea instance, double x, double z) {
        ((ViewAreaDuck) instance).columnic$repositionCamera3D(x, this.minecraft.getCameraEntity().getY(), z);
    }

    @Redirect(method = "setupRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ViewArea;repositionCamera(DD)V"))
    public void setupRenderViewArea(ViewArea instance, double x, double z) {
        ((ViewAreaDuck) instance).columnic$repositionCamera3D(x, this.lastCameraY, z);
    }
}
