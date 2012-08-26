package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.physics.box2d.Body;

public class SlowDownObstacle extends PhysicalObstacle {

    public boolean mIsEvolved = false;
    private Sound mDarwinHurtSound;
    private Sound mEvolutionSound;

    public SlowDownObstacle(Body b) {
        super(b);
        // TODO Auto-generated constructor stub
        mDarwinHurtSound = Gdx.audio.newSound(Gdx.files.internal("assets/DarwinHurt.wav"));
        mEvolutionSound = Gdx.audio.newSound(Gdx.files.internal("assets/Evolve.wav"));
    }

    @Override
    public void collide(Entity e) {
        if (e instanceof Enemy && mIsEvolved) {
            Enemy enemy = (Enemy) e;
            enemy.addStatusModifier(freshStatusModifier());
            mDarwinHurtSound.play();
        }
    }

    @Override
    public void hit() {
        System.out.println("evolving");
        mIsEvolved = true;
        mEvolutionSound.play();
    }

    @Override
    public StatusModifier freshStatusModifier() {
        return new SlowDownModifier();
    }

}
