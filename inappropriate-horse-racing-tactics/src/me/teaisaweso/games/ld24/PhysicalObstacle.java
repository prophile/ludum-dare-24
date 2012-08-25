package me.teaisaweso.games.ld24;

import com.badlogic.gdx.physics.box2d.Body;

public abstract class PhysicalObstacle extends Obstacle {
    public PhysicalObstacle(Body b) {
        mBody = b;
    }

    public abstract void collide(Entity e);
}
