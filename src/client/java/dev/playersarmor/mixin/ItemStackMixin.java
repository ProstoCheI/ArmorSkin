package dev.playersarmor.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.playersarmor.ArmorAppearance;
import dev.playersarmor.ArmorNameParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private Text playersarmor$hideFlags(Text original) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || !client.isOnThread() || client.currentScreen instanceof AnvilScreen) {
            return original;
        }

        if (ArmorAppearance.fromStack((ItemStack) (Object) this) == null) {
            return original;
        }

        String name = original.getString();
        String visible = ArmorNameParser.removeFlags(name);
        return name.equals(visible) ? original : Text.literal(visible).setStyle(original.getStyle());
    }
}
