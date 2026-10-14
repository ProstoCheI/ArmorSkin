package dev.playersarmor.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.playersarmor.ArmorAppearance;
import dev.playersarmor.ArmorNameParser;
import dev.playersarmor.ModelPartVisibility;
import dev.playersarmor.PlayerSkinCache;
import dev.playersarmor.SkinArmorRenderer;
import dev.playersarmor.LegSectionRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
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
    @Unique
    private final SkinArmorRenderer<T> playersarmor$renderer = new SkinArmorRenderer<>();

    @Unique
    private float playersarmor$bootDilation = Float.NaN;

    protected ArmorFeatureRendererMixin(FeatureRendererContext<T, BipedEntityModel<T>> context) {
        super(context);
    }

    @WrapMethod(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V")
    private void playersarmor$withBootClipping(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                              int light, T entity, float limbAngle, float limbDistance,
                                              float tickDelta, float animationProgress, float headYaw,
                                              float headPitch, Operation<Void> original) {
        float previous = playersarmor$bootDilation;
        playersarmor$bootDilation = Float.NaN;
        ArmorAppearance boots = ArmorAppearance.fromStack(entity.getEquippedStack(EquipmentSlot.FEET));
        if ((entity instanceof PlayerEntity || entity instanceof ArmorStandEntity)
                && boots != null && boots.slot() == EquipmentSlot.FEET
                && PlayerSkinCache.getSkin(boots.username()) != null) {
            playersarmor$bootDilation = ArmorNameParser.hasFlag(boots.flags(), 'r') ? 0.0F : 0.95F;
        }
        try {
            original.call(matrices, vertexConsumers, light, entity, limbAngle, limbDistance,
                    tickDelta, animationProgress, headYaw, headPitch);
        } finally {
            playersarmor$bootDilation = previous;
        }
    }

    @WrapOperation(method = "renderArmorParts", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private void playersarmor$clipArmorParts(BipedEntityModel<T> model, MatrixStack matrices,
                                           VertexConsumer vertices, int light, int overlay, int color,
                                           Operation<Void> original) {
        playersarmor$renderClippedArmor(model, matrices, vertices, light, overlay, color,
                () -> original.call(model, matrices, vertices, light, overlay, color));
    }

    @WrapOperation(method = {"renderTrim", "renderGlint"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"))
    private void playersarmor$clipArmorOverlay(BipedEntityModel<T> model, MatrixStack matrices,
                                             VertexConsumer vertices, int light, int overlay,
                                             Operation<Void> original) {
        playersarmor$renderClippedArmor(model, matrices, vertices, light, overlay, -1,
                () -> original.call(model, matrices, vertices, light, overlay));
    }

    @Unique
    private void playersarmor$renderClippedArmor(BipedEntityModel<T> model, MatrixStack matrices,
                                               VertexConsumer vertices, int light, int overlay,
                                               int color, Runnable original) {
        if (Float.isNaN(playersarmor$bootDilation)) {
            original.run();
            return;
        }

        try (ModelPartVisibility visibility = new ModelPartVisibility()) {
            visibility.hide(model.leftLeg, model.rightLeg);
            if (model instanceof PlayerEntityModel<?> player) {
                visibility.hide(player.leftPants, player.rightPants);
            }
            original.run();
        }
        LegSectionRenderer.renderUpper(model, matrices, vertices, light, overlay, color, playersarmor$bootDilation);
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private void playersarmor$renderArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                         T entity, EquipmentSlot slot, int light,
                                         BipedEntityModel<T> armorModel, CallbackInfo ci) {
        if (!(entity instanceof PlayerEntity || entity instanceof ArmorStandEntity)) {
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

        ArmorAppearance boots = null;
        if (slot == EquipmentSlot.LEGS) {
            boots = ArmorAppearance.fromStack(entity.getEquippedStack(EquipmentSlot.FEET));
            if (boots != null && (boots.slot() != EquipmentSlot.FEET
                    || PlayerSkinCache.getSkin(boots.username()) == null)) {
                boots = null;
            }
        }

        playersarmor$renderer.render(matrices, vertexConsumers, light, getContextModel(),
                appearance, skin, stack.hasGlint(), boots);
        ci.cancel();
    }
}
