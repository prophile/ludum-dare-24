package me.teaisaweso.games.ld24;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final List<StatusModifier> mStatusModifiers = new ArrayList<StatusModifier>();

    public Enemy(Sprite sprite, World world) {
        mSprite = sprite;
        mWidth = 200;
        mHeight = 400;
        mAttributes.mMaxSpeed = 30000;
        mAttributes.mAccel = 57;
        BodyDef bd = new BodyDef();
        FixtureDef fd = new FixtureDef();
        PolygonShape ps = new PolygonShape();
        ps.setAsBox(mWidth / (2 * GameWrapper.PHYSICS_RATIO), mHeight
                / (2 * GameWrapper.PHYSICS_RATIO));
        fd.density = 1;
        fd.shape = ps;
        bd.fixedRotation = true;
        bd.type = BodyType.DynamicBody;
        bd.fixedRotation = true;
        bd.position.set(-13, 3);
        Body b = world.createBody(bd);
        b.createFixture(fd);
        mBody = b;
    }

    @Override
    public Sprite getCurrentSprite() {
        Sprite currentSprite = mSprite;

        for (StatusModifier modifier : mStatusModifiers) {
            currentSprite = modifier.getCurrentSprite(currentSprite);
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
        GameWrapper.sIsGameOver = true;
    }

    public float getEffectiveMaxSpeed() {
        float currentSpeed = mAttributes.mMaxSpeed;

        for (StatusModifier modifier : mStatusModifiers) {
            currentSpeed = modifier.adjustMaxSpeed(currentSpeed);
        }

        return currentSpeed;
    }

    @Override
    public void update() {
        updateAndRemoveModifiers();
        mBody.applyLinearImpulse(new Vector2(getEffectiveAccel(), 0),
                mBody.getPosition());
        if (mBody.getLinearVelocity().x > getEffectiveMaxSpeed()) {
            mBody.setLinearVelocity(getEffectiveMaxSpeed(),
                    mBody.getLinearVelocity().y);
        }
    }

    private void updateAndRemoveModifiers() {
        Set<StatusModifier> endedModifiers = new HashSet<StatusModifier>();
        for (StatusModifier modifier : mStatusModifiers) {
            modifier.update();
            if (modifier.hasEnded()) {
                endedModifiers.add(modifier);
                System.out.println("removing");
            }
        }

        mStatusModifiers.removeAll(endedModifiers);
    }

    public void addStatusModifier(StatusModifier sm) {
        mStatusModifiers.add(sm);
    }

}
