package net.zeronexus.quickstackcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zeronexus.quickstackcraft.config.QscConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side renderer for highlighting containers (after a transfer, or via the preview keybind).
 * Outline colour, opacity and duration are read from the config.
 */
public final class ContainerHighlightRenderer {

    private static final List<BlockHighlight> blockHighlights = new ArrayList<>();
    private static final List<EntityHighlight> entityHighlights = new ArrayList<>();

    private ContainerHighlightRenderer() {}

    public static void onHighlightReceived(List<BlockPos> positions, List<Integer> entityIds) {
        long expiry = System.currentTimeMillis() + Math.max(100, QscConfig.highlightDurationMs);
        blockHighlights.clear();
        entityHighlights.clear();
        for (BlockPos pos : positions) {
            blockHighlights.add(new BlockHighlight(pos, expiry));
        }
        for (int id : entityIds) {
            entityHighlights.add(new EntityHighlight(id, expiry));
        }
    }

    public static void tick() {
        long now = System.currentTimeMillis();
        blockHighlights.removeIf(h -> now >= h.expiry);
        entityHighlights.removeIf(h -> now >= h.expiry);
    }

    public static boolean hasHighlights() {
        return !blockHighlights.isEmpty() || !entityHighlights.isEmpty();
    }

    /**
     * Called from LevelRendererMixin after world rendering to draw outlines.
     */
    public static void renderHighlights(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos) {
        if (!hasHighlights()) return;

        int rgb = parseColor(QscConfig.highlightColorHex);
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;
        float a = (float) Math.max(0.0, Math.min(1.0, QscConfig.highlightAlpha));

        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());

        for (BlockHighlight h : blockHighlights) {
            AABB box = new AABB(h.pos).inflate(0.002); // Slight inflate to avoid z-fighting
            LevelRenderer.renderLineBox(poseStack, lines,
                    box.minX - cameraPos.x, box.minY - cameraPos.y, box.minZ - cameraPos.z,
                    box.maxX - cameraPos.x, box.maxY - cameraPos.y, box.maxZ - cameraPos.z,
                    r, g, b, a);
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            for (EntityHighlight h : entityHighlights) {
                Entity entity = mc.level.getEntity(h.entityId);
                if (entity != null) {
                    AABB box = entity.getBoundingBox().inflate(0.002);
                    LevelRenderer.renderLineBox(poseStack, lines,
                            box.minX - cameraPos.x, box.minY - cameraPos.y, box.minZ - cameraPos.z,
                            box.maxX - cameraPos.x, box.maxY - cameraPos.y, box.maxZ - cameraPos.z,
                            r, g, b, a);
                }
            }
        }
    }

    private static int parseColor(String hex) {
        try {
            return Integer.parseInt(hex.replace("#", "").trim(), 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            return 0xFFD700; // gold fallback
        }
    }

    private record BlockHighlight(BlockPos pos, long expiry) {}
    private record EntityHighlight(int entityId, long expiry) {}
}
