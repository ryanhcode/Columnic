package dev.ryanhcode.columnic.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import dev.ryanhcode.columnic.Columnic;
import dev.ryanhcode.columnic.ColumnicChunkPos;
import dev.ryanhcode.columnic.duck.PacketYDuck;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {

    @Shadow
    @Final
    private PlayerMap playerMap;

    @Shadow
    private int viewDistance;
    @Shadow
    @Final
    private ChunkMap.DistanceManager distanceManager;
    @Shadow
    @Final
    private Int2ObjectMap<ChunkMap.TrackedEntity> entityMap;
    @Shadow
    @Final
    private ServerLevel level;

    @Shadow
    public static boolean isChunkInRange(int m, int n, int o, int p, int maxDistance) {
        throw new AssertionError("Mixin failed to apply");
    }

    @Inject(method = "euclideanDistanceSquared", at = @At("HEAD"), cancellable = true)
    private static void euclideanDistanceSquared(ChunkPos chunkPos, Entity entity, CallbackInfoReturnable<Double> cir) {
        double d = SectionPos.sectionToBlockCoord(chunkPos.x, 8);
        double e = SectionPos.sectionToBlockCoord(chunkPos.z, 8);
        double f = d - entity.getX();
        double g = e - entity.getZ();

        double dist = f * f + g * g;

        if (Math.abs(ColumnicChunkPos.getY(chunkPos) - ColumnicChunkPos.getY(entity.chunkPosition())) > Columnic.COLUMN_RENDER_DISTANCE) {
            dist += Double.MAX_VALUE / 2.0;
        }

        cir.setReturnValue(dist);
    }

    @Shadow
    protected abstract void updateChunkTracking(ServerPlayer player, ChunkPos chunkPos, MutableObject<ClientboundLevelChunkWithLightPacket> packetCache, boolean wasLoaded, boolean load);

    @Shadow
    protected abstract boolean skipPlayer(ServerPlayer player);

    @Shadow
    protected abstract SectionPos updatePlayerPos(ServerPlayer player);

    @Shadow
    public abstract boolean hasWork();

    @Shadow
    protected abstract ChunkHolder getUpdatingChunkIfPresent(long l);

    @Shadow
    public abstract ReportedException debugFuturesAndCreateReportedException(IllegalStateException exception, String details);

    @Inject(method = "updatePlayerPos", at = @At("HEAD"), cancellable = true)
    private void updatePlayerPos(ServerPlayer player, CallbackInfoReturnable<SectionPos> cir) {
        SectionPos sectionPos = SectionPos.of(player);
        player.setLastSectionPos(sectionPos);
        ClientboundSetChunkCacheCenterPacket packet = new ClientboundSetChunkCacheCenterPacket(sectionPos.x(), sectionPos.z());
        ((PacketYDuck) packet).setColumnY(Columnic.getColumnYFromSectionY(sectionPos.y()));
        player.connection.send(packet);
        cir.setReturnValue(sectionPos);
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    private CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> getChunkRangeFuture(ChunkHolder chunkHolder, int distance, IntFunction<ChunkStatus> distanceStatusProvider) {
        ChunkMap self = (ChunkMap) (Object) this;
        if (distance == 0) {
            ChunkStatus chunkStatus = distanceStatusProvider.apply(0);
            return chunkHolder.getOrScheduleFuture(chunkStatus, self).thenApply((either) -> {
                return either.mapLeft(List::of);
            });
        } else {
            List<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> list = new ArrayList();
            List<ChunkHolder> list2 = new ArrayList<>();
            ChunkPos chunkPos = chunkHolder.getPos();
            int holderX = chunkPos.x;
            int holderY = ColumnicChunkPos.getY(chunkPos);
            int holderZ = chunkPos.z;

            for (int cz = -distance; cz <= distance; ++cz) {
                for (int cx = -distance; cx <= distance; ++cx) {
//                    for (int cy = -Columnic.COLUMN_RENDER_DISTANCE; cy <= Columnic.COLUMN_RENDER_DISTANCE; ++cy) {
                    int dist = Math.max(Math.abs(cx), Math.abs(cz));
                    final ChunkPos chunkPos2 = ColumnicChunkPos.of(holderX + cx, holderY + 0, holderZ + cz);
                    long o = chunkPos2.toLong();
                    ChunkHolder chunkHolder2 = this.getUpdatingChunkIfPresent(o);
                    if (chunkHolder2 == null) {
                        return CompletableFuture.completedFuture(Either.right(new ChunkHolder.ChunkLoadingFailure() {
                            public String toString() {
                                return "Unloaded " + chunkPos2;
                            }
                        }));
                    }

                    ChunkStatus distanceStatus = distanceStatusProvider.apply(dist);
                    CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completableFuture = chunkHolder2.getOrScheduleFuture(distanceStatus, self);
                    list2.add(chunkHolder2);
                    list.add(completableFuture);
//                    }
                }
            }

            CompletableFuture<List<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> completableFuture2 = Util.sequence(list);
            CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> completableFuture3 = completableFuture2.thenApply((listx) -> {
                List<ChunkAccess> list3 = Lists.newArrayList();
                int i = 0;

                for (Iterator<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var7 = listx.iterator(); var7.hasNext(); ++i) {
                    final Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = var7.next();
                    if (either == null) {
                        throw this.debugFuturesAndCreateReportedException(new IllegalStateException("At least one of the chunk futures were null"), "n/a");
                    }

                    Optional<ChunkAccess> optional = either.left();
                    if (optional.isEmpty()) {
                        int index = i;
                        return Either.right(new ChunkHolder.ChunkLoadingFailure() {
                            @Override
                            public String toString() {
//                                ChunkPos chunk = ColumnicChunkPos.of(
//                                        holderX + index / Columnic.COLUMN_RENDER_DIAMETER,
//                                        holderY + index % Columnic.COLUMN_RENDER_DIAMETER,
//                                        holderZ + index / ((2 * distance + 1) * Columnic.COLUMN_RENDER_DIAMETER)
//                                );
//                                return "Unloaded " + chunk + " " + either.right().get();
                                ChunkPos chunk = ColumnicChunkPos.of(holderX + index % (distance * 2 + 1), holderY, holderZ + index / (distance * 2 + 1));
                                return "Unloaded " + chunk + " " + either.right().get();
                            }
                        });
                    }

                    list3.add(optional.get());
                }

                return Either.left(list3);
            });

            for (ChunkHolder chunkHolder3 : list2) {
                chunkHolder3.addSaveDependency("getChunkRangeFuture " + chunkPos + " " + distance, completableFuture3);
            }

            return completableFuture3;
        }
    }


    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    void updatePlayerStatus(ServerPlayer player, boolean track) {
        boolean bl = this.skipPlayer(player);
        boolean bl2 = this.playerMap.ignoredOrUnknown(player);

        int playerChunkX = SectionPos.blockToSectionCoord(player.getBlockX());
        int playerColumnY = Columnic.getColumnYFromSectionY(SectionPos.blockToSectionCoord(player.getBlockY()));
        int playerChunkZ = SectionPos.blockToSectionCoord(player.getBlockZ());
        if (track) {
            this.playerMap.addPlayer(SectionPos.asLong(playerChunkX, playerColumnY, playerChunkZ), player, bl);
            this.updatePlayerPos(player);
            if (!bl) {
                this.distanceManager.addPlayer(SectionPos.of(player), player);
            }
        } else {
            SectionPos sectionPos = player.getLastSectionPos();
            this.playerMap.removePlayer(sectionPos.chunk().toLong(), player);
            if (!bl2) {
                this.distanceManager.removePlayer(sectionPos, player);
            }
        }

        for (int x = playerChunkX - this.viewDistance - 1; x <= playerChunkX + this.viewDistance + 1; ++x) {
            for (int z = playerChunkZ - this.viewDistance - 1; z <= playerChunkZ + this.viewDistance + 1; ++z) {
                for (int y = playerColumnY - Columnic.COLUMN_RENDER_DISTANCE; y <= playerColumnY + Columnic.COLUMN_RENDER_DISTANCE; ++y) {
                    if (isChunkInRange(x, z, playerChunkX, playerChunkZ, this.viewDistance)) {
                        ChunkPos chunkPos = ColumnicChunkPos.of(x, y, z);
                        this.updateChunkTracking(player, chunkPos, new MutableObject<>(), !track, track);
                    }
                }
            }
        }
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public List<ServerPlayer> getPlayers(ChunkPos pos, boolean boundaryOnly) {
        Set<ServerPlayer> set = this.playerMap.getPlayers(pos.toLong());
        ImmutableList.Builder<ServerPlayer> builder = ImmutableList.builder();
        Iterator var5 = set.iterator();

        while (true) {
            ServerPlayer serverplayer;
            SectionPos sectionpos;
            do {
                if (!var5.hasNext()) {
                    return builder.build();
                }

                serverplayer = (ServerPlayer) var5.next();
                sectionpos = serverplayer.getLastSectionPos();
            } while ((!boundaryOnly || !isChunkOnRangeBorder(pos, sectionpos.chunk(), this.viewDistance)) && (boundaryOnly || !isChunkInRange(pos, sectionpos.chunk(), this.viewDistance)));

            builder.add(serverplayer);
        }
    }


    @Unique
    private static boolean isChunkOnRangeBorder(ChunkPos a, ChunkPos b, int viewDist) {
        if (!isChunkInRange(a, b, viewDist)) {
            return false;
        } else {
            return !isChunkInRange(offset(a, 1, 1, 1), b, viewDist)
                    || !isChunkInRange(offset(a, -1, 1, 1), b, viewDist)
                    || !isChunkInRange(offset(a, 1, -1, 1), b, viewDist)
                    || !isChunkInRange(offset(a, -1, -1, 1), b, viewDist)
                    || !isChunkInRange(offset(a, 1, 1, -1), b, viewDist)
                    || !isChunkInRange(offset(a, -1, 1, -1), b, viewDist)
                    || !isChunkInRange(offset(a, 1, -1, -1), b, viewDist)
                    || !isChunkInRange(offset(a, -1, -1, -1), b, viewDist);
        }
    }

    @Unique
    private static ChunkPos offset(ChunkPos pos, int x, int y, int z) {
        return ColumnicChunkPos.of(pos.x + x, ColumnicChunkPos.getY(pos) + y, pos.z + z);
    }

    @Unique
    private static boolean isChunkInRange(ChunkPos a, ChunkPos b, int viewDistance) {
        return Math.abs(ColumnicChunkPos.getY(a) - ColumnicChunkPos.getY(b)) <= Columnic.COLUMN_RENDER_DISTANCE && isChunkInRange(a.x, a.z, b.x, b.z, viewDistance);
    }


    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public void move(ServerPlayer player) {
        for (ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap.values()) {
            if (chunkmap$trackedentity.entity == player) {
                chunkmap$trackedentity.updatePlayers(this.level.players());
            } else {
                chunkmap$trackedentity.updatePlayer(player);
            }
        }


        int playerColumnY = ColumnicChunkPos.getY(player.chunkPosition());
        int playerSectionX = SectionPos.blockToSectionCoord(player.getBlockX());
        int playerSectionZ = SectionPos.blockToSectionCoord(player.getBlockZ());

        SectionPos lastSection = player.getLastSectionPos();
        SectionPos newSection = SectionPos.of(player);

        long lastChunkPos = lastSection.chunk().toLong();
        long newChunkPos = newSection.chunk().toLong();

        boolean ignored = this.playerMap.ignored(player);
        boolean skip = this.skipPlayer(player);
        boolean isNewChunk = lastSection.asLong() != newSection.asLong();
        if (isNewChunk || ignored != skip) {
            this.updatePlayerPos(player);
            if (!ignored) {
                this.distanceManager.removePlayer(lastSection, player);
            }

            if (!skip) {
                this.distanceManager.addPlayer(newSection, player);
            }

            if (!ignored && skip) {
                this.playerMap.ignorePlayer(player);
            }

            if (ignored && !skip) {
                this.playerMap.unIgnorePlayer(player);
            }

            if (lastChunkPos != newChunkPos) {
                this.playerMap.updatePlayer(lastChunkPos, newChunkPos, player);
            }
        }

        int columnY = ColumnicChunkPos.getY(player.chunkPosition());


        int lastColumnY = ColumnicChunkPos.getY(lastSection.chunk());
        int lastSectionX = lastSection.x();
        int lastSectionZ = lastSection.z();
        int viewDistPlusOne = this.viewDistance + 1;
        int cx;
        int cy;
        int cz;

        if (Math.abs(lastSectionX - playerSectionX) <= viewDistPlusOne * 2
                && Math.abs(lastSectionZ - playerSectionZ) <= viewDistPlusOne * 2
                && Math.abs(lastColumnY - columnY) < Columnic.COLUMN_RENDER_DISTANCE * 2) {
            cx = Math.min(playerSectionX, lastSectionX) - viewDistPlusOne;
            cy = Math.min(columnY, lastColumnY) - Columnic.COLUMN_RENDER_DISTANCE;
            cz = Math.min(playerSectionZ, lastSectionZ) - viewDistPlusOne;

            int regionMaxX = Math.max(playerSectionX, lastSectionX) + viewDistPlusOne;
            int regionMaxZ = Math.max(playerSectionZ, lastSectionZ) + viewDistPlusOne;
            int regionMaxY = Math.max(columnY, lastColumnY) + Columnic.COLUMN_RENDER_DISTANCE;
            for (int x = cx; x <= regionMaxX; ++x) {
                for (int z = cz; z <= regionMaxZ; ++z) {
                    for (int cY = cy; cY <= regionMaxY; ++cY) {
                        boolean flag5 = isChunkInRange(x, z, lastSectionX, lastSectionZ, this.viewDistance);
                        boolean flag6 = isChunkInRange(x, z, playerSectionX, playerSectionZ, this.viewDistance);
                        this.updateChunkTracking(player, ColumnicChunkPos.of(x, cY, z), new MutableObject<>(), flag5, flag6);
                    }
                }
            }
        } else {
            for (cx = lastSectionX - viewDistPlusOne; cx <= lastSectionX + viewDistPlusOne; ++cx) {
                for (cz = lastSectionZ - viewDistPlusOne; cz <= lastSectionZ + viewDistPlusOne; ++cz) {
                    for (cy = lastColumnY - Columnic.COLUMN_RENDER_DISTANCE; cy <= lastColumnY + Columnic.COLUMN_RENDER_DISTANCE; ++cy) {
                        if (isChunkInRange(cx, cz, playerSectionX, playerSectionZ, this.viewDistance)) {
                            this.updateChunkTracking(player, ColumnicChunkPos.of(cx, cy, cz), new MutableObject<>(), false, true);
                        }
                    }
                }
            }

            for (cx = playerSectionX - viewDistPlusOne; cx <= playerSectionX + viewDistPlusOne; ++cx) {
                for (cz = playerSectionZ - viewDistPlusOne; cz <= playerSectionZ + viewDistPlusOne; ++cz) {
                    for (cy = columnY - Columnic.COLUMN_RENDER_DISTANCE; cy <= columnY + Columnic.COLUMN_RENDER_DISTANCE; ++cy) {
                        if (isChunkInRange(cx, cz, lastSectionX, lastSectionZ, this.viewDistance)) {
                            this.updateChunkTracking(player, ColumnicChunkPos.of(cx, cy, cz), new MutableObject<>(), true, false);
                        }
                    }
                }
            }
        }
    }

}
