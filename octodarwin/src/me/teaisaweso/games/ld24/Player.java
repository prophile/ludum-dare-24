package me.teaisaweso.games.ld24;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
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

public class Player extends Entity {
    private final List<StatusModifier> mStatusModifiers = new ArrayList<StatusModifier>();
    private final Sprite mSprite;
    private int mLastJumpTicks;
    private final Sound mJumpSound, mHurtSound;

    public Player(World world) {
        Texture mTexture = new Texture(
                Gdx.files.internal("assets/AssetMonkey.png"));
        mTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        mSprite = new Sprite(mTexture, 200, 200);
        configureAttributes();
        createPhysicsBody(world);
        mJumpSound = Gdx.audio.newSound(Gdx.files.internal("assets/Jump.wav"));
        mHurtSound = Gdx.audio.newSound(Gdx.files
                .internal("assets/MonkeyHurt.wav"));
    }

    private void createPhysicsBody(World world) {
        BodyDef bd = new BodyDef();
        FixtureDef fd = new FixtureDef();
        PolygonShape ps = new PolygonShape();

        ps.setAsBox(mWidth / (2 * GameWrapper.PHYSICS_RATIO) - 4, mHeight
                / (2 * GameWrapper.PHYSICS_RATIO), new Vector2(-1, 0), 0);
        fd.density = 1;
        fd.shape = ps;

        bd.type = BodyType.DynamicBody;
        bd.fixedRotation = true;
        bd.position.set(0, 3);
        Body b = world.createBody(bd);
        b.createFixture(fd);
        mBody = b;
        mBody.setUserData(this);
    }

    private void configureAttributes() {
        mAttributes.mMaxSpeed = Constants.getFloat("playerMaxSpeed");
        mAttributes.mAccel = Constants.getFloat("playerAccel");
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
    public boolean update() {
        if (!GameWrapper.instance.isOnFloor()) {
            mBody.applyForceToCenter(0.0f, Constants.getFloat("gravity")
                    * mBody.getMass());
        }
        mLastJumpTicks++;
        updateAndRemoveModifiers();
        mBody.applyLinearImpulse(new Vector2(getEffectiveAccel(), 0),
                mBody.getPosition());
        if (mBody.getLinearVelocity().x > getEffectiveMaxSpeed()) {
            mBody.setLinearVelocity(getEffectiveMaxSpeed(),
                    mBody.getLinearVelocity().y);
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

    public void jump() {
        if (mLastJumpTicks > 10) {
            mBody.setLinearVelocity(mBody.getLinearVelocity().add(0,
                    Constants.getFloat("jumpVelocity")));
            mLastJumpTicks = 0;
            mJumpSound.play();
        }

    }

    public void doHurt() {
        mSprite.setColor(0.7f, 0.3f, 0.3f, 1.0f);
        mBody.setLinearVelocity(mBody.getLinearVelocity().mul(0.0000f));
        mAttributes.mAccel *= 0.5;
        mHurtSound.play();
        Timer t = new Timer();
        t.schedule(new TimerTask() {

            @Override
            public void run() {
                mSprite.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                mAttributes.mAccel *= 2;
                mBody.setLinearVelocity(mBody.getLinearVelocity().mul(1f));
            }
        }, 1000);
    }

    @Override
    public Vector2 getPosition() {
        // TODO Auto-generated method stub
        return super.getPosition().add(32, 0);
    }

    @Override
    public int drawOrder() {
        return 100;
    }

}
