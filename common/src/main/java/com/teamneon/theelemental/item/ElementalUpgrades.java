package com.teamneon.theelemental.item;

import com.teamneon.theelemental.data.ElementalData;
import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.helpers.UtilityHelper;
import com.teamneon.theelemental.store.ManaData;
import com.teamneon.theelemental.store.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.function.Consumer;

import static com.teamneon.theelemental.Theelemental.id;

public class ElementalUpgrades extends Item {
    public ElementalUpgrades(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            RandomSource random = level.random;

            // --- POOL 1: Magic Crystals and Amethyst (0 to 2 rolls) ---
            int pool1Rolls = random.nextIntBetweenInclusive(0, 2);
            for (int i = 0; i < pool1Rolls; i++) {
                // Weights: Magic(20) + Amethyst(10) = 30 total
                int weight = random.nextInt(30);
                if (weight < 20) {
                    // Magic Crystals (1-6)
                    giveItem(player, new ItemStack(ModItems.MAGIC_CRYSTAL.asItem(), random.nextIntBetweenInclusive(1, 6)));
                }
            }

            // --- POOL 2: Special Crystals (0 to 1 roll) ---
            int pool2Rolls = random.nextIntBetweenInclusive(0, 1);
            if (pool2Rolls > 0) {
                // Weights: Radius(10) + Level(6) + Slot(1) = 17 total
                int weight = random.nextInt(17);
                if (weight < 10) {
                    // Radius Crystal (1-4)
                    giveItem(player, new ItemStack(ModItems.RADIUS_CRYSTAL.asItem(), random.nextIntBetweenInclusive(1, 4)));
                } else if (weight < 16) {
                    // Level Crystal (1)
                    giveItem(player, new ItemStack(ModItems.LEVEL_CRYSTAL.asItem()));
                } else {
                    // Slot Crystal (1)
                    giveItem(player, new ItemStack(ModItems.SLOT_CRYSTAL.asItem()));
                }
            }

            // --- Post-Generation Logic ---
            stack.consume(1, player);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.8F, 1.5F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 2.0F, 0.8F);

            if (level instanceof ServerLevel serverLevel) {
                int[] colors = {0xffc35c, 0x9061ff, 0xb5f5ff, 0xff75ef};
                float particleSize = 3.0f;

                // Loop through every starting color
                for (int startColor : colors) {
                    // Loop through every ending color (Total 16 combinations)
                    for (int endColor : colors) {

                        double offsetX = (random.nextDouble() - 0.5) * 1.5;
                        double offsetY = random.nextDouble() * 2.0;
                        double offsetZ = (random.nextDouble() - 0.5) * 1.5;

                        DustColorTransitionOptions options = new DustColorTransitionOptions(
                                startColor,
                                endColor,
                                particleSize
                        );

                        serverLevel.sendParticles(
                                options,
                                player.getX() + offsetX,
                                player.getY() + offsetY,
                                player.getZ() + offsetZ,
                                1,      // count
                                0, 0, 0, // speed/delta (handled by our offset)
                                0.05    // actual speed
                        );
                    }
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    // Helper method to handle inventory full scenario
    private void giveItem(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        // Fades between Pink and White over 2 seconds
        int color = UtilityHelper.getMultiFadeColor(new int[]{0xffc35c, 0x9061ff, 0xb5f5ff, 0xff75ef}, 2000);

        // Uses the translation key from your lang file
        return Component.translatable("item.theelemental.elemental_upgrades")
                .withStyle(style -> style.withColor(color));
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {

        tooltipAdder.accept(Component.literal("Contains Goodies").withStyle(ChatFormatting.GRAY));

    }
    
}