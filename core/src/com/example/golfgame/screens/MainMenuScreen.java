package com.example.golfgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.example.golfgame.GolfGame;

/**
 * The main menu screen for the golf game, providing navigation options to start the game or access settings.
 * This class manages the layout and behavior of the UI elements displayed on the main menu.
 */
public class MainMenuScreen implements Screen {
    private GolfGame game;
    private Stage stage;
    private Skin skin;
    private Music music;

    /**
     * Constructs the main menu screen with buttons for starting the game and accessing settings.
     *
     * @param game The instance of {@link GolfGame} which this screen is a part of.
     */
    public MainMenuScreen(GolfGame game, AssetManager assetManager) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("uiskin.json")); // Load the UI skin
        music = assetManager.get("music/main-menu.mp3", Music.class);

        // Setup buttons and add them to the stage
        setupButtons();
    }

    /**
     * Initializes and places the UI components (buttons) on the screen.
     */
    private void setupButtons() {
        TextButton startGameButton = new TextButton("Start Game", skin);
        startGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.switchToGame(); // Switch to the game screen
            }
        });

        TextButton settingsButton = new TextButton("Settings", skin);
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.switchToSettings(); // Switch to the settings screen
            }
        });

        // Layout setup
        Table table = new Table();
        table.setBackground(new TextureRegionDrawable(new Texture(Gdx.files.internal("backgrounds/mainMenuBackground.jpg"))));
        table.setFillParent(true); // Fill the stage
        table.center(); // Center everything in the table
        table.add(startGameButton).width(200).height(80).pad(20);
        table.row(); // Move to the next row for the next button
        table.add(settingsButton).width(200).height(80).pad(20);
        stage.addActor(table);
    }

    /**
     * Sets up the input processor and prepares the screen to be displayed.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        Gdx.gl.glClearColor(0, 0, 0, 1); // Set clear color to black
        music.setLooping(true);
        music.play();
    }

    /**
     * Renders the menu screen and updates the stage components.
     *
     * @param delta The time in seconds since the last frame.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(delta, 1 / 30f)); // Cap frame time to maintain stable animations
        stage.draw();
    }

    /**
     * Adjusts the screen layout when the screen size is changed.
     *
     * @param width The new width of the screen.
     * @param height The new height of the screen.
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Handles any specific logic when the game is paused.
     */
    @Override
    public void pause() {
        // Optional handling for when the game is paused
    }

    /**
     * Handles any specific logic when the game is resumed.
     */
    @Override
    public void resume() {
        // Optional handling for when the game is resumed
    }

    /**
     * Disables the input processor when the screen is hidden.
     */
    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        music.stop();
    }

    /**
     * Frees up resources when the screen is disposed, including UI skins and the stage.
     */
    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
