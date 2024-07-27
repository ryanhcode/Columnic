package dev.ryanhcode.columnic.fabric;

import dev.ryanhcode.columnic.Columnic;
import net.fabricmc.api.ModInitializer;

public class ColumnicFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Columnic.init();
    }
}