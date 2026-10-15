package dev.playersarmor;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class ArmorTagButton extends ButtonWidget {
    public static final int SIZE = 14;
    private final char tag;
    private boolean selected;

    public ArmorTagButton(char tag, Text label, PressAction action) {
        super(0, 0, SIZE, SIZE, label, action, DEFAULT_NARRATION_SUPPLIER);
        this.tag = tag;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = getX();
        int y = getY();
        boolean highlighted = active && (isHovered() || isFocused());
        int border = !active ? 0xFF777777 : highlighted ? 0xFFFFFFFF : 0xFF373737;
        int background = !active ? 0xFF999999 : selected ? 0xFF537D42 : 0xFF777777;
        context.fill(x, y, x + SIZE, y + SIZE, border);
        context.fill(x + 1, y + 1, x + SIZE - 1, y + SIZE - 1, background);
        context.fill(x + 1, y + 1, x + SIZE - 1, y + 2, selected ? 0xFF86AD6C : 0xFFAAAAAA);
        context.fill(x + 1, y + SIZE - 2, x + SIZE - 1, y + SIZE - 1, 0xFF454545);
        drawIcon(context, x + 2, y + 2, active ? 0xFFFFFFFF : 0xFFBBBBBB);
    }

    private void drawIcon(DrawContext context, int x, int y, int color) {
        switch (tag) {
            case 'r' -> {
                context.fill(x + 4, y, x + 7, y + 3, color);
                context.fill(x + 2, y + 4, x + 9, y + 6, color);
                context.fill(x + 4, y + 3, x + 7, y + 8, color);
                context.fill(x + 3, y + 7, x + 5, y + 10, color);
                context.fill(x + 6, y + 7, x + 8, y + 10, color);
            }
            case 's' -> {
                context.fill(x + 1, y + 1, x + 3, y + 9, color);
                context.fill(x + 7, y + 1, x + 9, y + 9, color);
                context.fill(x + 4, y + 4, x + 6, y + 6, color);
            }
            case 'c' -> {
                context.fill(x + 2, y + 1, x + 8, y + 3, color);
                context.fill(x + 1, y + 3, x + 9, y + 9, color);
                context.fill(x + 3, y + 4, x + 7, y + 8, 0xFF6E9EC0);
            }
            case 'e' -> {
                for (int i = 0; i < 4; i++) {
                    context.fill(x + i, y + i + 1, x + i + 1, y + 9 - i, color);
                    context.fill(x + 9 - i, y + i + 1, x + 10 - i, y + 9 - i, color);
                }
            }
            default -> {
                context.fill(x + 1, y + 1, x + 4, y + 4, color);
                context.fill(x + 6, y + 1, x + 9, y + 4, color);
                context.fill(x + 1, y + 6, x + 4, y + 9, color);
                context.fill(x + 6, y + 6, x + 9, y + 9, color);
            }
        }
    }
}
