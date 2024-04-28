package com.example.golfgame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;

public class GolfGame extends Game {
    private Screen mainScreen;
    private Screen gameScreen;
    private Screen settingsScreen;
    private AssetManager assetManager;

    @Override
    public void create() {
        assetManager = new AssetManager();
        settingsScreen = new SettingsScreen(this);
        mainScreen = new MainMenuScreen(this);
        gameScreen = new GolfGameScreen(this,assetManager);
        this.setScreen(mainScreen);
    }

    public void switchToGame() {
        getGolfGameScreen().initializeComponents();
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
    public MainMenuScreen getMenuScreen(){
        return (MainMenuScreen)mainScreen;
    }

    @Override
    public void dispose() {
        assetManager.dispose();
    }
}
