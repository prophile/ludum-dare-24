package me.teaisaweso.games.ld24;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
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
    private int mLastJumpTicks;
    private final Sound mJumpSound, mHurtSound;

    public Player(Sprite sprite, World world) {
        configureAttributes();
        mSprite = sprite;
        createPhysicsBody(world);
        mJumpSound = Gdx.audio.newSound(Gdx.files.internal("assets/Jump.wav"));
        mHurtSound = Gdx.audio.newSound(Gdx.files
                .internal("assets/MonkeyHurt.wav"));
    }

    private void createPhysicsBody(World world) {
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
        bd.position.set(0, 3);
        Body b = world.createBody(bd);
        b.createFixture(fd);
        mBody = b;
    }

    private void configureAttributes() {
        mAttributes.mMaxSpeed = 33;
        mAttributes.mAccel = 100;
        mWidth = 200;
        mHeight = 200;
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
        float currentAccel = mAttributes.mAccel;
        for (StatusModifier modifier : mStatusModifiers) {
            currentAccel = modifier.adjustAccel(currentAccel);
        }

        return currentAccel;
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
        if (!GameWrapper.instance.isOnFloor()) {
            mBody.applyForceToCenter(0.0f, -98f * mBody.getMass());
        }
        mLastJumpTicks++;
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

    public void jump() {
        if (mLastJumpTicks > 10) {
            mBody.setLinearVelocity(mBody.getLinearVelocity().add(0, 50));
            mLastJumpTicks = 0;
            mJumpSound.play();
        }

    }

    public void doHurt() {
        mSprite.setColor(0.7f, 0.3f, 0.3f, 1.0f);
        mBody.setLinearVelocity(mBody.getLinearVelocity().mul(0.1f));
        mHurtSound.play();
        Timer t = new Timer();
        t.schedule(new TimerTask() {

            @Override
            public void run() {
                mSprite.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                mBody.setLinearVelocity(mBody.getLinearVelocity().mul(3f));
            }
        }, 1000);
    }

}
