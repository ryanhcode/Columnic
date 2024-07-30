package dev.ryanhcode.columnic.mixin;

import com.mojang.authlib.GameProfile;
import dev.ryanhcode.columnic.Columnic;
import dev.ryanhcode.columnic.ColumnicChunkPos;
import dev.ryanhcode.columnic.duck.PacketYDuck;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    @Shadow public ServerGamePacketListenerImpl connection;

    public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    /**
     * @author RyanH
     * @reason Columnic chunks.
     */
    @Overwrite
    public void untrackChunk(ChunkPos chunkPos) {
        if (this.isAlive()) {
            ClientboundForgetLevelChunkPacket packet = new ClientboundForgetLevelChunkPacket(chunkPos.x, chunkPos.z);
            ((PacketYDuck) packet).setColumnY(ColumnicChunkPos.getY(chunkPos));
            this.connection.send(packet);
        }

    }


}
