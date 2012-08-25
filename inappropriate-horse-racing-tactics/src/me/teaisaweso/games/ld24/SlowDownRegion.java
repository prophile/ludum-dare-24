package me.teaisaweso.games.ld24;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.physics.box2d.World;

public class SlowDownRegion extends RegionObstacle {

    private Set<Entity> mEntities = new HashSet<Entity>();
    
    public SlowDownRegion(World world, float x, float y, float width,
            float height) {
        super(world, x, y, width, height);
    }

    @Override
    public void enterRegion(Entity e) {
        
        if (e instanceof Player && !mEntities.contains(e)) {
            Player p = (Player)e;
            p.addStatusModifier(this.freshStatusModifier());
            System.out.println("enter region");
        }
        mEntities.add(e);
            
    }

    @Override
    public void leaveRegion(Entity e) {
        
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
