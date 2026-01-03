package com.teamneon.theelemental.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class UtilityHelper {

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
        spawnDust(level, player.getX(), player.getY()+1, player.getZ(), color, 50,0.5, 0.75, 0.5, 0.02, 3);

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
}
