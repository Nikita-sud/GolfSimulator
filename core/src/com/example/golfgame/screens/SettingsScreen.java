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
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.example.golfgame.utils.Function;
import com.example.golfgame.GolfGame;

/**
 * Provides a settings screen for the golf game, allowing users to adjust various gameplay settings such as wind
 * speed, sun level, and the height function for the terrain.
 */
public class SettingsScreen implements Screen {
    private GolfGame game;
    private Stage stage;
    private Skin skin;
    private Function curHeightFunction;
    private Music music;

    /**
     * Initializes a new settings screen with default or specified parameters for terrain, wind, and sunlight.
     *
     * @param game The main game control to allow navigation between screens.
     */
    public SettingsScreen(GolfGame game, AssetManager assetManager) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("assets/uiskin.json"));
        curHeightFunction = new Function("sin(0.3x)*cos(0.3y)+1", "x", "y");  // Default height function
        music = assetManager.get("assets/music/settings.mp3", Music.class);

        setupUI();
    }

    /**
     * Sets up the user interface components including buttons, sliders, and input fields.
     */
    private void setupUI() {
        TextButton mainMenuButton = new TextButton("Back to Main Menu", skin);
        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.switchToMenu();  // Change to the main menu screen
            }
        });
    
        TextButton playButton = new TextButton("Play!", skin);
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.switchToGame();  // Change to the game screen
            }
        });
    
        TextField heightFunction = new TextField("sin(0.3x)*cos(0.3y)+1", skin);
        heightFunction.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    curHeightFunction = new Function(heightFunction.getText(), "x", "y");
                } catch (Exception e) {
                    curHeightFunction = new Function("sin(0.3x)*cos(0.3y)+1", "x", "y");
                }
            }
        });
    
        Slider windSlider = new Slider(0f, 0.001f, 0.0001f, false, skin);
        Label windLabel = new Label("Wind speed magnitude: " + String.format("%.4f", windSlider.getValue()), skin);
        windSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = windSlider.getValue();
                game.getGolfGameScreen().getWeather().setWind(value);
                windLabel.setText("Wind speed magnitude: " + String.format("%.4f", value));
            }
        });
    
        Slider sunSlider = new Slider(0.2f, 1f, 0.001f, false, skin);
        sunSlider.setValue(1);
        Label sunLabel = new Label("Sunlight level: " + String.format("%.3f", sunSlider.getValue()), skin);
        sunSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = sunSlider.getValue();
                game.getGolfGameScreen().getWeather().setSun(value);
                sunLabel.setText("Sunlight level: " + String.format("%.3f", value));
            }
        });
    
        // Button to open the SandboxSettingsScreen
        TextButton sandboxSettingsButton = new TextButton("Sandbox Settings", skin);
        sandboxSettingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.switchToSandBoxSettings();  // Switch to SandboxSettingsScreen
            }
        });
    
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.setBackground(new TextureRegionDrawable(new Texture(Gdx.files.internal("backgrounds/settingsBackground.jpeg"))));
    
        Table middleTable = new Table();
        middleTable.add(heightFunction).width(200).height(50).pad(10).row();
        middleTable.add(windSlider).width(400).pad(10).row();
        middleTable.add(windLabel).pad(10).row();
        middleTable.add(sunSlider).width(400).pad(10).row();
        middleTable.add(sunLabel).pad(10).row();
        middleTable.add(sandboxSettingsButton).pad(20).row();  // Add the sandbox settings button here
    
        rootTable.add(mainMenuButton).width(200).height(50).bottom().left().pad(20);
        rootTable.add(middleTable).expand().center();
        rootTable.add(playButton).width(150).height(50).pad(20).bottom().right();
    
        stage.addActor(rootTable);
    }
    

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST); // Disable depth testing for 2D rendering
        Gdx.gl.glClearColor(0, 0, 0, 1); // Ensure background is set to black
        music.setLooping(true);
        music.play();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // Optionally handle anything specific on pause
    }

    @Override
    public void resume() {
        // Optionally handle anything specific on resume
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        music.stop();
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose(); // Ensure all resources are properly disposed to prevent memory leaks
    }

    /**
     * Retrieves the currently configured height function for terrain generation.
     *
     * @return The function that defines the terrain's height based on 'x' and 'y' coordinates.
     */
    public Function getCurHeightFunction() {
        return curHeightFunction;
    }
}
