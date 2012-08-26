package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class SoupObstacle extends PhysicalObstacle {

    enum EvolutionStage {
        NORMAL, TENTACLES;
    }

    private static boolean sTexturesLoaded = false;
    private static Texture sSoup1, sSoup2, sSoup3, sSoupTentacle1,
            sSoupTentacle2;

    private EvolutionStage mStage;
    private Sprite mSprite;
    private Sound mEvolutionSound;

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
        sSoupTentacle1 = new Texture(Gdx.files
                .internal("assets/Asset_Soup_tentacle1.png"));
        sSoupTentacle2 = new Texture(Gdx.files
                .internal("assets/Asset_Soup_tentacle2.png"));
    }

    public SoupObstacle(Vector2 v, World w) {
        super(null);
        loadTexturesOnDemand();
        mWidth = 180;
        mHeight = 115;
        mSprite = new Sprite(sSoup1, (int) mWidth, (int) mHeight);
        mSprite.setOrigin(0.0f, 0.0f);
        mSprite.setScale(1.0f);
        mEvolutionSound = Gdx.audio.newSound(Gdx.files
                .internal("assets/Evolve.wav"));

        BodyDef bd = new BodyDef();
        bd.type = BodyType.StaticBody;
        FixtureDef fd = new FixtureDef();
        PolygonShape ps = new PolygonShape();
        ps.setAsBox(mWidth, mHeight);
        fd.shape = ps;
        bd.fixedRotation = true;
        bd.position.set(v.x, v.y);
        mBody = w.createBody(bd);
        mBody.createFixture(fd);
    }

    @Override
    public void collide(Entity e) {
        if (e instanceof Enemy && mStage != EvolutionStage.NORMAL) {
            Enemy enemy = (Enemy) e;
            enemy.addStatusModifier(freshStatusModifier());
            enemy.mBody.setLinearVelocity(0.0f, 0.0f);
        }
    }

    @Override
    public void hit() {
        // TODO Auto-generated method stub
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
