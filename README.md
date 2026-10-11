# SoundCraft 0.0.1 — Fabric 26.2 source

Client-only Minecraft music-player prototype by Nir_el. This archive contains source code; it is **not a tested release JAR**.

## Changes in this repair pass
- Updated the source to Minecraft 26.2's `Identifier`, `GuiGraphicsExtractor`, `extractRenderState`, `Gui#setScreen` and `KeyMappingHelper` API names.
- Changed the default open-player key to **M** to avoid the common P key conflict. You can rebind it in Minecraft's Controls menu.
- Fixed widget duplication by using `rebuildWidgets()` when refreshing the screen.
- Added click-to-select/play and mouse-wheel scrolling for playlist tracks.
- Added a sound-event existence check before playback and wired the saved 1–10 volume to the music sound instance.
- Stop only the active SoundCraft sound event instead of stopping every sound in the MUSIC category.
- Kept selection/playback flags when closing and reopening the player screen.
- Added English and Russian keybind translations and made playlist IDs include the resource namespace to reduce collisions.
- Removed the legacy `pack_format` from the demo pack metadata; use the demo pack as an example and check it against your exact Minecraft resource-pack format if the game warns about it.
- Removed the old official-Mojang-mappings declaration, since Minecraft 26.2 uses unobfuscated names.

## Resource-pack playlist format
Put a playlist manifest at `assets/soundcraft/musics/<playlist_id>.json`:

```json
{
  "name": "Custom Mix",
  "author": "Your name",
  "description": "Playlist description",
  "tracks": [
    { "id": "demo_track", "name": "Demo track", "sound": "soundcraft:music.custom.demo" }
  ]
}
```

The `sound` field is a Minecraft sound-event ID declared in that pack's `assets/soundcraft/sounds.json`. Audio files follow Minecraft's normal sound layout under `assets/soundcraft/sounds/`, for example `assets/soundcraft/sounds/music/custom/demo.ogg`, referenced as `soundcraft:music/custom/demo` in `sounds.json`. Audio should be OGG Vorbis. Enable the resource pack and reload resources before opening SoundCraft.

## Build requirements
- JDK 25
- Gradle 9.5.1 or newer compatible with the selected Fabric Loom version
- Network access for Gradle to resolve Minecraft/Fabric dependencies on the first build

Open the project root in IntelliJ IDEA and sync Gradle, then run `gradle build` from a terminal where Gradle 9.5.1+ is installed. The wrapper properties are included, but the wrapper JAR and launch scripts could not be added from this offline environment. The expected output path is `build/libs/soundcraft-0.0.1.jar`.

## Not yet implemented / not verified
- This source was edited against the published Fabric 26.2 documentation, but **has not been compiled or launched in this environment**. The environment only has Java 21, not the required JDK 25; do not treat this archive as a confirmed working JAR.
- The built-in `Disc` playlist is a list of vanilla music-disc sound-event IDs. It does not yet implement all soundtrack albums or update playlists.
- `repeat` is a saved toggle only; automatic track-end detection/repeat is not implemented.
- `drown_out_external_tracks` is a saved setting only; audio ducking/muting is not implemented.
- Online URL playlists, HTTP downloads/cache management, user playlist editing, album-art rendering, seek/progress and track-duration display are not implemented.
- Playback validation confirms that a sound event is registered, but it cannot guarantee that the selected event has a playable audio file in every resource-pack combination.
- Playback status does not yet track the sound engine's natural end-of-track state, and a true pause/resume implementation is still pending.

Config path: `.minecraft/config/soundcraft/player.json`.
