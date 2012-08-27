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
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class HorseObstacle extends PhysicalObstacle {

    enum EvolutionStage {
        NORMAL, TENTACLES;
    }

    EvolutionStage mStage = EvolutionStage.NORMAL;
    private final Sound mDarwinHurtSound;
    private final Sound mEvolutionSound;
    private final Sprite mSprite;
    private static Texture sPoofTexture, sTentacleTextureHigh,
            sTentacleTextureLow;
    private final static Texture[] sUnevolvedTextures = new Texture[6];
    private static boolean texturesLoaded = false;
    public int mHitTicks = 0;
    public boolean mDead = false;

    private final EvolutionGlow mGlow = new EvolutionGlow(this, new Vector2(0,
            -30), 700);

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

        sUnevolvedTextures[0] = new Texture(
                Gdx.files.internal("assets/Asset_horse1.png"));
        setupFiltering(sUnevolvedTextures[0]);
        sPoofTexture = new Texture(
                Gdx.files.internal("assets/Asset_horse2.png"));
        sTentacleTextureHigh = new Texture(
                Gdx.files.internal("assets/Asset_horse3.png"));
        sTentacleTextureLow = new Texture(
                Gdx.files.internal("assets/Asset_horse4.png"));
        setupFiltering(sPoofTexture);
        setupFiltering(sTentacleTextureHigh);
        setupFiltering(sTentacleTextureLow);
    }

    public HorseObstacle(float x, World world) {
        loadTexturesOnDemand();

        BodyDef bd = new BodyDef();
        bd.position.set(x / 16, Constants.getInt("horseHeight") / 16);
        bd.type = BodyType.DynamicBody;
        FixtureDef fd = new FixtureDef();
        PolygonShape ps = new PolygonShape();
        mWidth = 333;
        mHeight = 199;
        ps.setAsBox(mWidth / (2 * GameWrapper.PHYSICS_RATIO), mHeight
                / (2 * GameWrapper.PHYSICS_RATIO));

        fd.shape = ps;
        fd.isSensor = false;
        fd.density = 1;
        mBody = world.createBody(bd);
        mBody.setFixedRotation(true);
        mBody.createFixture(fd);
        mBody.setUserData(this);

        mSprite = new Sprite(sUnevolvedTextures[0], (int) mWidth, (int) mHeight);
        mDarwinHurtSound = Gdx.audio.newSound(Gdx.files
                .internal("assets/DarwinHurt.wav"));
        mEvolutionSound = Gdx.audio.newSound(Gdx.files
                .internal("assets/Evolve.wav"));
    }

    @Override
    public void collide(Entity e, Contact c) {
        if (e instanceof Enemy && mStage != EvolutionStage.NORMAL) {
            // GameWrapper.instance.getExplosionManager().pew(getPosition());
            Enemy enemy = (Enemy) e;
            enemy.addStatusModifier(freshStatusModifier());
            enemy.mBody.setLinearVelocity(0.0f, 0.0f);
            mDarwinHurtSound.play();
            mDead = true;
        }
    }

    @Override
    public void hit() {
        if (mStage == EvolutionStage.NORMAL) {
            mStage = EvolutionStage.TENTACLES;
            mBody.setLinearVelocity(Constants.getFloat("horseRecoil"), 0.0f);
            mEvolutionSound.play();
        }
    }

    @Override
    public boolean update() {
        mGlow.update();
        if (mStage == EvolutionStage.NORMAL) {
            mSprite.setTexture(sUnevolvedTextures[0]);
        } else {
            ++mHitTicks;
            if (mHitTicks < Constants.getInt("horsePoofTime")) {
                mSprite.setTexture(sPoofTexture);
            } else {
                int flapTime = Constants.getInt("horseGallopAnimationTime");
                if (mHitTicks % (2 * flapTime) < flapTime) {
                    mSprite.setTexture(sTentacleTextureHigh);
                } else {
                    mSprite.setTexture(sTentacleTextureLow);
                }
            }
            if (mHitTicks >= Constants.getInt("horseAccelerationWaitTime")) {
                mBody.applyLinearImpulse(
                        new Vector2(Constants.getFloat("horseAcceleration"),
                                0.0f), mBody.getPosition());
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
