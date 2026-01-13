package com.teamneon.theelemental.particles;

import net.blay09.mods.balm.core.particles.BalmParticleTypeRegistrar;
import net.blay09.mods.balm.core.particles.BalmParticleTypeRegistration;
import net.minecraft.core.particles.SimpleParticleType;

import java.util.function.Supplier;

public class ModParticles {

    // We store the registration object, which acts as a Supplier for the SimpleParticleType
    public static BalmParticleTypeRegistration<SimpleParticleType> SORCERY_PARTICLE;

    public static void initialize(BalmParticleTypeRegistrar registrar) {
        // Balm's register method for SimpleParticleTypes usually takes a boolean for 'overrideLimiter'
        // true = always visible from far away; false = respects distance settings.
        SORCERY_PARTICLE = registrar.register("sorcery_particle", false);
    }
}