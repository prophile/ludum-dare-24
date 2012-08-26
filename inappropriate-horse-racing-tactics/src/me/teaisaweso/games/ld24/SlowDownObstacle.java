package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class SlowDownObstacle extends PhysicalObstacle {

    enum EvolutionStage {
        NORMAL, FLYING, TENTACLES;
    }

    EvolutionStage mStage = EvolutionStage.NORMAL;
    private final Sound mDarwinHurtSound;
    private final Sound mEvolutionSound;
    private final Sprite mSprite;
    private static Texture sUnevolvedTexture, sPoofTexture, sFlapTextureHigh,
            sFlapTextureLow, sTentacleTextureHigh, sTentacleTextureLow;
    private static boolean texturesLoaded = false;
    public int mHitTicks = 0;
    public boolean mDead = false;

    private static void loadTexturesOnDemand() {
        if (!texturesLoaded) {
            loadTextures();
            texturesLoaded = true;
        }
    }

    private static void loadTextures() {
        sUnevolvedTexture = new Texture(
                Gdx.files.internal("assets/Asset_Banana_1.png"));
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
    }

    public SlowDownObstacle(Body b) {
        super(b);
        loadTexturesOnDemand();
        mWidth = 150;
        mHeight = 150;
        mSprite = new Sprite(sUnevolvedTexture, (int) mWidth, (int) mHeight);
        mSprite.setOrigin(-120.0f, 210.0f);
        mSprite.setScale(1.2f);
        mDarwinHurtSound = Gdx.audio.newSound(Gdx.files
                .internal("assets/DarwinHurt.wav"));
        mEvolutionSound = Gdx.audio.newSound(Gdx.files
                .internal("assets/Evolve.wav"));
    }

    @Override
    public void collide(Entity e) {
        if (e instanceof Enemy && mStage != EvolutionStage.NORMAL) {
            Enemy enemy = (Enemy) e;
            enemy.addStatusModifier(freshStatusModifier());
            enemy.mBody.setLinearVelocity(0.0f, 0.0f);
            mDarwinHurtSound.play();
            mDead = true;
        }
    }

    @Override
    public void hit() {
        System.out.println("evolving");
        Vector2 position = getPosition();
        if ((GameWrapper.instance.getRNG().nextInt() & 1) == 0) {
            mStage = EvolutionStage.FLYING;
            mBody.applyLinearImpulse(new Vector2(120, 0), position);
        } else {
            mStage = EvolutionStage.TENTACLES;
            mBody.applyLinearImpulse(new Vector2(120, 220), position);
            mSprite.setScale(1.8f);
            mSprite.setOrigin(0.0f, 90.0f);
        }
        mEvolutionSound.play();
    }

    @Override
    public void update() {
        if (mStage != EvolutionStage.NORMAL) {
            ++mHitTicks;
            if (mHitTicks > 60 && mStage == EvolutionStage.FLYING) {
                Vector2 position = getPosition();
                Enemy e = GameWrapper.instance.getEnemy();
                Vector2 target = e.getPosition();
                target.add(new Vector2(30, 150));
                target.sub(position);
                target.mul(1.2f);
                mBody.applyForceToCenter(target);
            } else if (mStage == EvolutionStage.TENTACLES) {
                mBody.applyForceToCenter(new Vector2(0.0f, -500.0f));
            }
            if (mHitTicks < 18) {
                mSprite.setTexture(sPoofTexture);
            } else {
                if (mStage == EvolutionStage.FLYING) {
                    if (mHitTicks % 12 < 6) {
                        mSprite.setTexture(sFlapTextureHigh);
                    } else {
                        mSprite.setTexture(sFlapTextureLow);
                    }
                } else {
                    if (mHitTicks % 12 < 6) {
                        mSprite.setTexture(sTentacleTextureHigh);
                    } else {
                        mSprite.setTexture(sTentacleTextureLow);
                    }
                }
            }
            System.out.println(mHitTicks);
        }

    }

    @Override
    public Sprite getCurrentSprite() {
        return mSprite;
    }

    @Override
    public StatusModifier freshStatusModifier() {
        return new SlowDownModifier();
    }
}
