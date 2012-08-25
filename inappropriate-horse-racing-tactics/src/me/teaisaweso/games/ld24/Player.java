package me.teaisaweso.games.ld24;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Player extends Entity {
    private final List<StatusModifier> mStatusModifiers = new ArrayList<StatusModifier>();
    private final Sprite mSprite;

    public Player(Sprite sprite, World world) {
        mEa.mMaxSpeed = 30;
        mEa.mAccel = 30;
        mSprite = sprite;
        mWidth = 32;
        mHeight = 32;
        BodyDef bd = new BodyDef();
        FixtureDef fd = new FixtureDef();
        PolygonShape ps = new PolygonShape();
        ps.setAsBox(1, 1);
        fd.density = 100;
        fd.shape = ps;
        bd.fixedRotation = true;
        bd.type = BodyType.DynamicBody;
        bd.fixedRotation = true;
        bd.position.set(0,3);
        Body b = world.createBody(bd);
        b.createFixture(fd);
        mBody = b;
    }

    public void addStatusModifier(StatusModifier modifier) {
        mStatusModifiers.add(modifier);
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
        float currentAccel = mEa.mAccel;
        for (StatusModifier modifier : mStatusModifiers) {
            currentAccel = modifier.adjustAccel(currentAccel);
        }
        
        return currentAccel;
    }
    
    public float getEffectiveMaxSpeed() {
        float currentSpeed = mEa.mMaxSpeed;
        
        for (StatusModifier modifier : mStatusModifiers) {
            currentSpeed = modifier.adjustMaxSpeed(currentSpeed);
        }
        
        return currentSpeed;
    }

    @Override
    public void update() {
        for (StatusModifier modifier : mStatusModifiers) {
            modifier.update();
        }
        mBody.applyLinearImpulse(new Vector2(this.getEffectiveAccel(), 0), mBody.getPosition());
        if (mBody.getLinearVelocity().x > this.getEffectiveMaxSpeed())
        {
            mBody.setLinearVelocity(this.getEffectiveMaxSpeed(), mBody.getLinearVelocity().y);
        }
    }

}
