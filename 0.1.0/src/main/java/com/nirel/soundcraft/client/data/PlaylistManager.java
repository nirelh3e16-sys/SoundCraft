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
        addVolumeAlphaPlaylist();
        addVolumeBetaPlaylist();
        loadResourcePackPlaylists();
    }

    private static void addDiscPlaylist() {
        List<Track> discs = new ArrayList<>();

        String[][] discData = {
                {"13", "13"},
                {"cat", "Cat"},
                {"blocks", "Blocks"},
                {"chirp", "Chirp"},
                {"far", "Far"},
                {"mall", "Mall"},
                {"mellohi", "Mellohi"},
                {"stal", "Stal"},
                {"strad", "Strad"},
                {"ward", "Ward"},
                {"11", "11"},
                {"wait", "Wait"},
                {"otherside", "Otherside"},
                {"5", "5"},
                {"pigstep", "Pigstep"},
                {"relic", "Relic"},
                {"precipice", "Precipice"},
                {"creator", "Creator"},
                {"creator_music_box", "Creator (Music Box)"},
                {"tears", "Tears"},
                {"lava_chicken", "Lava Chicken"},
                {"bounce", "Bounce"}
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
                soundcraftTrack("below_and_above", "Below and Above", author,
                        "music.chase_the_skies.below_and_above"),
                soundcraftTrack("broken_clocks", "Broken Clocks", author,
                        "music.chase_the_skies.broken_clocks"),
                soundcraftTrack("fireflies", "Fireflies", author,
                        "music.chase_the_skies.fireflies"),
                soundcraftTrack("lilypad", "Lilypad", author,
                        "music.chase_the_skies.lilypad"),
                soundcraftTrack("os_piano", "O's Piano", author,
                        "music.chase_the_skies.os_piano"),
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

    private static void addVolumeAlphaPlaylist() {
        String author = "C418";

        List<Track> tracks = List.of(
                soundcraftTrack("key", "Key", author, "music.volume_alpha.key"),
                soundcraftTrack("subwoofer_lullaby", "Subwoofer Lullaby", author,
                        "music.volume_alpha.subwoofer_lullaby"),
                soundcraftTrack("living_mice", "Living Mice", author,
                        "music.volume_alpha.living_mice"),
                soundcraftTrack("haggstrom", "Haggstrom", author,
                        "music.volume_alpha.haggstrom"),
                soundcraftTrack("danny", "Danny", author,
                        "music.volume_alpha.danny"),
                soundcraftTrack("minecraft", "Minecraft", author,
                        "music.volume_alpha.minecraft"),
                soundcraftTrack("clark", "Clark", author,
                        "music.volume_alpha.clark"),
                soundcraftTrack("sweden", "Sweden", author,
                        "music.volume_alpha.sweden"),
                soundcraftTrack("oxygene", "Oxygène", author,
                        "music.volume_alpha.oxygene"),
                soundcraftTrack("dry_hands", "Dry Hands", author,
                        "music.volume_alpha.dry_hands"),
                soundcraftTrack("wet_hands", "Wet Hands", author,
                        "music.volume_alpha.wet_hands"),
                soundcraftTrack("mice_on_venus", "Mice on Venus", author,
                        "music.volume_alpha.mice_on_venus")
        );

        PLAYLISTS.add(new Playlist(
                "volume_alpha",
                "Volume Alpha",
                author,
                "Minecraft - Volume Alpha",
                tracks,
                true
        ));
    }

    private static void addVolumeBetaPlaylist() {
        String author = "C418";

        List<Track> tracks = List.of(
                soundcraftTrack("mutation", "Mutation", author,
                        "music.volume_beta.mutation"),
                soundcraftTrack("moog_city_2", "Moog City 2", author,
                        "music.volume_beta.moog_city_2"),
                soundcraftTrack("beginning_2", "Beginning 2", author,
                        "music.volume_beta.beginning_2"),
                soundcraftTrack("floating_trees", "Floating Trees", author,
                        "music.volume_beta.floating_trees"),
                soundcraftTrack("biome_fest", "Biome Fest", author,
                        "music.volume_beta.biome_fest"),
                soundcraftTrack("blind_spots", "Blind Spots", author,
                        "music.volume_beta.blind_spots"),
                soundcraftTrack("haunt_muskie", "Haunt Muskie", author,
                        "music.volume_beta.haunt_muskie"),
                soundcraftTrack("aria_math", "Aria Math", author,
                        "music.volume_beta.aria_math"),
                soundcraftTrack("dreiton", "Dreiton", author,
                        "music.volume_beta.dreiton"),
                soundcraftTrack("taswell", "Taswell", author,
                        "music.volume_beta.taswell"),
                soundcraftTrack("concrete_halls", "Concrete Halls", author,
                        "music.volume_beta.concrete_halls"),
                soundcraftTrack("dead_voxel", "Dead Voxel", author,
                        "music.volume_beta.dead_voxel"),
                soundcraftTrack("warmth", "Warmth", author,
                        "music.volume_beta.warmth"),
                soundcraftTrack("ballad_of_the_cats", "Ballad of the Cats", author,
                        "music.volume_beta.ballad_of_the_cats")
        );

        PLAYLISTS.add(new Playlist(
                "volume_beta",
                "Volume Beta",
                author,
                "Minecraft - Volume Beta",
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
            Map<Identifier, Resource> manifests = Minecraft.getInstance()
                    .getResourceManager()
                    .listResources(
                            "musics",
                            id -> id.getPath().endsWith(".json")
                                    && !id.getPath().equals("musics/sounds.json")
                    );

            for (var entry : manifests.entrySet()) {
                Identifier manifestId = entry.getKey();
                String file = manifestId.getPath().substring("musics/".length());
                String playlistId = manifestId.getNamespace()
                        + ":"
                        + file.substring(0, file.length() - 5);

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
                    // Повреждённый плейлист не должен ломать остальные.
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
