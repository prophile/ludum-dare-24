package me.teaisaweso.games.ld24;

import com.badlogic.gdx.physics.box2d.Body;

public class SoupObstacle extends PhysicalObstacle {

    enum EvolutionStage {
        NORMAL, TENTACLES;
    }
    
    private EvolutionStage mStage;
    
    public SoupObstacle(Body b) {
        super(b);
    }
    
    @Override
    public void collide(Entity e) {
        // TODO Auto-generated method stub

    }

    @Override
    public StatusModifier freshStatusModifier() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void hit() {
        // TODO Auto-generated method stub

    }

}
