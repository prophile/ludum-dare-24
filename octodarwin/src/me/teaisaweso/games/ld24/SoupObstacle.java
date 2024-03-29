package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class SoupObstacle extends PhysicalObstacle {

    enum EvolutionStage {
        NORMAL, TENTACLES;
    }

    private static boolean sTexturesLoaded = false;
    private static Texture sSoup1, sSoup2, sSoup3, sSoupTentacle1,
            sSoupTentacle2, sSoupPoof;

    private EvolutionStage mStage;
    private final Sprite mSprite;
    private final Sound mEvolutionSound;
    private int mTicks;
    public boolean mDead;

    private final EvolutionGlow mGlow = new EvolutionGlow(this, new Vector2(0,
            -70), 220);

    private static void loadTexturesOnDemand() {
        if (!sTexturesLoaded) {
            loadTextures();
            sTexturesLoaded = true;
        }
    }

    private static void loadTextures() {
        sSoup1 = new Texture(Gdx.files.internal("assets/Asset_Soup_1.png"));
        sSoup2 = new Texture(Gdx.files.internal("assets/Asset_Soup_2.png"));
        sSoup3 = new Texture(Gdx.files.internal("assets/Asset_Soup_3.png"));
        sSoupTentacle1 = new Texture(
                Gdx.files.internal("assets/Asset_Soup_tentacle1.png"));
        sSoupTentacle2 = new Texture(
                Gdx.files.internal("assets/Asset_Soup_tentacle2.png"));
        sSoupPoof = new Texture(
                Gdx.files.internal("assets/Asset_Soup_tentacle0.png"));
    }

    public SoupObstacle(float x, World w) {
        loadTexturesOnDemand();
        mWidth = 180;
        mHeight = 194;
        mSprite = new Sprite(sSoup1, (int) mWidth, (int) mHeight);
        mEvolutionSound = Gdx.audio.newSound(Gdx.files
                .internal("assets/Evolve.wav"));

        BodyDef bd = new BodyDef();
        bd.type = BodyType.DynamicBody;
        FixtureDef fd = new FixtureDef();
        PolygonShape ps = new PolygonShape();
        ps.setAsBox(70 / GameWrapper.PHYSICS_RATIO,
                20 / GameWrapper.PHYSICS_RATIO);
        fd.shape = ps;
        fd.density = 1;
        fd.isSensor = true;
        bd.fixedRotation = true;
        bd.position.set(new Vector2(x / GameWrapper.PHYSICS_RATIO,
                30 / GameWrapper.PHYSICS_RATIO));
        mBody = w.createBody(bd);
        mBody.createFixture(fd);
        mBody.setUserData(this);
        mTicks = 0;
        mDead = false;
        mStage = EvolutionStage.NORMAL;
    }

    @Override
    public void collide(Entity e, Contact c) {
        if (e instanceof Enemy && mStage != EvolutionStage.NORMAL) {
            Enemy enemy = (Enemy) e;
            enemy.addStatusModifier(freshStatusModifier());
            enemy.mBody.setLinearVelocity(0.0f, 0.0f);
            mDead = true;
        }
    }

    @Override
    public boolean update() {
        mGlow.update();
        if (mStage == EvolutionStage.NORMAL) {
            if (mTicks % 21 < 7) {
                mSprite.setTexture(sSoup1);
            } else if (mTicks % 21 > 14) {
                mSprite.setTexture(sSoup2);
            } else {
                mSprite.setTexture(sSoup3);
            }
        } else {
            if (mTicks < 12) {
                mSprite.setTexture(sSoupPoof);
            } else if (mTicks % 20 < 10) {
                mSprite.setTexture(sSoupTentacle1);
            } else {
                mSprite.setTexture(sSoupTentacle2);
            }
        }
        mTicks++;

        return shouldCull();
    }

    @Override
    public void hit() {
        if (mStage == EvolutionStage.NORMAL) {
            mStage = EvolutionStage.TENTACLES;
            mEvolutionSound.play();
            mTicks = 0;
        }
    }

    @Override
    public Sprite getCurrentSprite() {
        return mSprite;
    }

    @Override
    public Vector2 getPosition() {
        // TODO Auto-generated method stub
        return super.getPosition().add(0, 50);
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
