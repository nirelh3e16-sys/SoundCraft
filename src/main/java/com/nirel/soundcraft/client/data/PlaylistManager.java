package com.nirel.soundcraft.client.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Built-in playlists plus manifests supplied by enabled resource packs. */
public final class PlaylistManager {
    private static final List<Playlist> PLAYLISTS = new ArrayList<>();

    private PlaylistManager() {
    }

    public static void reload() {
        PLAYLISTS.clear();

        addDiscPlaylist();
        addChaseTheSkiesPlaylist();
        addChaosCubedPlaylist();
        loadResourcePackPlaylists();
    }

    private static void addDiscPlaylist() {
        List<Track> discs = new ArrayList<>();

        String[][] discData = {
                {"13", "13"}, {"cat", "Cat"}, {"blocks", "Blocks"}, {"chirp", "Chirp"},
                {"far", "Far"}, {"mall", "Mall"}, {"mellohi", "Mellohi"}, {"stal", "Stal"},
                {"strad", "Strad"}, {"ward", "Ward"}, {"11", "11"}, {"wait", "Wait"},
                {"otherside", "Otherside"}, {"5", "5"}, {"pigstep", "Pigstep"},
                {"relic", "Relic"}, {"precipice", "Precipice"}, {"creator", "Creator"},
                {"creator_music_box", "Creator (Music Box)"}, {"tears", "Tears"},
                {"lava_chicken", "Lava Chicken"}, {"bounce", "Bounce"}
        };

        for (String[] disc : discData) {
            discs.add(vanillaTrack(
                    disc[0],
                    disc[1],
                    "Minecraft",
                    "music_disc." + disc[0]
            ));
        }

        PLAYLISTS.add(new Playlist(
                "disc",
                "Disc",
                "Mojang",
                "Vanilla music discs",
                List.copyOf(discs),
                true
        ));
    }

    private static void addChaseTheSkiesPlaylist() {
        String author = "Amos Roddy";

        List<Track> tracks = List.of(
                soundcraftTrack("below_and_above", "Below and Above", author, "music.chase_the_skies.below_and_above"),
                soundcraftTrack("broken_clocks", "Broken Clocks", author, "music.chase_the_skies.broken_clocks"),
                soundcraftTrack("fireflies", "Fireflies", author, "music.chase_the_skies.fireflies"),
                soundcraftTrack("lilypad", "Lilypad", author, "music.chase_the_skies.lilypad"),
                soundcraftTrack("os_piano", "O's Piano", author, "music.chase_the_skies.os_piano"),
                vanillaTrack("tears", "Tears", author, "music_disc.tears")
        );

        PLAYLISTS.add(new Playlist(
                "chase_the_skies",
                "Chase the Skies",
                author,
                "Minecraft game drop soundtrack",
                tracks,
                true
        ));
    }

    private static void addChaosCubedPlaylist() {
        String author = "fingerspit";

        List<Track> tracks = List.of(
                soundcraftTrack("ebb", "Ebb", author, "music.chaos_cubed.ebb"),
                soundcraftTrack("home", "Home", author, "music.chaos_cubed.home"),
                soundcraftTrack("memories", "Memories", author, "music.chaos_cubed.memories"),
                soundcraftTrack("nightly", "Nightly", author, "music.chaos_cubed.nightly"),
                soundcraftTrack("shores", "Shores", author, "music.chaos_cubed.shores"),
                vanillaTrack("bounce", "Bounce", author, "music_disc.bounce")
        );

        PLAYLISTS.add(new Playlist(
                "chaos_cubed",
                "Chaos Cubed",
                author,
                "Minecraft game drop soundtrack",
                tracks,
                true
        ));
    }

    private static Track vanillaTrack(String id, String title, String author, String soundPath) {
        return new Track(
                id,
                title,
                author,
                Identifier.fromNamespaceAndPath("minecraft", soundPath),
                true
        );
    }

    private static Track soundcraftTrack(String id, String title, String author, String soundPath) {
        return new Track(
                id,
                title,
                author,
                Identifier.fromNamespaceAndPath("soundcraft", soundPath),
                true
        );
    }

    private static void loadResourcePackPlaylists() {
        try {
            Map<Identifier, Resource> manifests = Minecraft.getInstance().getResourceManager()
                    .listResources("musics", id ->
                            id.getPath().endsWith(".json")
                                    && !id.getPath().equals("musics/sounds.json"));

            for (var entry : manifests.entrySet()) {
                Identifier manifestId = entry.getKey();
                String file = manifestId.getPath().substring("musics/".length());
                String playlistId = manifestId.getNamespace() + ":" + file.substring(0, file.length() - 5);

                try (Reader reader = entry.getValue().openAsReader()) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                    String title = string(json, "name", playlistId);
                    String author = string(json, "author", "Unknown");
                    String description = string(json, "description", "Resource-pack playlist");

                    JsonArray tracksJson = json.has("tracks") && json.get("tracks").isJsonArray()
                            ? json.getAsJsonArray("tracks")
                            : new JsonArray();

                    List<Track> tracks = new ArrayList<>();

                    for (JsonElement element : tracksJson) {
                        if (!element.isJsonObject()) continue;

                        JsonObject trackJson = element.getAsJsonObject();
                        String eventText = string(trackJson, "sound", "");
                        Identifier event = Identifier.tryParse(eventText);

                        if (event == null) continue;

                        String trackId = string(trackJson, "id", eventText);

                        tracks.add(new Track(
                                trackId,
                                string(trackJson, "name", trackId),
                                author,
                                event,
                                false
                        ));
                    }

                    if (!tracks.isEmpty()) {
                        PLAYLISTS.add(new Playlist(
                                playlistId,
                                title,
                                author,
                                description,
                                List.copyOf(tracks),
                                false
                        ));
                    }
                } catch (Exception ignored) {
                    // Один повреждённый JSON не должен ломать остальные плейлисты.
                }
            }
        } catch (Exception ignored) {
            // Встроенные плейлисты останутся доступными.
        }
    }

    private static String string(JsonObject json, String key, String fallback) {
        return json.has(key) && json.get(key).isJsonPrimitive()
                ? json.get(key).getAsString()
                : fallback;
    }

    public static List<Playlist> all() {
        return List.copyOf(PLAYLISTS);
    }
}
