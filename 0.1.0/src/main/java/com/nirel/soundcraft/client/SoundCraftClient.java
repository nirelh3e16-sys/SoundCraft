package com.nirel.soundcraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.nirel.soundcraft.client.data.PlayerSettings;
import com.nirel.soundcraft.client.data.PlaylistManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class SoundCraftClient implements ClientModInitializer {
    public static final String MOD_ID = "soundcraft";
    public static final Identifier TRACK_ICON =
            Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/sprites/icon/standard_track_icon.png");

    private static final KeyMapping.Category PLAYER_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "player"));

    private static KeyMapping openPlayerKey;
    private static KeyMapping nextTrackKey;
    private static KeyMapping previousTrackKey;
    private static KeyMapping pauseResumeKey;

    @Override
    public void onInitializeClient() {
        PlayerSettings.load();

        openPlayerKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.soundcraft.open_player",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                PLAYER_CATEGORY
        ));
        nextTrackKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.soundcraft.next_track", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_PAGE_UP, PLAYER_CATEGORY
        ));
        previousTrackKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.soundcraft.previous_track", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_PAGE_DOWN, PLAYER_CATEGORY
        ));
        pauseResumeKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.soundcraft.pause_resume", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_END, PLAYER_CATEGORY
        ));

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> PlaylistManager.reload());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Работает всегда — даже при закрытом окне плеера.
            SoundCraftScreen.tickPlayback();

            while (openPlayerKey.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    PlaylistManager.reload();
                    client.gui.setScreen(new SoundCraftScreen());
                }
            }

            // consumeClick prevents key-repeat from turning one held key into several skips.
            while (nextTrackKey.consumeClick()) SoundCraftScreen.nextTrack();
            while (previousTrackKey.consumeClick()) SoundCraftScreen.previousTrack();
            while (pauseResumeKey.consumeClick()) SoundCraftScreen.togglePauseResume();
        });
    }

    public static Minecraft minecraft() {
        return Minecraft.getInstance();
    }
}
