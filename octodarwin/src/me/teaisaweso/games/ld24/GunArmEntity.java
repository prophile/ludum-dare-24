package me.teaisaweso.games.ld24;

import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class GunArmEntity extends Entity {

    private final Player mPlayer;
    private final Sprite mSprite;
    private final Sprite mFireSprite;
    private Vector2 mMousePos;
    private boolean mFired = false;

    public GunArmEntity(Player p) {
        float scale = Constants.getFloat("playerSize");
        mPlayer = p;
        Texture t = new Texture(Gdx.files.internal("assets/gun.png"));
        t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        mWidth = 184;
        mHeight = 109;
        mSprite = new Sprite(t, (int) mWidth, (int) mHeight);
        mSprite.setOrigin(0, mHeight / 2);
        mSprite.setScale(scale);

        t = new Texture(Gdx.files.internal("assets/gunfire.png"));
        t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        mFireSprite = new Sprite(t, (int) mWidth, (int) mHeight);
        mFireSprite.setOrigin(0, mHeight / 2);
        mFireSprite.setScale(scale);
    }

    public void passMousePosition(Vector2 pos) {
        mMousePos = new Vector2(pos);
        Vector2 myPos = mPlayer.getPosition();
        Vector2 dir = mMousePos.sub(myPos);
        float angle = (float) Math.atan2(dir.y, dir.x);
        mSprite.rotate((float) (angle * 180 / Math.PI) - mSprite.getRotation());
        mFireSprite.rotate((float) (angle * 180 / Math.PI)
                - mFireSprite.getRotation());

    }

    @Override
    public Sprite getCurrentSprite() {
        return mFired ? mFireSprite : mSprite;
    }

    public void fire() {
        if (!mFired) {
            mFired = true;
            Timer t = new java.util.Timer();
            t.schedule(new TimerTask() {

                @Override
                public void run() {
                    mFired = false;
                }
            }, 200);
        }
    }

    @Override
    public Vector2 getPosition() {
        return mPlayer.getPosition().add(80, 0);
    }

    @Override
    public int drawOrder() {
        return 99;
    }

}
