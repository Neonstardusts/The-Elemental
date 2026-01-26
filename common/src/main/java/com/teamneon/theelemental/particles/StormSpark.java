package com.teamneon.theelemental.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class StormSpark extends SimpleAnimatedParticle {

    protected StormSpark(ClientLevel level, double x, double y, double z, SpriteSet sprites, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, sprites, 0F);

        // Movement: Zero out velocities to keep it static
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;

        // Visuals: Set a random size and rotation
        this.quadSize *= 0.7F + this.random.nextFloat() * 0.6F;
        this.roll = this.random.nextFloat() * ((float)Math.PI * 2F); // Random rotation in radians
        this.oRoll = this.roll;

        // Life: Very short lifespan for "flashing" effect
        this.lifetime = 2 + this.random.nextInt(3);

        // Behavior
        this.friction = 1.0F; // No slowdown needed as it's static
        this.gravity = 0.0F;

        // Selection: Pick ONE random sprite and keep it for the lifetime
        this.setSprite(sprites.get(this.random));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
        // No move() or velocity updates here to keep it perfectly still
    }

    @Override
    public int getLightColor(float partialTick) {
        // Constant full-bright glow (Block Light 15, Sky Light 15)
        return 15728880;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new StormSpark(level, x, y, z, this.sprites, xSpeed, ySpeed, zSpeed);
        }
    }
}