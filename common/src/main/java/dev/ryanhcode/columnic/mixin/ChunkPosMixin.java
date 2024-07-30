package dev.ryanhcode.columnic.mixin;

import dev.ryanhcode.columnic.Columnic;
import dev.ryanhcode.columnic.ColumnicChunkPos;
import dev.ryanhcode.columnic.duck.ChunkPosDuck;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Mixin(ChunkPos.class)
public class ChunkPosMixin implements ChunkPosDuck {

    @Mutable
    @Shadow
    @Final
    public int x;

    @Mutable
    @Shadow
    @Final
    public int z;

    /**
     * @reason Columnic stores multiple layers of chunks.
     */
    @Unique
    private int columnic$y = 0;

    /**
     * @author Ocelot
     * @reason Columnic stores multiple layers of chunks
     */
    @Overwrite
    public static Stream<ChunkPos> rangeClosed(ChunkPos center, int radius) {
        return rangeClosed(ColumnicChunkPos.of(center.x - radius, ColumnicChunkPos.getY(center), center.z - radius), ColumnicChunkPos.of(center.x + radius,  ColumnicChunkPos.getY(center), center.z + radius));
    }
    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
//    @Overwrite
//    public static Stream<ChunkPos> rangeClosed(final ChunkPos start, final ChunkPos end) {
//        int xLen = Math.abs(start.x - end.x) + 1;
//        int zLen = Math.abs(start.z - end.z) + 1;
//        int yLen = Math.abs(((ChunkPosDuck) start).getY() - ((ChunkPosDuck) end).getY()) + 1;
//        final int k = start.x < end.x ? 1 : -1;
//        final int l = start.z < end.z ? 1 : -1;
//        final int m = ((ChunkPosDuck) start).getY() < ((ChunkPosDuck) end).getY() ? 1 : -1;
//        return StreamSupport.stream(new Spliterators.AbstractSpliterator<>((long) xLen * zLen, 64) {
//            @Nullable
//            private ChunkPos pos;
//
//            public boolean tryAdvance(Consumer<? super ChunkPos> consumer) {
//                if (this.pos == null) {
//                    this.pos = start;
//                } else {
//                    int x = this.pos.x;
//                    int z = this.pos.z;
//                    int y = ((ChunkPosDuck) this.pos).getY();
//
//
//                    if (x == end.x && z == end.z && y == ((ChunkPosDuck) end).getY()) {
//                        return false;
//                    }
//
//                    if (x == end.x) {
//                        if (z == end.z) {
//                            this.pos = ColumnicChunkPos.of(start.x, y + m, start.z);
//                        } else {
//                            this.pos = ColumnicChunkPos.of(start.x, y, z + l);
//                        }
//                    } else {
//                        this.pos = ColumnicChunkPos.of(x + k, y, z);
//                    }
//                }
//
//                consumer.accept(this.pos);
//                return true;
//            }
//        }, false);
//    }
    @Overwrite
    public static Stream<ChunkPos> rangeClosed(final ChunkPos start, final ChunkPos end) {
        int i = Math.abs(start.x - end.x) + 1;
        int j = Math.abs(start.z - end.z) + 1;
        final int k = start.x < end.x ? 1 : -1;
        final int l = start.z < end.z ? 1 : -1;
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<ChunkPos>((long)(i * j), 64) {
            @Nullable
            private ChunkPos pos;

            public boolean tryAdvance(Consumer<? super ChunkPos> consumer) {
                if (this.pos == null) {
                    this.pos = start;
                } else {
                    int i = this.pos.x;
                    int j = this.pos.z;
                    if (i == end.x) {
                        if (j == end.z) {
                            return false;
                        }

                        this.pos = ColumnicChunkPos.of(start.x, ColumnicChunkPos.getY(start), j + l);
                    } else {
                        this.pos = ColumnicChunkPos.of(i + k, ColumnicChunkPos.getY(start), j);
                    }
                }

                consumer.accept(this.pos);
                return true;
            }
        }, false);
    }

    private static long asLong(int x, int y, int z) {
        return SectionPos.asLong(x, y, z);
    }

    /**
     * Columnic fundamentally changes the way chunk positions are stored.
     * This is overwritten to intentionally cause crashes if other mods try and modify the same code,
     * as it is critical to the operation of Columnic.
     *
     * @reason Columnic chunks.
     * @author RyanH
     */
    @Overwrite
    public static long asLong(BlockPos pos) {
        return SectionPos.asLong(SectionPos.blockToSectionCoord(pos.getX()), Columnic.getColumnY(pos), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    /**
     * Columnic fundamentally changes the way chunk positions are stored.
     * This is overwritten to intentionally cause crashes if other mods try and modify the same code,
     * as it is critical to the operation of Columnic.
     *
     * @reason Columnic chunks.
     * @author RyanH
     */
    @Overwrite
    public static long asLong(int x, int z) {
        return asLong(x, 0, z);
    }

    /**
     * Columnic fundamentally changes the way chunk positions are stored.
     * This is overwritten to intentionally cause crashes if other mods try and modify the same code,
     * as it is critical to the operation of Columnic.
     *
     * @reason Columnic chunks.
     * @author RyanH
     */
    @Overwrite
    public static int getX(long chunkAsLong) {
        return SectionPos.x(chunkAsLong);
    }

    /**
     * Columnic fundamentally changes the way chunk positions are stored.
     * This is overwritten to intentionally cause crashes if other mods try and modify the same code,
     * as it is critical to the operation of Columnic.
     *
     * @reason Columnic chunks.
     * @author RyanH
     */
    @Overwrite
    public static int getZ(long chunkAsLong) {
        return SectionPos.z(chunkAsLong);
    }

    @Inject(method = "Lnet/minecraft/world/level/ChunkPos;<init>(J)V", at = @At("RETURN"))
    private void columnic$init(long packedPos, CallbackInfo ci) {
        this.x = SectionPos.x(packedPos);
        this.columnic$y = SectionPos.y(packedPos);
        this.z = SectionPos.z(packedPos);
    }

    @Inject(method = "Lnet/minecraft/world/level/ChunkPos;<init>(Lnet/minecraft/core/BlockPos;)V", at = @At("RETURN"))
    private void columnic$init(BlockPos pos, CallbackInfo ci) {
        this.columnic$y = Columnic.getColumnY(pos);
    }

    /**
     * Columnic fundamentally changes the way chunk positions are stored.
     * This is overwritten to intentionally cause crashes if other mods try and modify the same code,
     * as it is critical to the operation of Columnic.
     *
     * @reason Columnic chunks.
     * @author RyanH
     */
    @Overwrite
    public long toLong() {
        return asLong(this.x, this.columnic$y, this.z);
    }

    @Override
    public int getY() {
        return this.columnic$y;
    }

    @Override
    public void setY(int y) {
        this.columnic$y = y;
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public int hashCode() {
        int i = this.x;
        i = 31 * i + this.columnic$y;
        return 31 * i + this.z;
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof ChunkPos chunkPos) {
            return this.x == chunkPos.x && this.z == chunkPos.z && this.columnic$y == ((ChunkPosDuck) chunkPos).getY();
        }
        return false;
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public String toString() {
        return "[" + this.x + ", " + this.columnic$y + ", " + this.z + "]";
    }
}
