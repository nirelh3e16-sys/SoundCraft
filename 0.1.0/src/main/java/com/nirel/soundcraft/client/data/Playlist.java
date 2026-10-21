package com.nirel.soundcraft.client.data;

import java.util.List;

public record Playlist(String id, String title, String author, String description, List<Track> tracks, boolean vanilla) { }
