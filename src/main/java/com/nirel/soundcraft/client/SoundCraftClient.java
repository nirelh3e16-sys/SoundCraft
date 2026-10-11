package com.nirel.soundcraft.client;

import com.nirel.soundcraft.client.data.PlayerSettings;
import com.nirel.soundcraft.client.data.PlaylistManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class SoundCraftClient implements ClientModInitializer {
    public static final String MOD_ID = "soundcraft";
    public static final Identifier TRACK_ICON = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/sprites/icon/standard_track_icon.png");
    private static KeyMapping openPlayerKey;

    @Override
    public void onInitializeClient() {
        PlayerSettings.load();
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> PlaylistManager.reload());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openPlayerKey != null && openPlayerKey.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    PlaylistManager.reload();
                    client.gui.setScreen(new SoundCraftScreen());
                }
            }
        });
        openPlayerKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.soundcraft.open_player", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, KeyMapping.Category.MISC));
    }

    public static Minecraft minecraft() { return Minecraft.getInstance(); }
}
