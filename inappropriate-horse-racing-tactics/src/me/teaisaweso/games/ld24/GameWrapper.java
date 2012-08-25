package me.teaisaweso.games.ld24;

import java.util.HashSet;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
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
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class GameWrapper implements ApplicationListener {

    public static final float PHYSICS_RATIO = 16;

    private OrthographicCamera mCamera;
    private SpriteBatch mBatch;
    private SpriteBatch mGameOverBatch;
    private Texture mTexture;
    private Sprite mCrosshair;
    private Player mPlayer;
    private World mWorld;
    private Box2DDebugRenderer mDebugger;
    private Enemy mEnemy;
    private BackgroundManager mBackgroundManager;
    private Body mFloor;
    private SlowDownRegion mSlowDown;

    private boolean mIsOnFloor;

    private Sprite mGameOverSprite;

    private SlowDownObstacle mSdO;

    private Body mBullet = null;

    private HashSet<Body> mRemoveBodies = new HashSet<Body>();

    private int mBulletTicks;

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
        loadGameOverAssets();
        createCamera();
        mBullet = null;
        mBackgroundManager = new BackgroundManager();
        mWorld = new World(new Vector2(0, -30), true);

        mBatch = new SpriteBatch();

        createCrosshair();

        createPlayer();
        createDarwin();

        addFloor();
        createObstacles();

        mDebugger = new Box2DDebugRenderer(true, true, true, true);
    }

    private void createCrosshair() {
        Texture crosshair = new Texture(
                Gdx.files.internal("assets/crosshair.png"));
        mCrosshair = new Sprite(crosshair, 10, 10);
    }

    private void createCamera() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        mCamera = new OrthographicCamera(w, h);
    }

    private void createDarwin() {
        Texture t;
        Sprite s;
        t = new Texture(Gdx.files.internal("assets/DarwinDraft.png"));
        t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        s = new Sprite(t, 200, 400);

        mEnemy = new Enemy(s, mWorld);
    }

    private void createPlayer() {
        mTexture = new Texture(
                Gdx.files.internal("assets/AssetMonkeyDraft.png"));
        mTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        Sprite s = new Sprite(mTexture, 200, 200);
        mPlayer = new Player(s, mWorld);
        mPlayer.addStatusModifier(new CameraAttachedModifier(mPlayer));
    }

    private void createObstacles() {
        mSlowDown = new SlowDownRegion(mWorld, 3000, 0, 100, 20000);
        BodyDef bd = new BodyDef();
        bd.position.set(1000 / 16, 400 / 16);
        bd.type = BodyType.KinematicBody;
        FixtureDef fd = new FixtureDef();
        CircleShape cs = new CircleShape();
        cs.setRadius(1);
        fd.shape = cs;
        fd.isSensor = true;
        fd.density = 1;
        Body b = mWorld.createBody(bd);
        b.createFixture(fd);
        mSdO = new SlowDownObstacle(b);
    }

    private void loadGameOverAssets() {
        Texture t;
        t = new Texture(Gdx.files.internal("assets/gameoverscreen.png"));
        t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        mGameOverSprite = new Sprite(t, 800, 600);
        mGameOverBatch = new SpriteBatch();
    }

    @Override
    public void render() {
        if (!sGameOver) {
            renderGameWorld();
        } else {
            renderGameOverScreen();
        }

    }

    private void renderGameOverScreen() {
        mGameOverBatch.begin();
        mGameOverSprite.draw(mGameOverBatch);
        mGameOverBatch.end();
        if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            sGameOver = false;
            create();
        } else if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            System.exit(0);
        }
    }

    private void renderGameWorld() {
        update();

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        mBatch.setProjectionMatrix(mCamera.combined);
        mBatch.setTransformMatrix(new Matrix4().translate(-mCameraOrigin.x,
                -mCameraOrigin.y, 0));

        mBackgroundManager.drawSky();
        mBatch.begin();
        mBackgroundManager.draw(mBatch);
        mPlayer.draw(mBatch);
        mEnemy.draw(mBatch);
        drawCrosshair(mBatch);
        mBatch.end();
        Matrix4 m = new Matrix4(mCamera.combined);
        m.translate(-mCameraOrigin.x, -mCameraOrigin.y, 0);
        m.scale(PHYSICS_RATIO, PHYSICS_RATIO, 1);
        mDebugger.render(mWorld, m);
    }

    private void handleCollision(Fixture a, Fixture b) {
        if (a.getBody() == mBullet && b.getBody() != mPlayer.mBody) {
            mRemoveBodies.add(mBullet);
            if (b.getBody() == mSdO.mBody) {
                mSdO.hit();
            }

            mBullet = null;
        }

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

            if (mSdO != null && b.getBody() == mSdO.mBody) {
                mSdO.collide(mPlayer);
            }
        }
    }

    private void update() {
        mIsOnFloor = false;
        mWorld.step((float) (1.0 / 60.0), 3, 3);
        mBulletTicks += 1;
        if (mSdO != null && mSdO.mEvolved) {
            mSdO.mBody.setActive(false);
            mWorld.destroyBody(mSdO.mBody);
            mSdO = null;
        }
        if (mBulletTicks > 100 && mBullet != null) {
            mRemoveBodies.add(mBullet);
            mBullet = null;
        }

        mPlayer.update();
        mEnemy.update();
        mBackgroundManager.update(mCameraOrigin.x);

        for (Contact c : mWorld.getContactList()) {
            handleCollision(c.getFixtureA(), c.getFixtureB());
            handleCollision(c.getFixtureB(), c.getFixtureA());
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && mIsOnFloor) {
            System.out.println("jumping");
            mPlayer.jump();
        }

    }

    public void drawCrosshair(SpriteBatch sb) {
        // Fetch mouse location
        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());

        // Work out center of where the player is on the display,
        Sprite s = mPlayer.getCurrentSprite();
        float px = s.getX() + s.getWidth() / 2;
        float py = s.getY() + s.getHeight() / 2;
        Vector2 pos = new Vector2(px, py);

        // Work around mouse x/y being from top left
        mouse.y = 600 - mouse.y;
        mouse.y -= 300;
        mouse.x -= 400;

        // Work out where the put the crosshair,
        pos.sub(mCameraOrigin);
        pos.sub(mouse);
        pos.mul(0.7f);
        pos.add(mouse);
        pos.add(mCameraOrigin);

        mCrosshair.setPosition(pos.x - 5, pos.y - 5);

        if (Gdx.input.isButtonPressed(Buttons.LEFT) && mBullet == null) {
            System.out.println("touch");
            mBulletTicks = 0;
            BodyDef bd = new BodyDef();
            bd.type = BodyType.KinematicBody;
            px = s.getX() + (s.getWidth() / 2);
            py = s.getY() + (s.getHeight() / 2);
            pos.sub(new Vector2(px, py));
            pos.nor();
            pos.mul(PHYSICS_RATIO * 3);

            bd.linearVelocity.set(pos);
            bd.position.set((s.getX() + s.getWidth() / 2) / PHYSICS_RATIO,
                    (s.getY() + s.getHeight() / 2) / PHYSICS_RATIO);
            FixtureDef fd = new FixtureDef();
            CircleShape cs = new CircleShape();
            cs.setRadius(2);
            fd.shape = cs;
            fd.isSensor = true;
            mBullet = mWorld.createBody(bd);
            mBullet.createFixture(fd);
        }

        mCrosshair.draw(sb);
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
