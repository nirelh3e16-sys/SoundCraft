package com.nirel.soundcraft.client.data;

import net.minecraft.resources.Identifier;

public record Track(String id, String title, String author, Identifier soundEvent, boolean vanilla) { }
