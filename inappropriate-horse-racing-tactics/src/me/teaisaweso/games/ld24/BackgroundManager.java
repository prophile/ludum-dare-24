package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class BackgroundManager {

    private final Sprite mBackgroundSprite1;
    private final Sprite mBackgroundSprite2;
    private final ShapeRenderer mSkyGradientRenderer;

    public BackgroundManager() {
        Texture t = new Texture(
                Gdx.files.internal("assets/background-transparent.png"));
        t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        mBackgroundSprite1 = new Sprite(t, 800, 600);
        mBackgroundSprite1.setPosition(-400, 0);
        mBackgroundSprite2 = new Sprite(t, 800, 600);
        mBackgroundSprite2.setPosition(400, 0);
        mSkyGradientRenderer = new ShapeRenderer();
    }

    public void update(float cameraPosition) {
        if (mBackgroundSprite1.getX() + 800 - cameraPosition < -400) {
            mBackgroundSprite1.setPosition(mBackgroundSprite1.getX() + 800 * 2,
                    0);
        }

        if (mBackgroundSprite2.getX() + 800 - cameraPosition < -400) {
            mBackgroundSprite2.setPosition(mBackgroundSprite2.getX() + 800 * 2,
                    0);
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
        mBackgroundSprite1.draw(sb);
        mBackgroundSprite2.draw(sb);
    }
}
