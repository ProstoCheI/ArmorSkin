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
import net.minecraft.util.math.RotationAxis;

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
                && ArmorNameParser.hasFlag(appearance.flags(), 'c') && skin.capeTexture() != null;
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay,
                       ModelPart body, SkinTextures skin, boolean child, float dilation) {
        if (skin.capeTexture() == null) {
            return;
        }

        matrices.push();
        try {
            if (child) {
                matrices.scale(0.5F, 0.5F, 0.5F);
                matrices.translate(0.0F, 1.5F, 0.0F);
            }
            body.rotate(matrices);
            matrices.translate(0.0F, 0.0F, 0.15625F + dilation / 16.0F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
            cape.render(matrices, vertices.getBuffer(RenderLayer.getEntitySolid(skin.capeTexture())), light, overlay);
        } finally {
            matrices.pop();
        }
    }
}
