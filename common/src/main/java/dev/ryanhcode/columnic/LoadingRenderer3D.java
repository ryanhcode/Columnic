package dev.ryanhcode.columnic;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ryanhcode.columnic.duck.ChunkProgressDuck;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.world.level.chunk.ChunkStatus;

import java.awt.*;

public class LoadingRenderer3D {
    private static final Object2IntMap COLORS = Util.make(new Object2IntOpenHashMap(), (object2IntOpenHashMap) -> {
        object2IntOpenHashMap.defaultReturnValue(0);
        object2IntOpenHashMap.put(ChunkStatus.EMPTY, 5526612);
        object2IntOpenHashMap.put(ChunkStatus.STRUCTURE_STARTS, 10066329);
        object2IntOpenHashMap.put(ChunkStatus.STRUCTURE_REFERENCES, 6250897);
        object2IntOpenHashMap.put(ChunkStatus.BIOMES, 8434258);
        object2IntOpenHashMap.put(ChunkStatus.NOISE, 13750737);
        object2IntOpenHashMap.put(ChunkStatus.SURFACE, 7497737);
        object2IntOpenHashMap.put(ChunkStatus.CARVERS, 3159410);
        object2IntOpenHashMap.put(ChunkStatus.FEATURES, 2213376);
        object2IntOpenHashMap.put(ChunkStatus.INITIALIZE_LIGHT, 13421772);
        object2IntOpenHashMap.put(ChunkStatus.LIGHT, 16769184);
        object2IntOpenHashMap.put(ChunkStatus.SPAWN, 15884384);
        object2IntOpenHashMap.put(ChunkStatus.FULL, 16777215);
    });

    public static void renderLoadingScreen3D(GuiGraphics guiGraphics, StoringChunkProgressListener progressListener, int x, int y, int i, int j) {
        int k = i + j;
        int l = progressListener.getFullDiameter();
        int m = l * k - j;
        int n = progressListener.getDiameter();
        int o = n * k - j;
        int p = x - o / 2;
        int q = y - o / 2;
        int r = m / 2 + 1;
        int color = -16772609;
        guiGraphics.drawManaged(() -> {
            wrizz(guiGraphics, progressListener, x, y, i, j, r, color, n, p, k, q);
        });
    }

    private static void wrizz(GuiGraphics guiGraphics, StoringChunkProgressListener progressListener, int x, int y, int i, int j, int r, int color, int n, int p, int k, int q) {
        PoseStack ps = guiGraphics.pose();
        ps.pushPose();

        ps.translate(x, y, 0.0f);
        ps.mulPose(Axis.XN.rotationDegrees(-45.0f));
//        ps.mulPose(Axis.ZN.rotationDegrees(45.0f));


        double time = System.currentTimeMillis() / 1000.0 / 100.0;
        time = time - Math.floor(time);
        ps.mulPose(Axis.ZN.rotationDegrees((float) time * 360.0f));

        if (j != 0) {
            guiGraphics.fill(x * 0 - r, y - r, x - r + 1, y + r, color);
            guiGraphics.fill(x * 0 + r - 1, y - r, x + r, y + r, color);
            guiGraphics.fill(x * 0 - r, y - r, x + r, y - r + 1, color);
            guiGraphics.fill(x * 0 - r, y + r - 1, x + r, y + r, color);
        }

        for (int ry = 0; ry < n; ++ry) {
            ps.pushPose();
            ps.translate(0.0, 0.0, ry * i);
            for (int rx = 0; rx < n; ++rx) {
                for (int rz = 0; rz < n; ++rz) {
                    ChunkStatus chunkStatus = ((ChunkProgressDuck) progressListener).getStatus(ColumnicChunkPos.of(rx, ry, rz));
                    int t = p - x + rx * k;
                    int u = q - y + rz * k;

                    int alphaMask;


                    if (chunkStatus != ChunkStatus.EMPTY && chunkStatus != null) {
                        alphaMask = 0xFF000000;
                    } else {
                        alphaMask = 0x03000000;
                        if (ry == 0)
                            alphaMask = 0xFF000000;
                        else
                            alphaMask = 0x00000000;
                    }



                    int c = COLORS.getInt(chunkStatus);
//                    if (ry % 2 == 1) {
//                        Color jcolor = new Color(c);
//
//                        jcolor.darker().darker();
//                        c = jcolor.getRGB() & 0x00FFFFFF;
//                        alphaMask = 0x00000000;
//                    }
                    guiGraphics.fill(t, u, t + i, u + i, c | alphaMask);

                }
            }
            ps.popPose();
        }
        ps.popPose();
    }
}
