package me.teaisaweso.games.ld24;

import com.badlogic.gdx.math.Vector2;

public class CameraAttachedModifier extends StatusModifier {
    private final Entity mEntity;

    public CameraAttachedModifier(Entity entity) {
        mEntity = entity;
    }

    @Override
    public void update() {
        Vector2 position = mEntity.getPosition();
        GameWrapper.mCameraOrigin = new Vector2(position.x, 300);
    }

    @Override
    public boolean hasEnded() {
        return false;
    }
}
