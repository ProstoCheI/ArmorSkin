package dev.playersarmor;

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
    public static final int WIDTH = 128;
    public static final int GAP = 4;
    private static final int HEIGHT = 134;
    private static final int NAME_LIMIT = 50;
    private static final char[] FLAGS = {'r', 's', 'c', 'e'};
    private final TextRenderer textRenderer;
    private final TextFieldWidget nameField;
    private final Supplier<ItemStack> input;
    private final Runnable layout;
    private final ButtonWidget toggle;
    private final List<ButtonWidget> options = new ArrayList<>();
    private boolean open;
    private int x;
    private int y;

    public ArmorFlagPanel(TextRenderer textRenderer, TextFieldWidget nameField,
                          Supplier<ItemStack> input, Runnable layout) {
        this.textRenderer = textRenderer;
        this.nameField = nameField;
        this.input = input;
        this.layout = layout;
        toggle = ButtonWidget.builder(Text.literal("+"), button -> setOpen(!open))
                .dimensions(0, 0, 18, 16)
                .tooltip(Tooltip.of(Text.translatable("playersarmor.flags.open"))).build();
        for (char flag : FLAGS) {
            options.add(ButtonWidget.builder(Text.empty(), button -> toggleFlag(flag))
                    .dimensions(0, 0, WIDTH - 12, 20)
                    .tooltip(Tooltip.of(Text.translatable("playersarmor.flags." + flag + ".tooltip"))).build());
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
        toggle.setPosition(anvilX + anvilWidth - 27, anvilY + 4);
        for (int i = 0; i < options.size(); i++) {
            options.get(i).setPosition(x + 6, y + 22 + i * 23);
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
        toggle.setMessage(Text.literal(open ? "-" : "+"));
        String name = nameField.getText();
        String flags = ArmorNameParser.extractFlags(name);
        boolean valid = ArmorNameParser.extractUsername(name) != null;
        boolean chest = armor && ((ArmorItem) stack.getItem()).getSlotType() == EquipmentSlot.CHEST;
        for (int i = 0; i < FLAGS.length; i++) {
            char flag = FLAGS[i];
            ButtonWidget button = options.get(i);
            boolean selected = ArmorNameParser.hasFlag(flags, flag);
            button.visible = armor && open;
            button.active = valid && (flag == 'r' || chest)
                    && (selected || ArmorNameParser.setFlag(name, flag, true).length() <= NAME_LIMIT);
            button.setMessage(Text.literal(selected ? "[x] " : "[ ] ")
                    .append(Text.translatable("playersarmor.flags." + flag)));
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
        context.drawText(textRenderer, Text.translatable("playersarmor.flags.title"), x + 7, y + 7, 0x404040, false);
        Text hint = Text.translatable(ArmorNameParser.extractUsername(nameField.getText()) == null
                ? "playersarmor.flags.enter_name" : "playersarmor.flags.cape_elytra");
        context.drawTextWrapped(textRenderer, hint, x + 7, y + 116, WIDTH - 14, 0x404040);
    }
}
