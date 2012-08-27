package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BackgroundManager {

    private static final TextureFilter BACKGROUND_TEXTURE_FILTER = TextureFilter.Linear;
    private final Sprite[] mBackgroundSprites = new Sprite[8];
    private final static float[] sShifts = {
            0.25f,
            0.25f,
            Constants.getFloat("backBGLayerScrollSpeed"),
            Constants.getFloat("backBGLayerScrollSpeed"),
            Constants.getFloat("middleBGLayerScrollSpeed"),
            Constants.getFloat("middleBGLayerScrollSpeed"),
            Constants.getFloat("frontBGLayerScrollSpeed"),
            Constants.getFloat("frontBGLayerScrollSpeed") };

    private static Texture sBackLayer, sMidLayer, sFrontLayer;
    private static boolean sTexturesLoaded = false;

    private static float lastCameraPosition = 0.0f;
    private static Texture sHedgeLayer;

    private static void loadTextures() {
        // stuff
        sHedgeLayer = new Texture(Gdx.files.internal("assets/Asset_background_hedge_FINAL.png"));
        sBackLayer = new Texture(Gdx.files.internal("assets/bg-back.png"));
        sMidLayer = new Texture(Gdx.files.internal("assets/bg-mid.png"));
        sFrontLayer = new Texture(Gdx.files.internal("assets/bg-front.png"));
        sHedgeLayer.setFilter(BACKGROUND_TEXTURE_FILTER, BACKGROUND_TEXTURE_FILTER);
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
        mBackgroundSprites[2] = new Sprite(sHedgeLayer, 800, 600);
        mBackgroundSprites[2].setPosition(-400, 0);
        mBackgroundSprites[3] = new Sprite(sHedgeLayer, 800, 600);
        mBackgroundSprites[3].setPosition(400, 0);
        mBackgroundSprites[4] = new Sprite(sMidLayer, 800, 600);
        mBackgroundSprites[4].setPosition(-400, 0);
        mBackgroundSprites[5] = new Sprite(sMidLayer, 800, 600);
        mBackgroundSprites[5].setPosition(400, 0);
        mBackgroundSprites[6] = new Sprite(sFrontLayer, 800, 600);
        mBackgroundSprites[6].setPosition(-400, 0);
        mBackgroundSprites[7] = new Sprite(sFrontLayer, 800, 600);
        mBackgroundSprites[7].setPosition(400, 0);
    }

    public void update(float cameraPosition) {
        float deltaCameraPosition = cameraPosition - lastCameraPosition;
        lastCameraPosition = cameraPosition;
        float forwardVelocity = deltaCameraPosition / 60.0f;
        for (int i = 7; i > -1; --i) {
            Sprite backgroundSprite = mBackgroundSprites[i];
            float shiftAmount = sShifts[i] * forwardVelocity * 10.0f;
            backgroundSprite.setPosition(backgroundSprite.getX() + shiftAmount,
                    backgroundSprite.getY());
            if (backgroundSprite.getX() + 800 - cameraPosition < -400) {
                backgroundSprite.setPosition(backgroundSprite.getX() + 800 * 2,
                        backgroundSprite.getY());
            }
        }
    }

    public void draw(SpriteBatch sb) {
        for (int i = 0; i < 2; i++) {
            Sprite s = mBackgroundSprites[i];
            s.draw(sb);
        }
        for (Sprite backgroundSprite : mBackgroundSprites) {
            backgroundSprite.draw(sb);
        }
    }
    
    
}
