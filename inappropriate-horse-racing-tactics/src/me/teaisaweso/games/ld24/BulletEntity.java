package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class BulletEntity extends Entity {

    private final Sprite[] mBulletSprites = new Sprite[4];
    private int mTicks;
    private final ParticleEffectPool.PooledEffect mParticleEffect;
    private static ParticleEffectPool sEffectPool = null;

    private static ParticleEffectPool getEffectPool() {
        if (sEffectPool == null) {
            ParticleEffect prototypeEffect = new ParticleEffect();
            prototypeEffect.load(Gdx.files.internal("assets/ray"),
                    Gdx.files.internal("assets"));
            sEffectPool = new ParticleEffectPool(prototypeEffect, 4, 4);
        }
        return sEffectPool;
    }

    public BulletEntity(Body b) {

        mWidth = 40;
        mHeight = 40;
        mTicks = 0;
        for (int i = 1; i < 5; i++) {
            Texture t = new Texture(Gdx.files.internal("assets/bullet" + i
                    + ".png"));
            mBulletSprites[i - 1] = new Sprite(t, (int) mWidth, (int) mHeight);
        }

        mBody = b;

        mParticleEffect = getEffectPool().obtain();
    }

    @Override
    public Sprite getCurrentSprite() {
        return mBulletSprites[mTicks % 40 / 10];
    }

    @Override
    public void draw(SpriteBatch sb) {
        super.draw(sb);
        mParticleEffect.draw(sb);
    }

    @Override
    public void update() {
        Vector2 position = getPosition();
        mParticleEffect.setPosition(position.x, position.y);
        mTicks += 1;
        mParticleEffect.update(1.0f / 60.0f);
    }

}
