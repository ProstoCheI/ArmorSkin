package dev.playersarmor.mixin;

import dev.playersarmor.ArmorAppearance;
import dev.playersarmor.PlayerSkinCache;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin<T extends LivingEntity>
        extends FeatureRenderer<T, BipedEntityModel<T>> {
    // Keep the costume outside the wearer's head and outer skin layer.
    @Unique
    private final BipedEntityModel<T> playersarmor$headModel = new BipedEntityModel<>(
            BipedEntityModel.getModelData(new Dilation(0.6F), 0.0F).getRoot().createPart(64, 64));

    protected ArmorFeatureRendererMixin(FeatureRendererContext<T, BipedEntityModel<T>> context) {
        super(context);
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private void playersarmor$renderHelmet(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                          T entity, EquipmentSlot slot, int light,
                                          BipedEntityModel<T> armorModel, CallbackInfo ci) {
        if (slot != EquipmentSlot.HEAD
                || !(entity instanceof PlayerEntity || entity instanceof ArmorStandEntity)) {
            return;
        }

        ItemStack stack = entity.getEquippedStack(slot);
        ArmorAppearance appearance = ArmorAppearance.fromStack(stack);
        if (appearance == null || appearance.slot() != slot) {
            return;
        }

        SkinTextures skin = PlayerSkinCache.getSkin(appearance.username());
        if (skin == null) {
            return;
        }

        getContextModel().copyBipedStateTo(playersarmor$headModel);
        playersarmor$headModel.setVisible(false);
        playersarmor$headModel.head.visible = true;
        playersarmor$headModel.hat.visible = true;
        playersarmor$headModel.hat.copyTransform(playersarmor$headModel.head);

        VertexConsumer vertices = ItemRenderer.getArmorGlintConsumer(vertexConsumers,
                RenderLayer.getArmorCutoutNoCull(skin.texture()), stack.hasGlint());
        playersarmor$headModel.render(matrices, vertices, light, OverlayTexture.DEFAULT_UV);
        ci.cancel();
    }
}
