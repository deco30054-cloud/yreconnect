package com.yreconnect;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class YReconnectConfigScreen extends Screen {
    private final Screen parent;
    private boolean enabled;
    private double triggerY;
    private boolean triggerAbove;
    private int reconnectDelayTicks;

    private ButtonWidget enabledBtn;
    private ButtonWidget directionBtn;
    private TextFieldWidget triggerYField;
    private ButtonWidget delayDownBtn;
    private ButtonWidget delayUpBtn;

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
        int y = 55;
        int w = 200;

        // Enabled toggle
        enabledBtn = ButtonWidget.builder(enabledText(), btn -> {
            enabled = !enabled;
            btn.setMessage(enabledText());
        }).dimensions(cx - w / 2, y, w, 20).build();
        this.addDrawableChild(enabledBtn);

        // Direction toggle
        directionBtn = ButtonWidget.builder(directionText(), btn -> {
            triggerAbove = !triggerAbove;
            btn.setMessage(directionText());
        }).dimensions(cx - w / 2, y + 25, w, 20).build();
        this.addDrawableChild(directionBtn);

        // Y value field
        triggerYField = new TextFieldWidget(this.textRenderer, cx - w / 2, y + 55, w, 20, Text.literal("Trigger Y"));
        triggerYField.setMaxLength(10);
        triggerYField.setText(String.valueOf(triggerY));
        triggerYField.setPlaceholder(Text.literal("e.g. -64 or 320"));
        triggerYField.setChangedListener(text -> {
            try { triggerY = Double.parseDouble(text); triggerYField.setEditableColor(0xFFFFFF); }
            catch (NumberFormatException e) { triggerYField.setEditableColor(0xFF4444); }
        });
        this.addDrawableChild(triggerYField);

        // Delay controls
        delayDownBtn = ButtonWidget.builder(Text.literal("< "), btn -> {
            reconnectDelayTicks = Math.max(0, reconnectDelayTicks - 5);
            updateDelayLabel();
        }).dimensions(cx - w / 2, y + 85, 30, 20).build();
        this.addDrawableChild(delayDownBtn);

        delayUpBtn = ButtonWidget.builder(Text.literal(" >"), btn -> {
            reconnectDelayTicks = Math.min(200, reconnectDelayTicks + 5);
            updateDelayLabel();
        }).dimensions(cx + w / 2 - 30, y + 85, 30, 20).build();
        this.addDrawableChild(delayUpBtn);

        // Presets
        int pw = 58, pg = 4, py = y + 120;
        int px = cx - (4 * pw + 3 * pg) / 2;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Void"), btn -> applyPreset(-64, false)).dimensions(px, py, pw, 18).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Y<0"), btn -> applyPreset(0, false)).dimensions(px + pw + pg, py, pw, 18).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Y>256"), btn -> applyPreset(256, true)).dimensions(px + (pw + pg) * 2, py, pw, 18).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Y>320"), btn -> applyPreset(320, true)).dimensions(px + (pw + pg) * 3, py, pw, 18).build());

        // Save / Cancel
        int by = this.height - 32;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), btn -> saveAndClose()).dimensions(cx - 105, by, 100, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, btn -> closeWithoutSave()).dimensions(cx + 5, by, 100, 20).build());
    }

    private Text enabledText() { return Text.literal("Status: " + (enabled ? "§aEnabled" : "§cDisabled")); }
    private Text directionText() { return Text.literal("Trigger: " + (triggerAbove ? "§eAbove Y" : "§eBelow Y")); }
    private void updateDelayLabel() {}

    private void applyPreset(double y, boolean above) {
        triggerY = y; triggerAbove = above;
        triggerYField.setText(String.valueOf(y));
        triggerYField.setEditableColor(0xFFFFFF);
        directionBtn.setMessage(directionText());
    }

    private void saveAndClose() {
        try { triggerY = Double.parseDouble(triggerYField.getText()); } catch (NumberFormatException ignored) {}
        YReconnectConfig cfg = YReconnectConfig.get();
        cfg.enabled = enabled;
        cfg.triggerY = triggerY;
        cfg.triggerAbove = triggerAbove;
        cfg.reconnectDelayTicks = reconnectDelayTicks;
        YReconnectConfig.save();
        if (this.client != null) this.client.setScreen(parent);
    }

    private void closeWithoutSave() { if (this.client != null) this.client.setScreen(parent); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        int cx = this.width / 2;
        context.fill(cx - 120, 40, cx + 120, 215, 0xAA000000);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§b⚡ §fYReconnect Config §b⚡"), cx, 26, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7Trigger Y:"), cx, 112, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(
                String.format("§7Delay: §e%d ticks §7(%.1fs)", reconnectDelayTicks, reconnectDelayTicks / 20.0)),
                cx, 142, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7Presets:"), cx, 165, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§8Use /yreconnect config to reopen"), cx, this.height - 14, 0x666666);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override public boolean shouldPause() { return false; }
}
