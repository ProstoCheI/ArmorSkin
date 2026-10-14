package dev.playersarmor.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.playersarmor.ArmorAppearance;
import dev.playersarmor.PlayerSkinCache;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> {
    @WrapOperation(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private void playersarmor$hideOriginalHead(EntityModel<T> model, MatrixStack matrices,
                                               VertexConsumer vertices, int light, int overlay, int color,
                                               Operation<Void> original, T entity, float yaw, float tickDelta,
                                               MatrixStack entityMatrices, VertexConsumerProvider vertexConsumers,
                                               int entityLight) {
        if (!(model instanceof BipedEntityModel<?> biped)
                || !(entity instanceof PlayerEntity || entity instanceof ArmorStandEntity)) {
            original.call(model, matrices, vertices, light, overlay, color);
            return;
        }

        ArmorAppearance appearance = ArmorAppearance.fromStack(entity.getEquippedStack(EquipmentSlot.HEAD));
        if (appearance == null || appearance.slot() != EquipmentSlot.HEAD
                || PlayerSkinCache.getSkin(appearance.username()) == null) {
            original.call(model, matrices, vertices, light, overlay, color);
            return;
        }

        boolean headHidden = biped.head.hidden;
        boolean hatHidden = biped.hat.hidden;
        biped.head.hidden = true;
        biped.hat.hidden = true;
        try {
            original.call(model, matrices, vertices, light, overlay, color);
        } finally {
            biped.head.hidden = headHidden;
            biped.hat.hidden = hatHidden;
        }
    }
}
