package me.teaisaweso.games.ld24;

import com.badlogic.gdx.physics.box2d.Contact;

public abstract class PhysicalObstacle extends Obstacle {
    public PhysicalObstacle() {
    }

    public abstract void collide(Entity e, Contact c);

    protected boolean shouldCull() {
        return getPosition().x < GameWrapper.instance.getCameraOrigin().x - 600;
    }
}
