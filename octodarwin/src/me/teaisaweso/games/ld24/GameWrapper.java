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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.audio.Music;
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

    private int mLastFireCountdown;

    private Enemy mEnemy;
    private Sound mEvolutionShootsound;
    private Body mFloor;
    private SpriteBatch mGameOverBatch;
    private Sprite mGameOverSprite;
    private boolean mIsGameOver;
    public boolean mDying;

    private boolean mIsOnFloor;

    private Player mPlayer;

    final Set<Body> mRemoveBodies = new HashSet<Body>();
    final SortedSet<Entity> mEntities = new TreeSet<Entity>();

    public final Random mRng = new Random();

    private World mWorld;

    private boolean mSplashScreen = true;

    private Sprite mSplashScreenSprite;
    private ExplosionManager mBananaExplosionManager;
    private ExplosionManager mDustExplosionManager;
    private ExplosionManager mRainbowExplosionManager;

    private float mNextSpawnPosition = 0.0f;

    Music mDeathMusic;

    public ExplosionManager getRainbowExplosionManager() {
        return mRainbowExplosionManager;
    }

    public ExplosionManager getBananaExplosionManager() {
        return mBananaExplosionManager;
    }

    public ExplosionManager getDustExplosionManager() {
        return mDustExplosionManager;
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
        if (instance == null) {
            mMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/Mix.ogg"));
            mMusic.setLooping(true);
            mMusic.play();

            mDeathMusic = Gdx.audio.newMusic(Gdx.files
                    .internal("assets/Death.ogg"));
            mDeathMusic.setLooping(false);
        }

        instance = this;
        mEntities.clear();

        mLastFireCountdown = 0;

        loadGameOverAssets();

        Texture t = new Texture(
                Gdx.files.internal("assets/Screen_splashfinal1.png"));
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
        mDying = false;
        // Blank list of top scores, in case intertubes fail.
        mPublicTopScores = new ScoreDownloader();

        mRainbowExplosionManager = new ExplosionManager("rainbow_burst");
        mBananaExplosionManager = new ExplosionManager("bananarama");
        mDustExplosionManager = new ExplosionManager("dust");

        createCrosshair();

        createPlayer();
        createDarwin();

        createInitialObstacles(5);

        addFloor();
        mDebugger = new Box2DDebugRenderer(true, true, true, true);
    }

    private void createCamera() {
        setCameraOrigin(new Vector2(0, 0));
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        mCamera = new OrthographicCamera(w, h);
    }

    private void createInitialObstacles(int n) {
        float position = getRandomObstacleSpacing();
        for (int i = 1; i <= n; ++i) {
            createObstacle(position);
            position += getRandomObstacleSpacing();
        }
        mNextSpawnPosition = position;
    }

    private float getRandomObstacleSpacing() {
        float minSpacing = Constants.getFloat("obstacleMinSpacing");
        float maxSpacing = Constants.getFloat("obstacleMaxSpacing");
        float spacingRange = maxSpacing - minSpacing;
        return minSpacing + spacingRange * sRng.nextFloat();
    }

    private ObstacleType mPreviousChoice;
    private int mObstaclesWithoutEvolution = 0;

    Music mMusic;

    public boolean mCanShoot = true;

    private ObstacleType getRandomObstacleType() {

        LotteryChooser<ObstacleType> types = new LotteryChooser<ObstacleType>(
                sRng);
        types.addEntry(ObstacleType.SOUP, Constants.getFloat("spawnSoup"));
        types.addEntry(ObstacleType.BANANA, Constants.getFloat("spawnBanana"));
        types.addEntry(ObstacleType.HORSE, Constants.getFloat("spawnHorse"));
        if (mObstaclesWithoutEvolution < Constants.getInt("evolveForce")) {
            types.addEntry(ObstacleType.STUMP, Constants.getFloat("spawnStump"));
            types.addEntry(ObstacleType.DOUBLE_STUMP,
                    Constants.getFloat("spawnDoubleStump"));
            types.addEntry(ObstacleType.ROCK, Constants.getFloat("spawnRock"));
        } else {
            System.out.println("forcing an evolution");
        }
        if (mPreviousChoice != ObstacleType.GAP) {
            types.addEntry(ObstacleType.GAP, Constants.getFloat("spawnGap"));
        }

        ObstacleType choice = types.pick();
        if (choice != ObstacleType.SOUP && choice != ObstacleType.BANANA
                && choice != ObstacleType.HORSE) {
            mObstaclesWithoutEvolution++;
        } else {
            mObstaclesWithoutEvolution = 0;
        }

        mPreviousChoice = choice;
        return mPreviousChoice;
    }

    private void createObstacleObjectOfType(ObstacleType type, float x) {
        PhysicalObstacle obstacle;
        switch (type) {
        case ROCK:
            obstacle = new RockObstacle(x, mWorld);
            mEntities.add(obstacle);
            break;
        case BANANA:
            obstacle = new BananaObstacle(x, mWorld);
            mEntities.add(obstacle);
            break;
        case SOUP:
            obstacle = new SoupObstacle(x, mWorld);
            mEntities.add(obstacle);
            break;
        case STUMP:
            obstacle = new TreeStumpObstacle(x, mWorld);
            mEntities.add(obstacle);
            break;
        case DOUBLE_STUMP:
            obstacle = new TreeStumpObstacle(x, mWorld);
            mEntities.add(obstacle);
            obstacle = ((TreeStumpObstacle) obstacle).createNearbyStump(mWorld);
            mEntities.add(obstacle);
            break;
        case HORSE:
            obstacle = new HorseObstacle(x, mWorld);
            mEntities.add(obstacle);
        default:
            break;
        }
    }

    private void createObstacle(float x) {
        createObstacleObjectOfType(getRandomObstacleType(), x);
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

    private void createPhysicsSimulation() {
        mWorld = new World(new Vector2(0, 0), true);
        mWorld.setContactListener(new WorldContactListener(this));
    }

    private void createPlayer() {
        mPlayer = new Player(mWorld);
        mPlayer.addStatusModifier(new CameraAttachedModifier(mPlayer));
        mEntities.add(mPlayer);

        mGunArm = new GunArmEntity(mPlayer);
        mEntities.add(mGunArm);
    }

    /*
     * private void createTreeStumpObstacle() { mTreeStumpObstacle1 =
     * TreeStumpObstacle.createStumpObstacle(mWorld, 1);
     * mEntities.add(mTreeStumpObstacle1); if (sRng.nextFloat() <
     * Constants.getFloat("doubleTreeProbability")) { mTreeStumpObstacle2 =
     * mTreeStumpObstacle1.createNearbyStump(mWorld);
     * mEntities.add(mTreeStumpObstacle2); } }
     */

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

        if (mLastFireCountdown > 0) {
            --mLastFireCountdown;
        }
        if (Gdx.input.isButtonPressed(Buttons.LEFT) && mLastFireCountdown == 0 && mCanShoot) {
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
            mLastFireCountdown = Constants.getInt("reloadTime");
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
        if (mDying) {
            c.setEnabled(false);
            return;
        }

        Entity collider_a = (Entity) a.getBody().getUserData();
        Entity collider_b = (Entity) b.getBody().getUserData();
        if (collider_a instanceof BananaObstacle
                && collider_b instanceof BananaObstacle) {
            c.setEnabled(false);
            return;
        }
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
                ((Enemy) collider_b).catchPlayer((Player) collider_a);
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
        t = new Texture(Gdx.files.internal("assets/gameoverbackground.png"));
        t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        mGameOverSprite = new Sprite(t, 800, 600);
        mGameOverBatch = new SpriteBatch();
    }

    @Override
    public void pause() {
    }

    private void removeCondemnedBodies() {
        for (Body b : mRemoveBodies) {
            mWorld.destroyBody(b);
        }

        mRemoveBodies.clear();
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
        mPlayer.mBody.setTransform(600 / 16, 325 / 16, 0);
        mEnemy.mBody.setTransform(210 / 16, 520 / 16, 0);
        mEnemy.setSplashScreen();
        mPlayer.setSplashScreen();
        mIsOnFloor = true;
        mPlayer.update();
        mEnemy.update();
        mBatch.begin();
        mSplashScreenSprite.draw(mBatch);
        mEnemy.draw(mBatch);
        mPlayer.draw(mBatch);
        mBatch.end();

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            mSplashScreen = false;
            mWorld.destroyBody(mPlayer.mBody);
            mWorld.destroyBody(mEnemy.mBody);
            mEntities.remove(mPlayer);
            mEntities.remove(mEnemy);
            mEntities.remove(mGunArm);
            createPlayer();
            createDarwin();
            mPlayer.update();
            mEnemy.update();
        }
    }

    private void renderGameOverScreen() {
        mGameOverBatch.begin();
        mGameOverSprite.draw(mGameOverBatch);

        String text = new String();
        int i = 1;
        for (ScoreEntry e : mPublicTopScores.mScoreList) {
            text += "    ";
            if (e.mIsPlayer) {
                text += "yours   ";
            } else {
                text += "    " + i + "     ";
            }

            text += "          ";

            text += e.mName + "                            " + e.mScore + "m";

            text += "\n";
            i += 1;
        }

        Color oldColor = mTextFont.getColor();
        mTextFont.setScale(1.0f);
        mTextFont.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        mTextFont.drawMultiLine(mGameOverBatch, text, 40.0f, 400.0f);
        mTextFont.setColor(oldColor);
        i += 1;
        mGameOverBatch.end();

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            clearGameOver();
            String username = System.getProperty("user.name");
            try {
                URL u = new URL(
                        "http://immense-savannah-9950.herokuapp.com/score/"
                                + username + "/" + (mScore ^ 0x5f3759df));
                u.getContent();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            create();
            if (GameWrapper.instance.mDeathMusic.isPlaying()) {
                GameWrapper.instance.mDeathMusic.stop();
            }
            if (!GameWrapper.instance.mMusic.isPlaying()) {
                GameWrapper.instance.mMusic.play();
            }
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

        mRainbowExplosionManager.draw(mBatch);
        mBananaExplosionManager.draw(mBatch);
        mDustExplosionManager.draw(mBatch);

        // Reset transform to untransformed, draw distance/score text
        mBatch.setTransformMatrix(new Matrix4().translate(0, 0, 0));
        mScore = (int) (getCameraOrigin().x / (PHYSICS_RATIO * Constants
                .getFloat("scoreMultiplier")));
        String dist = "Score: " + Integer.toString(mScore) + "m";
        if (Constants.getBoolean("darwinDebug")) {
            dist += "player speed: " + mPlayer.mBody.getLinearVelocity().x
                    + " ";
            dist += "darwin speed: " + mEnemy.mBody.getLinearVelocity().x + " ";
        }
        mTextFont.setScale(2);
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

    private void update() {
        mIsOnFloor = false;
        simulatePhysicsStep();
        updateEntities();
        mBackgroundManager.update(getCameraOrigin().x);

        removeCondemnedBodies();

        if (shouldJump()) {
            mPlayer.jump();
        }

        if (!mIsOnFloor) {
            updatePlayerForAirControl();
        }

        mRainbowExplosionManager.update();
        mBananaExplosionManager.update();
        mDustExplosionManager.update();

        handleRespawn();
    }

    private void handleRespawn() {
        while (getCameraOrigin().x > mNextSpawnPosition - 900) {
            createObstacle(mNextSpawnPosition);
            mNextSpawnPosition += getRandomObstacleSpacing();
        }
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
                c.mBody = null;
            }

            mEntities.remove(c);
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
            mScoreList.add(new ScoreEntry(username, myScore, true));
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
                            .intValue(), false));
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
        public ScoreEntry(String n, int s, boolean isPlayer) {
            mName = n;
            mScore = s;
            mIsPlayer = isPlayer;
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
        boolean mIsPlayer;
    }
}
