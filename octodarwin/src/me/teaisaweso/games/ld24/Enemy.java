package me.teaisaweso.games.ld24;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Enemy extends Entity {

    private final Sprite mSprite;
    private final Sprite mSprite2;
    private final Sprite mAngrySprite;
    private final List<StatusModifier> mStatusModifiers = new ArrayList<StatusModifier>();
    private int mTicks;

    public Enemy(World world) {
        Texture t;
        t = new Texture(Gdx.files.internal("assets/Asset_Darwin1.png"));
        t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        mSprite = new Sprite(t, 200, 400);
        t = new Texture(Gdx.files.internal("assets/Asset_Darwin2.png"));
        mSprite2 = new Sprite(t, 200, 400);

        t = new Texture(Gdx.files.internal("assets/Asset_Darwinangry.png"));
        mAngrySprite = new Sprite(t, 200, 400);

        mWidth = 200;
        mHeight = 400;
        mAttributes.mMaxSpeed = Constants.getFloat("darwinMaxSpeed");
        mAttributes.mAccel = Constants.getFloat("darwinAccel");
        BodyDef bd = new BodyDef();
        FixtureDef fd = new FixtureDef();
        PolygonShape ps = new PolygonShape();
        ps.setAsBox(mWidth / (2 * GameWrapper.PHYSICS_RATIO), mHeight
                / (2 * GameWrapper.PHYSICS_RATIO) - 2);
        fd.density = 1;
        fd.shape = ps;
        bd.fixedRotation = true;
        bd.type = BodyType.DynamicBody;
        bd.fixedRotation = true;
        bd.position.set(-13, 3);
        Body b = world.createBody(bd);
        b.createFixture(fd);
        mBody = b;
        mBody.setUserData(this);
    }

    @Override
    public Sprite getCurrentSprite() {
        Sprite currentSprite = mTicks % 20 < 10 ? mSprite : mSprite2;
        if (mStatusModifiers.size() >= 1) {
            return mAngrySprite;
        }

        return currentSprite;

    }

    public float getEffectiveAccel() {
        float currentAccel = mAttributes.mAccel;
        for (StatusModifier modifier : mStatusModifiers) {
            currentAccel = modifier.adjustAccel(currentAccel);
        }

        return currentAccel;
    }

    public void catchPlayer() {
        GameWrapper.instance.setGameOver();
    }

    public float getEffectiveMaxSpeed() {
        float currentSpeed = mAttributes.mMaxSpeed;

        for (StatusModifier modifier : mStatusModifiers) {
            currentSpeed = modifier.adjustMaxSpeed(currentSpeed);
        }

        return currentSpeed;
    }

    @Override
    public boolean update() {
        float cameraX = GameWrapper.instance.getCameraOrigin().x;
        float playerX = GameWrapper.instance.getPlayer().getPosition().x;
        mTicks += 1;
        updateAndRemoveModifiers();
        mBody.applyLinearImpulse(new Vector2(getEffectiveAccel(), 0),
                mBody.getPosition());
        if (getPosition().x - cameraX < -600) {
            mBody.setLinearVelocity(new Vector2(getEffectiveMaxSpeed()
                    + Constants.getFloat("darwinBoostAmount"), 0));
        } else {
            if (mBody.getLinearVelocity().x > getEffectiveMaxSpeed()) {
                mBody.setLinearVelocity(
                        mBody.getLinearVelocity().x
                                * Constants.getFloat("darwinBoostFalloff"),
                        mBody.getLinearVelocity().y);
            }
        }

        if (playerX - getPosition().x < Constants
                .getFloat("darwinPlayerCushionSize")) {
            if (Constants.getBoolean("darwinDebug")) {
                mSprite.setColor(0.5f, 0.0f, 0.0f, 1f);
                mSprite.setColor(0.5f, 0.0f, 0.0f, 1);
            }
            mBody.setLinearVelocity(mBody.getLinearVelocity().mul(
                    Constants.getFloat("darwinPlayerCushionSlowDown")));
        } else {
            mSprite.setColor(1f, 1f, 1f, 1f);
            mSprite.setColor(1, 1, 1, 1);
        }

        return false;
    }

    private void updateAndRemoveModifiers() {
        Set<StatusModifier> endedModifiers = new HashSet<StatusModifier>();
        for (StatusModifier modifier : mStatusModifiers) {
            modifier.update();
            if (modifier.hasEnded()) {
                endedModifiers.add(modifier);
            }
        }

        mStatusModifiers.removeAll(endedModifiers);
    }

    public void addStatusModifier(StatusModifier sm) {
        mStatusModifiers.add(sm);
    }

    @Override
    public int drawOrder() {
        return 98;
    }

    @Override
    public Vector2 getPosition() {
        // TODO Auto-generated method stub
        return super.getPosition().add(0, 30);
    }

}
