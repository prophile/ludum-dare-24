package me.teaisaweso.games.ld24;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
    public static GameWrapper instance = null;

    public static final float PHYSICS_RATIO = 16;
    private BackgroundManager mBackgroundManager;

    private static Random sRng = new Random();

    private SpriteBatch mBatch;

    private OrthographicCamera mCamera;

    private Vector2 mCameraOrigin = new Vector2(0, 0);

    private BitmapFont mTextFont;
    private int mScore;
    private ScoreDownloader mPublicTopScores;

    private Sprite mCrosshair;
    private GunArmEntity mGunArm;

    private Box2DDebugRenderer mDebugger;

    private Enemy mEnemy;
    private Sound mEvolutionShootsound;
    private Body mFloor;
    private SpriteBatch mGameOverBatch;
    private Sprite mGameOverSprite;
    private boolean mIsGameOver;

    private boolean mIsOnFloor;

    private Player mPlayer;

    final Set<Body> mRemoveBodies = new HashSet<Body>();
    final Set<Entity> mEntities = new HashSet<Entity>();

    public final Random mRng = new Random();

    private RockObstacle mSingleRockObstacle;
    private SlowDownObstacle mSingleSlowDownObstacle;
    private TreeStumpObstacle mTreeStumpObstacle1;
    private TreeStumpObstacle mTreeStumpObstacle2;
    private SoupObstacle mSingleSoupObstacle;

    private int mTicks;
    private World mWorld;

    private boolean mSplashScreen = true;

    private Sprite mSplashScreenSprite;
    private ExplosionManager mExplosionManager;

    public ExplosionManager getExplosionManager() {
        return mExplosionManager;
    }

    public void addFloor() {
        BodyDef bd = new BodyDef();
        bd.type = BodyType.StaticBody;
        FixtureDef fd = new FixtureDef();
        PolygonShape ps = new PolygonShape();
        ps.setAsBox(40000000, 0.5f);
        fd.shape = ps;
        bd.fixedRotation = true;
        bd.position.set(0, 0);
        mFloor = mWorld.createBody(bd);
        mFloor.createFixture(fd);
    }

    public void clearGameOver() {
        mIsGameOver = false;
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
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

        pos.mul(0.0f);
        pos.add(mousePosition);
        pos.add(getCameraOrigin());
        mGunArm.passMousePosition(pos);
        return pos;
    }

    @Override
    public void create() {
        assert instance == null || instance == this;
        instance = this;
        mTicks = 0;
        mEntities.clear();

        loadGameOverAssets();

        Texture t = new Texture(Gdx.files.internal("assets/splash.png"));
        Sprite s = new Sprite(t, 800, 600);
        mSplashScreenSprite = s;
        mEvolutionShootsound = Gdx.audio.newSound(Gdx.files
                .internal("assets/EvolutionShoot.wav"));
        createCamera();
        mBackgroundManager = new BackgroundManager();
        createPhysicsSimulation();

        mBatch = new SpriteBatch();

        mTextFont = new BitmapFont();
        mTextFont.getRegion().getTexture()
                .setFilter(TextureFilter.Linear, TextureFilter.Linear);
        mScore = 0;
        // Blank list of top scores, in case intertubes fail.
        mPublicTopScores = new ScoreDownloader();

        mExplosionManager = new ExplosionManager();

        createCrosshair();

        createPlayer();
        createDarwin();

        addFloor();
        createObstacles();
        mGunArm = new GunArmEntity(mPlayer);
        mEntities.add(mGunArm);
        mDebugger = new Box2DDebugRenderer(true, true, true, true);
    }

    private void createCamera() {
        setCameraOrigin(new Vector2(0, 0));
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        mCamera = new OrthographicCamera(w, h);
    }

    private void createCrosshair() {
        Texture crosshair = new Texture(
                Gdx.files.internal("assets/crosshair.png"));
        mCrosshair = new Sprite(crosshair, 35, 35);

    }

    private void createDarwin() {
        mEnemy = new Enemy(mWorld);
        mEntities.add(mEnemy);
    }

    private void createObstacles() {
        createSlowDownObstacle();
        createTreeStumpObstacle();
        mSingleRockObstacle = new RockObstacle(
                Constants.getFloat("rockFirstPosition"), mWorld);
        mEntities.add(mSingleRockObstacle);
        createSoupObstacle();
    }

    private void createPhysicsSimulation() {
        mWorld = new World(new Vector2(0, 0), true);
        mWorld.setContactListener(new WorldContactListener(this));
    }

    private void createPlayer() {
        mPlayer = new Player(mWorld);
        mPlayer.addStatusModifier(new CameraAttachedModifier(mPlayer));
        mEntities.add(mPlayer);
    }

    private void createSlowDownObstacle() {
        mSingleSlowDownObstacle = new SlowDownObstacle(mWorld);
        mEntities.add(mSingleSlowDownObstacle);
    }

    private void createTreeStumpObstacle() {

        mTreeStumpObstacle1 = TreeStumpObstacle.createStumpObstacle(mWorld, 1);
        mEntities.add(mTreeStumpObstacle1);

        if (sRng.nextFloat() < Constants.getFloat("doubleTreeProbability")) {
            mTreeStumpObstacle2 = mTreeStumpObstacle1.createNearbyStump(mWorld);
            mEntities.add(mTreeStumpObstacle2);
        }
    }

    private void createSoupObstacle() {
        mSingleSoupObstacle = new SoupObstacle(getCameraOrigin().x + 1200
                + mRng.nextFloat() * 1200, mWorld);
        mEntities.add(mSingleSoupObstacle);
    }

    @Override
    public void dispose() {
        mBatch.dispose();
    }

    public void drawCrosshair(SpriteBatch sb) {
        Vector2 mouse = getMouseLocation();

        Sprite playerSprite = mPlayer.getCurrentSprite();

        Vector2 crosshairPosition = computeCrosshairPosition(mouse,
                playerSprite);

        mCrosshair.setPosition(crosshairPosition.x - 35 / 2,
                crosshairPosition.y - 35 / 2);

        if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
            System.out.println("touch");
            BodyDef bd = new BodyDef();
            bd.type = BodyType.KinematicBody;
            float px = playerSprite.getX() + playerSprite.getWidth() / 2;
            float py = playerSprite.getY() + playerSprite.getHeight() / 2;
            crosshairPosition.sub(new Vector2(px, py));
            crosshairPosition.nor();
            Vector2 targetVector = new Vector2(crosshairPosition);
            crosshairPosition.mul(PHYSICS_RATIO * 6);

            bd.linearVelocity.set(crosshairPosition.add(
                    mPlayer.mBody.getLinearVelocity().x, 0));
            bd.position
                    .set((playerSprite.getX() + playerSprite.getWidth() / 2 + targetVector.x
                            * Constants.getFloat("bulletFireOffset"))
                            / PHYSICS_RATIO, (playerSprite.getY()
                            + playerSprite.getHeight() / 2 + targetVector.y
                            * Constants.getFloat("bulletFireOffset"))
                            / PHYSICS_RATIO);
            FixtureDef fd = new FixtureDef();
            CircleShape cs = new CircleShape();
            cs.setRadius(2);
            fd.shape = cs;
            fd.isSensor = true;
            BulletEntity b = new BulletEntity(mWorld.createBody(bd));
            mEntities.add(b);

            b.mBody.createFixture(fd);
            mEvolutionShootsound.play();
            mGunArm.fire();
        }

        mCrosshair.draw(sb);
    }

    public Vector2 getCameraOrigin() {
        return mCameraOrigin;
    }

    public Random getRNG() {
        return mRng;
    }

    public Enemy getEnemy() {
        return mEnemy;
    }

    public Player getPlayer() {
        return mPlayer;
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

    void handleCollision(Fixture a, Fixture b, Contact c) {
        Entity collider_a = (Entity) a.getBody().getUserData();
        Entity collider_b = (Entity) b.getBody().getUserData();

        if (collider_a instanceof BulletEntity
                && !(collider_b instanceof Player)) {
            boolean suppressBulletRemoval = false;

            // If off screen, ignore collision.
            Vector2 pos = b.getBody().getPosition();
            pos.mul(PHYSICS_RATIO);
            if (pos.x > mCameraOrigin.x + 420 || pos.y > mCameraOrigin.y + 320) {
                ;
            } else if (collider_b instanceof PhysicalObstacle) {
                ((PhysicalObstacle) collider_b).hit();
            } else if (collider_b instanceof Enemy) {
                ((BulletEntity) collider_a).reflect();
                suppressBulletRemoval = true;
            }

            if (!suppressBulletRemoval) {
                // Register to remove this body
                mRemoveBodies.add(collider_a.mBody);
                mEntities.remove(collider_a);
            }
        }

        if (collider_a instanceof Player) {
            // Enable player jumping if player is on top of an object that
            // he is permitted to jump from.
            if (b.getBody() == mFloor
                    || collider_b instanceof TreeStumpObstacle
                    && mPlayer.getPosition().y > ((TreeStumpObstacle) collider_b)
                            .getWalkHeight()) {
                mIsOnFloor = true;
            }

            if (collider_b instanceof Enemy) {
                ((Enemy) collider_b).catchPlayer();
            }

            if (collider_b instanceof PhysicalObstacle) {
                c.setEnabled(false);
                ((PhysicalObstacle) collider_b).collide(collider_a, c);
            }
        }

        if (a.getBody() == mEnemy.mBody) {
            if (collider_b instanceof PhysicalObstacle) {
                ((PhysicalObstacle) collider_b).collide(collider_a, c);
                c.setEnabled(false);
            }
        }
    }

    public boolean isGameOver() {
        return mIsGameOver;
    }

    public boolean isOnFloor() {
        return mIsOnFloor;
    }

    private void loadGameOverAssets() {
        Texture t;
        t = new Texture(Gdx.files.internal("assets/gameoverscreen.png"));
        t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        mGameOverSprite = new Sprite(t, 800, 600);
        mGameOverBatch = new SpriteBatch();
    }

    @Override
    public void pause() {
    }

    private void removeCondemnedBodies() {
        for (Body b : mRemoveBodies) {
            b.setTransform(new Vector2(-9000, -9000), 0);
        }
    }

    private void removeTreeStumpObstacle() {
        mTreeStumpObstacle1.mBody.setActive(false);
        mWorld.destroyBody(mTreeStumpObstacle1.mBody);
        mEntities.remove(mTreeStumpObstacle1);
        mTreeStumpObstacle1 = null;

        if (mTreeStumpObstacle2 != null) {
            mTreeStumpObstacle2.mBody.setActive(false);
            mWorld.destroyBody(mTreeStumpObstacle2.mBody);
            mEntities.remove(mTreeStumpObstacle2);
            mTreeStumpObstacle2 = null;
        }
    }

    @Override
    public void render() {
        if (mSplashScreen) {
            renderSplashScreen();
        } else if (!isGameOver()) {
            renderGameWorld();
        } else {
            renderGameOverScreen();
        }

    }

    private void renderSplashScreen() {
        mBatch.begin();
        mSplashScreenSprite.draw(mBatch);
        mBatch.end();

        if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            mSplashScreen = false;
        }
    }

    private void renderGameOverScreen() {
        mGameOverBatch.begin();
        mGameOverSprite.draw(mGameOverBatch);

        String text = new String();
        for (ScoreEntry e : mPublicTopScores.mScoreList) {
            text += e.mName + ": " + e.mScore + "\n";
        }

        Color oldColor = mTextFont.getColor();

        mTextFont.setColor(1.0f, 1.0f, 0.0f, 0.5f);
        mTextFont.drawMultiLine(mGameOverBatch, text, 390.0f, 290.0f);
        mTextFont.setColor(oldColor);

        mGameOverBatch.end();

        if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            clearGameOver();
            String username = System.getProperty("user.name");
            try {
                URL u = new URL(
                        "http://immense-savannah-9950.herokuapp.com/score/"
                                + username + "/" + mScore);
                u.getContent();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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

        mBatch.begin();
        mBackgroundManager.draw(mBatch);

        for (Entity e : mEntities) {
            e.draw(mBatch);
        }

        drawCrosshair(mBatch);

        mExplosionManager.draw(mBatch);

        // Reset transform to untransformed, draw distance/score text
        mBatch.setTransformMatrix(new Matrix4().translate(0, 0, 0));
        mScore = (int) getCameraOrigin().x;
        String dist = "Score: " + Integer.toString(mScore);
        mTextFont.draw(mBatch, dist, -390.0f, +290.0f);

        mBatch.end();

        if (Constants.getBoolean("showHitboxes")) {
            Matrix4 m = new Matrix4(mCamera.combined);
            m.translate(-getCameraOrigin().x, -getCameraOrigin().y, 0);
            m.scale(PHYSICS_RATIO, PHYSICS_RATIO, 1);
            mDebugger.render(mWorld, m);
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    private void respawnTreeStumpObstacle() {
        removeTreeStumpObstacle();
        createTreeStumpObstacle();
    }

    @Override
    public void resume() {
    }

    public void setCameraOrigin(Vector2 newCameraOrigin) {
        mCameraOrigin = newCameraOrigin;
    }

    public void setGameOver() {
        mPublicTopScores.downloadScoresAgain(mScore);
        mIsGameOver = true;
    }

    private boolean shouldJump() {
        boolean shouldJump = Gdx.input.isKeyPressed(Input.Keys.SPACE)
                && mIsOnFloor;
        return shouldJump;
    }

    private void simulatePhysicsStep() {
        mWorld.step((float) (1.0 / 60.0), 3, 3);
    }

    private boolean treeStumpObstacleHasLeftScreen() {
        return mTreeStumpObstacle1.getPosition().x - getCameraOrigin().x < -600;
    }

    private void update() {
        mIsOnFloor = false;
        mTicks++;
        simulatePhysicsStep();
        updateEntities();
        mBackgroundManager.update(getCameraOrigin().x);

        removeCondemnedBodies();

        if (shouldJump()) {
            System.out.println("jumping");
            mPlayer.jump();
        }

        if (!mIsOnFloor) {
            updatePlayerForAirControl();
        }

        mExplosionManager.update();

    }

    private void updateEntities() {

        List<Entity> condemned = new ArrayList<Entity>();
        for (Entity e : mEntities) {
            if (e.update()) {
                // Exterminate
                condemned.add(e);
            }
        }

        for (Entity c : condemned) {
            if (c.mBody != null) {
                mRemoveBodies.add(c.mBody);
            }
            mEntities.remove(c);
        }

        respawnObstacles();
    }

    private void respawnObstacles() {
        if (mTreeStumpObstacle1 != null) {
            if (treeStumpObstacleHasLeftScreen()) {
                respawnTreeStumpObstacle();
            }
        }
        if (mSingleRockObstacle != null) {
            if (mSingleRockObstacle.mDead
                    || mSingleRockObstacle.getPosition().x
                            - getCameraOrigin().x < -600) {
                mSingleRockObstacle.mBody.setActive(false);
                mWorld.destroyBody(mSingleRockObstacle.mBody);
                mEntities.remove(mSingleRockObstacle);
                mSingleRockObstacle = new RockObstacle(getCameraOrigin().x
                        + Constants.getFloat("rockSpacing"), mWorld);
                mEntities.add(mSingleRockObstacle);
            }
        }
        if (mSingleSlowDownObstacle != null) {
            if (mSingleSlowDownObstacle.mDead
                    || mSingleSlowDownObstacle.getPosition().x
                            - getCameraOrigin().x < -600) {
                mSingleSlowDownObstacle.mBody.setActive(false);
                mWorld.destroyBody(mSingleSlowDownObstacle.mBody);
                mEntities.remove(mSingleSlowDownObstacle);
                createSlowDownObstacle();
                mEntities.add(mSingleSlowDownObstacle);
            }
        }

        if (mSingleSoupObstacle != null) {
            if (mSingleSoupObstacle.mDead
                    || mSingleSoupObstacle.getPosition().x
                            - getCameraOrigin().x < -600) {
                mSingleSoupObstacle.mBody.setActive(false);
                mWorld.destroyBody(mSingleSoupObstacle.mBody);
                mEntities.remove(mSingleSoupObstacle);
                createSoupObstacle();
                mEntities.add(mSingleSoupObstacle);
            }
        }
    }

    private void updatePlayerForAirControl() {
        /*
         * mPlayer.mBody.setLinearVelocity( mPlayer.mBody.getLinearVelocity().x
         * * 0.997f, mPlayer.mBody.getLinearVelocity().y);
         */
    }

    protected class ScoreDownloader implements Runnable {
        public ConcurrentSkipListSet<ScoreEntry> mScoreList;

        public ScoreDownloader() {
            mScoreList = new ConcurrentSkipListSet<ScoreEntry>();
        }

        public void downloadScoresAgain(int myScore) {
            mScoreList.clear();
            String username = System.getProperty("user.name");
            mScoreList.add(new ScoreEntry(username, myScore));
            new Thread(mPublicTopScores).start();
        }

        @Override
        public void run() {
            try {
                // Open scores url,
                URL u = new URL(
                        "http://immense-savannah-9950.herokuapp.com/csv_scores");
                u.getContent();

                // Setup read from it
                InputStream in = u.openStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in));
                String line = new String();

                // Read from it until we pass the Body tag,
                while (!line.contains("<body>")) {
                    line = reader.readLine();
                }

                // Now a blank line,
                line = reader.readLine();

                // And now some pairs of scores, until another blank line
                while (true) {
                    line = reader.readLine();
                    if (!line.contains(",")) {
                        break;
                    }

                    String[] pair = line.split(",");
                    assert pair.length == 2;
                    mScoreList.add(new ScoreEntry(pair[0], new Integer(pair[1])
                            .intValue()));
                }

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    protected class ScoreEntry implements Comparable<ScoreEntry> {
        public ScoreEntry(String n, int s) {
            mName = n;
            mScore = s;
        }

        @Override
        public int compareTo(ScoreEntry o) {
            if (o.mScore < mScore) {
                return -1;
            } else if (o.mScore > mScore) {
                return 1;
            } else {
                return 0;
            }
        }

        String mName;
        int mScore;
    }
}
