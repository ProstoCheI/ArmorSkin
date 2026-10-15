package dev.armorskin;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ArmorFlagPanel implements Drawable {
    public static final int WIDTH = 50;
    public static final int GAP = 1;
    private static final int HEIGHT = 166;
    private static final int NAME_LIMIT = 50;
    private static final char[] FLAGS = {'r', 's', 'c', 'e'};
    private final TextRenderer textRenderer;
    private final TextFieldWidget nameField;
    private final Supplier<ItemStack> input;
    private final Runnable layout;
    private final ArmorTagButton toggle;
    private final List<ArmorTagButton> options = new ArrayList<>();
    private boolean open;
    private int x;
    private int y;

    public ArmorFlagPanel(TextRenderer textRenderer, TextFieldWidget nameField,
                          Supplier<ItemStack> input, Runnable layout) {
        this.textRenderer = textRenderer;
        this.nameField = nameField;
        this.input = input;
        this.layout = layout;
        toggle = new ArmorTagButton('\0', Text.translatable("armorskin.flags.open"), button -> setOpen(!open));
        toggle.setTooltip(Tooltip.of(Text.translatable("armorskin.flags.open")));
        for (char flag : FLAGS) {
            Text label = Text.translatable("armorskin.flags." + flag);
            ArmorTagButton button = new ArmorTagButton(flag, label, clicked -> toggleFlag(flag));
            button.setTooltip(Tooltip.of(label.copy().append("\n")
                    .append(Text.translatable("armorskin.flags." + flag + ".tooltip"))));
            options.add(button);
        }
    }

    public List<ButtonWidget> buttons() {
        List<ButtonWidget> buttons = new ArrayList<>();
        buttons.add(toggle);
        buttons.addAll(options);
        return buttons;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
        layout.run();
        refresh();
    }

    public void setPosition(int anvilX, int anvilY, int anvilWidth) {
        x = anvilX + anvilWidth + GAP;
        y = anvilY;
        toggle.setPosition(anvilX + anvilWidth - 23, anvilY + 5);
        for (int i = 0; i < options.size(); i++) {
            options.get(i).setPosition(x + 10 + (i % 2) * 16, y + 33 + (i / 2) * 16);
        }
    }

    public void refresh() {
        ItemStack stack = input.get();
        boolean armor = !stack.isEmpty() && stack.getItem() instanceof ArmorItem;
        toggle.visible = armor;
        if (!armor && open) {
            open = false;
            layout.run();
        }
        toggle.setSelected(open);
        String name = nameField.getText();
        String flags = ArmorNameParser.extractFlags(name);
        boolean valid = ArmorNameParser.extractUsername(name) != null;
        boolean chest = armor && ((ArmorItem) stack.getItem()).getSlotType() == EquipmentSlot.CHEST;
        for (int i = 0; i < FLAGS.length; i++) {
            char flag = FLAGS[i];
            ArmorTagButton button = options.get(i);
            boolean selected = ArmorNameParser.hasFlag(flags, flag);
            button.visible = armor && open;
            button.active = valid && (flag == 'r' || chest)
                    && (selected || ArmorNameParser.setFlag(name, flag, true).length() <= NAME_LIMIT);
            button.setSelected(selected);
            button.setMessage(Text.translatable("armorskin.flags." + flag).append(": ")
                    .append(Text.translatable(selected ? "options.on" : "options.off")));
            Text description = Text.translatable("armorskin.flags." + flag).append("\n")
                    .append(Text.translatable("armorskin.flags." + flag + ".tooltip"));
            if (!valid) {
                description = description.copy().append("\n")
                        .append(Text.translatable("armorskin.flags.enter_name"));
            } else if (flag != 'r' && !chest) {
                description = description.copy().append("\n")
                        .append(Text.translatable("armorskin.flags.chest_only"));
            } else if (!button.active) {
                description = description.copy().append("\n")
                        .append(Text.translatable("armorskin.flags.name_limit"));
            }
            button.setTooltip(Tooltip.of(description));
        }
    }

    private void toggleFlag(char flag) {
        String name = nameField.getText();
        boolean selected = ArmorNameParser.hasFlag(ArmorNameParser.extractFlags(name), flag);
        String updated = ArmorNameParser.setFlag(name, flag, !selected);
        if (updated != null && updated.length() <= NAME_LIMIT) {
            nameField.setText(updated);
        }
        refresh();
    }

    public boolean contains(double mouseX, double mouseY) {
        return open && mouseX >= x && mouseX < x + WIDTH && mouseY >= y && mouseY < y + HEIGHT;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!open) {
            return;
        }
        context.fill(x, y, x + WIDTH, y + HEIGHT, 0xFF373737);
        context.fill(x + 1, y + 1, x + WIDTH - 1, y + HEIGHT - 1, 0xFFC6C6C6);
        context.fill(x + 1, y + 1, x + WIDTH - 1, y + 3, 0xFFFFFFFF);
        context.fill(x + 1, y + 3, x + 3, y + HEIGHT - 1, 0xFFFFFFFF);
        context.fill(x + WIDTH - 3, y + 3, x + WIDTH - 1, y + HEIGHT - 1, 0xFF555555);
        context.fill(x + 3, y + HEIGHT - 3, x + WIDTH - 3, y + HEIGHT - 1, 0xFF555555);
        Text title = Text.translatable("armorskin.flags.title");
        context.drawText(textRenderer, title, x + (WIDTH - textRenderer.getWidth(title)) / 2, y + 18, 0x404040, false);
    }
}
