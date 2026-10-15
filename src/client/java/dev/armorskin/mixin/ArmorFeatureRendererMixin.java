package dev.armorskin.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.armorskin.ArmorAppearance;
import dev.armorskin.ArmorNameParser;
import dev.armorskin.ModelPartVisibility;
import dev.armorskin.PlayerSkinCache;
import dev.armorskin.SkinArmorRenderer;
import dev.armorskin.LegSectionRenderer;
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
    private final SkinArmorRenderer<T> armorskin$renderer = new SkinArmorRenderer<>();

    @Unique
    private float armorskin$bootDilation = Float.NaN;

    @Unique
    private float armorskin$tickDelta;

    protected ArmorFeatureRendererMixin(FeatureRendererContext<T, BipedEntityModel<T>> context) {
        super(context);
    }

    @WrapMethod(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V")
    private void armorskin$withBootClipping(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                              int light, T entity, float limbAngle, float limbDistance,
                                              float tickDelta, float animationProgress, float headYaw,
                                              float headPitch, Operation<Void> original) {
        float previous = armorskin$bootDilation;
        float previousTickDelta = armorskin$tickDelta;
        armorskin$tickDelta = tickDelta;
        armorskin$bootDilation = Float.NaN;
        ArmorAppearance boots = ArmorAppearance.fromStack(entity.getEquippedStack(EquipmentSlot.FEET));
        if ((entity instanceof PlayerEntity || entity instanceof ArmorStandEntity)
                && boots != null && boots.slot() == EquipmentSlot.FEET
                && PlayerSkinCache.getSkin(boots.username()) != null) {
            armorskin$bootDilation = ArmorNameParser.hasFlag(boots.flags(), 'r') ? 0.0F : 0.95F;
        }
        try {
            original.call(matrices, vertexConsumers, light, entity, limbAngle, limbDistance,
                    tickDelta, animationProgress, headYaw, headPitch);
        } finally {
            armorskin$bootDilation = previous;
            armorskin$tickDelta = previousTickDelta;
        }
    }

    @WrapOperation(method = "renderArmorParts", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private void armorskin$clipArmorParts(BipedEntityModel<T> model, MatrixStack matrices,
                                           VertexConsumer vertices, int light, int overlay, int color,
                                           Operation<Void> original) {
        armorskin$renderClippedArmor(model, matrices, vertices, light, overlay, color,
                () -> original.call(model, matrices, vertices, light, overlay, color));
    }

    @WrapOperation(method = {"renderTrim", "renderGlint"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"))
    private void armorskin$clipArmorOverlay(BipedEntityModel<T> model, MatrixStack matrices,
                                             VertexConsumer vertices, int light, int overlay,
                                             Operation<Void> original) {
        armorskin$renderClippedArmor(model, matrices, vertices, light, overlay, -1,
                () -> original.call(model, matrices, vertices, light, overlay));
    }

    @Unique
    private void armorskin$renderClippedArmor(BipedEntityModel<T> model, MatrixStack matrices,
                                               VertexConsumer vertices, int light, int overlay,
                                               int color, Runnable original) {
        if (Float.isNaN(armorskin$bootDilation)) {
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
        LegSectionRenderer.renderUpper(model, matrices, vertices, light, overlay, color, armorskin$bootDilation);
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private void armorskin$renderArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
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

        armorskin$renderer.render(matrices, vertexConsumers, light, getContextModel(),
                appearance, skin, stack.hasGlint(), boots, entity, armorskin$tickDelta);
        ci.cancel();
    }
}
