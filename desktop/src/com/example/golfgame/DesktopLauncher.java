package com.example.golfgame;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
/**
 * Launches the desktop application for the Golf game.
 * Configures and starts a new instance of the game using LWJGL3.
 * <p>
 * Note: On macOS, the application must be started with the -XstartOnFirstThread JVM argument.
 */
public class DesktopLauncher {

    /**
     * The main method to launch the desktop application.
     * Configures the application window settings and starts the game.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(800, 480);
        config.setResizable(true);
        config.setWindowSizeLimits(800, 480, -1, -1);
        config.setForegroundFPS(60);
        config.setTitle("Golf");
        new Lwjgl3Application(new GolfGame(), config);
    }
}
