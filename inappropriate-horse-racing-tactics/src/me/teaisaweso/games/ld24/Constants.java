package me.teaisaweso.games.ld24;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;

public class Constants {
    private static final Constants sInstance = new Constants();

    private final HashMap<String, Float> mConstants = new HashMap<String, Float>();

    private Constants() {
        String s = Gdx.files.internal("constants.csv").readString();
        String[] lines = s.split("\n");
        for (String line : lines) {
            String[] components = line.split(",");
            String key = components[0];
            float value = Float.parseFloat(line.split(",")[1]);
            mConstants.put(key, value);
        }
    }

    public static float getFloat(String key) {
        return sInstance.mConstants.get(key);
    }

    public static int getInt(String key) {
        return (int) getFloat(key);
    }

    public static boolean getBoolean(String key) {
        return getFloat(key) > 0.5f;
    }
}
