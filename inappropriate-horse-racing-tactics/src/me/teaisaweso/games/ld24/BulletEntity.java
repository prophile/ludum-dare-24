package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;

public class BulletEntity extends Entity {

    private Sprite[] mBulletSprites = new Sprite[4];
    private int mTicks;
    
    public BulletEntity(Body b) {
        
        mWidth = 40;
        mHeight = 40;
        mTicks = 0;
        for (int i = 1; i < 5; i++) {
            Texture t = new Texture(Gdx.files.internal("assets/bullet" + i + ".png"));
            mBulletSprites[i-1] = new Sprite(t, (int)mWidth, (int)mHeight);
        }
        
        mBody = b;
    }
    
    @Override
    public Sprite getCurrentSprite() {
        return mBulletSprites[(mTicks % 40)/10];
    }

    @Override
    public void update() {
        mTicks += 1;
    }

}
