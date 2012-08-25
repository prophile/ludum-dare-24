package me.teaisaweso.games.ld24;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Inappropriate Horse Racing Tactics";
		cfg.useGL20 = true;
		configureWindowDimensions(cfg);
		
		new LwjglApplication(new GameWrapper(), cfg);
	}

	private static void configureWindowDimensions(
			LwjglApplicationConfiguration cfg) {
		cfg.width = 800;
		cfg.height = 600;
	}
}
