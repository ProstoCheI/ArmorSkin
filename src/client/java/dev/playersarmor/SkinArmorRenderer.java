package dev.playersarmor;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
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
    private final PlayerEntityModel<T> wideModel = createPlayerModel(false);
    private final PlayerEntityModel<T> slimModel = createPlayerModel(true);

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       BipedEntityModel<T> contextModel, EquipmentSlot slot, SkinTextures skin,
                       boolean glint) {
        BipedEntityModel<T> model;
        if (slot == EquipmentSlot.HEAD) {
            model = headModel;
            contextModel.copyBipedStateTo(model);
            model.setVisible(false);
            model.head.visible = true;
            model.hat.visible = true;
            model.hat.copyTransform(model.head);
        } else if (slot == EquipmentSlot.CHEST) {
            PlayerEntityModel<T> chestModel = skin.model() == SkinTextures.Model.SLIM
                    ? slimModel : wideModel;
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
        } else {
            return;
        }

        VertexConsumer vertices = ItemRenderer.getArmorGlintConsumer(vertexConsumers,
                RenderLayer.getArmorCutoutNoCull(skin.texture()), glint);
        model.render(matrices, vertices, light, OverlayTexture.DEFAULT_UV);
    }

    private static <T extends LivingEntity> PlayerEntityModel<T> createPlayerModel(boolean slim) {
        ModelData data = PlayerEntityModel.getTexturedModelData(new Dilation(0.6F), slim);
        if (slim) {
            // Slim skin arms must also cover a wearer with wide arms and sleeves.
            Dilation armDilation = new Dilation(1.3F);
            Dilation sleeveDilation = armDilation.add(0.25F);
            ModelPartData root = data.getRoot();
            root.addChild("right_arm", ModelPartBuilder.create().uv(40, 16)
                    .cuboid(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, armDilation),
                    ModelTransform.pivot(-5.0F, 2.5F, 0.0F));
            root.addChild("left_arm", ModelPartBuilder.create().uv(32, 48)
                    .cuboid(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, armDilation),
                    ModelTransform.pivot(5.0F, 2.5F, 0.0F));
            root.addChild("right_sleeve", ModelPartBuilder.create().uv(40, 32)
                    .cuboid(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, sleeveDilation),
                    ModelTransform.pivot(-5.0F, 2.5F, 0.0F));
            root.addChild("left_sleeve", ModelPartBuilder.create().uv(48, 48)
                    .cuboid(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, sleeveDilation),
                    ModelTransform.pivot(5.0F, 2.5F, 0.0F));
        }

        return new PlayerEntityModel<>(data.getRoot().createPart(64, 64), slim);
    }
}
