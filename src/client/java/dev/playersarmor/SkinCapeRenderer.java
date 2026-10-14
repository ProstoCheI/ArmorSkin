package dev.playersarmor;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;

public final class SkinCapeRenderer {
    private final ModelPart cape;

    public SkinCapeRenderer() {
        ModelData data = new ModelData();
        data.getRoot().addChild("cape", ModelPartBuilder.create().uv(0, 0)
                .cuboid(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F), ModelTransform.NONE);
        cape = data.getRoot().createPart(64, 32).getChild("cape");
    }

    public static boolean shouldRender(ArmorAppearance appearance, SkinTextures skin) {
        return appearance.slot() == EquipmentSlot.CHEST
                && ArmorNameParser.hasFlag(appearance.flags(), 'c')
                && !ArmorNameParser.hasFlag(appearance.flags(), 'e') && skin.capeTexture() != null;
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay,
                       ModelPart body, SkinTextures skin, boolean child, float dilation) {
        render(matrices, vertices, light, overlay, body, skin, child, dilation, null, 0.0F);
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay,
                       ModelPart body, SkinTextures skin, boolean child, float dilation,
                       @Nullable LivingEntity wearer, float tickDelta) {
        if (skin.capeTexture() == null) {
            return;
        }

        matrices.push();
        try {
            if (child) {
                matrices.scale(0.5F, 0.5F, 0.5F);
                matrices.translate(0.0F, 1.5F, 0.0F);
            }
            if (wearer instanceof PlayerEntity player) {
                boolean sneaking = player.isInSneakingPose();
                matrices.translate(0.0F, sneaking ? 1.85F / 16.0F : 0.0F,
                        0.15625F + dilation / 16.0F + (sneaking ? 1.4F / 16.0F : 0.0F));
                double dx = MathHelper.lerp(tickDelta, player.prevCapeX, player.capeX)
                        - MathHelper.lerp(tickDelta, player.prevX, player.getX());
                double dy = MathHelper.lerp(tickDelta, player.prevCapeY, player.capeY)
                        - MathHelper.lerp(tickDelta, player.prevY, player.getY());
                double dz = MathHelper.lerp(tickDelta, player.prevCapeZ, player.capeZ)
                        - MathHelper.lerp(tickDelta, player.prevZ, player.getZ());
                rotateCape(matrices, dx, dy, dz,
                        MathHelper.lerpAngleDegrees(tickDelta, player.prevBodyYaw, player.bodyYaw),
                        MathHelper.lerp(tickDelta, player.prevStrideDistance, player.strideDistance),
                        MathHelper.lerp(tickDelta, player.prevHorizontalSpeed, player.horizontalSpeed), sneaking);
            } else {
                body.rotate(matrices);
                matrices.translate(0.0F, 0.0F, 0.15625F + dilation / 16.0F);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
            }
            cape.render(matrices, vertices.getBuffer(RenderLayer.getEntitySolid(skin.capeTexture())), light, overlay);
        } finally {
            matrices.pop();
        }
    }

    private static void rotateCape(MatrixStack matrices, double dx, double dy, double dz,
                                   float bodyYaw, float stride, float walkDistance, boolean sneaking) {
        double sin = MathHelper.sin(bodyYaw * MathHelper.RADIANS_PER_DEGREE);
        double cos = -MathHelper.cos(bodyYaw * MathHelper.RADIANS_PER_DEGREE);
        float lift = MathHelper.clamp((float) dy * 10.0F, -6.0F, 32.0F);
        float forward = MathHelper.clamp((float) (dx * sin + dz * cos) * 100.0F, 0.0F, 150.0F);
        float sideways = MathHelper.clamp((float) (dx * cos - dz * sin) * 100.0F, -20.0F, 20.0F);
        lift += MathHelper.sin(walkDistance * 6.0F) * 32.0F * stride;
        if (sneaking) {
            lift += 25.0F;
        }
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0F + forward / 2.0F + lift));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sideways / 2.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - sideways / 2.0F));
    }
}
