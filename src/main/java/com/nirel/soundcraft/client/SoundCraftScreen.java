package com.nirel.soundcraft.client;

import com.nirel.soundcraft.client.data.PlayerSettings;
import com.nirel.soundcraft.client.data.Playlist;
import com.nirel.soundcraft.client.data.PlaylistManager;
import com.nirel.soundcraft.client.data.Track;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/** SoundCraft in-game library and player. */
public final class SoundCraftScreen extends Screen {
    private static final int ROW_H = 16;

    // Статические поля сохраняют воспроизведение после закрытия экрана.
    private static int selectedPlaylist;
    private static int selectedTrack;
    private static int scroll;
    private static boolean playing;
    private static Track nowPlaying;
    private static PlayerSoundInstance current;
    private static int ticksSinceStart;
    private static String status = "Choose a track";

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int listTop;
    private int listBottom;
    private int visibleRows;
    private List<Track> tracks = new ArrayList<>();
    private Button playButton;

    public SoundCraftScreen() {
        super(Component.literal("SoundCraft"));
    }

    @Override
    protected void init() {
        panelW = Math.min(430, this.width - 16);
        panelH = Math.min(300, this.height - 16);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        List<Playlist> playlists = PlaylistManager.all();

        if (playlists.isEmpty()) {
            PlaylistManager.reload();
            playlists = PlaylistManager.all();
        }

        selectedPlaylist = Math.max(0, Math.min(selectedPlaylist, playlists.size() - 1));
        tracks = playlists.isEmpty()
                ? new ArrayList<>()
                : new ArrayList<>(playlists.get(selectedPlaylist).tracks());

        selectedTrack = Math.max(0, Math.min(selectedTrack, tracks.size() - 1));

        listTop = panelY + 76;
        listBottom = panelY + panelH - 100;
        visibleRows = Math.max(1, (listBottom - listTop) / ROW_H);
        clampScroll();

        addRenderableWidget(Button.builder(Component.literal("‹ Playlist"), b -> changePlaylist(-1))
                .bounds(panelX + 12, panelY + 30, 84, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Playlist ›"), b -> changePlaylist(1))
                .bounds(panelX + 102, panelY + 30, 84, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal("− Track"), b -> moveTrack(-1))
                .bounds(panelX + 12, panelY + panelH - 42, 72, 20)
                .build());

        playButton = addRenderableWidget(Button.builder(
                        Component.literal(playing ? "Stop" : "Play"),
                        b -> togglePlay()
                )
                .bounds(panelX + panelW / 2 - 38, panelY + panelH - 42, 76, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Next +"), b -> moveTrack(1))
                .bounds(panelX + panelW - 84, panelY + panelH - 42, 72, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Shuffle: " + onOff(PlayerSettings.random)),
                        b -> {
                            PlayerSettings.random = !PlayerSettings.random;
                            PlayerSettings.save();
                            b.setMessage(Component.literal("Shuffle: " + onOff(PlayerSettings.random)));
                        }
                )
                .bounds(panelX + 12, panelY + panelH - 68, 120, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Vol −"), b -> changeVolume(-1))
                .bounds(panelX + panelW / 2 - 42, panelY + panelH - 68, 40, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Vol +"), b -> changeVolume(1))
                .bounds(panelX + panelW / 2 + 2, panelY + panelH - 68, 40, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Repeat: " + onOff(PlayerSettings.repeat)),
                        b -> {
                            PlayerSettings.repeat = !PlayerSettings.repeat;
                            PlayerSettings.save();
                            b.setMessage(Component.literal("Repeat: " + onOff(PlayerSettings.repeat)));
                        }
                )
                .bounds(panelX + panelW - 132, panelY + panelH - 68, 120, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(panelX + panelW - 60, panelY + 5, 48, 20)
                .build());
    }

    private static String onOff(boolean value) {
        return value ? "ON" : "OFF";
    }

    private void changePlaylist(int direction) {
        int count = PlaylistManager.all().size();

        if (count == 0) return;

        selectedPlaylist = Math.floorMod(selectedPlaylist + direction, count);
        selectedTrack = 0;
        scroll = 0;

        rebuildWidgets();
    }

    private void changeVolume(int delta) {
        PlayerSettings.volume = Math.max(1, Math.min(10, PlayerSettings.volume + delta));
        PlayerSettings.save();

        if (current != null) {
            current.setVolume(PlayerSettings.volume / 10.0F);
        }
    }

    private void togglePlay() {
        if (playing) {
            stopMusic();
            status = "Stopped";
        } else {
            playSelected();
        }

        refreshPlayButton();
    }

    private void moveTrack(int direction) {
        if (tracks.isEmpty()) return;

        if (PlayerSettings.random && direction > 0 && tracks.size() > 1) {
            int old = selectedTrack;

            while (selectedTrack == old) {
                selectedTrack = RandomSource.create().nextInt(tracks.size());
            }
        } else {
            selectedTrack = Math.floorMod(selectedTrack + direction, tracks.size());
        }

        ensureVisible();
        playSelected();
        refreshPlayButton();
    }

    private void playSelected() {
        if (tracks.isEmpty() || minecraft == null) {
            status = "No tracks found";
            return;
        }

        playTrack(tracks.get(selectedTrack));
    }

    private static void playTrack(Track track) {
        var client = SoundCraftClient.minecraft();

        if (client == null) return;

        stopMusic();

        try {
            current = new PlayerSoundInstance(
                    track.soundEvent(),
                    PlayerSettings.volume / 10.0F
            );

            client.getSoundManager().play(current);

            nowPlaying = track;
            playing = true;
            ticksSinceStart = 0;
            status = "Playing: " + track.title();

            if (track.vanilla()) {
                PlayerSettings.lastVanillaTrack = track.soundEvent().toString();
                PlayerSettings.save();
            }
        } catch (Exception exception) {
            current = null;
            nowPlaying = null;
            playing = false;
            status = "Could not play: " + track.title();
        }
    }

    /** Stops only SoundCraft's sound, not Minecraft's normal music. */
    private static void stopMusic() {
        var client = SoundCraftClient.minecraft();

        if (client != null && current != null) {
            client.getSoundManager().stop(current);
        }

        current = null;
        playing = false;
    }

    private void refreshPlayButton() {
        if (playButton != null) {
            playButton.setMessage(Component.literal(playing ? "Stop" : "Play"));
        }
    }

    private void clampScroll() {
        int max = Math.max(0, tracks.size() - visibleRows);
        scroll = Math.max(0, Math.min(scroll, max));
    }

    private void ensureVisible() {
        if (selectedTrack < scroll) {
            scroll = selectedTrack;
        } else if (selectedTrack >= scroll + visibleRows) {
            scroll = selectedTrack - visibleRows + 1;
        }

        clampScroll();
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (tracks.size() > visibleRows) {
            scroll -= (int) Math.signum(scrollY);
            clampScroll();
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;

        double x = event.x();
        double y = event.y();

        if (event.button() == 0
                && x >= panelX + 10
                && x < panelX + panelW - 10
                && y >= listTop
                && y < listTop + visibleRows * ROW_H) {

            int index = scroll + (int) ((y - listTop) / ROW_H);

            if (index >= 0 && index < tracks.size()) {
                selectedTrack = index;

                if (doubleClick) {
                    playSelected();
                    refreshPlayButton();
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public void tick() {
        super.tick();
    }

    /**
     * Вызывай этот метод каждый клиентский тик из SoundCraftClient.
     * Он продолжает следить за треком после закрытия экрана плеера.
     */
    public static void tickPlayback() {
        var client = SoundCraftClient.minecraft();

        if (!playing || current == null || client == null) return;

        ticksSinceStart++;

        // Звуковому движку нужно немного времени, чтобы запустить поток.
        if (ticksSinceStart <= 10 || client.getSoundManager().isActive(current)) return;

        Track finished = nowPlaying;

        if (ticksSinceStart < 40) {
            current = null;
            playing = false;
            status = "Track not found or failed to play";
            return;
        }

        if (PlayerSettings.repeat && finished != null) {
            playTrack(finished);
            return;
        }

        playNextTrackAutomatically();
    }

    private static void playNextTrackAutomatically() {
        List<Playlist> playlists = PlaylistManager.all();

        if (playlists.isEmpty()) {
            current = null;
            playing = false;
            status = "No playlists found";
            return;
        }

        selectedPlaylist = Math.max(0, Math.min(selectedPlaylist, playlists.size() - 1));

        List<Track> playlistTracks = playlists.get(selectedPlaylist).tracks();

        if (playlistTracks.isEmpty()) {
            current = null;
            playing = false;
            status = "No tracks found";
            return;
        }

        selectedTrack = Math.max(0, Math.min(selectedTrack, playlistTracks.size() - 1));

        if (PlayerSettings.random && playlistTracks.size() > 1) {
            int oldTrack = selectedTrack;

            while (selectedTrack == oldTrack) {
                selectedTrack = RandomSource.create().nextInt(playlistTracks.size());
            }
        } else {
            selectedTrack = Math.floorMod(selectedTrack + 1, playlistTracks.size());
        }

        playTrack(playlistTracks.get(selectedTrack));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);

        List<Playlist> playlists = PlaylistManager.all();

        String playlistName = playlists.isEmpty()
                ? "No playlists"
                : playlists.get(Math.min(selectedPlaylist, playlists.size() - 1)).title();

        g.text(this.font, "SoundCraft  •  0.0.1", panelX + 12, panelY + 12, 0xFFFFFFFF, true);
        g.text(this.font, "PLAYLIST  /  " + playlistName, panelX + 14, panelY + 59, 0xFF74D9A6, true);

        if (!tracks.isEmpty()) {
            String counter = (selectedTrack + 1) + " / " + tracks.size();
            g.text(
                    this.font,
                    counter,
                    panelX + panelW - 14 - this.font.width(counter),
                    panelY + 59,
                    0xFF858D99,
                    true
            );
        }

        int left = panelX + 10;
        int right = panelX + panelW - 10;

        g.fill(left, listTop - 4, right, listBottom, 0xCC17191D);
        drawOutline(g, left, listTop - 4, right - left, listBottom - (listTop - 4), 0xFF050608);

        int authorX = panelX + panelW - 110;
        int titleMaxWidth = authorX - (panelX + 16) - 14;

        for (int i = 0; i < visibleRows; i++) {
            int index = scroll + i;

            if (index >= tracks.size()) break;

            Track track = tracks.get(index);
            int rowY = listTop + i * ROW_H;

            boolean selected = index == selectedTrack;
            boolean isPlaying = playing && nowPlaying != null && nowPlaying.equals(track);

            if (selected) {
                g.fill(left + 2, rowY - 2, right - 2, rowY + ROW_H - 3, 0xFF303640);
            }

            String prefix = isPlaying ? "♫  " : "    ";

            g.text(
                    this.font,
                    fit(prefix + track.title(), titleMaxWidth),
                    panelX + 16,
                    rowY,
                    selected ? 0xFFFFFFFF : 0xFFB8BEC8,
                    true
            );

            g.text(
                    this.font,
                    fit(track.author(), 90),
                    authorX,
                    rowY,
                    0xFF858D99,
                    true
            );
        }

        if (tracks.size() > visibleRows) {
            int barTop = listTop - 4;
            int barHeight = listBottom - barTop;
            int thumbHeight = Math.max(8, barHeight * visibleRows / tracks.size());
            int maxScroll = tracks.size() - visibleRows;
            int thumbY = barTop + (barHeight - thumbHeight) * scroll / Math.max(1, maxScroll);

            g.fill(right - 4, thumbY, right - 2, thumbY + thumbHeight, 0xFF74D9A6);
        }

        g.text(this.font, status, panelX + 14, panelY + panelH - 92, 0xFFDDDDDD, true);

        g.text(
                this.font,
                "Volume " + PlayerSettings.volume + "/10  •  Double-click a track to play",
                panelX + 14,
                panelY + panelH - 80,
                0xFF9AA1AD,
                true
        );
    }

    private String fit(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) return text;

        String trimmed = text;

        while (trimmed.length() > 1 && this.font.width(trimmed + "…") > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        return trimmed + "…";
    }

    private static void drawOutline(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
