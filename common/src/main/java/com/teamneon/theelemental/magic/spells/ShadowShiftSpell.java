package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.magic.base.ActiveSpellManager;
import com.teamneon.theelemental.magic.base.DurationSpell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class ShadowShiftSpell extends DurationSpell {

    public ShadowShiftSpell(int manaCost, int cooldownTicks, String name, long durationTicks) {
        super(manaCost, cooldownTicks, name, durationTicks);
    }

    @Override
    public void tick(Level level, Player player) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {

            // --- Action Bar Message ---
            // ticksElapsed is inherited from DurationSpell
            long remainingTicks = Math.max(0, this.durationTicks - this.ticksElapsed);
            float secondsRemaining = remainingTicks / 20.0f;

            // Dark Indigo: #4B0082 | Light Indigo: #7F00FF
            Component actionBarMessage = Component.literal("")
                    .append(Component.literal("Shadow Spell: ").withStyle(s -> s.withColor(0x2c2163)))
                    .append(Component.literal(String.format("%.1fs", secondsRemaining)).withStyle(s -> s.withColor(0x6b5cb5)));

            player.displayClientMessage(actionBarMessage, true);

            // --- Visuals & Mechanics ---
            serverLevel.sendParticles(ParticleTypes.SQUID_INK,
                    player.getX(), player.getY() + 0.5, player.getZ(),
                    8, 0.2, 0.4, 0.2, 0.03);

            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    player.getX(), player.getY() + 1.2, player.getZ(),
                    3, 0.1, 0.3, 0.1, 0.02);

            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.getAbilities().setFlyingSpeed(0.10f);
            player.onUpdateAbilities();
        }
    }

    @Override
    public void onDurationEnd(Level level, Player player) {
        if (!level.isClientSide()) {
            player.removeEffect(MobEffects.INVISIBILITY);

            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.setInvulnerable(false);
            player.getAbilities().setFlyingSpeed(0.05f);
            player.onUpdateAbilities();

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.DRIPPING_OBSIDIAN_TEAR,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        20, 0.3, 0.3, 0.3, 0);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0f, 0.5f);
        }
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        if (!level.isClientSide()) {
            // Apply Invisibility (Ambient=true, ShowParticles=false)
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, (int)this.durationTicks + 20, 0, true, false));

            player.setInvulnerable(true);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.5f, 1.5f);

            ActiveSpellManager.addSpell(player, this);
            return SpellCastResult.success();
        }
        return SpellCastResult.fail();
    }
}