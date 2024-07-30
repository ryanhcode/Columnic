package dev.ryanhcode.columnic.mixin.net;

import dev.ryanhcode.columnic.ColumnicChunkPos;
import dev.ryanhcode.columnic.duck.ClientChunkCacheDuck;
import dev.ryanhcode.columnic.duck.PacketYDuck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Set;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Shadow
    private ClientLevel level;
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract Set<ResourceKey<Level>> levels();

    @Shadow
    protected abstract void applyLightData(int x, int z, ClientboundLightUpdatePacketData data);

    @Shadow
    protected abstract void enableChunkLight(LevelChunk chunk, int x, int z);

    @Shadow protected abstract void queueLightRemoval(ClientboundForgetLevelChunkPacket packet);

    @Inject(method = "handleLevelChunkWithLight", at = @At("HEAD"), cancellable = true)
    public void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet, (ClientPacketListener) (Object) this, this.minecraft);
        int x = packet.getX();
        int z = packet.getZ();
        int columnY = ((PacketYDuck) packet).getColumnY();

        ClientboundLevelChunkPacketData data = packet.getChunkData();
        ((ClientChunkCacheDuck) this.level.getChunkSource()).replaceWithPacketData(x, columnY, z, data.getReadBuffer(), data.getHeightmaps(), data.getBlockEntitiesTagsConsumer(x, z));

        ClientboundLightUpdatePacketData clientboundlightupdatepacketdata = packet.getLightData();
        this.level.queueLightUpdate(() -> {
            this.applyLightData(x, z, clientboundlightupdatepacketdata);
            LevelChunk levelchunk = this.level.getChunkSource().getChunk(x, z, false);
            if (levelchunk != null) {
                this.enableChunkLight(levelchunk, x, z);
            }

        });
        ci.cancel();
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, (ClientPacketListener) (Object) this, this.minecraft);
        int x = packet.getX();
        int y = ((PacketYDuck) packet).getColumnY();
        int z = packet.getZ();
        ClientChunkCache clientChunkCache = this.level.getChunkSource();
        ((ClientChunkCacheDuck) clientChunkCache).drop(x, y, z);
        this.queueLightRemoval(packet);
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, (ClientPacketListener) (Object) this, this.minecraft);
        ((ClientChunkCacheDuck) this.level.getChunkSource()).updateViewCenter(packet.getX(), ((PacketYDuck) packet).getColumnY(), packet.getZ());
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public void handleChunksBiomes(ClientboundChunksBiomesPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, (ClientPacketListener) (Object) this, this.minecraft);
        Iterator<ClientboundChunksBiomesPacket.ChunkBiomeData> iter = packet.chunkBiomeData().iterator();

        ClientboundChunksBiomesPacket.ChunkBiomeData chunkBiomeData;
        while (iter.hasNext()) {
            chunkBiomeData = iter.next();
            ((ClientChunkCacheDuck) this.level.getChunkSource()).replaceBiomes(chunkBiomeData.pos(), chunkBiomeData.getReadBuffer());
        }

        iter = packet.chunkBiomeData().iterator();

        while (iter.hasNext()) {
            chunkBiomeData = iter.next();
            this.level.onChunkLoaded(chunkBiomeData.pos());
        }

        iter = packet.chunkBiomeData().iterator();

        while (iter.hasNext()) {
            chunkBiomeData = iter.next();

            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    for (int k = this.level.getMinSection(); k < this.level.getMaxSection(); ++k) {
                        int y = k - this.level.getMinSection() + ColumnicChunkPos.getMinSectionY(chunkBiomeData.pos());
                        this.minecraft.levelRenderer.setSectionDirty(chunkBiomeData.pos().x + i, y, chunkBiomeData.pos().z + j);
                    }
                }
            }
        }

    }

}
