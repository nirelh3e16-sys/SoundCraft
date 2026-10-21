package com.nirel.soundcraft.sound;

import com.nirel.soundcraft.SoundCraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
    private static final String[] SOUND_IDS = {
            "music.chase_the_skies.below_and_above",
            "music.chase_the_skies.broken_clocks",
            "music.chase_the_skies.fireflies",
            "music.chase_the_skies.lilypad",
            "music.chase_the_skies.os_piano",

            "music.chaos_cubed.ebb",
            "music.chaos_cubed.home",
            "music.chaos_cubed.memories",
            "music.chaos_cubed.nightly",
            "music.chaos_cubed.shores",

            "music.volume_alpha.key",
            "music.volume_alpha.subwoofer_lullaby",
            "music.volume_alpha.living_mice",
            "music.volume_alpha.haggstrom",
            "music.volume_alpha.danny",
            "music.volume_alpha.minecraft",
            "music.volume_alpha.clark",
            "music.volume_alpha.sweden",
            "music.volume_alpha.oxygene",
            "music.volume_alpha.dry_hands",
            "music.volume_alpha.wet_hands",
            "music.volume_alpha.mice_on_venus",

            "music.volume_beta.mutation",
            "music.volume_beta.moog_city_2",
            "music.volume_beta.beginning_2",
            "music.volume_beta.floating_trees",
            "music.volume_beta.biome_fest",
            "music.volume_beta.blind_spots",
            "music.volume_beta.haunt_muskie",
            "music.volume_beta.aria_math",
            "music.volume_beta.dreiton",
            "music.volume_beta.taswell",
            "music.volume_beta.concrete_halls",
            "music.volume_beta.dead_voxel",
            "music.volume_beta.warmth",
            "music.volume_beta.ballad_of_the_cats"
    };

    private ModSounds() {
    }

    public static void initialize() {
        for (String soundId : SOUND_IDS) {
            Identifier id = Identifier.fromNamespaceAndPath(SoundCraft.MOD_ID, soundId);

            Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    id,
                    SoundEvent.createVariableRangeEvent(id)
            );
        }
    }
}
