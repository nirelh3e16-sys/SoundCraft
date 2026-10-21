package com.nirel.soundcraft.client;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;

/**
 * Non-positional MUSIC-category sound with a custom volume (0.0 - 1.0).
 * Replaces SimpleSoundInstance.forMusic(...), which no longer accepts a volume.
 */
public final class PlayerSoundInstance extends AbstractSoundInstance {
    public PlayerSoundInstance(Identifier soundEventId, float volume) {
        super(soundEventId, SoundSource.MUSIC, SoundInstance.createUnseededRandom());
        this.volume = volume;
        this.pitch = 1.0F;
        this.looping = false;
        this.delay = 0;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.relative = true;
    }

    /** Changes the volume of the sound while it is playing. */
    public void setVolume(float volume) {
        this.volume = Math.max(0.0F, Math.min(1.0F, volume));
    }
}
