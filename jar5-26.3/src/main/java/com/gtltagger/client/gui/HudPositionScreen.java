package com.gtltagger.client.gui;

import net.minecraft.client.input.MouseButtonEvent;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import com.gtltagger.client.hud.GTLTaggerHud;
import com.gtltagger.client.compat.Mc;
import com.gtltagger.config.GTLTaggerConfig;

import java.util.List;

/**
 * Lets the user drag the GTLTagger HUD to a new position (spec
 * section 7 — "HUD editor/settings option ... reposition it").
 * Click-drag the preview box, then "Done" to save.
 */
public class HudPositionScreen extends Screen {

    private final Screen parent;
    private final GTLTaggerConfig config;
    private boolean dragging = false;

    protected HudPositionScreen(Screen parent, GTLTaggerConfig config) {
        super(Component.literal("Position GTLTagger HUD"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> {
            config.save();
            Mc.openScreen(this.minecraft, parent);
        }).bounds(this.width / 2 - 75, this.height - 30, 150, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.centeredText(
                this.font,
                "Drag the box below to reposition the HUD",
                this.width / 2, 20, 0xFFFFFFFF
        );

        List<GTLTaggerHud.HudLine> lines = GTLTaggerHud.buildLines(this.minecraft, config);
        if (lines.isEmpty()) {
            lines = List.of(
                    new GTLTaggerHud.HudLine("Kit: NethPot"),
                    new GTLTaggerHud.HudLine("Tier: ?"));
        }
        GTLTaggerHud.renderBox(graphics, this.minecraft, lines, config.hudX, config.hudY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() == InputConstants.MOUSE_BUTTON_LEFT && isInsideHud(click.x(), click.y())) {
            dragging = true;
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        if (dragging) {
            config.hudX = Math.max(0, (int) (config.hudX + deltaX));
            config.hudY = Math.max(0, (int) (config.hudY + deltaY));
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        dragging = false;
        return super.mouseReleased(click);
    }

    private boolean isInsideHud(double mouseX, double mouseY) {
        // Generous fixed hit box around the HUD's anchor point — good enough for a drag handle.
        int width = 120;
        int height = 60;
        return mouseX >= config.hudX && mouseX <= config.hudX + width
                && mouseY >= config.hudY && mouseY <= config.hudY + height;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
