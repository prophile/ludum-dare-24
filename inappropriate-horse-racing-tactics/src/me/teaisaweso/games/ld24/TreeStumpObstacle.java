package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class TreeStumpObstacle extends PhysicalObstacle {

    private final Sprite mSprite;

    public static TreeStumpObstacle createStumpObstacle(World w, float scale) {
        float minSpacing = Constants.getFloat("treeStumpMinSpacing");
        float maxSpacing = Constants.getFloat("treeStumpMaxSpacing");
        float spacingRange = maxSpacing - minSpacing;
        Vector2 position = new Vector2(GameWrapper.instance.getCameraOrigin().x + minSpacing
                + GameWrapper.instance.mRng.nextFloat() * spacingRange, 50);
        return new TreeStumpObstacle(position, w, scale);
    }
 
    public TreeStumpObstacle(Vector2 worldPosition, World w, float scale) {
        super(null);
        Texture t = new Texture(Gdx.files.internal("assets/Stump.png"));
        mSprite = new Sprite(t, 161, 122);
        mSprite.setScale(scale);

        BodyDef bd = new BodyDef();
        bd.type = BodyType.StaticBody;
        FixtureDef fd = new FixtureDef();
        PolygonShape ps = new PolygonShape();
        ps.setAsBox(161 * scale / (2 * GameWrapper.PHYSICS_RATIO) - 2, 122
                * scale / (2 * GameWrapper.PHYSICS_RATIO));
        fd.shape = ps;
        fd.friction = Constants.getFloat("treeStumpFriction");
        bd.position.set(new Vector2(
                worldPosition.x / GameWrapper.PHYSICS_RATIO, worldPosition.y
                        / GameWrapper.PHYSICS_RATIO));
        Body b = w.createBody(bd);
        b.createFixture(fd);
        mBody = b;
        mWidth = 161 * scale;
        mHeight = 122 * scale;
    }

    public TreeStumpObstacle createNearbyStump(World w) {
        return new TreeStumpObstacle(
                new Vector2(getPosition()
                        .add(Constants
                                .getFloat("treeStumpSecondarySpacing"), 30)), w, 1.5f);
    }

    @Override
    public void collide(Entity e) {
        // TODO Auto-generated method stub

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

}
