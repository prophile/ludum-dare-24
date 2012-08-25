package me.teaisaweso.games.ld24;

public class SlowDownModifier extends StatusModifier {

    private static final int TOTAL_TICKS = 120;
    
    private int mTicksRemaining = TOTAL_TICKS;

    @Override
    public float adjustSpeed(float speed) {
        return 1f;
    }
    
    @Override
    public float adjustAccel(float currentAccel) {
        return 300;
    }



    @Override
    public void update() {
        mTicksRemaining--;
    }

    @Override
    public boolean hasEnded() {
        return mTicksRemaining < 0;
    }
    
    
    
}
