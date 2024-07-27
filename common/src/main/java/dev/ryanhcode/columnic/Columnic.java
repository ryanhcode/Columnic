package dev.ryanhcode.columnic;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Columnic {
    public static final String MOD_ID = "columnic";
	public static Logger LOGGER = LogManager.getLogger(MOD_ID);

	/**
	 * The amount of columns to render above and below the player.
	 */
	public static final int COLUMN_RENDER_DISTANCE = 2;

	/**
	 * Effective chunk minimum
	 */
	public static final int EFFECTIVE_MINIMUM_CHUNK = -16 * 100_000;

	/**
	 * Effective chunk maximum
	 */
	public static final int EFFECTIVE_MAXIMUM = 16 * 100_000 - 1;

    public static void init() {

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
}
