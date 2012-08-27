package me.teaisaweso.games.ld24;

public abstract class Obstacle extends Entity {
    static int kObstacles = 85;
    private int mOrder;

    public Obstacle() {
        super();
        if (kObstacles == 18) {
            kObstacles = 85;
        }
        mOrder = kObstacles--;
    }

    public abstract void hit();

    public abstract StatusModifier freshStatusModifier();

    @Override
    public int drawOrder() {
        return mOrder;
    }
    
    
}
