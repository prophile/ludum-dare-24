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

    private Sprite mSprite;
    private final List<StatusModifier> mStatusModifiers = new ArrayList<StatusModifier>();

    public Enemy(Sprite sprite, World world) {
        mSprite = sprite;
        mWidth = 32;
        mHeight = 32;
        mEa.mMaxSpeed = 1;
        mEa.mAccel = 30;
        BodyDef bd = new BodyDef();
        FixtureDef fd = new FixtureDef();
        PolygonShape ps = new PolygonShape();
        ps.setAsBox(1, 1);
        fd.density = 100;
        fd.shape = ps;
        bd.fixedRotation = true;
        bd.type = BodyType.DynamicBody;
        bd.fixedRotation = true;
        bd.position.set(-8, 3);
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
        updateAndRemoveModifiers();
        mBody.applyLinearImpulse(new Vector2(this.getEffectiveAccel(), 0), mBody.getPosition());
        if (mBody.getLinearVelocity().x > this.getEffectiveMaxSpeed())
        {
            mBody.setLinearVelocity(this.getEffectiveMaxSpeed(), mBody.getLinearVelocity().y);
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

}
