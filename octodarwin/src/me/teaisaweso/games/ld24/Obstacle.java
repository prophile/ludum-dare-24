package me.teaisaweso.games.ld24;

public abstract class Obstacle extends Entity {
    static int kObstacles = 85;
    public Obstacle() {
        super();
    }

    public abstract void hit();

    public abstract StatusModifier freshStatusModifier();

    @Override
    public int drawOrder() {
        if (kObstacles == 18) {
            kObstacles = 85;
        }
        return kObstacles--;
        
    }
    
    
}
