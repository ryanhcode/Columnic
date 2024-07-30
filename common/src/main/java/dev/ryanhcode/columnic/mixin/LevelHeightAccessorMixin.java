package dev.ryanhcode.columnic.mixin;

import net.minecraft.world.level.LevelHeightAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LevelHeightAccessor.class)
public interface LevelHeightAccessorMixin {



    /**
     * @author RyanH
     * @reason Columnic
     */
    @Overwrite
    default boolean isOutsideBuildHeight(int y) {
        return false;
    }


}
