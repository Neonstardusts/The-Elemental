package com.teamneon.theelemental.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class SorceryParticle extends SimpleAnimatedParticle {

    protected SorceryParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, double xSpeed, double ySpeed, double zSpeed) {
        // SimpleAnimatedParticle takes sprites in constructor and sets the first sprite by default
        super(level, x, y, z, sprites, 0.01F);

        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.quadSize *= 0.85F;
        this.lifetime = 30 + this.random.nextInt(10);
        this.friction = 0.96F;
        this.gravity = 0f;

        // --- RANDOM COLOR LOGIC ---
        float r1 = 0.537f, g1 = 0.980f, b1 = 0.196f; // Vibrant Green (137, 250, 50)
        float r2 = 0.859f, g2 = 1.0f,   b2 = 0.651f; // Pale Green (219, 255, 166)
        float pct = this.random.nextFloat();

        this.rCol = Mth.lerp(pct, r1, r2);
        this.gCol = Mth.lerp(pct, g1, g2);
        this.bCol = Mth.lerp(pct, b1, b2);

        // --- THE FIX: PICK ONE RANDOM SPRITE ---
        // In 1.21.11, SpriteSet (sprites) has a method to get a random sprite.
        // We use setSprite from SingleQuadParticle to lock it in.
        this.setSprite(sprites.get(this.random));
    }

    @Override
    public void tick() {
        // We do NOT call super.tick() because SimpleAnimatedParticle.tick()
        // contains "this.setSpriteFromAge(this.sprites)", which forces animation.

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
            this.xd *= this.friction;
            this.yd *= this.friction;
            this.zd *= this.friction;
            this.yd -= this.gravity;

            // --- SMOOTH ALPHA FADE ---
            float agePct = (float) this.age / (float) this.lifetime;
            this.alpha = 1.0F - agePct;
        }
    }

    @Override
    public int getLightColor(float partialTick) {
        // Returns full brightness so the particle glows
        return 15728880;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new SorceryParticle(level, x, y, z, this.sprites, xSpeed, ySpeed, zSpeed);
        }
    }
}