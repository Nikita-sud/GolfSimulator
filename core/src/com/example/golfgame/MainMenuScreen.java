package com.example.golfgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenuScreen implements Screen {
    @SuppressWarnings("unused")
    private GolfGame game;
    private Stage stage;
    private Skin skin;  // Store the skin at the class level to dispose it properly

    public MainMenuScreen(GolfGame game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("assets/uiskin.json"));  // Load the UI skin
        TextButton startGameButton = new TextButton("Start Game", skin);
        startGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("Wind in x Direction: "+game.getGolfGameScreen().getWeather().getWind()[0]);
                System.out.println("Wind in y Direction: "+game.getGolfGameScreen().getWeather().getWind()[1]);
                System.out.println("Wind in z Direction: "+game.getGolfGameScreen().getWeather().getWind()[2]);
                game.switchToGame();  // Change to the game screen
            }
        });

        TextButton settingsButton = new TextButton("Settings", skin);
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.switchToSettings();  // Change to the game screen
            }
        });

        // Layout setup
        Table table = new Table();
        table.setFillParent(true);  // Fill the stage
        table.center();  // Center everything in the table

        // Add the Start Game button
        table.add(startGameButton).width(200).height(80).pad(20);
        table.row();  // Move to the next row for the next button

        // Add the Settings button
        table.add(settingsButton).width(200).height(80).pad(20);

        // Add the table to the stage
        stage.addActor(table);

        
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST); // Disable depth testing for 2D rendering
        Gdx.gl.glClearColor(0, 0, 0, 1); // Ensure background is set to black
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1); // Set clear color to black
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
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
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();  // Make sure to dispose of the Skin to free up resources
    }
}
