package me.teaisaweso.games.ld24;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Player extends Entity {
    private final List<StatusModifier> mStatusModifiers = new ArrayList<StatusModifier>();
    private Sprite mSprite;
    
    public Player(Sprite sprite, World world) {
        mSprite = sprite;
        BodyDef bd = new BodyDef();
        FixtureDef fd = new FixtureDef();
        PolygonShape ps = new PolygonShape();
        ps.setAsBox(1, 1);
        fd.density = 100;
        fd.shape = ps;
        bd.fixedRotation = true;
        bd.type = BodyType.DynamicBody;
        bd.fixedRotation = true;
        bd.position.set(0,3);
        Body b = world.createBody(bd);
        b.createFixture(fd);
        mBody = b;
    }
    
    
    @Override
    public Sprite getCurrentSprite() {
        Sprite currentSprite = mSprite;
        
        for (StatusModifier modifier : mStatusModifiers) {
            currentSprite = modifier.getCurrentSprite(currentSprite);
        }
        
        return currentSprite;
    }

    @Override
    public void update() {
        mBody.applyLinearImpulse(new Vector2(30,0), mBody.getPosition());
        super.update();
    }

}
