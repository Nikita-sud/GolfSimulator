package com.example.golfgame;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;


// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
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

