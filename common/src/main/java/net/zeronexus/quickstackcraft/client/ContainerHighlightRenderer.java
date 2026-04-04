package net.zeronexus.quickstackcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side renderer for highlighting containers that received items.
 * Stores highlight targets with expiry timestamps and renders gold outlines.
 */
public final class ContainerHighlightRenderer {

    private static final long HIGHLIGHT_DURATION_MS = 3000;
    private static final float R = 1.0f, G = 0.84f, B = 0.0f, A = 0.8f; // Gold

    private static final List<BlockHighlight> blockHighlights = new ArrayList<>();
    private static final List<EntityHighlight> entityHighlights = new ArrayList<>();

    private ContainerHighlightRenderer() {}

    public static void onHighlightReceived(List<BlockPos> positions, List<Integer> entityIds) {
        long expiry = System.currentTimeMillis() + HIGHLIGHT_DURATION_MS;
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

        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());

        for (BlockHighlight h : blockHighlights) {
            AABB box = new AABB(h.pos).inflate(0.002); // Slight inflate to avoid z-fighting
            ShapeRenderer.renderLineBox(poseStack, lines,
                    box.minX - cameraPos.x, box.minY - cameraPos.y, box.minZ - cameraPos.z,
                    box.maxX - cameraPos.x, box.maxY - cameraPos.y, box.maxZ - cameraPos.z,
                    R, G, B, A);
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            for (EntityHighlight h : entityHighlights) {
                Entity entity = mc.level.getEntity(h.entityId);
                if (entity != null) {
                    AABB box = entity.getBoundingBox().inflate(0.002);
                    ShapeRenderer.renderLineBox(poseStack, lines,
                            box.minX - cameraPos.x, box.minY - cameraPos.y, box.minZ - cameraPos.z,
                            box.maxX - cameraPos.x, box.maxY - cameraPos.y, box.maxZ - cameraPos.z,
                            R, G, B, A);
                }
            }
        }
    }

    private record BlockHighlight(BlockPos pos, long expiry) {}
    private record EntityHighlight(int entityId, long expiry) {}
}
