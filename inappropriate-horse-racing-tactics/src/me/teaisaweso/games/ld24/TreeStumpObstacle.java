package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;

public class TreeStumpObstacle extends PhysicalObstacle {

    private Sprite mSprite;

    public TreeStumpObstacle(Vector2 worldPosition, World w, float scale) {
        super(null);
        Texture t = new Texture(Gdx.files.internal("assets/Stump.png"));
        mSprite = new Sprite(t, (int)(161), (int)(122));
        mSprite.setScale(scale);

        BodyDef bd = new BodyDef();
        bd.type = BodyType.StaticBody;
        FixtureDef fd = new FixtureDef();
        PolygonShape ps = new PolygonShape();
        ps.setAsBox(161*scale / (2 * GameWrapper.PHYSICS_RATIO) - 2,
                122*scale / (2 * GameWrapper.PHYSICS_RATIO));
        fd.shape = ps;
        fd.friction = 0;
        bd.position.set(new Vector2(worldPosition.x / GameWrapper.PHYSICS_RATIO,
                worldPosition.y / GameWrapper.PHYSICS_RATIO));
        Body b = w.createBody(bd);
        b.createFixture(fd);
        mBody = b;
        mWidth = 161*scale;
        mHeight = 122*scale;
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
