package dev.playersarmor;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class SkinElytraRenderer {
    private static final Identifier DEFAULT_TEXTURE = Identifier.ofVanilla("textures/entity/elytra.png");
    private final ModelPart root = ElytraEntityModel.getTexturedModelData().createModel();
    private final ElytraEntityModel<LivingEntity> model = new ElytraEntityModel<>(root);

    public static boolean shouldRender(ArmorAppearance appearance) {
        return appearance.slot() == EquipmentSlot.CHEST && ArmorNameParser.hasFlag(appearance.flags(), 'e');
    }

    public static Identifier getTexture(ArmorAppearance appearance, SkinTextures skin) {
        if (ArmorNameParser.hasFlag(appearance.flags(), 'c') && skin.capeTexture() != null) {
            return skin.capeTexture();
        }
        return DEFAULT_TEXTURE;
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay,
                       ModelPart body, ArmorAppearance appearance, SkinTextures skin,
                       boolean child, float dilation, boolean glint,
                       @Nullable LivingEntity wearer) {
        model.child = child;
        if (wearer != null) {
            model.setAngles(wearer, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        } else {
            root.getChild("left_wing").resetTransform();
            root.getChild("right_wing").resetTransform();
        }

        matrices.push();
        try {
            if (!(wearer instanceof PlayerEntity)) {
                body.rotate(matrices);
            }
            matrices.translate(0.0F, 0.0F, 0.125F + dilation / 16.0F);
            VertexConsumer buffer = ItemRenderer.getArmorGlintConsumer(vertices,
                    RenderLayer.getArmorCutoutNoCull(getTexture(appearance, skin)), glint);
            model.render(matrices, buffer, light, overlay);
        } finally {
            matrices.pop();
        }
    }
}
