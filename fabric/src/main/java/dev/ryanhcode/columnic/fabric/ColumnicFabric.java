package dev.ryanhcode.columnic.fabric;

import dev.ryanhcode.columnic.Columnic;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.client.multiplayer.ClientChunkCache;

public class ColumnicFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Columnic.init();
    }
}