package me.teaisaweso.games.ld24;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public abstract class RegionObstacle extends Obstacle {
    public RegionObstacle(World world, float x, float y, float width,
            float height) {
        BodyDef bd = new BodyDef();
        FixtureDef fd = new FixtureDef();
        PolygonShape ps = new PolygonShape();
        ps.setAsBox(width / (2 * GameWrapper.PHYSICS_RATIO), height
                / (2 * GameWrapper.PHYSICS_RATIO));
        fd.isSensor = true;
        bd.position.set(x / GameWrapper.PHYSICS_RATIO, y
                / GameWrapper.PHYSICS_RATIO);
        mBody = world.createBody(bd);
        mBody.createFixture(fd);
    }
}
