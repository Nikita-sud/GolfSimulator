package com.example.golfgame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public class GolfGame extends Game {
    private Screen mainScreen;
    private Screen gameScreen;
    private Screen settingsScreen;

    @Override
    public void create() {
        settingsScreen = new SettingsScreen(this);
        mainScreen = new MainMenuScreen(this);
        gameScreen = new GolfGameScreen(this);

        this.setScreen(mainScreen);
    }

    public void switchToGame() {
        ((GolfGameScreen)gameScreen).setHeightFunction(((SettingsScreen)settingsScreen).getCurHeightFunction());

        setScreen(gameScreen);
    }

    public void switchToSettings() {
        setScreen(settingsScreen);
    }

    public void switchToMenu() {
        setScreen(mainScreen);
    }

    public GolfGameScreen getGolfGameScreen(){
        return (GolfGameScreen)gameScreen;
    }

    public SettingsScreen getSettingsScreen(){
        return (SettingsScreen)settingsScreen;
    }
}
