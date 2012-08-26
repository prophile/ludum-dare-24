package me.teaisaweso.games.ld24;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;

public class Constants {
    public static final Constants sInstance = new Constants();
    
    public HashMap<String, Float> mConstants = new HashMap<String, Float>();
    
    public Constants() {
        String s = Gdx.files.internal("constants.csv").readString();
        String[] lines = s.split("\n");
        for (String line : lines) {
            String key  = line.split(",")[0];
            float value = Float.parseFloat(line.split(",")[1]);
            mConstants.put(key, value);
        }
        
    }
}
