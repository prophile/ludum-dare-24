package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class BackgroundManager {

    private static final TextureFilter BACKGROUND_TEXTURE_FILTER = TextureFilter.Linear;
    private final Sprite[] mBackgroundSprites = new Sprite[2];
    private final ShapeRenderer mSkyGradientRenderer;

    public BackgroundManager() {
        createBackgroundSprites();
        mSkyGradientRenderer = new ShapeRenderer();
    }

    private void createBackgroundSprites() {
        Texture background = loadBackgroundTexture();
        mBackgroundSprites[0] = new Sprite(background, 800, 600);
        mBackgroundSprites[0].setPosition(-400, 0);
        mBackgroundSprites[1] = new Sprite(background, 800, 600);
        mBackgroundSprites[1].setPosition(400, 0);
    }

    private Texture loadBackgroundTexture() {
        Texture background = new Texture(
                Gdx.files.internal("assets/background-transparent.png"));
        background.setFilter(BACKGROUND_TEXTURE_FILTER,
                BACKGROUND_TEXTURE_FILTER);
        return background;
    }

    public void update(float cameraPosition) {
        for (Sprite backgroundSprite : mBackgroundSprites) {
            if (backgroundSprite.getX() + 800 - cameraPosition < -400) {
                backgroundSprite.setPosition(backgroundSprite.getX() + 800 * 2,
                        0);
            }
        }
    }

    public void drawSky() {
        mSkyGradientRenderer.begin(ShapeRenderer.ShapeType.FilledRectangle);
        Color skyTop = new Color(0.4f, 0.8f, 0.9f, 1.0f);
        Color skyBottom = new Color(skyTop.r * 0.8f, skyTop.g * 0.8f,
                skyTop.b * 0.8f, 1.0f);
        mSkyGradientRenderer.filledRect(0, 0, 800, 600, skyBottom, skyBottom,
                skyTop, skyTop);
        mSkyGradientRenderer.end();
    }

    public void draw(SpriteBatch sb) {
        for (Sprite backgroundSprite : mBackgroundSprites) {
            backgroundSprite.draw(sb);
        }
    }
}
