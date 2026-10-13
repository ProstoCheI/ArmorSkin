package dev.playersarmor;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

public final class SkinHelmetItemRenderer {
    private final BipedEntityModel<LivingEntity> model = new BipedEntityModel<>(
            BipedEntityModel.getModelData(Dilation.NONE, 0.0F).getRoot().createPart(64, 64));

    public SkinHelmetItemRenderer() {
        model.child = false;
        model.setVisible(false);
        model.head.visible = true;
        model.hat.visible = true;
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                       int light, int overlay, SkinTextures skin, boolean glint) {
        matrices.translate(0.0F, -0.5F, 0.0F);
        matrices.scale(1.0F, -1.0F, -1.0F);

        VertexConsumer vertices = ItemRenderer.getItemGlintConsumer(vertexConsumers,
                RenderLayer.getEntityTranslucent(skin.texture()), true, glint);
        model.render(matrices, vertices, light, overlay);
    }
}
