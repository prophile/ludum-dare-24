package me.teaisaweso.games.ld24;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class GameWrapper implements ApplicationListener {
    
    
    public static final float PHYSICS_RATIO = 16;
    
	private OrthographicCamera mCamera;
	private SpriteBatch mBatch;
	private Texture mTexture;
	private Player mPlayer;
	private World mWorld;
	private Box2DDebugRenderer mDebugger;
	
	
	public void addFloor() {
	    BodyDef bd = new BodyDef();
	    bd.type = BodyType.StaticBody;
	    FixtureDef fd = new FixtureDef();
	    PolygonShape ps = new PolygonShape();
	    ps.setAsBox(3000f, 0.5f);
	    fd.shape = ps;
	    bd.fixedRotation = true;
	    bd.position.set(0,0);
	    Body body = mWorld.createBody(bd);
	    body.createFixture(fd);
	    
	}
	
	@Override
	public void create() {		
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		mWorld = new World(new Vector2(0,-3), true);
		
		mCamera = new OrthographicCamera(w, h);
	
		mBatch = new SpriteBatch();
		
		mTexture = new Texture(Gdx.files.internal("assets/libgdx.png"));
		mTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		this.addFloor();
		
		Sprite s =	new Sprite(mTexture, 153, 37);
		s.setPosition(0, 0);
		mPlayer = new Player(s, mWorld);
	    mDebugger = new Box2DDebugRenderer( true, true, true, true );

	}

	@Override
	public void dispose() {
		mBatch.dispose();
		mTexture.dispose();
	}

	@Override
	public void render() {
	    
	    mWorld.step((float) (1.0/60.0), 3, 3);
	    
	    System.out.println(mWorld.getBodyCount());
	    mPlayer.update();
	    
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		mDebugger.render(mWorld, mCamera.combined.scale(16, 16, 1));
        mCamera.combined.scale(1.0f/16,1.0f/16,1);
		mBatch.setProjectionMatrix(mCamera.combined);
		float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
		mBatch.setTransformMatrix(new Matrix4().translate(-w/2, -h/2, 0));
		mBatch.begin();
		mPlayer.draw(mBatch);
		mBatch.end();
		
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
