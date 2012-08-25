package me.teaisaweso.games.ld24;

import com.badlogic.gdx.physics.box2d.Body;

public class SlowDownObstacle extends PhysicalObstacle {

    public SlowDownObstacle(Body b) {
        super(b);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void collide(Entity e) {
        if (e instanceof Player) {
            Player p = (Player)e;
            p.addStatusModifier(this.freshStatusModifier());
        }
    }

    @Override
    public void hit() {
        // TODO Auto-generated method stub

    }

    @Override
    public StatusModifier freshStatusModifier() {
        return new SlowDownModifier();
    }

}
