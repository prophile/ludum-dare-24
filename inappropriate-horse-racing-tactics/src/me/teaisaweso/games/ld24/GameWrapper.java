package me.teaisaweso.games.ld24;

import java.util.HashSet;
import java.util.Random;

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
    public static final Random sRng = new Random();

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
    private TreeStumpObstacle mSingleTreeStumpObstacle;
    private SlowDownRegion mSlowDownRegion;

    private RockObstacle mSingleRockObstacle;

    private boolean mIsOnFloor;

    private Sprite mGameOverSprite;

    private SlowDownObstacle mSingleSlowDownObstacle;

    private Body mBullet = null;

    private final HashSet<Body> mRemoveBodies = new HashSet<Body>();

    private int mBulletTicks;

    private static Vector2 mCameraOrigin = new Vector2(0, 0);

    public static boolean sIsGameOver;

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

        createPhysicsSimulation();

        mBatch = new SpriteBatch();

        createCrosshair();

        createPlayer();
        createDarwin();

        addFloor();
        createObstacles();

        mDebugger = new Box2DDebugRenderer(true, true, true, true);
    }

    private void createPhysicsSimulation() {
        mWorld = new World(new Vector2(0, -100), true);
        mWorld.setContactListener(new WorldContactListener(this));
    }

    private void createCrosshair() {
        Texture crosshair = new Texture(
                Gdx.files.internal("assets/crosshair.png"));
        mCrosshair = new Sprite(crosshair, 10, 10);
    }

    private void createCamera() {
        setCameraOrigin(new Vector2(0, 0));
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
        createSlowDownRegion();
        createSlowDownObstacle();
        createTreeStumpObstacle();
        mSingleRockObstacle = new RockObstacle(new Vector2(2000, 50), mWorld);
    }

    private void createSlowDownRegion() {
        mSlowDownRegion = new SlowDownRegion(mWorld, 300000000, 0, 100, 20000);
    }

    private void createSlowDownObstacle() {
        mSingleSlowDownObstacle = new SlowDownObstacle(
                createSlowDownObstaclePhysicsBody());
    }

    private Body createSlowDownObstaclePhysicsBody() {
        BodyDef bd = new BodyDef();
        bd.position.set(1000 / 16, 400 / 16);
        bd.type = BodyType.DynamicBody;
        FixtureDef fd = new FixtureDef();
        CircleShape cs = new CircleShape();
        cs.setRadius(1);
        fd.shape = cs;
        fd.isSensor = false;
        fd.density = 1;
        Body body = mWorld.createBody(bd);
        body.createFixture(fd);
        return body;
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
        if (!sIsGameOver) {
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
            sIsGameOver = false;
            create();
        } else if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            System.exit(0);
        }
    }

    private void renderGameWorld() {
        update();

        clearScreen();

        mBatch.setProjectionMatrix(mCamera.combined);
        mBatch.setTransformMatrix(new Matrix4().translate(-getCameraOrigin().x,
                -getCameraOrigin().y, 0));

        mBackgroundManager.drawSky();
        mBatch.begin();
        mBackgroundManager.draw(mBatch);
        mPlayer.draw(mBatch);
        mEnemy.draw(mBatch);
        mSingleRockObstacle.draw(mBatch);
        mSingleTreeStumpObstacle.draw(mBatch);
        drawCrosshair(mBatch);
        mBatch.end();
        Matrix4 m = new Matrix4(mCamera.combined);
        m.translate(-getCameraOrigin().x, -getCameraOrigin().y, 0);
        m.scale(PHYSICS_RATIO, PHYSICS_RATIO, 1);
        mDebugger.render(mWorld, m);
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
    }

    void handleCollision(Fixture a, Fixture b, Contact c) {
        if (a.getBody() == mBullet && b.getBody() != mPlayer.mBody) {
            mRemoveBodies.add(mBullet);
            if (b.getBody() == mSingleSlowDownObstacle.mBody) {
                mSingleSlowDownObstacle.hit();
            }

            mBullet = null;
        }

        if (a.getBody() == mPlayer.mBody) {
            if (b.getBody() == mFloor
                    || b.getBody() == mSingleTreeStumpObstacle.mBody
                    && mPlayer.getPosition().y > mSingleTreeStumpObstacle
                            .getPosition().y + 122) {
                mIsOnFloor = true;
            }

            if (b.getBody() == mSlowDownRegion.mBody) {
                mSlowDownRegion.enterRegion(mPlayer);
            }

            if (b.getBody() == mEnemy.mBody) {
                mEnemy.catchPlayer();
            }

            if (mSingleSlowDownObstacle != null
                    && b.getBody() == mSingleSlowDownObstacle.mBody) {
                mSingleSlowDownObstacle.collide(mPlayer);
                c.setEnabled(false);
            }

            if (mSingleRockObstacle != null
                    && b.getBody() == mSingleRockObstacle.mBody) {
                mSingleRockObstacle.collide(mPlayer);
                c.setEnabled(false);
            }

        }

        if (a.getBody() == mEnemy.mBody) {
            if (mSingleSlowDownObstacle != null
                    && b.getBody() == mSingleSlowDownObstacle.mBody) {
                mSingleSlowDownObstacle.collide(mEnemy);
                mRemoveBodies.add(mSingleSlowDownObstacle.mBody);
            }

            if (mSingleTreeStumpObstacle != null
                    && b.getBody() == mSingleTreeStumpObstacle.mBody) {
                c.setEnabled(false);
            }

            if (mSingleRockObstacle != null
                    && b.getBody() == mSingleRockObstacle.mBody) {
                c.setEnabled(false);
            }
        }
    }

    private void update() {
        mIsOnFloor = false;
        simulatePhysicsStep();
        updateEntities();
        mBackgroundManager.update(getCameraOrigin().x);

        // for (Contact c : mWorld.getContactList()) {
        // handleCollision(c.getFixtureA(), c.getFixtureB(), c);
        // handleCollision(c.getFixtureB(), c.getFixtureA(), c);
        // }

        removeCondemnedBodies();

        if (shouldJump()) {
            System.out.println("jumping");
            mPlayer.jump();
        }

        if (!mIsOnFloor) {
            updatePlayerForAirControl();
        }

    }

    private void updatePlayerForAirControl() {
        mPlayer.mBody.setLinearVelocity(
                mPlayer.mBody.getLinearVelocity().x * 0.997f,
                mPlayer.mBody.getLinearVelocity().y);
    }

    private void simulatePhysicsStep() {
        mWorld.step((float) (1.0 / 60.0), 3, 3);
    }

    private void updateEntities() {
        updateBullet();

        mPlayer.update();
        mEnemy.update();
        updateObstacles();
    }

    private void updateObstacles() {
        mSingleTreeStumpObstacle.update();
        mSingleRockObstacle.update();
        if (treeStumpObstacleHasLeftScreen()) {
            respawnTreeStumpObstacle();
        }

        if (mSingleRockObstacle.mDead
                || mSingleRockObstacle.getPosition().x - getCameraOrigin().x < -600) {
            mSingleRockObstacle.mBody.setActive(false);
            mWorld.destroyBody(mSingleRockObstacle.mBody);
            mSingleRockObstacle = new RockObstacle(new Vector2(
                    getCameraOrigin().x + 1600, 50), mWorld);
        }
    }

    private void respawnTreeStumpObstacle() {
        removeTreeStumpObstacle();
        createTreeStumpObstacle();
    }

    private void createTreeStumpObstacle() {
        mSingleTreeStumpObstacle = new TreeStumpObstacle(new Vector2(
                getCameraOrigin().x + 800 + sRng.nextFloat() * 100, 50), mWorld);
    }

    private void removeTreeStumpObstacle() {
        mSingleTreeStumpObstacle.mBody.setActive(false);
        mWorld.destroyBody(mSingleTreeStumpObstacle.mBody);
        mSingleTreeStumpObstacle = null;
    }

    private boolean treeStumpObstacleHasLeftScreen() {
        return mSingleTreeStumpObstacle.getPosition().x - getCameraOrigin().x < -600;
    }

    private void updateBullet() {
        mBulletTicks += 1;
        removeBulletIfExpired();
    }

    private void removeBulletIfExpired() {
        if (bulletHasExpired()) {
            removeBullet();
        }
    }

    private void removeBullet() {
        mRemoveBodies.add(mBullet);
        mBullet = null;
    }

    private boolean bulletHasExpired() {
        return mBulletTicks > 100 && mBullet != null;
    }

    private boolean shouldJump() {
        boolean shouldJump = Gdx.input.isKeyPressed(Input.Keys.SPACE)
                && mIsOnFloor;
        return shouldJump;
    }

    private void removeCondemnedBodies() {
        for (Body b : mRemoveBodies) {
            b.setTransform(new Vector2(-9000, -9000), 0);
        }
    }

    public void drawCrosshair(SpriteBatch sb) {
        Vector2 mouse = getMouseLocation();

        Sprite playerSprite = mPlayer.getCurrentSprite();

        Vector2 crosshairPosition = computeCrosshairPosition(mouse,
                playerSprite);

        mCrosshair
                .setPosition(crosshairPosition.x - 5, crosshairPosition.y - 5);

        if (Gdx.input.isButtonPressed(Buttons.LEFT) && mBullet == null) {
            System.out.println("touch");
            mBulletTicks = 0;
            BodyDef bd = new BodyDef();
            bd.type = BodyType.KinematicBody;
            float px = playerSprite.getX() + playerSprite.getWidth() / 2;
            float py = playerSprite.getY() + playerSprite.getHeight() / 2;
            crosshairPosition.sub(new Vector2(px, py));
            crosshairPosition.nor();
            crosshairPosition.mul(PHYSICS_RATIO * 3);

            bd.linearVelocity.set(crosshairPosition);
            bd.position.set((playerSprite.getX() + playerSprite.getWidth() / 2)
                    / PHYSICS_RATIO,
                    (playerSprite.getY() + playerSprite.getHeight() / 2)
                            / PHYSICS_RATIO);
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

    private Vector2 computeCrosshairPosition(Vector2 mousePosition,
            Sprite playerSprite) {
        // Work out center of where the player is on the display,
        float px = playerSprite.getX() + playerSprite.getWidth() / 2;
        float py = playerSprite.getY() + playerSprite.getHeight() / 2;
        Vector2 pos = new Vector2(px, py);

        // Work out where the put the crosshair,
        pos.sub(getCameraOrigin());
        pos.sub(mousePosition);
        pos.mul(0.7f);
        pos.add(mousePosition);
        pos.add(getCameraOrigin());
        return pos;
    }

    private Vector2 getMouseLocation() {
        // Fetch mouse location
        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());

        // Work around mouse x/y being from top left
        mouse.y = 600 - mouse.y;
        mouse.y -= 300;
        mouse.x -= 400;
        return mouse;
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

    public static Vector2 getCameraOrigin() {
        return mCameraOrigin;
    }

    public static void setCameraOrigin(Vector2 mCameraOrigin) {
        GameWrapper.mCameraOrigin = mCameraOrigin;
    }
}
