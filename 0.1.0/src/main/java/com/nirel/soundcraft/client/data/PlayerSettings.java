package com.nirel.soundcraft.client.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Persistent SoundCraft playback preferences stored in config/soundcraft/player.json. */
public final class PlayerSettings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("soundcraft/player.json");
    public static int volume = 7;
    public static boolean repeat = false;
    public static boolean random = false;
    public static boolean drownOutExternalTracks = false;
    /** Whether the in-game "now playing" toast is shown for a newly started track. */
    public static boolean nowPlayingNotifications = true;
    public static String lastVanillaTrack = "minecraft:music_disc.13";

    private PlayerSettings() {}

    public static void load() {
        if (!Files.exists(FILE)) { save(); return; }
        try (var reader = Files.newBufferedReader(FILE)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            volume = clamp(json.has("volume") ? json.get("volume").getAsInt() : 7, 1, 10);
            repeat = json.has("replay") && json.get("replay").getAsBoolean();
            random = json.has("random") && json.get("random").getAsBoolean();
            drownOutExternalTracks = json.has("drown_out_external_tracks") && json.get("drown_out_external_tracks").getAsBoolean();
            nowPlayingNotifications = !json.has("now_playing_notifications")
                    || json.get("now_playing_notifications").getAsBoolean();
            if (json.has("last_vanilla_track")) lastVanillaTrack = json.get("last_vanilla_track").getAsString();
        } catch (Exception ignored) { /* Keep safe defaults if the config is malformed. */ }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            JsonObject json = new JsonObject();
            json.addProperty("volume", clamp(volume, 1, 10));
            json.addProperty("replay", repeat);
            json.addProperty("random", random);
            json.addProperty("drown_out_external_tracks", drownOutExternalTracks);
            json.addProperty("now_playing_notifications", nowPlayingNotifications);
            json.addProperty("last_vanilla_track", lastVanillaTrack);
            Files.writeString(FILE, GSON.toJson(json));
        } catch (IOException ignored) { }
    }

    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
}
