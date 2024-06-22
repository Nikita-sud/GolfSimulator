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
     * @param assetManager The asset manager to load and manage game assets.
     */
    public SettingsScreen(GolfGame game, AssetManager assetManager) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("assets/uiskin.json"));
        curHeightFunction = new Function("sin(0.1x)*cos(0.1y)+1", "x", "y");  // Default height function
        music = assetManager.get("assets/music/settings.mp3", Music.class);

        setupUI();
    }

    /**
     * Sets up the user interface components including buttons, sliders, and input fields.
     */
    private void setupUI() {
        // Create main menu button
        TextButton mainMenuButton = new TextButton("Back to Main Menu", skin);
        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.switchToMenu();  // Change to the main menu screen
            }
        });

        // Create play button
        TextButton playButton = new TextButton("Play!", skin);
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.switchToGame();  // Change to the game screen
            }
        });

        // Create height function text field
        TextField heightFunction = new TextField("sin(0.1x)*cos(0.1y)+1", skin);
        heightFunction.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    curHeightFunction = new Function(heightFunction.getText(), "x", "y");
                } catch (Exception e) {
                    curHeightFunction = new Function("sin(0.1x)*cos(0.1y)+1", "x", "y");
                }
            }
        });

        // Create goal position input fields and label
        Label goalPositionLabel = new Label("Goal Position (X, Y):", skin);
        TextField goalXPosition = new TextField("-20", skin);
        TextField goalYPosition = new TextField("20", skin);

        goalXPosition.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    game.getGolfGameScreen().setGoalCoords(new float[]{Float.parseFloat(goalXPosition.getText()), Float.parseFloat(goalYPosition.getText())});
                    goalPositionLabel.setText("Goal Position (X, Y):");
                } catch (Exception e) {
                    goalPositionLabel.setText("Invalid X Position");
                }
            }
        });

        goalYPosition.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    game.getGolfGameScreen().setGoalCoords(new float[]{Float.parseFloat(goalXPosition.getText()), Float.parseFloat(goalYPosition.getText())});
                    goalPositionLabel.setText("Goal Position (X, Y):");
                } catch (Exception e) {
                    goalPositionLabel.setText("Invalid Y Position");
                }
            }
        });

        // Create ball position input fields and label
        Label ballPositionLabel = new Label("Ball Position (X, Y):", skin);
        TextField ballXPosition = new TextField("0", skin);
        TextField ballYPosition = new TextField("0", skin);

        ballXPosition.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    game.getGolfGameScreen().setBallCoords(new float[]{Float.parseFloat(ballXPosition.getText()), Float.parseFloat(ballYPosition.getText())});
                    ballPositionLabel.setText("Ball Position (X, Y):");
                } catch (Exception e) {
                    ballPositionLabel.setText("Invalid X Position");
                }
            }
        });

        ballYPosition.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    game.getGolfGameScreen().setBallCoords(new float[]{Float.parseFloat(ballXPosition.getText()), Float.parseFloat(ballYPosition.getText())});
                    ballPositionLabel.setText("Ball Position (X, Y):");
                } catch (Exception e) {
                    ballPositionLabel.setText("Invalid Y Position");
                }
            }
        });

        // Create goal radius input field and label
        Label goalRadiusLabel = new Label("Goal Radius:", skin);
        TextField goalRadius = new TextField("1.5", skin);

        goalRadius.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    game.getGolfGameScreen().setGoalRadius(Float.parseFloat(goalRadius.getText()));
                    goalRadiusLabel.setText("Goal Radius:");
                } catch (Exception e) {
                    goalRadiusLabel.setText("Invalid Radius");
                }
            }
        });

        // Create wind slider and label
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

        // Create sunlight slider and label
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

        // Create sandbox settings button
        TextButton sandboxSettingsButton = new TextButton("Sandbox Settings", skin);
        sandboxSettingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.switchToSandBoxSettings();  // Switch to SandboxSettingsScreen
            }
        });

        // Add bot labels
        Label ruleBasedBotStatus = new Label("Rule-Based Bot: Off", skin);
        Label hillClimbingBotStatus = new Label("Advanced Bot: Off", skin);

        // Add the rule-based bot UI
        TextButton toggleRuleBasedBot = new TextButton("Toggle Rule-Based Bot", skin);
        toggleRuleBasedBot.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getGolfGameScreen().toggleRuleBasedBotActiveness();
                if (ruleBasedBotStatus.getText().toString().equals("Rule-Based Bot: Off")) {
                    ruleBasedBotStatus.setText("Rule-Based Bot: On");
                    hillClimbingBotStatus.setText("Advanced Bot: Off");
                    game.getGolfGameScreen().setHillClimbingBotActive(false);
                } else {
                    ruleBasedBotStatus.setText("Rule-Based Bot: Off");
                }
            }
        });

        // Add the Advanced Bot bot UI
        TextButton toggleHillClimbingBot = new TextButton("Toggle Advanced Bot", skin);
        toggleHillClimbingBot.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getGolfGameScreen().toggleHillClimbingBotActiveness();
                if (hillClimbingBotStatus.getText().toString().equals("Advanced Bot: Off")) {
                    hillClimbingBotStatus.setText("Advanced Bot: On");
                    ruleBasedBotStatus.setText("Rule-Based Bot: Off");
                    game.getGolfGameScreen().setRuleBasedBotActive(false);
                } else {
                    hillClimbingBotStatus.setText("Advanced Bot: Off");
                }
            }
        });

        // Create root table
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.setBackground(new TextureRegionDrawable(new Texture(Gdx.files.internal("backgrounds/settingsBackground.jpeg"))));

        // Create middle table
        Table middleTable = new Table();
        middleTable.add(heightFunction).width(200).height(50).pad(10).row();
        middleTable.add(windSlider).width(400).pad(10).row();
        middleTable.add(windLabel).pad(10).row();
        middleTable.add(sunSlider).width(400).pad(10).row();
        middleTable.add(sunLabel).pad(10).row();

        // Create goal position table
        Table goalPositionTable = new Table();
        goalPositionTable.add(goalPositionLabel).pad(10);
        goalPositionTable.add(goalXPosition).width(50).height(50).pad(10);
        goalPositionTable.add(goalYPosition).width(50).height(50).pad(10);
        middleTable.add(goalPositionTable).pad(10).row();

        // Create ball position table
        Table ballPositionTable = new Table();
        ballPositionTable.add(ballPositionLabel).pad(10);
        ballPositionTable.add(ballXPosition).width(50).height(50).pad(10);
        ballPositionTable.add(ballYPosition).width(50).height(50).pad(10);
        middleTable.add(ballPositionTable).pad(10).row();

        // Add goal radius input field to middle table
        Table goalRadiusPositionTable = new Table();
        goalRadiusPositionTable.add(goalRadiusLabel).pad(10);
        goalRadiusPositionTable.add(goalRadius).width(50).height(50).pad(10).row();
        middleTable.add(goalRadiusPositionTable).pad(10).row();

        // Add sandbox settings button to middle table
        middleTable.add(sandboxSettingsButton).pad(20).row();

        // Add bot toggle buttons and labels to middle table
        Table botTable = new Table();
        botTable.add(ruleBasedBotStatus).pad(10);
        botTable.add(toggleRuleBasedBot).pad(10).row();
        botTable.add(hillClimbingBotStatus).pad(10);
        botTable.add(toggleHillClimbingBot).pad(10).row();
        middleTable.add(botTable).pad(10).row();

        // Add components to root table
        rootTable.add(mainMenuButton).width(200).height(50).bottom().left().pad(20);
        rootTable.add(middleTable).expand().center();
        rootTable.add(playButton).width(150).height(50).pad(20).bottom().right();

        // Add root table to stage
        stage.addActor(rootTable);
    }

    
    
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST); // Disable depth testing for 2D rendering
        Gdx.gl.glClearColor(0, 0, 0, 1); // Ensure background is set to black
        if (!game.shouldKeepSettingsMusic() || !music.isPlaying()) {
            music.setLooping(true);
            music.play();
        }
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
        if (!game.shouldKeepSettingsMusic()) {
            music.stop();
        }
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
