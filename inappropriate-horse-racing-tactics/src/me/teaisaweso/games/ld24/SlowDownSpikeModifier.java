package me.teaisaweso.games.ld24;

public class SlowDownSpikeModifier extends StatusModifier {

    private static final int TOTAL_TICKS = Constants
            .getInt("spikeSlowdownDuration");

    private int mTicksRemaining = TOTAL_TICKS;

    @Override
    public float adjustSpeed(float speed) {
        return speed * Constants.getFloat("spikeSlowdownAmount");
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
