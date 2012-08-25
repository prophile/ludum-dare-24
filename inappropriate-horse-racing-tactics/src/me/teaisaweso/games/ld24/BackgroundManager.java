package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BackgroundManager {
    
    private Sprite mBackgroundSprite1;
    private Sprite mBackgroundSprite2;
    
    public BackgroundManager() {
        Texture t =  new Texture(Gdx.files.internal("assets/background.png"));
        t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        mBackgroundSprite1 = new Sprite(t, 800, 600);
        mBackgroundSprite1.setPosition(-400, 0);
        mBackgroundSprite2 = new Sprite(t, 800, 600);
        mBackgroundSprite2.setPosition(400, 0);
    }
    
    
    public void update(float cameraPosition) {
       
       if (mBackgroundSprite1.getX()+800-cameraPosition < -400) {
           mBackgroundSprite1.setPosition(mBackgroundSprite1.getX()+800*2, 0);
       }
       
       if (mBackgroundSprite2.getX()+800-cameraPosition < -400) {
           mBackgroundSprite2.setPosition(mBackgroundSprite2.getX()+800*2, 0);
       }
    }
    
    public void draw(SpriteBatch sb) {
        mBackgroundSprite1.draw(sb);
        mBackgroundSprite2.draw(sb);
    }
}
