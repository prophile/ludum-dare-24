package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class TreeStumpObstacle extends PhysicalObstacle {

    private final Sprite mSprite;
    private final float mScale;

    public TreeStumpObstacle(float x, World w) {
        this(new Vector2(x, 50), w, 1.0f);
    }

    public TreeStumpObstacle(Vector2 worldPosition, World w, float scale) {
        Texture t = new Texture(Gdx.files.internal("assets/Stump.png"));
        mSprite = new Sprite(t, 161, 122);
        mScale = scale;
        mSprite.setScale(scale);

        BodyDef bd = new BodyDef();
        bd.type = BodyType.StaticBody;
        FixtureDef fd = new FixtureDef();
        PolygonShape ps = new PolygonShape();
        ps.setAsBox(161 * scale / (2 * GameWrapper.PHYSICS_RATIO) - 2 * scale,
                122 * scale / (2 * GameWrapper.PHYSICS_RATIO) - 2*(scale-1.0f));
        fd.shape = ps;
        fd.friction = Constants.getFloat("treeStumpFriction");
        bd.position.set(new Vector2(
                worldPosition.x / GameWrapper.PHYSICS_RATIO, worldPosition.y
                        / GameWrapper.PHYSICS_RATIO));
        Body b = w.createBody(bd);
        b.createFixture(fd);
        mBody = b;
        mBody.setUserData(this);
        mWidth = 161 * scale;
        mHeight = 122 * scale;
    }

    public TreeStumpObstacle createNearbyStump(World w) {
        return new TreeStumpObstacle(new Vector2(getPosition().add(
                Constants.getFloat("treeStumpSecondarySpacing"), 30)), w, 1.5f);
    }

    @Override
    public void collide(Entity e, Contact c) {
        // Re-enabled collision for players
        if (e instanceof Player) {
            c.setEnabled(true);
        }

    }

    public float getWalkHeight() {
        return 122 * mSprite.getScaleY();
    }

    @Override
    public void hit() {
        // TODO Auto-generated method stub

    }

    @Override
    public Vector2 getPosition() {
        if (mScale == 1.0) {
            return super.getPosition();
        } else {
            return super.getPosition().add(32, 32);
        }
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
