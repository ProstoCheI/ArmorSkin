package dev.playersarmor.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.playersarmor.ArmorAppearance;
import dev.playersarmor.ArmorNameParser;
import dev.playersarmor.ModelPartVisibility;
import dev.playersarmor.PlayerSkinCache;
import dev.playersarmor.LegSectionRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> {
    @WrapOperation(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private void playersarmor$hideOriginalParts(EntityModel<T> model, MatrixStack matrices,
                                              VertexConsumer vertices, int light, int overlay, int color,
                                              Operation<Void> original, T entity, float yaw, float tickDelta,
                                              MatrixStack entityMatrices, VertexConsumerProvider vertexConsumers,
                                              int entityLight) {
        if (!(model instanceof BipedEntityModel<?> biped)
                || !(entity instanceof PlayerEntity || entity instanceof ArmorStandEntity)) {
            original.call(model, matrices, vertices, light, overlay, color);
            return;
        }

        boolean hideHead = playersarmor$hasSkin(entity, EquipmentSlot.HEAD);
        boolean hideChest = playersarmor$hasSkin(entity, EquipmentSlot.CHEST);
        boolean hideLegs = playersarmor$hasSkin(entity, EquipmentSlot.LEGS);
        ArmorAppearance boots = playersarmor$getAppearance(entity, EquipmentSlot.FEET);
        if (!hideHead && !hideChest && !hideLegs && boots == null) {
            original.call(model, matrices, vertices, light, overlay, color);
            return;
        }

        try (ModelPartVisibility visibility = new ModelPartVisibility()) {
            if (hideHead) {
                visibility.hide(biped.head, biped.hat);
            }
            if (hideChest) {
                visibility.hide(biped.body, biped.leftArm, biped.rightArm);
                if (biped instanceof PlayerEntityModel<?> player) {
                    visibility.hide(player.jacket, player.leftSleeve, player.rightSleeve);
                }
                if (biped instanceof ArmorStandModelAccessor stand) {
                    visibility.hide(stand.playersarmor$getRightBodyStick(), stand.playersarmor$getLeftBodyStick(),
                            stand.playersarmor$getShoulderStick());
                }
            }
            if (hideLegs || boots != null) {
                visibility.hide(biped.leftLeg, biped.rightLeg);
                if (biped instanceof PlayerEntityModel<?> player) {
                    visibility.hide(player.leftPants, player.rightPants);
                }
            }
            original.call(model, matrices, vertices, light, overlay, color);
        }
        if (boots != null && !hideLegs) {
            float bootDilation = ArmorNameParser.hasFlag(boots.flags(), 'r') ? 0.0F : 0.95F;
            LegSectionRenderer.renderUpper(biped, matrices, vertices, light, overlay, color, bootDilation);
        } else if (hideLegs && boots == null) {
            LegSectionRenderer.renderLower(biped, matrices, vertices, light, overlay, color);
        }
    }

    @Unique
    private static boolean playersarmor$hasSkin(LivingEntity entity, EquipmentSlot slot) {
        return playersarmor$getAppearance(entity, slot) != null;
    }

    @Unique
    @Nullable
    private static ArmorAppearance playersarmor$getAppearance(LivingEntity entity, EquipmentSlot slot) {
        ArmorAppearance appearance = ArmorAppearance.fromStack(entity.getEquippedStack(slot));
        return appearance != null && appearance.slot() == slot
                && PlayerSkinCache.getSkin(appearance.username()) != null ? appearance : null;
    }
}
