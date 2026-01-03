package com.teamneon.theelemental.block;

import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.kingdoms.KingdomSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

public class WorldReactor extends Block {

    public WorldReactor(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 0.5;
        double centerZ = pos.getZ() + 0.5;

        int particleCount = 24;
        double radius = 1.5;
        double speed = 0.5;

        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI / particleCount) * i;

            // Spawn position on the ring
            double px = centerX + Math.cos(angle) * radius;
            double pz = centerZ + Math.sin(angle) * radius;
            double py = centerY;

            // Direction vector toward center
            double dx = centerX - px;
            double dy = centerY - py;
            double dz = centerZ - pz;

            // Normalize
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            dx /= length;
            dy /= length;
            dz /= length;

            // Apply speed
            dx *= speed;
            dy *= speed;
            dz *= speed;

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    px, py, pz,
                    1,      // ONE particle
                    dx, dy, dz,
                    0.0     // must be 0 for exact velocity
            );
        }

        level.playSound(
                null,
                pos,
                SoundEvents.BELL_RESONATE,
                SoundSource.BLOCKS,
                0.5F,
                2.0F
        );

        // Continue particle ring every 20 ticks
        level.scheduleTick(pos, this, 20);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide()) {
            ((ServerLevel) level).scheduleTick(pos, this, 20);
        }
    }


    // Right-click with no item
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        level.setBlock(
                pos,
                ModBlocks.ELEMENTAL_ALTAR.defaultBlockState(),
                Block.UPDATE_ALL
        );

        KingdomSavedData globalData = KingdomSavedData.get((ServerLevel) level);
        globalData.registerCore(-1, pos);

        int radius = 10;
        int pillarCount = 9;
        convertArea(level, pos, radius);
        spawnAllPillars((ServerLevel) level, pos, radius, pillarCount);


        level.playSound(
                null,
                pos,
                SoundEvents.END_PORTAL_SPAWN,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        return InteractionResult.CONSUME;
    }



    public void convertArea(Level level, BlockPos center, int radius) {

        clearLand((ServerLevel) level,center,radius);

        BlockPos.betweenClosedStream(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius)
        ).forEach(pos -> {

            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            if (pos.distSqr(center) > radius * radius) return;

            if (
                    state.is(BlockTags.LOGS) ||
                            state.is(BlockTags.LEAVES) ||
                            state.is(BlockTags.SAPLINGS) ||
                            state.is(BlockTags.REPLACEABLE) ||
                            state.is(BlockTags.FLOWERS) ||
                            state.is(Blocks.VINE) ||
                            state.is(Blocks.SHORT_GRASS) ||
                            state.is(Blocks.TALL_GRASS) ||
                            state.is(Blocks.FERN) ||
                            state.is(Blocks.LARGE_FERN) ||
                            state.is(Blocks.DEAD_BUSH) ||
                            state.is(Blocks.BROWN_MUSHROOM) ||
                            state.is(Blocks.RED_MUSHROOM)
            ) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        });
    }

    private void clearLand(ServerLevel level, BlockPos center, int radius) {
        int minY = center.getY() - 2;
        int maxY = center.getY() + radius;

        int cx = center.getX();
        int cz = center.getZ();

        for (int y = minY; y <= maxY; y++) {
            for (int x = cx - radius; x <= cx + radius; x++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    // Check if within cylinder radius

                    if (x == cx && z == cz) continue;
                    
                    double dx = x - cx;
                    double dz = z - cz;
                    if (dx * dx + dz * dz <= radius * radius) {
                        BlockPos pos = new BlockPos(x, y, z);
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }


    // === SPAWN ALL PILLARS INSTANTLY ===
    private void spawnAllPillars(ServerLevel level, BlockPos center, int radius, int pillarCount) {
        int R = radius - 2;
        for (int i = 0; i < pillarCount; i++) {
            // Compute position in a circle around the center
            double angle = 2 * Math.PI / pillarCount * i;
            int x = center.getX() + (int) Math.round(Math.cos(angle) * R);
            int z = center.getZ() + (int) Math.round(Math.sin(angle) * R);
            BlockPos pillarPos = new BlockPos(x, center.getY(), z);

            // Build the pillar instantly
            for (int y = -10; y < 0; y++) {
                level.setBlock(pillarPos.above(y), Blocks.BASALT.defaultBlockState(), 3);
                if (y == -1) {
                    level.setBlock(pillarPos.above(y), Blocks.BEDROCK.defaultBlockState(), 3);
                }
            }

            // Spawn particle beam from center to pillar
            spawnBeam(level, center, pillarPos);

            // Play sound for each pillar
            level.playSound(null, pillarPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1f, 1f);
        }
    }

    // Particle beam from center to pillar
    private void spawnBeam(ServerLevel level, BlockPos from, BlockPos to) {
        double steps = 20.0;
        double dx = (to.getX() + 0.5 - (from.getX() + 0.5)) / steps;
        double dy = (to.getY() + 0.5 - (from.getY() + 0.5)) / steps;
        double dz = (to.getZ() + 0.5 - (from.getZ() + 0.5)) / steps;

        for (int i = 0; i <= steps; i++) {
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    from.getX() + 0.5 + dx * i,
                    from.getY() + 0.5 + dy * i,
                    from.getZ() + 0.5 + dz * i,
                    1, 0, 0, 0, 0
            );
        }
    }


}