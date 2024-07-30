package dev.ryanhcode.columnic;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Columnic {
    public static final String MOD_ID = "columnic";
	public static Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final int BASE_MIN_SECTION = 0;
	public static final int BASE_MAX_SECTION = 16;

	/**
	 * The amount of columns to render above and below the player.
	 */
	public static final int COLUMN_RENDER_DISTANCE = 1;

	/**
	 * The total side length of the column render box.
	 */
	public static final int COLUMN_RENDER_DIAMETER = COLUMN_RENDER_DISTANCE * 2 + 1;

	/**
	 * Effective chunk minimum
	 */
	public static final int EFFECTIVE_MINIMUM_CHUNK = -16 * 100_000;

	/**
	 * Blocks per column
	 */
	public static final int BLOCKS_PER_COLUMN = (BASE_MAX_SECTION - BASE_MIN_SECTION) << 4;

	/**
	 * Effective chunk maximum
	 */
	public static final int EFFECTIVE_MAXIMUM = 16 * 100_000 - 1;

    public static void init() {

    }

	/**
	 * Gets the column Y coordinate the block resides in.
	 */
	public static int getColumnY(BlockPos pos) {
		int blockY = pos.getY();
		int sectionY = SectionPos.blockToSectionCoord(blockY);

		return getColumnYFromSectionY(sectionY);
	}

	/**
	 * Gets the column Y coordinate the block resides in.
	 */
	public static int getColumnY(int blockY) {
		int sectionY = SectionPos.blockToSectionCoord(blockY);
		return getColumnYFromSectionY(sectionY);
	}

	/**
	 * Gets the column Y coordinate the section resides in.
	 */
	public static int getColumnY(SectionPos pos) {
		int sectionY = pos.y();

		return getColumnYFromSectionY(sectionY);
	}

	/**
	 * Gets the column Y coordinate the section resides in.
	 */
	public static int getColumnYFromSectionY(int sectionY) {
		return (int) Math.floor((double) (sectionY - BASE_MIN_SECTION) / (BASE_MAX_SECTION - BASE_MIN_SECTION));
	}


	public static long chunkToSectionLong(long chunkLong, int y) {
		int x = ChunkPos.getX(chunkLong);
		int z = ChunkPos.getZ(chunkLong);
		return SectionPos.asLong(x, y, z);
	}

	public static long sectionToChunkLong(long sectionLong) {
		int x = SectionPos.x(sectionLong);
		int z = SectionPos.z(sectionLong);
		return ChunkPos.asLong(x, z);
	}

	@ExpectPlatform
	public static void dispatchUnloadChunkEvent(int x, int y, int z, int i, LevelChunk levelchunk) {

	}
}
