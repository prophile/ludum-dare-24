package me.teaisaweso.games.ld24;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

final class WorldContactListener implements ContactListener {
    /**
     * 
     */
    private final GameWrapper gameWrapper;

    /**
     * @param gameWrapper
     */
    WorldContactListener(GameWrapper gameWrapper) {
        this.gameWrapper = gameWrapper;
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        this.gameWrapper.handleCollision(contact.getFixtureA(), contact.getFixtureB(),
                contact);
        this.gameWrapper.handleCollision(contact.getFixtureB(), contact.getFixtureA(),
                contact);
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        // TODO Auto-generated method stub

    }

    @Override
    public void endContact(Contact contact) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beginContact(Contact contact) {
        this.gameWrapper.handleCollision(contact.getFixtureA(), contact.getFixtureB(),
                contact);
        this.gameWrapper.handleCollision(contact.getFixtureB(), contact.getFixtureA(),
                contact);
    }
}