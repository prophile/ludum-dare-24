package me.teaisaweso.games.ld24;

import com.badlogic.gdx.physics.box2d.Body;

public class SlowDownObstacle extends PhysicalObstacle {

    public boolean mIsEvolved = false;

    public SlowDownObstacle(Body b) {
        super(b);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void collide(Entity e) {
        if (e instanceof Enemy && mIsEvolved) {
            Enemy enemy = (Enemy) e;
            enemy.addStatusModifier(freshStatusModifier());
        }
    }

    @Override
    public void hit() {
        System.out.println("evolving");
        mIsEvolved = true;
    }

    @Override
    public StatusModifier freshStatusModifier() {
        return new SlowDownModifier();
    }

}
