package dev.playersarmor;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.render.OverlayTexture;
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

public final class SkinArmorRenderer<T extends LivingEntity> {
    private final BipedEntityModel<T> headModel = new BipedEntityModel<>(
            BipedEntityModel.getModelData(new Dilation(0.6F), 0.0F).getRoot().createPart(64, 64));
    private final BipedEntityModel<T> replacementHeadModel = new BipedEntityModel<>(
            BipedEntityModel.getModelData(Dilation.NONE, 0.0F).getRoot().createPart(64, 64));
    private final PlayerEntityModel<T> wideModel = new PlayerEntityModel<>(
            PlayerEntityModel.getTexturedModelData(new Dilation(0.6F), false)
                    .getRoot().createPart(64, 64), false);
    private final SkinBootsModel<T> bootsModel = new SkinBootsModel<>();

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       BipedEntityModel<T> contextModel, EquipmentSlot slot, SkinTextures skin,
                       boolean glint, boolean replacement) {
        BipedEntityModel<T> model;
        if (slot == EquipmentSlot.HEAD) {
            model = replacement ? replacementHeadModel : headModel;
            contextModel.copyBipedStateTo(model);
            model.setVisible(false);
            model.head.visible = true;
            model.hat.visible = true;
            model.hat.copyTransform(model.head);
        } else if (slot == EquipmentSlot.CHEST) {
            PlayerEntityModel<T> chestModel = wideModel;
            contextModel.copyBipedStateTo(chestModel);
            chestModel.setVisible(false);
            chestModel.body.visible = true;
            chestModel.leftArm.visible = true;
            chestModel.rightArm.visible = true;
            chestModel.jacket.visible = true;
            chestModel.leftSleeve.visible = true;
            chestModel.rightSleeve.visible = true;
            chestModel.jacket.copyTransform(chestModel.body);
            chestModel.leftSleeve.copyTransform(chestModel.leftArm);
            chestModel.rightSleeve.copyTransform(chestModel.rightArm);
            model = chestModel;
        } else if (slot == EquipmentSlot.LEGS) {
            contextModel.copyBipedStateTo(wideModel);
            wideModel.setVisible(false);
            wideModel.leftLeg.visible = true;
            wideModel.rightLeg.visible = true;
            wideModel.leftPants.visible = true;
            wideModel.rightPants.visible = true;
            wideModel.leftPants.copyTransform(wideModel.leftLeg);
            wideModel.rightPants.copyTransform(wideModel.rightLeg);
            model = wideModel;
        } else if (slot == EquipmentSlot.FEET) {
            contextModel.copyBipedStateTo(bootsModel);
            bootsModel.setVisible(false);
            bootsModel.leftLeg.visible = true;
            bootsModel.rightLeg.visible = true;
            model = bootsModel;
        } else {
            return;
        }

        VertexConsumer vertices = ItemRenderer.getArmorGlintConsumer(vertexConsumers,
                RenderLayer.getArmorCutoutNoCull(skin.texture()), glint);
        model.render(matrices, vertices, light, OverlayTexture.DEFAULT_UV);
    }
}
