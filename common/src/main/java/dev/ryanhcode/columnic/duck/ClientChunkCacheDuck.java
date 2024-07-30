package dev.ryanhcode.columnic.duck;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.function.Consumer;

public interface ClientChunkCacheDuck {
    LevelChunk replaceWithPacketData(int x, int y, int z, FriendlyByteBuf buffer, CompoundTag tag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer);
    void replaceBiomes(ChunkPos pos, FriendlyByteBuf readBuffer);
    void updateViewCenter(int x, int y, int z);
    void drop(int x, int y, int z);
}
