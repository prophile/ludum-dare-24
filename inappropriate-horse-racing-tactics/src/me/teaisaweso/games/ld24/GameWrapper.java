package me.teaisaweso.games.ld24;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
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
    private Enemy mEnemy;
    private BackgroundManager mBackgroundManager;
    private Body mFloor;
    private SlowDownRegion mSlowDown;

    private boolean mIsOnFloor;

    public static Vector2 mCameraOrigin = new Vector2(0, 0);

    public static boolean sGameOver;

    public void addFloor() {
        BodyDef bd = new BodyDef();
        bd.type = BodyType.StaticBody;
        FixtureDef fd = new FixtureDef();
        PolygonShape ps = new PolygonShape();
        ps.setAsBox(3000f, 0.5f);
        fd.shape = ps;
        bd.fixedRotation = true;
        bd.position.set(0, 0);
        mFloor = mWorld.createBody(bd);
        mFloor.createFixture(fd);
    }

    @Override
    public void create() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        mBackgroundManager = new BackgroundManager();
        mWorld = new World(new Vector2(0, -30), true);
        mSlowDown = new SlowDownRegion(mWorld, 3000, 0, 100, 20000);

        mCamera = new OrthographicCamera(w, h);

        mBatch = new SpriteBatch();

        mTexture = new Texture(
                Gdx.files.internal("assets/AssetMonkeyDraft.png"));
        mTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        Sprite s = new Sprite(mTexture, 200, 200);
        mPlayer = new Player(s, mWorld);

        this.addFloor();
        Texture t;
        t = new Texture(Gdx.files.internal("assets/DarwinDraft.png"));
        t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        s = new Sprite(t, 200, 400);

        mEnemy = new Enemy(s, mWorld);

        mDebugger = new Box2DDebugRenderer(true, true, true, true);
        mPlayer.addStatusModifier(new CameraAttachedModifier(mPlayer));
        
    }

    @Override
    public void render() {
        update();

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        Matrix4 m = new Matrix4(mCamera.combined);
        m.translate(-mCameraOrigin.x, -mCameraOrigin.y, 0);
        m.scale(PHYSICS_RATIO, PHYSICS_RATIO, 1);
        mDebugger.render(mWorld, m);
        mBatch.setProjectionMatrix(mCamera.combined);
        mBatch.setTransformMatrix(new Matrix4().translate(-mCameraOrigin.x,
                -mCameraOrigin.y, 0));

        mBatch.begin();
        mBackgroundManager.draw(mBatch);
        mPlayer.draw(mBatch);
        mEnemy.draw(mBatch);
        mBatch.end();
        
        if (sGameOver) {
            System.out.println("lol you died!");
            System.exit(1);
        }

    }
    
    private void handleCollision(Fixture a, Fixture b) {
        if (a.getBody() == mPlayer.mBody) {
            if (b.getBody() == mFloor) {
                mIsOnFloor = true;
            }
            
            if (b.getBody() == mSlowDown.mBody) {
                mSlowDown.enterRegion(mPlayer);
            }
            
            if (b.getBody() == mEnemy.mBody) {
                mEnemy.catchPlayer();
            }
        }
    }

    private void update() {
        mIsOnFloor = false;
        mWorld.step((float) (1.0 / 60.0), 3, 3);
        mPlayer.update();
        mEnemy.update();
        mBackgroundManager.update(mCameraOrigin.x);
        
        for (Contact c : mWorld.getContactList()) {
            this.handleCollision(c.getFixtureA(), c.getFixtureB());
            this.handleCollision(c.getFixtureB(), c.getFixtureA());
        }
        
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && mIsOnFloor) {
            System.out.println("jumping");
            mPlayer.jump();
        }
        
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

    @Override
    public void dispose() {
        mBatch.dispose();
    }
}
