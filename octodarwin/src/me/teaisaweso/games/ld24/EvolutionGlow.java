package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class EvolutionGlow {
    private final Entity mAttachedEntity;
    private final ParticleEffect mParticleSystem;
    private final Vector2 mOffset;

    public EvolutionGlow(Entity attachedEntity, Vector2 offset, float spawnWidth) {
        mAttachedEntity = attachedEntity;
        mParticleSystem = new ParticleEffect();
        mParticleSystem.load(Gdx.files.internal("assets/evoglow"),
                Gdx.files.internal("assets"));
        mOffset = new Vector2(offset);
        for (ParticleEmitter emitter : mParticleSystem.getEmitters()) {
            emitter.setMaxParticleCount(1500);
            emitter.getSpawnWidth().setHigh(-spawnWidth, spawnWidth);
            emitter.getEmission().setHigh(300f + spawnWidth * 0.8f);
        }
    }

    public EvolutionGlow(Entity attachedEntity, float spawnWidth) {
        this(attachedEntity, new Vector2(0, 0), spawnWidth);
    }

    public void update() {
        Vector2 position = mAttachedEntity.getPosition();
        position.add(mOffset);
        mParticleSystem.setPosition(position.x, position.y);
        mParticleSystem.update(1.0f / 60.0f);
    }

    public void draw(SpriteBatch sb) {
        mParticleSystem.draw(sb);
    }
}
