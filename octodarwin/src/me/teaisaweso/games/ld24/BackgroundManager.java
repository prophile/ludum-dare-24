package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BackgroundManager {

    private static final TextureFilter BACKGROUND_TEXTURE_FILTER = TextureFilter.Linear;
    private final Sprite[] mBackgroundSprites = new Sprite[6];
    private final static float[] sShifts = {
            Constants.getFloat("backBGLayerScrollSpeed"),
            Constants.getFloat("backBGLayerScrollSpeed"),
            Constants.getFloat("middleBGLayerScrollSpeed"),
            Constants.getFloat("middleBGLayerScrollSpeed"),
            Constants.getFloat("frontBGLayerScrollSpeed"),
            Constants.getFloat("frontBGLayerScrollSpeed") };

    private static Texture sBackLayer, sMidLayer, sFrontLayer;
    private static boolean sTexturesLoaded = false;

    private static float lastCameraPosition = 0.0f;

    private static void loadTextures() {
        // stuff
        sBackLayer = new Texture(Gdx.files.internal("assets/bg-back.png"));
        sMidLayer = new Texture(Gdx.files.internal("assets/bg-mid.png"));
        sFrontLayer = new Texture(Gdx.files.internal("assets/bg-front.png"));
        sBackLayer.setFilter(BACKGROUND_TEXTURE_FILTER,
                BACKGROUND_TEXTURE_FILTER);
        sMidLayer.setFilter(BACKGROUND_TEXTURE_FILTER,
                BACKGROUND_TEXTURE_FILTER);
        sFrontLayer.setFilter(BACKGROUND_TEXTURE_FILTER,
                BACKGROUND_TEXTURE_FILTER);
        sBackLayer.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
    }

    private static void loadTexturesOnDemand() {
        if (!sTexturesLoaded) {
            loadTextures();
            sTexturesLoaded = true;
        }
    }

    public BackgroundManager() {
        loadTexturesOnDemand();
        createBackgroundSprites();
    }

    private void createBackgroundSprites() {
        mBackgroundSprites[0] = new Sprite(sBackLayer, 800, 600);
        mBackgroundSprites[0].setPosition(-400, 0);
        mBackgroundSprites[1] = new Sprite(sBackLayer, 800, 600);
        mBackgroundSprites[1].setPosition(400, 0);
        mBackgroundSprites[2] = new Sprite(sMidLayer, 800, 600);
        mBackgroundSprites[2].setPosition(-400, 0);
        mBackgroundSprites[3] = new Sprite(sMidLayer, 800, 600);
        mBackgroundSprites[3].setPosition(400, 0);
        mBackgroundSprites[4] = new Sprite(sFrontLayer, 800, 600);
        mBackgroundSprites[4].setPosition(-400, 0);
        mBackgroundSprites[5] = new Sprite(sFrontLayer, 800, 600);
        mBackgroundSprites[5].setPosition(400, 0);
    }

    public void update(float cameraPosition) {
        float deltaCameraPosition = cameraPosition - lastCameraPosition;
        lastCameraPosition = cameraPosition;
        float forwardVelocity = deltaCameraPosition / 60.0f;
        for (int i = 0; i < 6; ++i) {
            Sprite backgroundSprite = mBackgroundSprites[i];
            float shiftAmount = sShifts[i] * forwardVelocity * 10.0f;
            backgroundSprite.setPosition(backgroundSprite.getX() + shiftAmount,
                    backgroundSprite.getY());
            if (backgroundSprite.getX() + 800 - cameraPosition < -400) {
                backgroundSprite.setPosition(backgroundSprite.getX() + 800 * 2,
                        0);
            }
        }
    }

    public void draw(SpriteBatch sb) {
        for (Sprite backgroundSprite : mBackgroundSprites) {
            backgroundSprite.draw(sb);
        }
    }
    
    
}