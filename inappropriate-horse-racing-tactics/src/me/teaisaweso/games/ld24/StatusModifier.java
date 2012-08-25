package me.teaisaweso.games.ld24;

import com.badlogic.gdx.graphics.g2d.Sprite;

public abstract class StatusModifier {
    public float adjustSpeed(float speed) {
        return speed;
    }

    public float adjustCurrentSpeed(float currentSpeed) {
        return adjustSpeed(currentSpeed);
    }

    public float adjustMaxSpeed(float maxSpeed) {
        return adjustSpeed(maxSpeed);
    }

    public Sprite getCurrentSprite(Sprite sprite) {
        return sprite;
    }

    public void update() {
        // do nothing
    }

    public float adjustAccel(float currentAccel) {
        return currentAccel;
    }

    public boolean hasEnded() {
        return true;
    }
}
