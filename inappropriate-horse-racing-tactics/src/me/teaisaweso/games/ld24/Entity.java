package me.teaisaweso.games.ld24;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Entity {
    
    protected Body mBody;
    protected float mWidth = 0, mHeight = 0;

    public Sprite getCurrentSprite() {
        return null;
    }

    public Vector2 getPosition() {
        Vector2 physicsPosition = mBody.getWorldCenter();
        physicsPosition.mul(GameWrapper.PHYSICS_RATIO);
        return physicsPosition;
    }

    public void update() {
        
    }

    public void draw(SpriteBatch sb) {
        Sprite image = this.getCurrentSprite();
        image.setPosition(this.getPosition().x-mWidth/2, this.getPosition().y-mHeight/2);
        
        image.draw(sb);
    }
}
