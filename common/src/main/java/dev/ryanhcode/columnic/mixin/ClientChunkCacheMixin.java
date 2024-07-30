package dev.ryanhcode.columnic.mixin;

import dev.ryanhcode.columnic.Columnic;
import dev.ryanhcode.columnic.ColumnicChunkPos;
import dev.ryanhcode.columnic.duck.ClientChunkCacheDuck;
import dev.ryanhcode.columnic.duck.ClientChunkCacheStorageDuck;
import dev.ryanhcode.columnic.duck.LevelAccess3D;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public class ClientChunkCacheMixin implements ClientChunkCacheDuck, LevelAccess3D {

    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    private volatile ClientChunkCache.Storage storage;
    @Shadow
    @Final
    private LevelChunk emptyChunk;
    @Shadow
    @Final
    private ClientLevel level;

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    private static boolean isValidChunk(@Nullable LevelChunk chunk, int x, int z) {
        throw new UnsupportedOperationException("Columnic does not support this operation.");
//        return isValidChunk(chunk, x, 0, z);
    }

    private static boolean isValidChunk(@Nullable LevelChunk chunk, int x, int columnY, int z) {
        if (chunk == null) {
            return false;
        } else {
            ChunkPos chunkpos = chunk.getPos();
            return chunkpos.x == x && ColumnicChunkPos.getY(chunkpos) == columnY && chunkpos.z == z;
        }
    }

    private static int calculateStorageRange(int viewDistance) {
        return Math.max(2, viewDistance) + 3;
    }


    public void drop(int x, int y, int z) {
        ClientChunkCacheStorageDuck storage = (ClientChunkCacheStorageDuck) (Object) this.storage;
        if (storage.inRange(x, y, z)) {
            int i = storage.getIndex(x, y, z);
            LevelChunk levelchunk = this.storage.getChunk(i);
            if (isValidChunk(levelchunk, x, y, z)) {
                Columnic.dispatchUnloadChunkEvent(x, y, z, i, levelchunk);
                this.storage.replace(i, levelchunk, null);
            }
        }
    }

    @Inject(method = "drop", at = @At("HEAD"), cancellable = true)
    public void drop(int x, int z, CallbackInfo ci) {
        // drop whole column
        for (int i = -Columnic.COLUMN_RENDER_DISTANCE; i <= Columnic.COLUMN_RENDER_DISTANCE; i++) {
            this.drop(x, i, z);
        }
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Nullable
    @Overwrite
    public LevelChunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean nonnull) {
        ClientChunkCacheStorageDuck storage = (ClientChunkCacheStorageDuck) (Object) this.storage;

        if (storage.inRange(chunkX, 0, chunkZ)) {
            LevelChunk levelchunk = this.storage.getChunk(storage.getIndex(chunkX, 0, chunkZ));
            if (isValidChunk(levelchunk, chunkX, 0, chunkZ)) {
                return levelchunk;
            }
        }

        return nonnull ? this.emptyChunk : null;
    }


    @Override
    @Nullable
    public LevelChunk replaceWithPacketData(int x, int y, int z, FriendlyByteBuf buffer, CompoundTag tag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer) {
        ClientChunkCacheStorageDuck storage = (ClientChunkCacheStorageDuck) (Object) this.storage;
        if (!storage.inRange(x, y, z)) {
            LOGGER.warn("Ignoring chunk since it's not in the view range: {}", ColumnicChunkPos.of(x,y,z));
            return null;
        } else {
            int i = storage.getIndex(x, y, z);
            LevelChunk levelChunk = this.storage.chunks.get(i);
            ChunkPos chunkPos = ColumnicChunkPos.of(x, y, z);
            if (!isValidChunk(levelChunk, x, y, z)) {
                levelChunk = new LevelChunk(this.level, chunkPos);
                levelChunk.replaceWithPacketData(buffer, tag, consumer);
                this.storage.replace(i, levelChunk);
            } else {
                levelChunk.replaceWithPacketData(buffer, tag, consumer);
            }

            this.level.onChunkLoaded(chunkPos);
            return levelChunk;
        }
    }

    @Override
    public void replaceBiomes(ChunkPos pos, FriendlyByteBuf buffer) {
        int x = pos.x;
        int y = ColumnicChunkPos.getY(pos);
        int z = pos.z;

        ClientChunkCacheStorageDuck storage = (ClientChunkCacheStorageDuck) (Object) this.storage;
        if (!storage.inRange(x, y, z)) {
            LOGGER.warn("Ignoring chunk since it's not in the view range: {}", ColumnicChunkPos.of(x,y,z));
        } else {
            int i = storage.getIndex(x, y, z);
            LevelChunk levelchunk = this.storage.chunks.get(i);
            if (!isValidChunk(levelchunk, x, y, z)) {
                LOGGER.warn("Ignoring chunk since it's not present: {}", ColumnicChunkPos.of(x,y,z));
            } else {
                levelchunk.replaceBiomes(buffer);
            }
        }
    }

    @Override
    public void updateViewCenter(int x, int y, int z) {
        ClientChunkCacheStorageDuck storage = (ClientChunkCacheStorageDuck) (Object) this.storage;
        this.storage.viewCenterX = x;
        storage.setViewCenterY(y);
        this.storage.viewCenterZ = z;
    }


    @Inject(method = "replaceBiomes", at = @At("HEAD"), cancellable = true)
    public void replaceBiomes(int x, int z, FriendlyByteBuf buffer, CallbackInfo ci) {
        throw new UnsupportedOperationException("Columnic does not support this operation.");
    }

    @Inject(method = "replaceWithPacketData", at = @At("HEAD"))
    public void replaceWithPacketData(int x, int z, FriendlyByteBuf buffer, CompoundTag tag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer, CallbackInfoReturnable<LevelChunk> cir) {
        throw new UnsupportedOperationException("Columnic does not support this operation.");
    }

    @Inject(method = "updateViewCenter", at = @At("HEAD"))
    public void updateViewCenter(int x, int z, CallbackInfo ci) {
        throw new UnsupportedOperationException("Columnic does not support this operation.");
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public void updateViewRadius(int viewDistance) {
        int i = this.storage.chunkRadius;
        int j = calculateStorageRange(viewDistance);
        if (i != j) {
            ClientChunkCache self = (ClientChunkCache) (Object) this;
            ClientChunkCache.Storage clientchunkcache$storage = self.new Storage(j);
            clientchunkcache$storage.viewCenterX = this.storage.viewCenterX;
            clientchunkcache$storage.viewCenterZ = this.storage.viewCenterZ;

            ClientChunkCacheStorageDuck accessor = ((ClientChunkCacheStorageDuck) (Object) clientchunkcache$storage);
            accessor.setViewCenterY(((ClientChunkCacheStorageDuck) (Object) this.storage).getViewCenterY());

            for (int k = 0; k < this.storage.chunks.length(); ++k) {
                LevelChunk levelchunk = this.storage.chunks.get(k);
                if (levelchunk != null) {
                    ChunkPos chunkpos = levelchunk.getPos();
                    if (accessor.inRange(chunkpos.x, ColumnicChunkPos.getY(chunkpos), chunkpos.z)) {
                        clientchunkcache$storage.replace(clientchunkcache$storage.getIndex(chunkpos.x, chunkpos.z), levelchunk);
                    }
                }
            }

            this.storage = clientchunkcache$storage;
        }
    }

    @Override
    public ChunkAccess getChunk3D(int x, int y, int z, ChunkStatus requiredStatus, boolean nonnull) {
        ClientChunkCacheStorageDuck storage = (ClientChunkCacheStorageDuck) (Object) this.storage;
        if (storage.inRange(x, y, z)) {
            LevelChunk chunk = this.storage.getChunk(storage.getIndex(x, y, z));
            if (isValidChunk(chunk, x, y, z)) {
                return chunk;
            }
        }

        return nonnull ? this.emptyChunk : null;
    }

    @Mixin(ClientChunkCache.Storage.class)
    static class Storage implements ClientChunkCacheStorageDuck {
        @Unique
        volatile int columnic$viewCenterY;
        @Final
        @Shadow(aliases = {"field_16254", "f_104465_"})
        private ClientChunkCache this$0;
        @Shadow
        private volatile int viewCenterZ;

        @Shadow
        @Final
        private int viewRange;

        @Shadow
        @Final
        private int chunkRadius;

        @Shadow
        private volatile int viewCenterX;

        @Mutable
        @Shadow @Final private AtomicReferenceArray<LevelChunk> chunks;

        @Inject(method = "<init>", at = @At("RETURN"))
        private void init(ClientChunkCache clientChunkCache, int chunkRadius, CallbackInfo ci) {
            int vertRange = Columnic.COLUMN_RENDER_DISTANCE * 2 + 1;
            this.chunks = new AtomicReferenceArray(this.viewRange * this.viewRange * vertRange);
        }

        /**
         * @author RyanH
         * @reason Columnic chunks.
         */
        @Overwrite
        public int getIndex(int x, int z) {
            throw new UnsupportedOperationException("Columnic does not support this operation.");
        }

        /**
         * @author RyanH
         * @reason Columnic chunks.
         */
        @Overwrite
        public boolean inRange(int x, int z) {
            throw new UnsupportedOperationException("Columnic does not support this operation.");
        }

        public int getIndex(int x, int y, int z) {
            int wrappedX = Math.floorMod(x, this.viewRange);
            int wrappedY = Math.floorMod(y, Columnic.COLUMN_RENDER_DISTANCE * 2 + 1);
            int wrappedZ = Math.floorMod(z, this.viewRange);

            return wrappedZ * this.viewRange + wrappedX + wrappedY * this.viewRange * this.viewRange;
        }

        @Override
        public int getViewCenterY() {
            return this.columnic$viewCenterY;
        }

        @Override
        public void setViewCenterY(int y) {
            this.columnic$viewCenterY = y;
        }

        public boolean inRange(int x, int y, int z) {
            return Math.abs(x - this.viewCenterX) <= this.chunkRadius
                    && Math.abs(z - this.viewCenterZ) <= this.chunkRadius
                    && Math.abs(y - this.columnic$viewCenterY) <= Columnic.COLUMN_RENDER_DISTANCE;

        }
    }
}
