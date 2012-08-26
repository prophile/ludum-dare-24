package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class SlowDownObstacle extends PhysicalObstacle {

    public boolean mIsEvolved = false;
    private final Sound mDarwinHurtSound;
    private final Sound mEvolutionSound;
    private final Sprite mSprite;
    private static Texture unevolvedTexture, poofTexture, flapTextureHigh,
            flapTextureLow;
    private static boolean texturesLoaded = false;
    private int mHitTicks = 0;
    public boolean mDead = false;

    private static void loadTexturesOnDemand() {
        if (!texturesLoaded) {
            loadTextures();
            texturesLoaded = true;
        }
    }

    private static void loadTextures() {
        unevolvedTexture = new Texture(
                Gdx.files.internal("assets/Asset_Banana_1.png"));
        poofTexture = new Texture(
                Gdx.files.internal("assets/Asset_Banana_2.png"));
        flapTextureHigh = new Texture(
                Gdx.files.internal("assets/Asset_Banana_3.png"));
        flapTextureLow = new Texture(
                Gdx.files.internal("assets/Asset_Banana_4.png"));
    }

    public SlowDownObstacle(Body b) {
        super(b);
        loadTexturesOnDemand();
        mWidth = 150;
        mHeight = 150;
        mSprite = new Sprite(unevolvedTexture, (int) mWidth, (int) mHeight);
        mSprite.setOrigin(-120.0f, 210.0f);
        mSprite.setScale(1.2f);
        mDarwinHurtSound = Gdx.audio.newSound(Gdx.files
                .internal("assets/DarwinHurt.wav"));
        mEvolutionSound = Gdx.audio.newSound(Gdx.files
                .internal("assets/Evolve.wav"));
    }

    @Override
    public void collide(Entity e) {
        if (e instanceof Enemy && mIsEvolved) {
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
        mIsEvolved = true;
        mEvolutionSound.play();
        Vector2 position = getPosition();
        mBody.applyLinearImpulse(new Vector2(0, 50), position);
    }

    @Override
    public void update() {
        if (mIsEvolved) {
            ++mHitTicks;
            Vector2 position = getPosition();
            Enemy e = GameWrapper.instance.getEnemy();
            Vector2 target = e.getPosition();
            target.add(new Vector2(0, 150));
            target.sub(position);
            target.mul(3.8f);
            mBody.applyForceToCenter(target);
            if (mHitTicks < 6) {
                mSprite.setTexture(poofTexture);
            } else {
                if (mHitTicks % 6 < 3) {
                    mSprite.setTexture(flapTextureHigh);
                } else {
                    mSprite.setTexture(flapTextureLow);
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
