package dev.playersarmor.mixin;

import dev.playersarmor.ArmorAppearance;
import dev.playersarmor.PlayerSkinCache;
import dev.playersarmor.SkinArmorRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
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

    protected ArmorFeatureRendererMixin(FeatureRendererContext<T, BipedEntityModel<T>> context) {
        super(context);
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

        playersarmor$renderer.render(matrices, vertexConsumers, light, getContextModel(),
                slot, skin, stack.hasGlint());
        ci.cancel();
    }
}
