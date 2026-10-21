package com.nirel.soundcraft.client;

import com.nirel.soundcraft.client.data.Track;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** A visual-only achievement-style toast; it neither grants nor changes an advancement. */
public final class NowPlayingToast implements Toast {
    private static final Item[] DISCS = {
            Items.MUSIC_DISC_13, Items.MUSIC_DISC_CAT, Items.MUSIC_DISC_BLOCKS,
            Items.MUSIC_DISC_CHIRP, Items.MUSIC_DISC_FAR, Items.MUSIC_DISC_MALL,
            Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_STAL, Items.MUSIC_DISC_STRAD,
            Items.MUSIC_DISC_WARD, Items.MUSIC_DISC_WAIT, Items.MUSIC_DISC_OTHERSIDE,
            Items.MUSIC_DISC_5, Items.MUSIC_DISC_PIGSTEP, Items.MUSIC_DISC_RELIC
    };
    private final ItemStack disc;
    private final Component title;
    private final Component subtitle;

    private NowPlayingToast(Track track) {
        disc = new ItemStack(DISCS[RandomSource.create().nextInt(DISCS.length)]);
        title = Component.literal("Сейчас играет");
        subtitle = Component.literal(safe(track == null ? null : track.title(), "Unknown track")
                + " — " + safe(track == null ? null : track.author(), "Unknown artist"));
    }

    public static void show(Minecraft client, Track track) { client.getToasts().addToast(new NowPlayingToast(track)); }

    @Override public Visibility render(GuiGraphics graphics, ToastComponent component, long shownFor) {
        graphics.fill(0, 0, width(), height(), 0xEE202020);
        graphics.fill(0, 0, width(), 1, 0xFF7A7A7A);
        graphics.fill(0, height() - 1, width(), height(), 0xFF080808);
        graphics.renderItem(disc, 7, 7);
        graphics.drawString(Minecraft.getInstance().font, title, 31, 7, 0xFFFFFF55, true);
        graphics.drawString(Minecraft.getInstance().font, subtitle, 31, 18, 0xFFFFFFFF, false);
        return shownFor < 5_000L ? Visibility.SHOW : Visibility.HIDE;
    }

    private static String safe(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
}
