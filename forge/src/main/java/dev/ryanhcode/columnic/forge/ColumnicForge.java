package dev.ryanhcode.columnic.forge;

import dev.ryanhcode.columnic.Columnic;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Columnic.MOD_ID)
public class ColumnicForge {
    public ColumnicForge() {
        Columnic.init();
    }
}