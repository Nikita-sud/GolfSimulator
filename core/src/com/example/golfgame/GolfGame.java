package com.example.golfgame;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.example.golfgame.screens.MainMenuScreen;
import com.example.golfgame.screens.SandboxSettingsScreen;
import com.example.golfgame.screens.GolfGameScreen;
import com.example.golfgame.screens.SettingsScreen;
import com.example.golfgame.utils.gameUtils.Sandbox;

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
    private List<Sandbox> sandboxes = new ArrayList<>();
    private boolean keepSettingsMusic = false;

    /**
     * Initializes the game, creating and setting up the main menu, game, and settings screens.
     * It also initializes the {@link AssetManager} for managing game assets.
     */
    @Override
    public void create() {
        assetManager = new AssetManager();

        // Load music files
        assetManager.load("music/main-menu.mp3", Music.class);
        assetManager.load("music/game-screen.mp3", Music.class);
        assetManager.load("music/settings.mp3",    Music.class);

        assetManager.finishLoading(); // Ensure all assets are loaded before using them

        // Create screens after loading assets
        settingsScreen = new SettingsScreen(this, assetManager);
        sandboxSettingScreen = new SandboxSettingsScreen(this, assetManager);
        mainScreen = new MainMenuScreen(this, assetManager);
        gameScreen = new GolfGameScreen(this, assetManager);
        this.setScreen(mainScreen);
    }

    /**
     * Switches the current screen to the game screen and initializes game components.
     * It also updates the game screen with the current height function settings.
     */
    public void switchToGame() {
        keepSettingsMusic = false;
        getGolfGameScreen().initializeComponents();
        ((GolfGameScreen) gameScreen).setHeightFunction(((SettingsScreen) settingsScreen).getCurHeightFunction());
        setScreen(gameScreen);
    }

    /**
     * Switches the current screen to the settings screen.
     */
    public void switchToSettings() {
        keepSettingsMusic = true;
        setScreen(settingsScreen);
    }

    /**
     * Switches the current screen to the sandbox settings screen.
     */
    public void switchToSandBoxSettings() {
        keepSettingsMusic = true;
        setScreen(sandboxSettingScreen);
    }

    /**
     * Switches the current screen back to the main menu screen.
     */
    public void switchToMenu() {
        keepSettingsMusic = false;
        setScreen(mainScreen);
    }

    /**
     * Retrieves the current {@link GolfGameScreen} instance.
     *
     * @return The currently configured {@link GolfGameScreen}.
     */
    public GolfGameScreen getGolfGameScreen() {
        return (GolfGameScreen) gameScreen;
    }

    /**
     * Retrieves the current {@link SettingsScreen} instance.
     *
     * @return The currently configured {@link SettingsScreen}.
     */
    public SettingsScreen getSettingsScreen() {
        return (SettingsScreen) settingsScreen;
    }

    /**
     * Retrieves the current {@link MainMenuScreen} instance.
     *
     * @return The currently configured {@link MainMenuScreen}.
     */
    public MainMenuScreen getMenuScreen() {
        return (MainMenuScreen) mainScreen;
    }

    /**
     * Retrieves the games {@link sandboxes} instance.
     * 
     * @return the current games' sandbox representations.
     */
    public List<Sandbox> getSandboxes() {
        return sandboxes;
    }

    /**
     * Resets the games {@link sandboxes} instance.
     */
    public void resetSandboxes() {
        sandboxes.clear();
    }

    /**
     * Add new sandbox to current GolfGame instance.
     * 
     * @param newSandbox representation of new sandbox to be added.
     */
    public void addSandbox(Sandbox newSandbox) {
        sandboxes.add(newSandbox);
    }

    /**
     * Disposes of all assets and resources used by the game to free up memory.
     */
    @Override
    public void dispose() {
        assetManager.dispose();
    }

    public boolean shouldKeepSettingsMusic() {
        return keepSettingsMusic;
    }
}
