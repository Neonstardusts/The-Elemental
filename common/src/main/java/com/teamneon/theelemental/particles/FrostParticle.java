package com.teamneon.theelemental.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class FrostParticle extends SimpleAnimatedParticle {
    private final float rotSpeed;    // Individual spin speed
    private final double swayOffset; // Unique starting point for the sway direction

    protected FrostParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, sprites, 0.01F);

        // Movement and physics
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.friction = 0.96F;
        this.gravity = 0.015F; // Gentle falling speed

        // Randomize lifetime (approx 2-3 seconds)
        this.lifetime = 40 + this.random.nextInt(25);

        // Randomize size on spawn
        this.quadSize *= (0.75F + this.random.nextFloat() * 0.3F);

        // --- RANDOM ROTATION & SWAY ---
        // Sets a random initial rotation (roll)
        this.roll = this.random.nextFloat() * ((float)Math.PI * 2F);
        this.oRoll = this.roll;

        // Random spin speed: some spin left, some spin right
        this.rotSpeed = (this.random.nextFloat() - 0.5F) * 0.1F;

        // Unique offset ensures particles don't all sway in the same direction
        this.swayOffset = this.random.nextDouble() * Math.PI * 2.0;

        // --- RANDOM COLOR LOGIC ---
        float r1 = 0.40f, g1 = 0.88f, b1 = 0.98f; // Light Blue
        float r2 = 0.94f, g2 = 0.97f, b2 = 1.0f;  // Near White
        float pct = this.random.nextFloat();

        this.rCol = Mth.lerp(pct, r1, r2);
        this.gCol = Mth.lerp(pct, g1, g2);
        this.bCol = Mth.lerp(pct, b1, b2);

        // Lock in one random sprite from the sheet so it doesn't animate
        this.setSprite(sprites.get(this.random));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        // Store old rotation for smooth interpolation between frames
        this.oRoll = this.roll;

        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            // --- RANDOMIZED SWAY ---
            // Adding swayOffset makes the "starting" direction unique for every particle
            this.xd += Math.cos(this.age * 0.1 + swayOffset) * 0.003;
            this.zd += Math.sin(this.age * 0.1 + swayOffset) * 0.003;

            // --- SPINNING ---
            this.roll += rotSpeed;

            this.move(this.xd, this.yd, this.zd);

            // Apply drag and gravity
            this.xd *= this.friction;
            this.yd *= this.friction;
            this.zd *= this.friction;
            this.yd -= this.gravity;

            // --- SHRINK AND FADE ---
            float lifePct = (float)this.age / (float)this.lifetime;

            // Shrink slightly over time
            this.quadSize *= 0.99F;

            // Fade out transparency (1.0 = solid, 0.0 = invisible)
            this.alpha = 1.0F - lifePct;
        }
    }


    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new FrostParticle(level, x, y, z, this.sprites, xSpeed, ySpeed, zSpeed);
        }
    }
}