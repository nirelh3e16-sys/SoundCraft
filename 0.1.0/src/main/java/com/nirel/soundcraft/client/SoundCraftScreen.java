package com.nirel.soundcraft.client;

import com.mojang.math.Axis;
import com.nirel.soundcraft.client.data.PlayerSettings;
import com.nirel.soundcraft.client.data.Playlist;
import com.nirel.soundcraft.client.data.PlaylistManager;
import com.nirel.soundcraft.client.data.Track;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.Util;
import net.minecraft.util.RandomSource;
import java.util.ArrayList;
import java.util.List;

/** SoundCraft player. Static playback fields allow it to keep running after this screen closes. */
public final class SoundCraftScreen extends Screen {
    private static final int ROW_H = 16;
    private static final Identifier DISC_TEXTURE = Identifier.fromNamespaceAndPath(
            SoundCraftClient.MOD_ID, "textures/gui/sprites/icon/disc.png");
    private static int selectedPlaylist, selectedTrack, scroll, ticksSinceStart;
    private static long playbackMillis, resumedAtMillis;
    private static boolean playing, paused;
    private static Track nowPlaying;
    private static PlayerSoundInstance current;
    private static String status = "Choose a track";
    private int panelX, panelY, panelW, panelH, listTop, listBottom, visibleRows;
    private List<Track> tracks = new ArrayList<>();
    private Button playButton;

    public SoundCraftScreen() { super(Component.literal("SoundCraft")); }

    @Override protected void init() {
        panelW = Math.min(430, width - 16); panelH = Math.min(300, height - 16);
        panelX = (width - panelW) / 2; panelY = (height - panelH) / 2;
        refreshTracks(); listTop = panelY + 76; listBottom = panelY + panelH - 120;
        visibleRows = Math.max(1, (listBottom - listTop) / ROW_H); clampScroll();
        addRenderableWidget(Button.builder(Component.literal("‹ Playlist"), b -> changePlaylist(-1)).bounds(panelX + 12, panelY + 30, 84, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Playlist ›"), b -> changePlaylist(1)).bounds(panelX + 102, panelY + 30, 84, 20).build());
        addRenderableWidget(Button.builder(Component.literal("− Track"), b -> { previousTrack(); refreshPlayButton(); }).bounds(panelX + 12, panelY + panelH - 38, 72, 20).build());
        playButton = addRenderableWidget(Button.builder(Component.literal(buttonLabel()), b -> { togglePauseResume(); refreshPlayButton(); }).bounds(panelX + panelW / 2 - 38, panelY + panelH - 38, 76, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Next +"), b -> { nextTrack(); refreshPlayButton(); }).bounds(panelX + panelW - 84, panelY + panelH - 38, 72, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Shuffle: " + onOff(PlayerSettings.random)), b -> { PlayerSettings.random = !PlayerSettings.random; PlayerSettings.save(); b.setMessage(Component.literal("Shuffle: " + onOff(PlayerSettings.random))); }).bounds(panelX + 12, panelY + panelH - 92, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Vol −"), b -> changeVolume(-1)).bounds(panelX + panelW / 2 - 42, panelY + panelH - 92, 40, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Vol +"), b -> changeVolume(1)).bounds(panelX + panelW / 2 + 2, panelY + panelH - 92, 40, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Repeat: " + onOff(PlayerSettings.repeat)), b -> { PlayerSettings.repeat = !PlayerSettings.repeat; PlayerSettings.save(); b.setMessage(Component.literal("Repeat: " + onOff(PlayerSettings.repeat))); }).bounds(panelX + panelW - 132, panelY + panelH - 92, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Notifications: " + onOff(PlayerSettings.nowPlayingNotifications)), b -> { PlayerSettings.nowPlayingNotifications = !PlayerSettings.nowPlayingNotifications; PlayerSettings.save(); b.setMessage(Component.literal("Notifications: " + onOff(PlayerSettings.nowPlayingNotifications))); }).bounds(panelX + 12, panelY + panelH - 66, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose()).bounds(panelX + panelW - 60, panelY + 5, 48, 20).build());
    }

    private void refreshTracks() {
        List<Playlist> playlists = PlaylistManager.all();
        if (playlists.isEmpty()) { PlaylistManager.reload(); playlists = PlaylistManager.all(); }
        selectedPlaylist = Math.max(0, Math.min(selectedPlaylist, playlists.size() - 1));
        tracks = playlists.isEmpty() ? new ArrayList<>() : new ArrayList<>(playlists.get(selectedPlaylist).tracks());
        selectedTrack = Math.max(0, Math.min(selectedTrack, tracks.size() - 1));
    }
    private void changePlaylist(int direction) {
        int count = PlaylistManager.all().size(); if (count == 0) return;
        selectedPlaylist = Math.floorMod(selectedPlaylist + direction, count); selectedTrack = scroll = 0; rebuildWidgets();
    }
    private void changeVolume(int delta) {
        PlayerSettings.volume = Math.max(1, Math.min(10, PlayerSettings.volume + delta)); PlayerSettings.save();
        if (current != null) current.setVolume(PlayerSettings.volume / 10.0F);
    }
    private static String onOff(boolean value) { return value ? "ON" : "OFF"; }
    private static String buttonLabel() { return playing ? "Pause" : paused ? "Resume" : "Play"; }

    /** Shared by the pause button and End. It never starts a track if none is active. */
    public static void togglePauseResume() {
        var client = SoundCraftClient.minecraft(); if (client == null || current == null) return;
        if (playing) {
            playbackMillis += Util.getMillis() - resumedAtMillis;
            client.getSoundManager().pause(); playing = false; paused = true; status = "Paused";
        } else if (paused) {
            client.getSoundManager().resume(); resumedAtMillis = Util.getMillis(); paused = false; playing = true; status = "Playing: " + safeTitle(nowPlaying);
        }
    }
    public static void nextTrack() { moveTrackAndPlay(1); }
    public static void previousTrack() { moveTrackAndPlay(-1); }
    private static void moveTrackAndPlay(int direction) {
        List<Playlist> playlists = PlaylistManager.all(); if (playlists.isEmpty()) return;
        selectedPlaylist = Math.max(0, Math.min(selectedPlaylist, playlists.size() - 1));
        List<Track> list = playlists.get(selectedPlaylist).tracks(); if (list.isEmpty()) return;
        selectedTrack = Math.max(0, Math.min(selectedTrack, list.size() - 1));
        if (PlayerSettings.random && direction > 0 && list.size() > 1) {
            int old = selectedTrack; while (selectedTrack == old) selectedTrack = RandomSource.create().nextInt(list.size());
        } else selectedTrack = Math.floorMod(selectedTrack + direction, list.size());
        playTrack(list.get(selectedTrack));
    }
    private void playSelected() { if (tracks.isEmpty()) status = "No tracks found"; else playTrack(tracks.get(selectedTrack)); }
    private static void playTrack(Track track) {
        var client = SoundCraftClient.minecraft(); if (client == null || track == null) return;
        stopMusic();
        try {
            current = new PlayerSoundInstance(track.soundEvent(), PlayerSettings.volume / 10.0F);
            client.getSoundManager().play(current);
            nowPlaying = track; playing = true; paused = false; ticksSinceStart = 0; playbackMillis = 0; resumedAtMillis = Util.getMillis();
            status = "Playing: " + safeTitle(track);
            if (PlayerSettings.nowPlayingNotifications) NowPlayingToast.show(client, track);
            if (track.vanilla()) { PlayerSettings.lastVanillaTrack = track.soundEvent().toString(); PlayerSettings.save(); }
        } catch (Exception exception) {
            current = null; nowPlaying = null; playing = paused = false; status = "Could not play: " + safeTitle(track);
        }
    }
    private static void stopMusic() {
        var client = SoundCraftClient.minecraft(); if (client != null && current != null) client.getSoundManager().stop(current);
        current = null; playing = paused = false; playbackMillis = 0;
    }
    private void refreshPlayButton() { if (playButton != null) playButton.setMessage(Component.literal(buttonLabel())); }
    private void clampScroll() { scroll = Math.max(0, Math.min(scroll, Math.max(0, tracks.size() - visibleRows))); }
    @Override public boolean mouseScrolled(double x, double y, double sx, double sy) { if (tracks.size() <= visibleRows) return false; scroll -= (int)Math.signum(sy); clampScroll(); return true; }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean twice) {
        if (super.mouseClicked(event, twice)) return true;
        if (event.button() != 0 || event.x() < panelX + 10 || event.x() >= panelX + panelW - 10 || event.y() < listTop || event.y() >= listTop + visibleRows * ROW_H) return false;
        int index = scroll + (int)((event.y() - listTop) / ROW_H);
        if (index < 0 || index >= tracks.size()) return false;
        selectedTrack = index; if (twice) { playSelected(); refreshPlayButton(); } return true;
    }

    /** Invoked every client tick even while the player window is closed. */
    public static void tickPlayback() {
        var client = SoundCraftClient.minecraft(); if (!playing || current == null || client == null) return;
        ticksSinceStart++;
        if (ticksSinceStart <= 10 || client.getSoundManager().isActive(current)) return;
        Track finished = nowPlaying;
        if (ticksSinceStart < 40) { current = null; playing = false; status = "Track not found or failed to play"; return; }
        if (PlayerSettings.repeat && finished != null) playTrack(finished); else moveTrackAndPlay(1);
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        List<Playlist> playlists = PlaylistManager.all();
        String playlist = playlists.isEmpty() ? "No playlists" : playlists.get(Math.min(selectedPlaylist, playlists.size() - 1)).title();
        g.text(font, "SoundCraft  •  0.1.0 BETA", panelX + 12, panelY + 12, 0xFFFFFFFF, true);
        g.text(font, "PLAYLIST  /  " + playlist, panelX + 14, panelY + 59, 0xFF74D9A6, true);
        int left = panelX + 10, right = panelX + panelW - 10;
        g.fill(left, listTop - 4, right, listBottom, 0xCC17191D); outline(g, left, listTop - 4, right - left, listBottom - listTop + 4, 0xFF050608);
        int authorX = panelX + panelW - 110, titleWidth = authorX - panelX - 30;
        for (int i = 0; i < visibleRows; i++) {
            int index = scroll + i; if (index >= tracks.size()) break;
            Track track = tracks.get(index); int y = listTop + i * ROW_H; boolean selected = index == selectedTrack;
            if (selected) g.fill(left + 2, y - 2, right - 2, y + ROW_H - 3, 0xFF303640);
            g.text(font, fit((playing && track.equals(nowPlaying) ? "♫  " : "    ") + safeTitle(track), titleWidth), panelX + 16, y, selected ? 0xFFFFFFFF : 0xFFB8BEC8, true);
            g.text(font, fit(safeAuthor(track), 90), authorX, y, 0xFF858D99, true);
        }
        drawProgress(g, left, right);
        g.text(font, status, panelX + 14, panelY + panelH - 112, 0xFFDDDDDD, true);
        g.text(font, "Volume " + PlayerSettings.volume + "/10  •  Double-click a track to play", panelX + 14, panelY + panelH - 104, 0xFF9AA1AD, true);
    }
    private void drawProgress(GuiGraphicsExtractor g, int left, int right) {
        int y = panelY + panelH - 50, discX = right - 16, barRight = discX - 8;
        g.fill(left, y, barRight, y + 5, 0xFF303640);
        // The public engine exposes source activity, but no duration/seek API. Never fabricate a duration.
        // Resource packs with no duration metadata therefore retain an empty, valid progress bar.
        long elapsed = playbackMillis + (playing ? Util.getMillis() - resumedAtMillis : 0L);
        g.pose().pushPose(); g.pose().translate(discX + 8.0F, y + 2.5F, 0.0F);
        g.pose().mulPose(Axis.ZP.rotationDegrees((elapsed * 0.072F) % 360.0F));
        g.blit(DISC_TEXTURE, -8, -8, 0, 0, 16, 16, 16, 16); g.pose().popPose();
    }
    private String fit(String text, int width) { String value = text; while (value.length() > 1 && font.width(value + "…") > width) value = value.substring(0, value.length() - 1); return font.width(value) <= width ? value : value + "…"; }
    private static String safeTitle(Track t) { return t == null || t.title() == null || t.title().isBlank() ? "Unknown track" : t.title(); }
    private static String safeAuthor(Track t) { return t == null || t.author() == null || t.author().isBlank() ? "Unknown artist" : t.author(); }
    private static void outline(GuiGraphicsExtractor g, int x, int y, int w, int h, int c) { g.fill(x,y,x+w,y+1,c); g.fill(x,y+h-1,x+w,y+h,c); g.fill(x,y,x+1,y+h,c); g.fill(x+w-1,y,x+w,y+h,c); }
    @Override public boolean isPauseScreen() { return false; }
}
