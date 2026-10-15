package dev.armorskin.mixin;

import dev.armorskin.ArmorAppearance;
import dev.armorskin.ArmorNameParser;
import dev.armorskin.PlayerSkinCache;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeFeatureRenderer.class)
public abstract class CapeFeatureRendererMixin {
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;FFFFFF)V",
            at = @At("HEAD"), cancellable = true)
    private void armorskin$hideOriginalCape(MatrixStack matrices, VertexConsumerProvider vertices,
                                              int light, AbstractClientPlayerEntity player, float limbAngle,
                                              float limbDistance, float tickDelta, float animationProgress,
                                              float headYaw, float headPitch, CallbackInfo ci) {
        ArmorAppearance appearance = ArmorAppearance.fromStack(player.getEquippedStack(EquipmentSlot.CHEST));
        if (appearance != null && appearance.slot() == EquipmentSlot.CHEST
                && (ArmorNameParser.hasFlag(appearance.flags(), 'c')
                    || ArmorNameParser.hasFlag(appearance.flags(), 'e'))
                && PlayerSkinCache.getSkin(appearance.username()) != null) {
            ci.cancel();
        }
    }
}
