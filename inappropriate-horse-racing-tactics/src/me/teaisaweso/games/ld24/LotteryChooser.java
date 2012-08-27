package me.teaisaweso.games.ld24;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LotteryChooser<T> {
    private class Pair {
        public final float mBoundary;
        public final T mValue;

        public Pair(float boundary, T value) {
            mBoundary = boundary;
            mValue = value;
        }
    }

    private final List<Pair> mEntries = new ArrayList<Pair>();
    private float mUpperBound = 0.0f;
    private final Random mRNG;

    public LotteryChooser(Random rng) {
        mRNG = rng;
    }

    public void addEntry(T entry, float weight) {
        float boundary = mUpperBound += weight;
        mEntries.add(new Pair(boundary, entry));
    }

    public T pick() {
        float point = mRNG.nextFloat() * mUpperBound;
        for (Pair p : mEntries) {
            if (point < p.mBoundary) {
                return p.mValue;
            }
        }
        assert false : "floating point no longer works";
        return null;
    }
}
