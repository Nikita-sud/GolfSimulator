package com.example.golfgame;

import java.util.ArrayList;
import java.util.List;
import com.example.golfgame.utils.Sandbox;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.example.golfgame.screens.MainMenuScreen;
import com.example.golfgame.screens.SandboxSettingsScreen;
import com.example.golfgame.screens.GolfGameScreen;
import com.example.golfgame.screens.SettingsScreen;

/**
 * The main class for the golf game application. It manages the different screens of the game
 * such as the main menu, game screen, and settings screen. It also manages the lifecycle of
 * these screens and the assets used throughout the game.
 */
public class GolfGame extends Game {
    private Screen mainScreen;
    private Screen gameScreen;
    private Screen settingsScreen;
    private Screen sandboxSettingScreen;
    private AssetManager assetManager;
    private Music backgroundMusic;
    private List<Sandbox> sandboxes = new ArrayList<>();
    /**
     * Initializes the game, creating and setting up the main menu, game, and settings screens.
     * It also initializes the {@link AssetManager} for managing game assets.
     */
    @Override
    public void create() {
        assetManager = new AssetManager();
        assetManager.load("core/src/com/example/golfgame/Music/goodbadugly-whistle-long.mp3", Music.class);
        settingsScreen = new SettingsScreen(this);
        sandboxSettingScreen = new SandboxSettingsScreen(this);
        mainScreen = new MainMenuScreen(this);
        gameScreen = new GolfGameScreen(this, assetManager);
        this.setScreen(mainScreen);
        backgroundMusic = assetManager.get("core/src/com/example/golfgame/Music/goodbadugly-whistle-long.mp3", Music.class);
        backgroundMusic.setLooping(true);
        backgroundMusic.play();
    }

    /**
     * Switches the current screen to the game screen and initializes game components.
     * It also updates the game screen with the current height function settings.
     */
    public void switchToGame() {
        getGolfGameScreen().initializeComponents();
        ((GolfGameScreen)gameScreen).setHeightFunction(((SettingsScreen)settingsScreen).getCurHeightFunction());
        setScreen(gameScreen);
    }

    /**
     * Switches the current screen to the settings screen.
     */
    public void switchToSettings() {
        setScreen(settingsScreen);
    }

    /**
     * Switches the current screen to the sandbox settings screen.
     */
    public void switchToSandBoxSettings() {
        setScreen(sandboxSettingScreen);
    }

    /**
     * Switches the current screen back to the main menu screen.
     */
    public void switchToMenu() {
        setScreen(mainScreen);
    }

    /**
     * Retrieves the current {@link GolfGameScreen} instance.
     *
     * @return The currently configured {@link GolfGameScreen}.
     */
    public GolfGameScreen getGolfGameScreen(){
        return (GolfGameScreen) gameScreen;
    }

    /**
     * Retrieves the current {@link SettingsScreen} instance.
     *
     * @return The currently configured {@link SettingsScreen}.
     */
    public SettingsScreen getSettingsScreen(){
        return (SettingsScreen)settingsScreen;
    }

    /**
     * Retrieves the current {@link MainMenuScreen} instance.
     *
     * @return The currently configured {@link MainMenuScreen}.
     */
    public MainMenuScreen getMenuScreen(){
        return (MainMenuScreen) mainScreen;
    }

    /**
     * Retrieves the games {@link sandboxes} instance.
     * 
     * @return the current games' sandbox representations.
     */
    public List<Sandbox> getSandboxes(){
        return sandboxes;
    }

    /**
     * Resets the games {@link sandboxes} instance.
     */
    public void resetSandboxes(){
        sandboxes.clear();
    }

    /**
     * Add new sandbox to current GolfGame instance.
     * 
     * @param newSandbox representation of new sandbox to be added.
     */
    public void addSandbox(Sandbox newSandbox){
        sandboxes.add(newSandbox);
    }

    /**
     * Disposes of all assets and resources used by the game to free up memory.
     */
    @Override
    public void dispose() {
        assetManager.dispose();
        backgroundMusic.dispose();
    }
}
