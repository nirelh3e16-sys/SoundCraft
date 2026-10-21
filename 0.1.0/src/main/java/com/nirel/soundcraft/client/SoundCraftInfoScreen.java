package com.nirel.soundcraft.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Small informational screen for features planned for later releases. */
public final class SoundCraftInfoScreen extends Screen {
    private final Screen parent;
    private final String message;

    public SoundCraftInfoScreen(Screen parent, String message) {
        super(Component.literal("SoundCraft"));
        this.parent = parent;
        this.message = message;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int panelWidth = Math.min(300, this.width - 24);
        int x = (this.width - panelWidth) / 2;
        int y = this.height / 2 - 30;
        graphics.fill(x, y, x + panelWidth, y + 76, 0xFF242424);
        graphics.outline(x, y, panelWidth, 76, 0xFF111111);
        graphics.text(font, "SoundCraft", this.width / 2 - font.width("SoundCraft") / 2, y + 10, 0xFFFFFFFF, false);
        graphics.text(font, message, x + 12, y + 30, 0xFFDDDDDD, false);
        graphics.text(font, "Press ESC to return", this.width / 2 - font.width("Press ESC to return") / 2, y + 59, 0xFFAAAAAA, false);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
