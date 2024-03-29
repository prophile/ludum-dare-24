package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class BananaObstacle extends PhysicalObstacle {

    enum EvolutionStage {
        NORMAL, FLYING, TENTACLES;
    }

    EvolutionStage mStage = EvolutionStage.NORMAL;
    private final Sound mDarwinHurtSound;
    private final Sound mEvolutionSound;
    private final Sprite mSprite;
    private static Texture sPoofTexture, sFlapTextureHigh, sFlapTextureLow,
            sTentacleTextureHigh, sTentacleTextureLow;
    private final static Texture[] sUnevolvedTextures = new Texture[6];
    private static boolean texturesLoaded = false;
    public int mHitTicks = 0;
    private int mLifeTicks = 0;
    public boolean mDead = false;

    private int mTicks = 0;
    private int mLastPlayedSoundTicks = 0;
    private final EvolutionGlow mGlow = new EvolutionGlow(this, 0.2f);

    private static void loadTexturesOnDemand() {
        if (!texturesLoaded) {
            loadTextures();
            texturesLoaded = true;
        }
    }

    private static void setupFiltering(Texture texture) {
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        // texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
    }

    private static void loadTextures() {
        for (int i = 1; i <= 6; ++i) {
            sUnevolvedTextures[i - 1] = new Texture(
                    Gdx.files
                            .internal("assets/Asset_Banana_Swing" + i + ".png"));
            setupFiltering(sUnevolvedTextures[i - 1]);
        }
        sPoofTexture = new Texture(
                Gdx.files.internal("assets/Asset_Banana_2.png"));
        sFlapTextureHigh = new Texture(
                Gdx.files.internal("assets/Asset_Banana_3.png"));
        sFlapTextureLow = new Texture(
                Gdx.files.internal("assets/Asset_Banana_4.png"));
        sTentacleTextureHigh = new Texture(
                Gdx.files.internal("assets/Asset_Banana_Tentacle1.png"));
        sTentacleTextureLow = new Texture(
                Gdx.files.internal("assets/Asset_Banana_Tentacle2.png"));
        setupFiltering(sPoofTexture);
        setupFiltering(sFlapTextureHigh);
        setupFiltering(sFlapTextureLow);
        setupFiltering(sTentacleTextureHigh);
        setupFiltering(sTentacleTextureLow);
    }

    public BananaObstacle(float x, World world) {
        loadTexturesOnDemand();

        BodyDef bd = new BodyDef();
        bd.position.set(x / 16, Constants.getFloat("bananaHeight") / 16);
        bd.type = BodyType.DynamicBody;
        FixtureDef fd = new FixtureDef();
        CircleShape cs = new CircleShape();
        cs.setRadius(2);
        fd.shape = cs;
        fd.isSensor = false;
        fd.restitution = Constants.getFloat("bananaTentaclesRestitution");
        fd.density = 0.2f;
        mBody = world.createBody(bd);
        mBody.createFixture(fd);
        mBody.setUserData(this);

        mWidth = 150;
        mHeight = 150;
        mSprite = new Sprite(sUnevolvedTextures[0], (int) mWidth, (int) mHeight);
        mSprite.setOrigin(-120.0f, 210.0f);
        mSprite.setScale(1.2f);
        mDarwinHurtSound = Gdx.audio.newSound(Gdx.files
                .internal("assets/DarwinHurt.wav"));
        mEvolutionSound = Gdx.audio.newSound(Gdx.files
                .internal("assets/Evolve.wav"));
    }

    @Override
    public void collide(Entity e, Contact c) {
        System.out.println("enemy collide");
        if (e instanceof Enemy && mStage != EvolutionStage.NORMAL) {
            GameWrapper.instance.getBananaExplosionManager().pew(getPosition());
            Enemy enemy = (Enemy) e;
            enemy.addStatusModifier(freshStatusModifier());
            if (mTicks-mLastPlayedSoundTicks > 60) {
                System.out.println("darwin bananas");
                mDarwinHurtSound.play();
                mLastPlayedSoundTicks = mTicks;
            }
            mDead = true;
        }
    }

    @Override
    public void hit() {
        
        if (mStage == EvolutionStage.NORMAL) {
            Vector2 position = getPosition();
            mEvolutionSound.play();
            if (GameWrapper.instance.getRNG().nextFloat() < Constants
                    .getFloat("bananaFlyingChance")) {
                mStage = EvolutionStage.FLYING;
                mBody.applyLinearImpulse(
                        new Vector2(
                                Constants
                                        .getFloat("bananaFlyingInitialImpulseX"),
                                Constants
                                        .getFloat("bananaFlyingInitialImpulseY")),
                        position);
            } else {
                mStage = EvolutionStage.TENTACLES;
                mBody.applyLinearImpulse(
                        new Vector2(
                                Constants
                                        .getFloat("bananaTentaclesInitialImpulseX"),
                                Constants
                                        .getFloat("bananaTentaclesInitialImpulseY")),
                        position);
                mSprite.setScale(1.8f);
                mSprite.setOrigin(0.0f, 90.0f);
            }
            
        }
    }

    @Override
    public boolean update() {
        mGlow.update();
        mTicks++;
        if (mStage == EvolutionStage.NORMAL) {
            ++mLifeTicks;
            int rotateSpeed = Constants.getInt("bananaSwingTime");
            mSprite.setTexture(sUnevolvedTextures[mLifeTicks % rotateSpeed * 6
                    / rotateSpeed]);
        } else {
            ++mHitTicks;
            if (mHitTicks > Constants.getInt("bananaFlyingHoldTime")
                    && mStage == EvolutionStage.FLYING) {
                Vector2 position = getPosition();
                Enemy e = GameWrapper.instance.getEnemy();
                Vector2 target = e.getPosition();
                target.add(new Vector2(30, 150));
                target.sub(position);
                target.mul(Constants.getFloat("bananaFlyingHomingForce"));
                mBody.applyForceToCenter(target);
            } else if (mStage == EvolutionStage.TENTACLES) {
                mBody.applyForceToCenter(new Vector2(0.0f, Constants
                        .getFloat("bananaTentaclesGravity")));
            }
            if (mHitTicks < Constants.getInt("bananaPoofTime")) {
                mSprite.setTexture(sPoofTexture);
            } else {
                if (mStage == EvolutionStage.FLYING) {
                    int flapTime = Constants.getInt("bananaFlyingWingFlapTime");
                    if (mHitTicks % (2 * flapTime) < flapTime) {
                        mSprite.setTexture(sFlapTextureHigh);
                    } else {
                        mSprite.setTexture(sFlapTextureLow);
                    }
                } else {
                    int flapTime = Constants.getInt("bananaTentaclesFlailTime");
                    if (mHitTicks % (2 * flapTime) < flapTime) {
                        mSprite.setTexture(sTentacleTextureHigh);
                    } else {
                        mSprite.setTexture(sTentacleTextureLow);
                    }
                }
            }
        }

        return shouldCull();
    }

    @Override
    public Sprite getCurrentSprite() {
        return mSprite;
    }

    @Override
    public StatusModifier freshStatusModifier() {
        return new SlowDownModifier();
    }

    @Override
    public void draw(SpriteBatch sb) {
        if (mStage == EvolutionStage.NORMAL) {
            mGlow.draw(sb);
        }
        super.draw(sb);
    }
}
