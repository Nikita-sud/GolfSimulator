package com.example.golfgame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;

public class GolfGame extends Game {
    private Screen mainScreen;
    private Screen gameScreen;
    private AssetManager assetManager;

    @Override
    public void create() {
        assetManager = new AssetManager();
        mainScreen = new MainMenuScreen(this);
        gameScreen = new GolfGameScreen(this,assetManager);
        this.setScreen(mainScreen);
    }

    public void switchToGame() {
        setScreen(gameScreen);
    }

    public void switchToMenu() {
        setScreen(mainScreen);
    }
}
