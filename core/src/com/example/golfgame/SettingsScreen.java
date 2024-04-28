package com.example.golfgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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

public class SettingsScreen implements Screen {
    @SuppressWarnings("unused")
    private GolfGame game;
    private Stage stage;
    private Skin skin;
    private Function curHeightFunction = new Function("sin(0.3x)*cos(0.3y)+1", "x", "y");  
    

    public SettingsScreen(GolfGame game){
        this.game = game;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("assets/uiskin.json"));  // Load the UI skin
    
        // Creating buttons
        TextButton mainMenuButton = new TextButton("Back to Main Menu", skin);
        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.switchToMenu();  // Change to the game screen
            }
        });
    
        TextButton playButton = new TextButton("Play!", skin);
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                System.out.println("Wind in x Direction: "+game.getGolfGameScreen().getWeather().getWind()[0]);
                System.out.println("Wind in y Direction: "+game.getGolfGameScreen().getWeather().getWind()[1]);
                System.out.println("Wind in z Direction: "+game.getGolfGameScreen().getWeather().getWind()[2]);
                game.getGolfGameScreen().initializeComponents();
                game.switchToGame();
            }
        });
    
        // TextField and Sliders
        TextField heightFunction = new TextField("sin(0.3x)*cos(0.3y)+1", skin);
        heightFunction.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                // Error handling for function input
                try{
                    curHeightFunction = new Function(heightFunction.getText(), "x", "y");
                } catch (Exception e) {
                    curHeightFunction = new Function("sin(0.3x)*cos(0.3y)+1", "x", "y");
                }
            }
        });
    
        Slider windSlider = new Slider(0f, 0.001f, 0.0001f, false, skin);
        Label windLabel = new Label("Wind speed magnitude: "+String.format("%.4f", windSlider.getValue()), skin);
        windSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                float value = windSlider.getValue();
                game.getGolfGameScreen().getWeather().setWind(value);
                windLabel.setText("Wind speed magnitude: "+String.format("%.4f", windSlider.getValue()));
            }
        });
    
        Slider sunSlider = new Slider(0.2f, 1f, 0.001f, false, skin);
        sunSlider.setValue(1);
        Label sunLabel = new Label("Sunlight level: "+String.format("%.3f", sunSlider.getValue()), skin);
        sunSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                float value = sunSlider.getValue();
                game.getGolfGameScreen().getWeather().setSun(value);
                sunLabel.setText("Sunlight level: "+String.format("%.3f", sunSlider.getValue()));
            }
        });
    
        // Root table to arrange the layout
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.setBackground(new TextureRegionDrawable(new Texture(Gdx.files.internal("textures/SettingsBackground.jpeg"))));
    
        // Create a table for the middle contents
        Table middleTable = new Table();
        middleTable.add(heightFunction).width(200).height(50).pad(10).row();
        middleTable.add(windSlider).width(400).pad(10).row();
        middleTable.add(windLabel).pad(10).row();
        middleTable.add(sunSlider).width(400).pad(10).row();
        middleTable.add(sunLabel).pad(10).row();
    
        // Add to root table
        rootTable.add(mainMenuButton).width(200).height(50).bottom().left().pad(10);
        rootTable.add(middleTable).expand().center();
        rootTable.add(playButton).width(150).height(50).pad(20).bottom().right();
    
        stage.addActor(rootTable);
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

    public Function getCurHeightFunction(){
        return curHeightFunction;
    }
}


