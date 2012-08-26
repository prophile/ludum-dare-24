package me.teaisaweso.games.ld24;

import com.badlogic.gdx.physics.box2d.Body;

public class SlowDownObstacle extends PhysicalObstacle {

    public boolean mEvolved = false;
    
    public SlowDownObstacle(Body b) {
        super(b);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void collide(Entity e) {
        
        
        if (e instanceof Enemy && mEvolved) {
            Enemy en = (Enemy)e;
            en.addStatusModifier(this.freshStatusModifier());
        }
    }

    @Override
    public void hit() {
        System.out.println("evolving");
        mEvolved = true;
    }

    @Override
    public StatusModifier freshStatusModifier() {
        return new SlowDownModifier();
    }

}
