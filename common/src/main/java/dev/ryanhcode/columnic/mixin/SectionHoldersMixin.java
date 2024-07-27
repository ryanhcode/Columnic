package dev.ryanhcode.columnic.mixin;

import dev.ryanhcode.columnic.duck.SectionPosHolder;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({ LevelChunk.class, ChunkHolder.class, ProtoChunk.class})
public class SectionHoldersMixin implements SectionPosHolder {

    @Unique
    private SectionPos columnic$sectionPos;

    @Override
    public void setSectionPos(SectionPos sectionPos) {
        this.columnic$sectionPos = sectionPos;
    }

    @Override
    public SectionPos getSectionPos() {
        return this.columnic$sectionPos;
    }
}
