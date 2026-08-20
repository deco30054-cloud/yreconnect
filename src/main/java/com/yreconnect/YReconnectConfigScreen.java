package com.yreconnect;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class YReconnectConfigScreen extends Screen {
    private final Screen parent;
    private boolean enabled;
    private double triggerY;
    private boolean triggerAbove;
    private int reconnectDelayTicks;
    private TextFieldWidget triggerYField;

    public YReconnectConfigScreen(Screen parent) {
        super(Text.literal("YReconnect Config"));
        this.parent = parent;
        YReconnectConfig cfg = YReconnectConfig.get();
        this.enabled = cfg.enabled;
        this.triggerY = cfg.triggerY;
        this.triggerAbove = cfg.triggerAbove;
        this.reconnectDelayTicks = cfg.reconnectDelayTicks;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = 50;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Status: " + (enabled ? "§aON" : "§cOFF")),
                btn -> { enabled = !enabled; btn.setMessage(Text.literal("Status: " + (enabled ? "§aON" : "§cOFF"))); }
        ).dimensions(cx - 100, y, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Direction: " + (triggerAbove ? "§eAbove Y" : "§eBelow Y")),
                btn -> { triggerAbove = !triggerAbove; btn.setMessage(Text.literal("Direction: " + (triggerAbove ? "§eAbove Y" : "§eBelow Y"))); }
        ).dimensions(cx - 100, y + 25, 200, 20).build());

        triggerYField = new TextFieldWidget(textRenderer, cx - 100, y + 55, 200, 20, Text.literal("Y"));
        triggerYField.setMaxLength(10);
        triggerYField.setText(String.valueOf(triggerY));
        triggerYField.setChangedListener(t -> {
            try { triggerY = Double.parseDouble(t); triggerYField.setEditableColor(0xFFFFFF); }
            catch (NumberFormatException e) { triggerYField.setEditableColor(0xFF4444); }
        });
        addDrawableChild(triggerYField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Delay: -5 ticks"),
                btn -> { reconnectDelayTicks = Math.max(0, reconnectDelayTicks - 5); }
        ).dimensions(cx - 100, y + 85, 95, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Delay: +5 ticks"),
                btn -> { reconnectDelayTicks = Math.min(200, reconnectDelayTicks + 5); }
        ).dimensions(cx + 5, y + 85, 95, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Preset: Void (Y<-64)"),
                btn -> setPreset(-64, false)).dimensions(cx - 100, y + 115, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Preset: Y < 0"),
                btn -> setPreset(0, false)).dimensions(cx - 100, y + 140, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Preset: Y > 256"),
                btn -> setPreset(256, true)).dimensions(cx - 100, y + 165, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§aSave & Close"), btn -> saveAndClose())
                .dimensions(cx - 100, this.height - 40, 95, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§cCancel"), btn -> closeScreen())
                .dimensions(cx + 5, this.height - 40, 95, 20).build());
    }

    private void setPreset(double y, boolean above) {
        triggerY = y;
        triggerAbove = above;
        triggerYField.setText(String.valueOf(y));
        init();
    }

    private void saveAndClose() {
        try { triggerY = Double.parseDouble(triggerYField.getText()); } catch (NumberFormatException ignored) {}
        YReconnectConfig cfg = YReconnectConfig.get();
        cfg.enabled = enabled;
        cfg.triggerY = triggerY;
        cfg.triggerAbove = triggerAbove;
        cfg.reconnectDelayTicks = reconnectDelayTicks;
        YReconnectConfig.save();
        closeScreen();
    }

    private void closeScreen() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("§bYReconnect Config"), this.width / 2, 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7Trigger Y:"), this.width / 2, 108, 0xAAAAAA);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(String.format("§7Delay: §e%d §7ticks (%.1fs)", reconnectDelayTicks, reconnectDelayTicks / 20.0)),
                this.width / 2, 160, 0xAAAAAA);
    }

    @Override
    public boolean shouldPause() { return false; }
}
}
