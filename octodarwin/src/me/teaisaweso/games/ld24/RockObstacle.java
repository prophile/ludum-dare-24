package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class RockObstacle extends PhysicalObstacle {

    private final Sprite mSprite;

    public boolean mDead;

    public RockObstacle(float x, World w) {
        Vector2 worldPos = new Vector2(x,
                50.0f + (Constants.getFloat("rockSize") - 90.0f));
        configureAttributes();
        mSprite = new Sprite(loadRockTexture(), 90, 90);
        mSprite.setScale(Constants.getFloat("rockSize") / 90.0f);
        mSprite.setOrigin(0, 0);
        createPhysicsBody(worldPos, w);
    }

    private Texture loadRockTexture() {
        Texture t = new Texture(Gdx.files.internal("assets/AssetRockDraft.png"));
        t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        return t;
    }

    private void createPhysicsBody(Vector2 worldPosition, World w) {
        BodyDef bd = new BodyDef();
        bd.position.set(worldPosition.mul(1.0f / GameWrapper.PHYSICS_RATIO));
        FixtureDef fd = new FixtureDef();
        PolygonShape ps = new PolygonShape();
        ps.setAsBox(mWidth / (2 * GameWrapper.PHYSICS_RATIO), mHeight
                / (2 * GameWrapper.PHYSICS_RATIO));
        fd.shape = ps;
        mBody = w.createBody(bd);
        mBody.createFixture(fd);
        mBody.setUserData(this);
    }

    private void configureAttributes() {
        mWidth = Constants.getInt("rockSize");
        mHeight = Constants.getInt("rockSize");
        mDead = false;
    }

    @Override
    public void collide(Entity e, Contact c) {
        if (e instanceof Player && !mDead) {
            mDead = true;
            Player p = (Player) e;
            p.doHurt();
        }
    }

    @Override
    public void hit() {
        // TODO Auto-generated method stub

    }

    @Override
    public StatusModifier freshStatusModifier() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Sprite getCurrentSprite() {
        return mSprite;
    }

    @Override
    public boolean update() {
        return shouldCull();
    }

}
