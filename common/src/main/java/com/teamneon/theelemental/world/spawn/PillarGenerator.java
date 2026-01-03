package com.teamneon.theelemental.world.spawn;

import com.teamneon.theelemental.block.ModBlocks;
import com.teamneon.theelemental.world.PillarSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;

public class PillarGenerator {

    public static void generate(ServerPlayer player) {
        ServerLevel level = player.level();

        // Get SavedData for this world
        PillarSavedData data = PillarSavedData.get(level);

        // Prevent regeneration
        if (data.isGenerated()) {
            return;
        }

        int x = player.blockPosition().getX();
        int z = player.blockPosition().getZ() + 1; // Behind player

        // Find surface
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        // Build Bedrock Pillar
        for (int y = -64; y <= surfaceY; y++) {
            level.setBlock(new BlockPos(x, y, z), Blocks.BEDROCK.defaultBlockState(), 3);
        }

        // Place Stone and Altar
        level.setBlock(new BlockPos(x, surfaceY + 0, z), Blocks.BASALT.defaultBlockState(), 3);
        level.setBlock(new BlockPos(x, surfaceY + 1, z), Blocks.BASALT.defaultBlockState(), 3); // stone block replaced with bedrock per original
        level.setBlock(new BlockPos(x, surfaceY + 2, z), ModBlocks.WORLD_REACTOR.defaultBlockState(), 3);

        // Set World Spawn to behind the pillar
        level.setRespawnData(LevelData.RespawnData.of(level.dimension(), new BlockPos(x, surfaceY, z - 1), 0f, 0f));

        // Mark as generated and save position as string
        data.markGenerated(x, surfaceY + 2, z);
    }
}
