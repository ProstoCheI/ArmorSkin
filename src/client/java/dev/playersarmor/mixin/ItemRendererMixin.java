package dev.playersarmor.mixin;

import dev.playersarmor.ArmorAppearance;
import dev.playersarmor.PlayerSkinCache;
import dev.playersarmor.SkinArmorItemRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow
    @Final
    private ItemModels models;

    @Unique
    private final SkinArmorItemRenderer playersarmor$itemRenderer = new SkinArmorItemRenderer();

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At("HEAD"), cancellable = true)
    private void playersarmor$renderArmorItem(ItemStack stack, ModelTransformationMode mode,
                                             boolean leftHanded, MatrixStack matrices,
                                             VertexConsumerProvider vertexConsumers,
                                             int light, int overlay, BakedModel model, CallbackInfo ci) {
        ArmorAppearance appearance = ArmorAppearance.fromStack(stack);
        if (appearance == null
                || (appearance.slot() != EquipmentSlot.HEAD && appearance.slot() != EquipmentSlot.CHEST
                && appearance.slot() != EquipmentSlot.LEGS)) {
            return;
        }

        SkinTextures skin = PlayerSkinCache.getSkin(appearance.username());
        if (skin == null) {
            return;
        }

        matrices.push();
        try {
            models.getModel(Items.PLAYER_HEAD).getTransformation().getTransformation(mode)
                    .apply(leftHanded, matrices);
            playersarmor$itemRenderer.render(matrices, vertexConsumers, light, overlay,
                    appearance.slot(), skin, stack.hasGlint());
        } finally {
            matrices.pop();
        }
        ci.cancel();
    }
}
