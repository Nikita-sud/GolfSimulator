package com.example.golfgame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public class GolfGame extends Game {
    private Screen mainScreen;
    private Screen gameScreen;

    @Override
    public void create() {
        mainScreen = new MainMenuScreen(this);
        gameScreen = new GolfGameScreen(this);
        this.setScreen(mainScreen);
    }

    public void switchToGame() {
        setScreen(gameScreen);
    }

    public void switchToMenu() {
        setScreen(mainScreen);
    }
}
