package dev.playersarmor.mixin;

import dev.playersarmor.ArmorAppearance;
import dev.playersarmor.PlayerSkinCache;
import dev.playersarmor.SkinHelmetItemRenderer;
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
    private final SkinHelmetItemRenderer playersarmor$helmetRenderer = new SkinHelmetItemRenderer();

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At("HEAD"), cancellable = true)
    private void playersarmor$renderHelmetItem(ItemStack stack, ModelTransformationMode mode,
                                              boolean leftHanded, MatrixStack matrices,
                                              VertexConsumerProvider vertexConsumers,
                                              int light, int overlay, BakedModel model, CallbackInfo ci) {
        ArmorAppearance appearance = ArmorAppearance.fromStack(stack);
        if (appearance == null || appearance.slot() != EquipmentSlot.HEAD) {
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
            playersarmor$helmetRenderer.render(matrices, vertexConsumers, light, overlay, skin, stack.hasGlint());
        } finally {
            matrices.pop();
        }
        ci.cancel();
    }
}
