package dev.playersarmor;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;

public final class SkinArmorItemRenderer {
    private final BipedEntityModel<LivingEntity> headModel = new BipedEntityModel<>(
            BipedEntityModel.getModelData(Dilation.NONE, 0.0F).getRoot().createPart(64, 64));
    private final PlayerEntityModel<LivingEntity> chestModel = new PlayerEntityModel<>(
            PlayerEntityModel.getTexturedModelData(Dilation.NONE, false).getRoot().createPart(64, 64), false);
    private final PlayerEntityModel<LivingEntity> legsModel = new PlayerEntityModel<>(
            PlayerEntityModel.getTexturedModelData(Dilation.NONE, false).getRoot().createPart(64, 64), false);
    private final SkinBootsModel<LivingEntity> bootsModel = new SkinBootsModel<>(0.0F);

    public SkinArmorItemRenderer() {
        headModel.child = false;
        headModel.setVisible(false);
        headModel.head.visible = true;
        headModel.hat.visible = true;

        chestModel.child = false;
        chestModel.setVisible(false);
        chestModel.body.visible = true;
        chestModel.leftArm.visible = true;
        chestModel.rightArm.visible = true;
        chestModel.jacket.visible = true;
        chestModel.leftSleeve.visible = true;
        chestModel.rightSleeve.visible = true;

        legsModel.child = false;
        legsModel.setVisible(false);
        legsModel.leftLeg.visible = true;
        legsModel.rightLeg.visible = true;
        legsModel.leftPants.visible = true;
        legsModel.rightPants.visible = true;

        bootsModel.child = false;
        bootsModel.setVisible(false);
        bootsModel.leftLeg.visible = true;
        bootsModel.rightLeg.visible = true;
        bootsModel.leftLeg.copyTransform(legsModel.leftLeg);
        bootsModel.rightLeg.copyTransform(legsModel.rightLeg);
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                       int light, int overlay, EquipmentSlot slot, SkinTextures skin, boolean glint) {
        BipedEntityModel<LivingEntity> model;
        if (slot == EquipmentSlot.HEAD) {
            matrices.translate(0.0F, -0.5F, 0.0F);
            matrices.scale(1.0F, -1.0F, -1.0F);
            model = headModel;
        } else if (slot == EquipmentSlot.CHEST) {
            matrices.translate(0.0F, -0.25F, 0.0F);
            matrices.scale(0.75F, -0.75F, -0.75F);
            matrices.translate(0.0F, -0.375F, 0.0F);
            model = chestModel;
        } else if (slot == EquipmentSlot.LEGS) {
            matrices.translate(0.0F, -0.25F, 0.0F);
            matrices.scale(0.9F, -0.9F, -0.9F);
            matrices.translate(0.0F, -1.125F, 0.0F);
            model = legsModel;
        } else if (slot == EquipmentSlot.FEET) {
            matrices.translate(0.0F, -0.25F, 0.0F);
            matrices.scale(1.2F, -1.2F, -1.2F);
            matrices.translate(0.0F, -1.375F, 0.0F);
            model = bootsModel;
        } else {
            return;
        }

        VertexConsumer vertices = ItemRenderer.getItemGlintConsumer(vertexConsumers,
                RenderLayer.getEntityTranslucent(skin.texture()), true, glint);
        model.render(matrices, vertices, light, overlay);
    }
}
