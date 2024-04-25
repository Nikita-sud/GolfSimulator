package com.example.golfgame;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class SettingsScreen implements Screen {
    private GolfGame game;
    private Stage stage;
    private Skin skin;
    private Function curHeightFunction = new Function("sin(x) * cos(y)", "x", "y");  

    public SettingsScreen(GolfGame game){
        this.game = game;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("assets/uiskin.json"));  // Load the UI skin
        TextButton mainMenuButton = new TextButton("Back to Main Menu", skin);
        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.switchToMenu();  // Change to the game screen
            }
        });

        TextField heightFunction = new TextField("Enter height Function", skin);
        TextButton enterHeightFunction = new TextButton("Enter", skin);
        enterHeightFunction.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                curHeightFunction = new Function(heightFunction.getText(), "x", "y");
            }
        });
        Table table = new Table();
        table.center();
        table.setFillParent(true);
        table.add(mainMenuButton).width(200).height(80).pad(20);
        table.add(heightFunction).width(200).height(80).pad(20);
        table.add(enterHeightFunction).width(100).height(50).pad(20);

        stage.addActor(table);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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


