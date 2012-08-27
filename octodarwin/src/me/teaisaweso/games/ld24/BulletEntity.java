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
    private final int mRotateSpeed;

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

        mRotateSpeed = Constants.getInt("bulletSpriteRotateSpeed");
        mWidth = mHeight = Constants.getInt("bulletSpriteSize");
        mTicks = 0;
        for (int i = 1; i < 5; i++) {
            Texture t = new Texture(Gdx.files.internal("assets/bullet" + i
                    + ".png"));
            mBulletSprites[i - 1] = new Sprite(t, (int) mWidth, (int) mHeight);
        }

        mBody = b;
        mBody.setUserData(this);

        mParticleEffect = getEffectPool().obtain();
    }

    @Override
    public Sprite getCurrentSprite() {
        return mBulletSprites[mTicks % (4 * mRotateSpeed) / mRotateSpeed];
    }

    @Override
    public void draw(SpriteBatch sb) {
        mParticleEffect.draw(sb);
        super.draw(sb);
    }

    @Override
    public boolean update() {
        Vector2 position = getPosition();
        mParticleEffect.setPosition(position.x, position.y);
        mTicks += 1;
        mParticleEffect.update(1.0f / 60.0f);
        if (mTicks > 50) {
            // We've expired; remove ourselves from the list of entities
            return true;
        }

        return false;
    }

    public void reflect() {
        Vector2 velocity = mBody.getLinearVelocity();
        mBody.setLinearVelocity(-velocity.x, velocity.y);
    }
}
