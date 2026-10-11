# SoundCraft

**A client-side music player for Minecraft, with playlists you can add through resource packs.**

![Minecraft](https://img.shields.io/badge/Minecraft-26.2-62B47A)
![Loader](https://img.shields.io/badge/Fabric-client--side-DBD0B4)
![Java](https://img.shields.io/badge/Java-25-orange)
![License](https://img.shields.io/badge/License-MIT-blue)
![Status](https://img.shields.io/badge/status-early%20alpha-yellow)

SoundCraft adds an in-game player that opens on top of the world without pausing the game. Pick a playlist, pick a track, and listen while you build, mine or explore. Anyone can add their own playlists with a plain resource pack. No coding is needed.

> **Status: 0.0.1, early alpha.** This is the first public version. Some features are still planned, see [Roadmap](#roadmap).

---

## Features

- **Non-pausing player screen.** Open it with a key (default **P**, rebindable in *Controls*). The game keeps running while it is open.
- **Built-in "Disc" playlist** with vanilla music discs.
- **Resource-pack playlists.** Any enabled resource pack can ship its own playlists as simple JSON files.
- **Playback controls:** play/stop, next/previous track, shuffle and repeat toggles.
- **Saved settings** in `config/soundcraft/player.json`.
- **Client-only.** Install it on your own game. No server-side installation is required, and it works on any server.
- **No extra dependencies** besides Fabric API. Nothing is downloaded from the internet.

## Requirements

| Component    | Version |
|--------------|---------|
| Minecraft    | 26.2    |
| Fabric Loader | 0.19.5 or newer |
| Fabric API   | any version for 26.2 |
| Java         | 25 or newer |

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 26.2.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and put it in your `mods` folder.
3. Download the SoundCraft `.jar` from the [Releases](../../releases) page and put it in the same `mods` folder.
4. Launch the game and press **P** in a world.

## Usage

| Control | Action |
|---------|--------|
| **P** (rebindable) | Open or close the player |
| `‹ Playlist` / `Playlist ›` | Switch playlist |
| `− Track` / `Next +` | Previous / next track |
| `Play` | Play the selected track, or stop it |
| `Shuffle` | Random order when pressing *Next* |
| `Repeat` | Repeat setting (saved, see [Roadmap](#roadmap)) |

Resource-pack playlists are re-read every time you open the player. After enabling a new pack, press the key again.

## Adding your own playlist (resource packs)

Create a resource pack and put a manifest at:

```
assets/<namespace>/musics/<playlist_id>.json
```

Example manifest:

```json
{
  "name": "Custom Mix",
  "author": "Your name",
  "description": "A playlist provided by a resource pack.",
  "icon": true,
  "tracks": [
    { "id": "demo_track", "name": "Demo track", "sound": "soundcraft:music.custom.demo" }
  ]
}
```

Then declare the sound event in `assets/soundcraft/sounds.json`:

```json
{
  "music.custom.demo": {
    "sounds": [
      { "name": "soundcraft:musics/custom/demo", "stream": true }
    ]
  }
}
```

And put the audio file at `assets/soundcraft/sounds/musics/custom/demo.ogg`.

Notes:

- The `sound` field is a Minecraft sound event ID declared in the pack's `sounds.json`.
- Audio must be **OGG Vorbis**.
- Only **enabled** resource packs are read.
- A ready-to-copy example is in [`examples/SoundCraft-Demo-Pack`](examples/SoundCraft-Demo-Pack). Add your own `demo.ogg` to make it play.

## Configuration

The config file is created at `.minecraft/config/soundcraft/player.json`:

| Key | Description |
|-----|-------------|
| `volume` | Volume level, 1 to 10 |
| `replay` | Repeat toggle |
| `random` | Shuffle toggle |
| `drown_out_external_tracks` | Reserved for a future feature |
| `last_vanilla_track` | Last played vanilla disc |

## Building from source

1. Install **JDK 25**.
2. Clone the repository and open it in IntelliJ IDEA, or use the command line.
3. Run:

```bash
gradle build
```

The jar will appear in `build/libs/`.

Note: the repository does not include the Gradle wrapper scripts. Install Gradle 9.5 or newer and run `gradle wrapper` once if you want `./gradlew`.

## Roadmap

Planned, but **not implemented yet** in 0.0.1:

- [ ] Volume slider (the setting is stored, but not applied yet)
- [ ] Automatic next track and working *Repeat* at the end of a track
- [ ] Real pause, progress bar and seeking
- [ ] Scrolling and click-to-select in long track lists
- [ ] Ducking or muting the game's own background music
- [ ] Playlist artwork
- [ ] Full catalogue of vanilla soundtrack albums
- [ ] Online playlists and playlist creation UI

## Known limitations

- The "Play" button currently stops and restarts a track; there is no real pause.
- Minecraft's sound API does not expose a stable catalogue of all vanilla soundtracks, so only music discs are listed.
- The player state is reset when the screen is closed, although the music keeps playing.

## Reporting issues

Found a bug or have an idea? Open an [issue](../../issues) and include your Minecraft version, Fabric Loader / Fabric API versions and `latest.log`.

## License

Released under the [MIT License](LICENSE).

Made by **# SoundCraft

**A client-side music player for Minecraft, with playlists you can add through resource packs.**

![Minecraft](https://img.shields.io/badge/Minecraft-26.2-62B47A)
![Loader](https://img.shields.io/badge/Fabric-client--side-DBD0B4)
![Java](https://img.shields.io/badge/Java-25-orange)
![License](https://img.shields.io/badge/License-MIT-blue)
![Status](https://img.shields.io/badge/status-early%20alpha-yellow)

SoundCraft adds an in-game player that opens on top of the world without pausing the game. Pick a playlist, pick a track, and listen while you build, mine or explore. Anyone can add their own playlists with a plain resource pack. No coding is needed.

> **Status: 0.0.1, early alpha.** This is the first public version. Some features are still planned, see [Roadmap](#roadmap).

---

## Features

- **Non-pausing player screen.** Open it with a key (default **P**, rebindable in *Controls*). The game keeps running while it is open.
- **Built-in "Disc" playlist** with vanilla music discs.
- **Resource-pack playlists.** Any enabled resource pack can ship its own playlists as simple JSON files.
- **Playback controls:** play/stop, next/previous track, shuffle and repeat toggles.
- **Saved settings** in `config/soundcraft/player.json`.
- **Client-only.** Install it on your own game. No server-side installation is required, and it works on any server.
- **No extra dependencies** besides Fabric API. Nothing is downloaded from the internet.

## Requirements

| Component    | Version |
|--------------|---------|
| Minecraft    | 26.2    |
| Fabric Loader | 0.19.5 or newer |
| Fabric API   | any version for 26.2 |
| Java         | 25 or newer |

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 26.2.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and put it in your `mods` folder.
3. Download the SoundCraft `.jar` from the [Releases](../../releases) page and put it in the same `mods` folder.
4. Launch the game and press **P** in a world.

## Usage

| Control | Action |
|---------|--------|
| **P** (rebindable) | Open or close the player |
| `‹ Playlist` / `Playlist ›` | Switch playlist |
| `− Track` / `Next +` | Previous / next track |
| `Play` | Play the selected track, or stop it |
| `Shuffle` | Random order when pressing *Next* |
| `Repeat` | Repeat setting (saved, see [Roadmap](#roadmap)) |

Resource-pack playlists are re-read every time you open the player. After enabling a new pack, press the key again.

## Adding your own playlist (resource packs)

Create a resource pack and put a manifest at:

```
assets/<namespace>/musics/<playlist_id>.json
```

Example manifest:

```json
{
  "name": "Custom Mix",
  "author": "Your name",
  "description": "A playlist provided by a resource pack.",
  "icon": true,
  "tracks": [
    { "id": "demo_track", "name": "Demo track", "sound": "soundcraft:music.custom.demo" }
  ]
}
```

Then declare the sound event in `assets/soundcraft/sounds.json`:

```json
{
  "music.custom.demo": {
    "sounds": [
      { "name": "soundcraft:musics/custom/demo", "stream": true }
    ]
  }
}
```

And put the audio file at `assets/soundcraft/sounds/musics/custom/demo.ogg`.

Notes:

- The `sound` field is a Minecraft sound event ID declared in the pack's `sounds.json`.
- Audio must be **OGG Vorbis**.
- Only **enabled** resource packs are read.
- A ready-to-copy example is in [`examples/SoundCraft-Demo-Pack`](examples/SoundCraft-Demo-Pack). Add your own `demo.ogg` to make it play.

## Configuration

The config file is created at `.minecraft/config/soundcraft/player.json`:

| Key | Description |
|-----|-------------|
| `volume` | Volume level, 1 to 10 |
| `replay` | Repeat toggle |
| `random` | Shuffle toggle |
| `drown_out_external_tracks` | Reserved for a future feature |
| `last_vanilla_track` | Last played vanilla disc |

## Building from source

1. Install **JDK 25**.
2. Clone the repository and open it in IntelliJ IDEA, or use the command line.
3. Run:

```bash
gradle build
```

The jar will appear in `build/libs/`.

Note: the repository does not include the Gradle wrapper scripts. Install Gradle 9.5 or newer and run `gradle wrapper` once if you want `./gradlew`.

## Roadmap

Planned, but **not implemented yet** in 0.0.1:

- [ ] Volume slider (the setting is stored, but not applied yet)
- [ ] Automatic next track and working *Repeat* at the end of a track
- [ ] Real pause, progress bar and seeking
- [ ] Scrolling and click-to-select in long track lists
- [ ] Ducking or muting the game's own background music
- [ ] Playlist artwork
- [ ] Full catalogue of vanilla soundtrack albums
- [ ] Online playlists and playlist creation UI

## Known limitations

- The "Play" button currently stops and restarts a track; there is no real pause.
- Minecraft's sound API does not expose a stable catalogue of all vanilla soundtracks, so only music discs are listed.
- The player state is reset when the screen is closed, although the music keeps playing.

## Reporting issues

Found a bug or have an idea? Open an [issue](../../issues) and include your Minecraft version, Fabric Loader / Fabric API versions and `latest.log`.

## License

Released under the [MIT License](LICENSE).

Made by **nirelh3e16-sys**.
