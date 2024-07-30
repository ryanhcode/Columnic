package dev.ryanhcode.columnic.mixin;

import dev.ryanhcode.columnic.Columnic;
import dev.ryanhcode.columnic.ColumnicChunkPos;
import dev.ryanhcode.columnic.duck.LevelAccess3D;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess {

    @Shadow
    @Final
    private Level level;

    public LevelChunkMixin(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> biomeRegistry, long inhabitedTime, @Nullable LevelChunkSection[] sections, @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, biomeRegistry, inhabitedTime, sections, blendingData);
    }

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void columnic$getBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        if (!this.level.isDebug()) {
            try {
                int l = this.getSectionIndex(j);
                if ((l < 0 || l >= this.sections.length)) {
                    int sectionY = SectionPos.blockToSectionCoord(j);
                    int columnY = Columnic.getColumnYFromSectionY(sectionY);

                    ChunkPos threeDPos = ColumnicChunkPos.of(this.getPos().x, columnY, this.getPos().z);
                    ChunkAccess otherchunk = ((LevelAccess3D) this.level).getChunk3D(threeDPos);

                    if (otherchunk == null) {
                        cir.setReturnValue(Blocks.AIR.defaultBlockState());
                        return;
                    }

                    LevelChunkSection levelChunkSection = otherchunk.getSection(sectionY - ColumnicChunkPos.getMinSectionY(threeDPos));
                    cir.setReturnValue(levelChunkSection.getBlockState(i & 0xF, j & 0xF, k & 0xF));
                }
            } catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Getting block state");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Block being got");
                crashReportCategory.setDetail("Location", () -> CrashReportCategory.formatLocation((LevelHeightAccessor) this, i, j, k));
                throw new ReportedException(crashReport);
            }
        }

    }

    @Inject(method = "setBlockState", at = @At("HEAD"), cancellable = true)
    private void columnic$setBlockState(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        if (!this.level.isDebug()) {
            try {
                int l = this.getSectionIndex(j);
                if ((l < 0 || l >= this.sections.length)) {
                    int sectionY = SectionPos.blockToSectionCoord(j);
                    int columnY = Columnic.getColumnYFromSectionY(sectionY);

                    ChunkPos threeDPos = ColumnicChunkPos.of(SectionPos.blockToSectionCoord(i), columnY, SectionPos.blockToSectionCoord(k));
                    ChunkAccess otherchunk = ((LevelAccess3D) this.level).getChunk3D(threeDPos);

                    BlockPos offpos = new BlockPos(i, j - columnY * Columnic.BLOCKS_PER_COLUMN, k);

                    cir.setReturnValue(otherchunk.setBlockState(offpos, state, moved));
                }
            } catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Setting block state");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Block being set");
                crashReportCategory.setDetail("Location", () -> CrashReportCategory.formatLocation((LevelHeightAccessor) this, i, j, k));
                crashReportCategory.setDetail("State", state::toString);
                throw new ReportedException(crashReport);
            }
        }
    }

    @Override
    public int getSectionYFromSectionIndex(int sectionIndex) {
        return sectionIndex + ColumnicChunkPos.getMinSectionY(this.getPos());
    }
}
