package com.teamneon.theelemental.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.blay09.mods.balm.platform.event.callback.ServerTickCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.WeakHashMap;

public class UtilityHelper {

    private static final WeakHashMap<ServerLevel, List<Runnable>> nextTickTasks = new WeakHashMap<>();

    static {
        // Permanent tick listener
        ServerTickCallback.ServerLevelTick.AFTER.register(level -> {
            List<Runnable> tasks = nextTickTasks.get(level);
            if (tasks != null) {
                for (Runnable task : tasks) {
                    task.run();
                }
                tasks.clear();
            }
        });
    }


    public static void runNextTick(ServerLevel level, Runnable task) {
        nextTickTasks.computeIfAbsent(level, l -> new ArrayList<>()).add(task);
    }


    /**
     * Attempts to teleport the player to a safe nearby location.
     *
     * @param player The player to teleport
     * @param targetX Target X coordinate
     * @param targetY Target Y coordinate
     * @param targetZ Target Z coordinate
     * @param color Int RGB color for dust particles (0xRRGGBB)
     * @return true if teleport succeeded, false if no free spot found
     */
    public static boolean teleportToNearestSafeSpot(Player player, int targetX, int targetY, int targetZ, int color) {
        if (!(player.level() instanceof ServerLevel level)) return false;

        // Max radius to search horizontally
        final int MAX_RADIUS = 7;
        final int MAX_DROP = 10;

        BlockPos original = new BlockPos(targetX, targetY, targetZ);

        Optional<BlockPos> safePosOpt = findSafeSpot(level, original, MAX_RADIUS, MAX_DROP, player.blockPosition());

        if (safePosOpt.isEmpty()) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Teleport cancelled: no free spot found!"), true
            );
            return false;
        }

        BlockPos safePos = safePosOpt.get();

        level.playSound(
                null, // no specific source (null = all players nearby hear it)
                player.blockPosition().getX() + 0.5,
                player.blockPosition().getY() + 1.0,
                player.blockPosition().getZ() + 0.5,
                SoundEvents.PLAYER_TELEPORT, // teleport sound
                net.minecraft.sounds.SoundSource.PLAYERS, // sound category
                1.0f, // volume
                1.0f  // pitch
        );

        // Spawn particles at current position
        UtilityHelper.runNextTick(level, () -> {
            UtilityHelper.spawnDust(
                    level,
                    safePos.getX() + 0.5,
                    safePos.getY() + 1.5,
                    safePos.getZ() + 0.5,
                    color,
                    50,
                    0.5, 0.75, 0.5,
                    0.02,
                    3
            );
        });




        // Teleport player slightly above the block
        player.teleportTo(
                safePos.getX() + 0.5,
                safePos.getY() + 1.5,
                safePos.getZ() + 0.5
        );

        // Spawn particles at destination
        spawnDust(level, safePos.getX()+0.5, safePos.getY()+1.5, safePos.getZ()+0.5, color, 50,0.5, 0.75, 0.5, 0.02, 3);


        level.playSound(
                null, // no specific source (null = all players nearby hear it)
                safePos.getX() + 0.5,
                safePos.getY() + 1.0,
                safePos.getZ() + 0.5,
                SoundEvents.PLAYER_TELEPORT, // teleport sound
                net.minecraft.sounds.SoundSource.PLAYERS, // sound category
                1.0f, // volume
                1.0f  // pitch
        );

        return true;
    }

    /**
     * Finds a nearby safe block position
     *
     * @param level ServerLevel
     * @param target Target position to start search
     * @param maxRadius Max horizontal search radius
     * @param maxDrop Max blocks to drop below target
     * @param excludePos Position to avoid (usually the player's current block)
     * @return Optional<BlockPos> of safe spot
     */
    private static Optional<BlockPos> findSafeSpot(ServerLevel level, BlockPos target, int maxRadius, int maxDrop, BlockPos excludePos) {
        for (int radius = 0; radius <= maxRadius; radius++) {
            // Iterate in a square ring around the target
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dz == 0) continue; // skip center
                    int x = target.getX() + dx;
                    int z = target.getZ() + dz;

                    // Check from target Y down to maxDrop below
                    for (int dy = 0; dy <= maxDrop; dy++) {
                        int y = target.getY() - dy;
                        BlockPos pos = new BlockPos(x, y, z);

                        // Skip player's current block
                        if (pos.equals(excludePos)) continue;

                        BlockState state = level.getBlockState(pos);

                        // Prefer solid ground
                        if (!state.isAir() && level.getBlockState(pos.above()).isAir() && level.getBlockState(pos.above(2)).isAir()) {
                            return Optional.of(pos);
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Spawns server-side colored dust particles at a given block
     */
    public static void spawnDust(ServerLevel level, double x, double y, double z, int color, int count, double dx, double dy, double dz, double speed, float scale) {

        float factor = 0.5f; // 50% darker

        int r = (int)(((color >> 16) & 0xFF) * factor);
        int g = (int)(((color >> 8) & 0xFF) * factor);
        int b = (int)((color & 0xFF) * factor);

        int color2 = (r << 16) | (g << 8) | b;

        DustColorTransitionOptions dust = new DustColorTransitionOptions(color, color2, scale);

        level.sendParticles(dust,
                x,
                y,
                z,
                count,  // count
                dx, dy, dz, speed // spread x,y,z + speed
        );
    }



    public static String toSmallCaps(String input) {
        if (input == null) return "";

        String normal = "abcdefghijklmnopqrstuvwxyz";
        String smallCaps = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ";

        StringBuilder builder = new StringBuilder();
        for (char c : input.toCharArray()) {
            int index = normal.indexOf(Character.toLowerCase(c));
            if (index != -1) {
                builder.append(smallCaps.charAt(index));
            } else {
                builder.append(c); // Keep numbers, spaces, and punctuation as they are
            }
        }
        return builder.toString();
    }

    public static Component getGradientComponent(String text, int colorStart, int colorEnd) {
        MutableComponent container = Component.empty();
        int length = text.length();

        // Prevent division by zero for single-character names
        if (length <= 1) {
            return Component.literal(text).withStyle(style -> style.withColor(colorStart));
        }

        for (int i = 0; i < length; i++) {
            // Calculate how far we are through the string (0.0 to 1.0)
            float ratio = (float) i / (float) (length - 1);

            // Extract RGB channels for Start color
            int r1 = (colorStart >> 16) & 0xFF;
            int g1 = (colorStart >> 8) & 0xFF;
            int b1 = colorStart & 0xFF;

            // Extract RGB channels for End color
            int r2 = (colorEnd >> 16) & 0xFF;
            int g2 = (colorEnd >> 8) & 0xFF;
            int b2 = colorEnd & 0xFF;

            // Linearly interpolate between the two colors
            int r = (int) (r1 + ratio * (r2 - r1));
            int g = (int) (g1 + ratio * (g2 - g1));
            int b = (int) (b1 + ratio * (b2 - b1));

            int blendedColor = (r << 16) | (g << 8) | b;

            // Append each character with its specific blended color
            container.append(Component.literal(String.valueOf(text.charAt(i)))
                    .withStyle(style -> style.withColor(blendedColor)));
        }

        return container;
    }

    public static int darkShiftColor(int color, float amount) {
        float factor = 1 - amount; // 50% darker

        int r = (int)(((color >> 16) & 0xFF) * factor);
        int g = (int)(((color >> 8) & 0xFF) * factor);
        int b = (int)((color & 0xFF) * factor);

        int color2 = (r << 16) | (g << 8) | b;

        return color2;
    }

    public static int desaturateColor(int color, float amount) {
        // Clamp amount between 0 (no change) and 1 (fully desaturated)
        amount = Math.max(0f, Math.min(1f, amount));

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // Calculate grayscale luminance using standard coefficients
        int gray = (int)(0.299f * r + 0.587f * g + 0.114f * b);

        // Interpolate between original color and gray
        r = (int)(r * (1 - amount) + gray * amount);
        g = (int)(g * (1 - amount) + gray * amount);
        b = (int)(b * (1 - amount) + gray * amount);

        return (r << 16) | (g << 8) | b;
    }


    public static int hueShiftColor(int color, float amount) {
        // Extract RGB
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // Hue rotation matrix
        float cosA = (float) Math.cos(amount * 2 * Math.PI);
        float sinA = (float) Math.sin(amount * 2 * Math.PI);

        float newR = 0.299f + 0.701f * cosA + 0.168f * sinA;
        float newG = 0.587f - 0.587f * cosA + 0.330f * sinA;
        float newB = 0.114f - 0.114f * cosA - 0.497f * sinA;

        float r2 = clamp(newR * r + newG * g + newB * b);
        float g2 = clamp(newR * r + newG * g + newB * b); // will adjust below
        float b2 = clamp(newR * r + newG * g + newB * b); // will adjust below

        // Quick fix: use proper rotation matrix for RGB to preserve brightness
        float newR2 = clamp((0.299f + 0.701f * cosA + 0.168f * sinA) * r +
                (0.587f - 0.587f * cosA + 0.330f * sinA) * g +
                (0.114f - 0.114f * cosA - 0.497f * sinA) * b);

        float newG2 = clamp((0.299f - 0.299f * cosA - 0.328f * sinA) * r +
                (0.587f + 0.413f * cosA + 0.035f * sinA) * g +
                (0.114f - 0.114f * cosA + 0.292f * sinA) * b);

        float newB2 = clamp((0.299f - 0.3f * cosA + 1.25f * sinA) * r +
                (0.587f - 0.588f * cosA - 1.05f * sinA) * g +
                (0.114f + 0.886f * cosA - 0.203f * sinA) * b);

        // Convert back to integer
        int R = (int) (newR2 * 255);
        int G = (int) (newG2 * 255);
        int B = (int) (newB2 * 255);

        return (R << 16) | (G << 8) | B;
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    public static float randomFromStringRange(String s, float min, float max) {
        int hash = s.hashCode();
        float r = ((hash & 0xFFFFFFFFL) / (float)0x100000000L); // 0..1
        return min + (max - min) * r; // scale to min..max
    }

    /**
     * Calculates a rainbow color based on the current system time.
     * Purely mathematical, no AWT/Graphics dependencies.
     * @param speed Speed of the cycle (e.g., 5000 is 5 seconds per loop)
     * @return The resulting RGB color as an integer
     */
    public static int getRainbowColor(int speed) {
        float hue = (System.currentTimeMillis() % speed) / (float) speed;
        return hsbToRgb(hue, 0.7f, 1.0f);
    }

    public static int getMultiFadeColor(int[] colors, int speed) {
        if (colors.length == 0) return 0xFFFFFF;
        if (colors.length == 1) return colors[0];

        // 1. Get total progress (0.0 to 1.0) through the entire list
        double totalProgress = (System.currentTimeMillis() % speed) / (double) speed;

        // 2. Determine which two colors we are between
        double scaledProgress = totalProgress * colors.length;
        int index = (int) scaledProgress;
        int nextIndex = (index + 1) % colors.length;

        // 3. Get the progress between just those two specific colors (0.0 to 1.0)
        float localFraction = (float) (scaledProgress - index);

        // 4. Smooth out the transition using a cosine curve (optional, but looks better)
        float smoothFraction = (float) (1.0 - Math.cos(localFraction * Math.PI)) / 2.0f;

        return lerpColor(colors[index], colors[nextIndex], smoothFraction);
    }

    // Simple RGB Lerp Helper
    private static int lerpColor(int c1, int c2, float t) {
        int r = (int) (((c1 >> 16) & 0xFF) + t * (((c2 >> 16) & 0xFF) - ((c1 >> 16) & 0xFF)));
        int g = (int) (((c1 >> 8) & 0xFF) + t * (((c2 >> 8) & 0xFF) - ((c1 >> 8) & 0xFF)));
        int b = (int) ((c1 & 0xFF) + t * ((c2 & 0xFF) - (c1 & 0xFF)));
        return (r << 16) | (g << 8) | b;
    }

    public static int getPaleRainbowColor(int speed) {
        float hue = (System.currentTimeMillis() % speed) / (float) speed;
        return hsbToRgb(hue, 0.3f, 1.0f);
    }

    /**
     * Manual HSB to RGB conversion to avoid using java.awt.Color
     */
    private static int hsbToRgb(float h, float s, float b) {
        float r = 0, g = 0, blue = 0;
        if (s == 0) {
            r = g = blue = b;
        } else {
            float hPos = (h - (float)Math.floor(h)) * 6.0f;
            float f = hPos - (float)Math.floor(hPos);
            float p = b * (1.0f - s);
            float q = b * (1.0f - s * f);
            float t = b * (1.0f - (s * (1.0f - f)));
            switch ((int) hPos) {
                case 0 -> { r = b; g = t; blue = p; }
                case 1 -> { r = q; g = b; blue = p; }
                case 2 -> { r = p; g = b; blue = t; }
                case 3 -> { r = p; g = q; blue = b; }
                case 4 -> { r = t; g = p; blue = b; }
                case 5 -> { r = b; g = p; blue = q; }
            }
        }
        return ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(blue * 255);
    }


}
