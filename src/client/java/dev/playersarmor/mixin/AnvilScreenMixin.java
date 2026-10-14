package dev.playersarmor.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.playersarmor.ArmorAppearance;
import dev.playersarmor.ArmorFlagPanel;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin extends ForgingScreen<AnvilScreenHandler> {
    @Shadow
    private TextFieldWidget nameField;

    @Unique
    private ArmorFlagPanel playersarmor$flags;

    protected AnvilScreenMixin(AnvilScreenHandler handler, PlayerInventory inventory, Text title, Identifier texture) {
        super(handler, inventory, title, texture);
    }

    @Inject(method = "setup", at = @At("TAIL"))
    private void playersarmor$createFlagPanel(CallbackInfo ci) {
        boolean open = playersarmor$flags != null && playersarmor$flags.isOpen();
        playersarmor$flags = new ArmorFlagPanel(textRenderer, nameField,
                () -> handler.getSlot(0).getStack(), this::playersarmor$positionFlagPanel);
        addDrawable(playersarmor$flags);
        playersarmor$flags.buttons().forEach(this::addDrawableChild);
        playersarmor$flags.setOpen(open);
    }

    @Unique
    private void playersarmor$positionFlagPanel() {
        int extraWidth = playersarmor$flags.isOpen() ? ArmorFlagPanel.WIDTH + ArmorFlagPanel.GAP : 0;
        x = (width - backgroundWidth - extraWidth) / 2;
        nameField.setPosition(x + 62, y + 24);
        playersarmor$flags.setPosition(x, y, backgroundWidth);
    }

    @Inject(method = "onRenamed", at = @At("TAIL"))
    private void playersarmor$updateFlagSelection(String name, CallbackInfo ci) {
        if (playersarmor$flags != null) {
            playersarmor$flags.refresh();
        }
    }

    @Inject(method = "onSlotUpdate", at = @At("TAIL"))
    private void playersarmor$updateFlagPanel(ScreenHandler handler, int slot, ItemStack stack, CallbackInfo ci) {
        if (playersarmor$flags != null) {
            playersarmor$flags.refresh();
        }
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        return super.isClickOutsideBounds(mouseX, mouseY, left, top, button)
                && (playersarmor$flags == null || !playersarmor$flags.contains(mouseX, mouseY));
    }

    @WrapOperation(method = "onSlotUpdate", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;getName()Lnet/minecraft/text/Text;"))
    private Text playersarmor$keepFlagsInRenameField(ItemStack stack, Operation<Text> original) {
        if (ArmorAppearance.fromStack(stack) != null) {
            return stack.get(DataComponentTypes.CUSTOM_NAME);
        }
        return original.call(stack);
    }
}
