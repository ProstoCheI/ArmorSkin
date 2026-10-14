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
import org.jetbrains.annotations.Nullable;

public final class SkinArmorRenderer<T extends LivingEntity> {
    private final BipedEntityModel<T> headModel = new BipedEntityModel<>(
            BipedEntityModel.getModelData(new Dilation(0.6F), 0.0F).getRoot().createPart(64, 64));
    private final BipedEntityModel<T> replacementHeadModel = new BipedEntityModel<>(
            BipedEntityModel.getModelData(Dilation.NONE, 0.0F).getRoot().createPart(64, 64));
    private final PlayerEntityModel<T> wideModel = new PlayerEntityModel<>(
            PlayerEntityModel.getTexturedModelData(new Dilation(0.6F), false)
                    .getRoot().createPart(64, 64), false);
    private final PlayerEntityModel<T> replacementModel = new PlayerEntityModel<>(
            PlayerEntityModel.getTexturedModelData(Dilation.NONE, false)
                    .getRoot().createPart(64, 64), false);
    private final SkinBootsModel<T> bootsModel = new SkinBootsModel<>();
    private final SkinBootsModel<T> replacementBootsModel = new SkinBootsModel<>(0.0F);

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       BipedEntityModel<T> contextModel, EquipmentSlot slot, SkinTextures skin,
                       boolean glint, boolean replacement, @Nullable ArmorAppearance boots) {
        BipedEntityModel<T> model;
        if (slot == EquipmentSlot.HEAD) {
            model = replacement ? replacementHeadModel : headModel;
            contextModel.copyBipedStateTo(model);
            model.setVisible(false);
            model.head.visible = true;
            model.hat.visible = true;
            model.hat.copyTransform(model.head);
        } else if (slot == EquipmentSlot.CHEST) {
            PlayerEntityModel<T> chestModel = replacement ? replacementModel : wideModel;
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
            PlayerEntityModel<T> legsModel = replacement ? replacementModel : wideModel;
            contextModel.copyBipedStateTo(legsModel);
            legsModel.setVisible(false);
            legsModel.leftLeg.visible = true;
            legsModel.rightLeg.visible = true;
            legsModel.leftPants.visible = true;
            legsModel.rightPants.visible = true;
            legsModel.leftPants.copyTransform(legsModel.leftLeg);
            legsModel.rightPants.copyTransform(legsModel.rightLeg);
            model = legsModel;
        } else if (slot == EquipmentSlot.FEET) {
            model = replacement ? replacementBootsModel : bootsModel;
            contextModel.copyBipedStateTo(model);
            model.setVisible(false);
            model.leftLeg.visible = true;
            model.rightLeg.visible = true;
        } else {
            return;
        }

        VertexConsumer vertices = ItemRenderer.getArmorGlintConsumer(vertexConsumers,
                RenderLayer.getArmorCutoutNoCull(skin.texture()), glint);
        if (slot == EquipmentSlot.LEGS && boots != null) {
            float bootDilation = ArmorNameParser.hasFlag(boots.flags(), 'r') ? 0.0F : 0.95F;
            UpperLegRenderer.render(model, matrices, vertices, light, OverlayTexture.DEFAULT_UV, -1, bootDilation);
        } else {
            model.render(matrices, vertices, light, OverlayTexture.DEFAULT_UV);
        }
    }
}
