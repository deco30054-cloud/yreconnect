package com.yreconnect;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class YReconnectConfigScreen extends Screen {
    private final Screen parent;
    private boolean enabled;
    private double triggerY;
    private boolean triggerAbove;
    private int reconnectDelayTicks;

    private CyclingButtonWidget<Boolean> enabledBtn;
    private CyclingButtonWidget<Boolean> directionBtn;
    private TextFieldWidget triggerYField;
    private DelaySliderWidget delaySlider;

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
        int startY = 60;
        int rowH = 28;
        int btnW = 200;

        enabledBtn = CyclingButtonWidget.<Boolean>builder(v -> v ? Text.literal("§aEnabled") : Text.literal("§cDisabled"))
                .values(Boolean.TRUE, Boolean.FALSE).initially(enabled)
                .tooltip(v -> Tooltip.of(Text.literal("Toggle auto-disconnect on or off.")))
                .build(cx - btnW / 2, startY, btnW, 20, Text.literal("Status: "), (btn, val) -> enabled = val);
        this.addDrawableChild(enabledBtn);

        directionBtn = CyclingButtonWidget.<Boolean>builder(v -> v ? Text.literal("§eAbove threshold") : Text.literal("§eBelow threshold"))
                .values(Boolean.FALSE, Boolean.TRUE).initially(triggerAbove)
                .tooltip(v -> Tooltip.of(Text.literal(v ? "Disconnect when Y is ABOVE trigger." : "Disconnect when Y is BELOW trigger.")))
                .build(cx - btnW / 2, startY + rowH, btnW, 20, Text.literal("Trigger: "), (btn, val) -> triggerAbove = val);
        this.addDrawableChild(directionBtn);

        triggerYField = new TextFieldWidget(this.textRenderer, cx - btnW / 2, startY + rowH * 2, btnW, 20, Text.literal("Trigger Y"));
        triggerYField.setMaxLength(10);
        triggerYField.setText(String.valueOf(triggerY));
        triggerYField.setPlaceholder(Text.literal("e.g. -64 or 320"));
        triggerYField.setChangedListener(text -> {
            try { triggerY = Double.parseDouble(text); triggerYField.setEditableColor(0xFFFFFF); }
            catch (NumberFormatException e) { triggerYField.setEditableColor(0xFF4444); }
        });
        this.addDrawableChild(triggerYField);

        delaySlider = new DelaySliderWidget(cx - btnW / 2, startY + rowH * 3, btnW, 20, reconnectDelayTicks);
        this.addDrawableChild(delaySlider);

        int presetY = startY + rowH * 4 + 8;
        int pW = 60, gap = 5, total = 4;
        int px = cx - (total * pW + (total - 1) * gap) / 2;

        ButtonWidget voidBtn = ButtonWidget.builder(Text.literal("Void"), btn -> applyPreset(-64, false)).dimensions(px, presetY, pW, 18).build();
        voidBtn.setTooltip(Tooltip.of(Text.literal("Below Y = -64 (void)"))); this.addDrawableChild(voidBtn);
        ButtonWidget zeroBtn = ButtonWidget.builder(Text.literal("Y < 0"), btn -> applyPreset(0, false)).dimensions(px + pW + gap, presetY, pW, 18).build();
        zeroBtn.setTooltip(Tooltip.of(Text.literal("Below Y = 0"))); this.addDrawableChild(zeroBtn);
        ButtonWidget highBtn = ButtonWidget.builder(Text.literal("Y>256"), btn -> applyPreset(256, true)).dimensions(px + (pW + gap) * 2, presetY, pW, 18).build();
        highBtn.setTooltip(Tooltip.of(Text.literal("Above Y = 256"))); this.addDrawableChild(highBtn);
        ButtonWidget topBtn = ButtonWidget.builder(Text.literal("Y>320"), btn -> applyPreset(320, true)).dimensions(px + (pW + gap) * 3, presetY, pW, 18).build();
        topBtn.setTooltip(Tooltip.of(Text.literal("Above Y = 320"))); this.addDrawableChild(topBtn);

        int bottomY = this.height - 32;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), btn -> saveAndClose()).dimensions(cx - 105, bottomY, 100, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, btn -> closeWithoutSave()).dimensions(cx + 5, bottomY, 100, 20).build());
    }

    private void applyPreset(double y, boolean above) {
        triggerY = y; triggerAbove = above;
        triggerYField.setText(String.valueOf(y)); triggerYField.setEditableColor(0xFFFFFF);
        directionBtn.setValue(above);
    }

    private void saveAndClose() {
        try { triggerY = Double.parseDouble(triggerYField.getText()); } catch (NumberFormatException ignored) {}
        YReconnectConfig cfg = YReconnectConfig.get();
        cfg.enabled = enabledBtn.getValue();
        cfg.triggerY = triggerY;
        cfg.triggerAbove = directionBtn.getValue();
        cfg.reconnectDelayTicks = delaySlider.getTickValue();
        YReconnectConfig.save();
        if (this.client != null) this.client.setScreen(parent);
    }

    private void closeWithoutSave() { if (this.client != null) this.client.setScreen(parent); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int panelW = 240, panelH = 210, panelX = cx - panelW / 2 - 10, panelY = 45;
        context.fill(panelX, panelY, panelX + panelW + 20, panelY + panelH, 0xAA000000);
        context.drawBorder(panelX, panelY, panelW + 20, panelH, 0xFF555555);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§b⚡ §fYReconnect §7Config §b⚡"), cx, 20, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("§7Trigger Y value:"), cx - 100, 118, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, Text.literal("§7Reconnect delay:"), cx - 100, 146, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, Text.literal("§7Presets:"), cx - 100, 180, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§8Press §7K §8to open this screen"), cx, this.height - 14, 0x666666);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override public boolean shouldPause() { return false; }

    static class DelaySliderWidget extends SliderWidget {
        private static final int MIN = 0, MAX = 200;
        DelaySliderWidget(int x, int y, int width, int height, int initialTicks) {
            super(x, y, width, height, Text.empty(), (double)(initialTicks - MIN) / (MAX - MIN));
            this.updateMessage();
        }
        int getTickValue() { return MIN + (int) Math.round(this.value * (MAX - MIN)); }
        @Override protected void updateMessage() {
            int ticks = getTickValue();
            this.setMessage(Text.literal(String.format("Delay: §e%d ticks §7(%.1fs)", ticks, ticks / 20.0)));
        }
        @Override protected void applyValue() {}
    }
}
