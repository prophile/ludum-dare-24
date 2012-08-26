package me.teaisaweso.games.ld24;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class ExplosionManager {
    private final ParticleEffectPool mEffectPool;
    private final Set<ParticleEffectPool.PooledEffect> mActiveEffects = new HashSet<ParticleEffectPool.PooledEffect>();

    public ExplosionManager() {
        ParticleEffect effect = new ParticleEffect();
        effect.load(Gdx.files.internal("assets/bananarama"),
                Gdx.files.internal("assets"));
        mEffectPool = new ParticleEffectPool(effect, 4, 4);
    }

    public void pew(float x, float y) {
        ParticleEffectPool.PooledEffect effect = mEffectPool.obtain();
        effect.setPosition(x, y);
        mActiveEffects.add(effect);
    }

    public void draw(SpriteBatch sb) {
        for (ParticleEffect effect : mActiveEffects) {
            effect.draw(sb);
        }
    }

    public void update() {
        List<ParticleEffectPool.PooledEffect> condemned = new ArrayList<ParticleEffectPool.PooledEffect>();
        for (ParticleEffectPool.PooledEffect effect : mActiveEffects) {
            effect.update(1.0f / 60.0f);
            if (effect.isComplete()) {
                condemned.add(effect);
            }
        }
        for (ParticleEffectPool.PooledEffect effect : condemned) {
            mActiveEffects.remove(effect);
            effect.free();
        }
    }

    public void pew(Vector2 position) {
        pew(position.x, position.y);
    }
}
