package com.nirel.soundcraft.client;

import com.nirel.soundcraft.client.data.Playlist;
import com.nirel.soundcraft.client.data.PlaylistManager;
import com.nirel.soundcraft.client.data.PlayerSettings;
import com.nirel.soundcraft.client.data.Track;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/** SoundCraft v0.0.1 — compact in-game library and player. */
public final class SoundCraftScreen extends Screen {
    private int panelX, panelY, panelW, panelH;
    private static int selectedPlaylist;
    private static int selectedTrack;
    private static boolean playing;
    private static Identifier activeSound;
    private int scrollOffset;
    private List<Track> tracks = new ArrayList<>();
    private String status = "Choose a track";

    public SoundCraftScreen() { super(Component.literal("SoundCraft")); }

    @Override protected void init() {
        panelW = Math.min(430, this.width - 16);
        panelH = Math.min(300, this.height - 16);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;
        List<Playlist> playlists = PlaylistManager.all();
        if (playlists.isEmpty()) { PlaylistManager.reload(); playlists = PlaylistManager.all(); }
        selectedPlaylist = Math.max(0, Math.min(selectedPlaylist, playlists.size() - 1));
        tracks = playlists.isEmpty() ? new ArrayList<>() : new ArrayList<>(playlists.get(selectedPlaylist).tracks());
        selectedTrack = Math.max(0, Math.min(selectedTrack, tracks.size() - 1));

        addRenderableWidget(Button.builder(Component.literal("‹ Playlist"), b -> {
            if (!playlistsSafe().isEmpty()) { selectedPlaylist = Math.floorMod(selectedPlaylist - 1, playlistsSafe().size()); selectedTrack = 0; rebuildWidgets(); }
        }).bounds(panelX + 12, panelY + 30, 84, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Playlist ›"), b -> {
            if (!playlistsSafe().isEmpty()) { selectedPlaylist = Math.floorMod(selectedPlaylist + 1, playlistsSafe().size()); selectedTrack = 0; rebuildWidgets(); }
        }).bounds(panelX + 102, panelY + 30, 84, 20).build());
        addRenderableWidget(Button.builder(Component.literal("− Track"), b -> moveTrack(-1))
                .bounds(panelX + 12, panelY + panelH - 42, 72, 20).build());
        addRenderableWidget(Button.builder(Component.literal(playing ? "Pause" : "Play"), b -> {
            if (playing) { stopMusic(); playing = false; status = "Stopped"; b.setMessage(Component.literal("Play")); }
            else { playSelected(); b.setMessage(Component.literal(playing ? "Pause" : "Play")); }
        }).bounds(panelX + panelW / 2 - 38, panelY + panelH - 42, 76, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Next +"), b -> moveTrack(1))
                .bounds(panelX + panelW - 84, panelY + panelH - 42, 72, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Shuffle: " + (PlayerSettings.random ? "ON" : "OFF")), b -> {
            PlayerSettings.random = !PlayerSettings.random; PlayerSettings.save(); b.setMessage(Component.literal("Shuffle: " + (PlayerSettings.random ? "ON" : "OFF")));
        }).bounds(panelX + 12, panelY + panelH - 68, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Repeat: " + (PlayerSettings.repeat ? "ON" : "OFF")), b -> {
            PlayerSettings.repeat = !PlayerSettings.repeat; PlayerSettings.save(); b.setMessage(Component.literal("Repeat: " + (PlayerSettings.repeat ? "ON" : "OFF")));
        }).bounds(panelX + panelW - 132, panelY + panelH - 68, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Vol −"), b -> {
            PlayerSettings.volume = Math.max(1, PlayerSettings.volume - 1);
            PlayerSettings.save();
            if (playing) playSelected();
            status = "Volume: " + PlayerSettings.volume + "/10";
        }).bounds(panelX + 140, panelY + panelH - 68, 48, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Vol +"), b -> {
            PlayerSettings.volume = Math.min(10, PlayerSettings.volume + 1);
            PlayerSettings.save();
            if (playing) playSelected();
            status = "Volume: " + PlayerSettings.volume + "/10";
        }).bounds(panelX + 192, panelY + panelH - 68, 48, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(panelX + panelW - 60, panelY + 5, 48, 20).build());
    }

    private List<Playlist> playlistsSafe() { return PlaylistManager.all(); }

    private void moveTrack(int direction) {
        if (tracks.isEmpty()) return;
        if (PlayerSettings.random && direction > 0 && tracks.size() > 1) {
            int old = selectedTrack;
            while (selectedTrack == old) selectedTrack = RandomSource.create().nextInt(tracks.size());
        } else selectedTrack = Math.floorMod(selectedTrack + direction, tracks.size());
        playSelected();
        rebuildWidgets();
    }

    private void playSelected() {
        if (tracks.isEmpty() || minecraft == null) { status = "No tracks found"; return; }
        Track track = tracks.get(selectedTrack);
        stopMusic();
        try {
            SoundEvent event = SoundEvent.createVariableRangeEvent(track.soundEvent());
            if (minecraft.getSoundManager().getSoundEvent(track.soundEvent()) == null) {
                playing = false;
                status = "Missing sound event: " + track.soundEvent();
                return;
            }
            minecraft.getSoundManager().play(SimpleSoundInstance.forMusic(event, PlayerSettings.volume / 10.0F));
            playing = true;
            activeSound = track.soundEvent();
            status = "Playing: " + track.title();
            if (track.vanilla()) { PlayerSettings.lastVanillaTrack = track.soundEvent().toString(); PlayerSettings.save(); }
        } catch (Exception exception) {
            playing = false;
            activeSound = null;
            status = "Could not play: " + track.title();
        }
    }

    private void stopMusic() {
        if (minecraft != null && activeSound != null) {
            minecraft.getSoundManager().stop(activeSound, SoundSource.MUSIC);
            activeSound = null;
        }
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        g.text(font, "SoundCraft  •  0.0.1", panelX + 10, panelY + 8, 0xFFFFFFFF, false);
        List<Playlist> playlists = PlaylistManager.all();
        String playlistName = playlists.isEmpty() ? "No playlists" : playlists.get(Math.min(selectedPlaylist, playlists.size() - 1)).title();
        g.text(font, "PLAYLIST  /  " + playlistName, panelX + 14, panelY + 59, 0xFF74D9A6, false);
        int y = panelY + 78;
        int visibleRows = Math.max(1, Math.min(8, (panelH - 174) / 19));
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, tracks.size() - visibleRows)));
        for (int row = 0; row < Math.min(tracks.size() - scrollOffset, visibleRows); row++) {
            int i = row + scrollOffset;
            Track track = tracks.get(i);
            int rowY = y + row * 19;
            if (i == selectedTrack) g.fill(panelX + 10, rowY - 3, panelX + panelW - 10, rowY + 14, 0xFF303640);
            g.text(font, (i == selectedTrack ? "♪  " : "    ") + track.title(), panelX + 16, rowY,
                    i == selectedTrack ? 0xFFFFFFFF : 0xFFB8BEC8, false);
            g.text(font, track.author(), panelX + panelW - 110, rowY, 0xFF858D99, false);
        }
        g.text(font, status, panelX + 14, panelY + panelH - 92, 0xFFB8BEC8, false);
        g.text(font, "Volume " + PlayerSettings.volume + "/10  •  Resource-pack playlists refresh when M opens the player", panelX + 14, panelY + panelH - 84, 0xFF858D99, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int listTop = panelY + 75;
            int listBottom = panelY + panelH - 95;
            if (mouseX >= panelX + 10 && mouseX <= panelX + panelW - 10 && mouseY >= listTop && mouseY < listBottom) {
                int row = (int) ((mouseY - (panelY + 78)) / 19);
                int visibleRows = Math.max(1, Math.min(8, (panelH - 174) / 19));
                if (row >= 0 && row < visibleRows) {
                    int index = scrollOffset + row;
                    if (index >= 0 && index < tracks.size()) {
                        selectedTrack = index;
                        playSelected();
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= panelY + 70 && mouseY < panelY + panelH - 90) {
            int visibleRows = Math.max(1, Math.min(8, (panelH - 174) / 19));
            scrollOffset = Math.max(0, Math.min(Math.max(0, tracks.size() - visibleRows), scrollOffset - (int) Math.signum(verticalAmount))); 
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override public boolean isPauseScreen() { return false; }
}
