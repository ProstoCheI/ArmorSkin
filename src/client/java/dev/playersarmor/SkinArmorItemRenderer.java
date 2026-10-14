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
    private final PlayerEntityModel<LivingEntity> chestModel = createChestModel(false);
    private final PlayerEntityModel<LivingEntity> slimChestModel = createChestModel(true);
    private final PlayerEntityModel<LivingEntity> legsModel = new PlayerEntityModel<>(
            PlayerEntityModel.getTexturedModelData(Dilation.NONE, false).getRoot().createPart(64, 64), false);
    private final SkinBootsModel<LivingEntity> bootsModel = new SkinBootsModel<>(0.0F);

    public SkinArmorItemRenderer() {
        headModel.child = false;
        headModel.setVisible(false);
        headModel.head.visible = true;
        headModel.hat.visible = true;

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
                       int light, int overlay, ArmorAppearance appearance, SkinTextures skin, boolean glint) {
        EquipmentSlot slot = appearance.slot();
        BipedEntityModel<LivingEntity> model;
        if (slot == EquipmentSlot.HEAD) {
            matrices.translate(0.0F, -0.5F, 0.0F);
            matrices.scale(1.0F, -1.0F, -1.0F);
            model = headModel;
        } else if (slot == EquipmentSlot.CHEST) {
            matrices.translate(0.0F, -0.25F, 0.0F);
            matrices.scale(0.75F, -0.75F, -0.75F);
            matrices.translate(0.0F, -0.375F, 0.0F);
            model = getChestModel(appearance);
        } else if (slot == EquipmentSlot.LEGS) {
            matrices.translate(0.0F, -0.25F, 0.0F);
            matrices.scale(0.9F, -0.9F, -0.9F);
            matrices.translate(0.0F, -1.0F, 0.0F);
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
        if (slot == EquipmentSlot.LEGS) {
            LegSectionRenderer.renderUpper(model, matrices, vertices, light, overlay, -1, 0.0F);
        } else {
            model.render(matrices, vertices, light, overlay);
        }
    }

    private PlayerEntityModel<LivingEntity> getChestModel(ArmorAppearance appearance) {
        return ArmorNameParser.hasFlag(appearance.flags(), 's') ? slimChestModel : chestModel;
    }

    private static PlayerEntityModel<LivingEntity> createChestModel(boolean slim) {
        PlayerEntityModel<LivingEntity> model = new PlayerEntityModel<>(
                PlayerEntityModel.getTexturedModelData(Dilation.NONE, slim).getRoot().createPart(64, 64), slim);
        model.child = false;
        model.setVisible(false);
        model.body.visible = true;
        model.leftArm.visible = true;
        model.rightArm.visible = true;
        model.jacket.visible = true;
        model.leftSleeve.visible = true;
        model.rightSleeve.visible = true;
        return model;
    }
}
