package me.teaisaweso.games.ld24;

import com.badlogic.gdx.graphics.g2d.Sprite;

public abstract class StatusModifier {
    public double adjustSpeed(double speed) {
        return speed;
    }

    public double adjustCurrentSpeed(double currentSpeed) {
        return adjustSpeed(currentSpeed);
    }

    public double adjustMaxSpeed(double maxSpeed) {
        return adjustSpeed(maxSpeed);
    }

    public Sprite getCurrentSprite(Sprite sprite) {
        return sprite;
    }

    public void update() {
        // do nothing
    }
}
