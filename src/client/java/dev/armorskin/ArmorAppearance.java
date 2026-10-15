package dev.armorskin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public record ArmorAppearance(String username, String flags, EquipmentSlot slot) {
    @Nullable
    public static ArmorAppearance fromStack(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)) {
            return null;
        }

        EquipmentSlot slot = armor.getSlotType();
        if (slot != EquipmentSlot.HEAD && slot != EquipmentSlot.CHEST
                && slot != EquipmentSlot.LEGS && slot != EquipmentSlot.FEET) {
            return null;
        }

        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName == null) {
            return null;
        }

        String name = customName.getString();
        String username = ArmorNameParser.extractUsername(name);
        if (username == null) {
            return null;
        }

        return new ArmorAppearance(username, ArmorNameParser.extractFlags(name), slot);
    }
}
