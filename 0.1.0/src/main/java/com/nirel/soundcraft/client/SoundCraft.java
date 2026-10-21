package com.nirel.soundcraft;

import com.nirel.soundcraft.sound.ModSounds;
import net.fabricmc.api.ModInitializer;

public final class SoundCraft implements ModInitializer {
    public static final String MOD_ID = "soundcraft";

    @Override
    public void onInitialize() {
        ModSounds.initialize();
    }
}
