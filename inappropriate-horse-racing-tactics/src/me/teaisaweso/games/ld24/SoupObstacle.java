package me.teaisaweso.games.ld24;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;

public class SoupObstacle extends PhysicalObstacle {

    enum EvolutionStage {
        NORMAL, TENTACLES;
    }

    private static boolean sTexturesLoaded = false;
    private static Texture sSoup1, sSoup2, sSoup3, sSoupTentacle1, sSoupTentacle2;
    
    private EvolutionStage mStage;

    private static void loadTexturesOnDemand() {
        if (!sTexturesLoaded) {
            loadTextures();
            sTexturesLoaded = true;
        }
    }

    private static void loadTextures() {
        sSoup1 = new Texture(Gdx.files.internal("assets/Asset_Soup_1.png"));
        sSoup2 = new Texture(Gdx.files.internal("assets/Asset_Soup_2.png"));
        sSoup3 = new Texture(Gdx.files.internal("assets/Asset_Soup_3.png"));
        sSoupTentacle1 = new Texture(Gdx.files.internal("assets/Asset_Soup_tentacle1.png"));
        sSoupTentacle2 = new Texture(Gdx.files.internal("assets/Asset_Soup_tentacle2.png"));
    }

    public SoupObstacle(Body b) {
        super(b);
    }
    
    @Override
    public void collide(Entity e) {
        // TODO Auto-generated method stub

    }

    @Override
    public StatusModifier freshStatusModifier() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void hit() {
        // TODO Auto-generated method stub

    }

}
