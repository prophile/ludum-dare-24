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
    private int mTicks = 0;
    private int mDeathTick = 0;
    private boolean mGotHurt = false;
    private boolean mDying = false;

    private static Texture sFlapTexture1, sFlapTexture2, sJumpTexture,
            sAirTexture, sHurtTexture;
    private static boolean sTexturesLoaded = false;

    private static void loadTextures() {
        sFlapTexture1 = new Texture(
                Gdx.files.internal("assets/Asset_Monkey_Flapping1.png"));
        sFlapTexture2 = new Texture(
                Gdx.files.internal("assets/Asset_Monkey_Flapping2.png"));
        sJumpTexture = new Texture(
                Gdx.files.internal("assets/Asset_Monkey_jump.png"));
        sAirTexture = new Texture(
                Gdx.files.internal("assets/Asset_Monkey_midair.png"));
        sHurtTexture = new Texture(
                Gdx.files.internal("assets/Asset_Monkey_Hurt.png"));
        sFlapTexture1.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        sFlapTexture2.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        sJumpTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        sAirTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        sHurtTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    }

    private static void loadTexturesOnDemand() {
        if (!sTexturesLoaded) {
            loadTextures();
            sTexturesLoaded = true;
        }
    }

    public Player(World world) {
        loadTexturesOnDemand();

        mSprite = new Sprite(sFlapTexture1, 200, 200);
        configureAttributes();
        createPhysicsBody(world);
        mJumpSound = Gdx.audio.newSound(Gdx.files.internal("assets/Jump.wav"));
        mHurtSound = Gdx.audio.newSound(Gdx.files
                .internal("assets/MonkeyHurt.wav"));
    }

    private void createPhysicsBody(World world) {
        float scale = Constants.getFloat("playerSize");
        BodyDef bd = new BodyDef();
        FixtureDef fd = new FixtureDef();
        PolygonShape ps = new PolygonShape();

        mSprite.setScale(scale);
        ps.setAsBox(scale * (mWidth / (2 * GameWrapper.PHYSICS_RATIO) - 4),
                scale * (mHeight / (2 * GameWrapper.PHYSICS_RATIO)),
                new Vector2(-1, 0), 0);
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
        ++mTicks;
        if (mDeathTick != 0 && mTicks > mDeathTick + 80) {
            GameWrapper.instance.setGameOver();
            return false;
        }

        if (!GameWrapper.instance.isOnFloor()) {
            mBody.applyForceToCenter(0.0f, Constants.getFloat("gravity")
                    * mBody.getMass());
        }
        mLastJumpTicks++;
        updateAndRemoveModifiers();
        mBody.applyLinearImpulse(new Vector2(getEffectiveAccel(), 0),
                mBody.getPosition());
        if (mDying) {
            mBody.setLinearVelocity(getEffectiveMaxSpeed(),
                    mBody.getLinearVelocity().y);
        } else if (mBody.getLinearVelocity().x > getEffectiveMaxSpeed()) {
            mBody.setLinearVelocity(getEffectiveMaxSpeed(),
                    mBody.getLinearVelocity().y);
        }
        int flapTime = Constants.getInt("playerAnimationFlapTime");
        if (mGotHurt || mDying) {
            mSprite.setTexture(sHurtTexture);
        } else if (mLastJumpTicks < Constants.getInt("playerAnimationJumpTime")) {
            mSprite.setTexture(sJumpTexture);
        } else if (!GameWrapper.instance.isOnFloor()) {
            mSprite.setTexture(sAirTexture);
        } else {
            if (mTicks % (flapTime * 2) < flapTime) {
                mSprite.setTexture(sFlapTexture1);
            } else {
                mSprite.setTexture(sFlapTexture2);
            }
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
            GameWrapper.instance.getDustExplosionManager().pew(getPosition());
        }
    }

    public void doHurt() {
        mSprite.setColor(0.7f, 0.3f, 0.3f, 1.0f);
        mBody.setLinearVelocity(mBody.getLinearVelocity().mul(0.0000f));
        mAttributes.mAccel *= 0.5;
        mHurtSound.play();
        mGotHurt = true;
        Timer t = new Timer();
        t.schedule(new TimerTask() {

            @Override
            public void run() {
                mSprite.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                mAttributes.mAccel *= 2;
                mBody.setLinearVelocity(mBody.getLinearVelocity().mul(1f));
                mGotHurt = false;
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

    public void caught() {
        mAttributes.mAccel = 0.0f;
        mAttributes.mMaxSpeed = 0.0f;
        mDying = true;
        GameWrapper.instance.mDying = true;

        mBody.applyLinearImpulse(new Vector2(0.0f, 5000.0f),
                mBody.getPosition());

        mDeathTick = mTicks;
        mSprite.setColor(0.7f, 0.3f, 0.3f, 1.0f);
        mSprite.setTexture(sHurtTexture);

        // GameWrapper.instance.mMusic.stop();
        GameWrapper.instance.mDeathMusic.play();
    }
}
