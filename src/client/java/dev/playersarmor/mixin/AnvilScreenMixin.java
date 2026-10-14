package dev.playersarmor.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.playersarmor.ArmorAppearance;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin {
    @WrapOperation(method = "onSlotUpdate", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;getName()Lnet/minecraft/text/Text;"))
    private Text playersarmor$keepFlagsInRenameField(ItemStack stack, Operation<Text> original) {
        if (ArmorAppearance.fromStack(stack) != null) {
            return stack.get(DataComponentTypes.CUSTOM_NAME);
        }
        return original.call(stack);
    }
}
