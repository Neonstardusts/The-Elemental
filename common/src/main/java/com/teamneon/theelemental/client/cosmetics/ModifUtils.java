package com.teamneon.theelemental.client.cosmetics;

import net.minecraft.world.entity.player.Player;

import java.util.Random;

public class ModifUtils {

    public static float getScaleFromPlayer(Player player) {
        long seed = player.getUUID().getMostSignificantBits();
        Random random = new Random(seed);
        return 0.8f + random.nextFloat() * 0.4f;
    }

    public static int getColorFromPlayer(Player player) {
        long seed = player.getUUID().getLeastSignificantBits();
        Random random = new Random(seed);

        float hue = random.nextFloat();
        return hsbToRgb(hue, 0.8f, 1.0f);
    }

    // ✔ Safe replacement for java.awt.Color.HSBtoRGB
    private static int hsbToRgb(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;

        if (saturation == 0.0F) {
            r = g = b = (int)(brightness * 255.0F + 0.5F);
        } else {
            float h = (hue - (float)Math.floor(hue)) * 6.0F;
            float f = h - (float)Math.floor(h);
            float p = brightness * (1.0F - saturation);
            float q = brightness * (1.0F - saturation * f);
            float t = brightness * (1.0F - saturation * (1.0F - f));

            switch ((int)h) {
                case 0 -> { r = (int)(brightness * 255); g = (int)(t * 255); b = (int)(p * 255); }
                case 1 -> { r = (int)(q * 255); g = (int)(brightness * 255); b = (int)(p * 255); }
                case 2 -> { r = (int)(p * 255); g = (int)(brightness * 255); b = (int)(t * 255); }
                case 3 -> { r = (int)(p * 255); g = (int)(q * 255); b = (int)(brightness * 255); }
                case 4 -> { r = (int)(t * 255); g = (int)(p * 255); b = (int)(brightness * 255); }
                case 5 -> { r = (int)(brightness * 255); g = (int)(p * 255); b = (int)(q * 255); }
            }
        }

        return (r << 16) | (g << 8) | b;
    }
}
