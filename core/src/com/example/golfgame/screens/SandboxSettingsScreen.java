package com.example.golfgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.example.golfgame.GolfGame;
import com.example.golfgame.utils.gameUtils.Sandbox;

/**
 * Represents the screen for configuring sandbox settings in the golf game.
 * Allows the user to add and reset sandbox boundaries, providing a UI for
 * interaction.
 */
public class SandboxSettingsScreen implements Screen {
    private GolfGame game;
    private Stage stage;
    private Skin skin;
    private Music music;

    /**
     * Constructs a new SandboxSettingsScreen with necessary dependencies.
     *
     * @param game the main game control object, handling overall game state and logic
     * @param assetManager the asset manager to load and manage game assets
     */
    public SandboxSettingsScreen(GolfGame game, AssetManager assetManager) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        this.skin = new Skin(Gdx.files.internal("uiskin.json"));  // Ensure 'uiskin.json' exists in assets
        music = assetManager.get("assets/music/settings.mp3", Music.class);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        if (!game.shouldKeepSettingsMusic() || !music.isPlaying()) {
            music.setLooping(true);
            music.play();
        }
        // Setup the UI for Sandbox settings
        setupUI();
    }

    /**
     * Sets up the UI components for the sandbox settings screen.
     */
    private void setupUI() {
        Table mainTable = new Table();
        Table sandboxInfoTable = new Table();
        sandboxInfoTable.add(new Label("Sandbox List:", skin)).row();
        mainTable.center();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);
    
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.switchToSettings(); 
            }
        });
        mainTable.add(backButton).width(100).height(50).expand().bottom().left().pad(10);
        mainTable.setBackground(new TextureRegionDrawable(new Texture(Gdx.files.internal("backgrounds/settingsBackground.jpeg"))));
    
        // Separate table for sandbox boundary controls
        Table boundaryTable = new Table();
        boundaryTable.center();
        
        Label sandboxLabel = new Label("Enter sandbox boundaries", skin);
        boundaryTable.add(sandboxLabel).colspan(3).padBottom(10).row();  // Span across all three columns
    
        TextField xLowBound = new TextField("", skin);
        Label xSandboxBoundLabel = new Label("<X<", skin);
        TextField xHighBound = new TextField("", skin);
        TextField yLowBound = new TextField("", skin);
        Label ySandboxBoundLabel = new Label("<Y<", skin);
        TextField yHighBound = new TextField("", skin);
        TextButton addSandbox = new TextButton("Add Sandbox", skin);
        addSandbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    Sandbox newSandbox = new Sandbox(Float.parseFloat(xLowBound.getText()), Float.parseFloat(xHighBound.getText()), Float.parseFloat(yLowBound.getText()), Float.parseFloat(yHighBound.getText()));
                    game.addSandbox(newSandbox);
                    sandboxInfoTable.add(new Label("Sandbox: " + newSandbox.toString(), skin)).row();
                    xLowBound.setText("");
                    xHighBound.setText("");
                    yLowBound.setText("");
                    yHighBound.setText("");
                } catch (Exception e) {
                    sandboxLabel.setText("Invalid sandbox Boundaries");
                }
            }
        });
    
        // Adding X boundary controls in one row
        boundaryTable.add(xLowBound).width(50).height(50).pad(10);
        boundaryTable.add(xSandboxBoundLabel).pad(10);
        boundaryTable.add(xHighBound).width(50).height(50).pad(10).row();
    
        // Adding Y boundary controls in another row
        boundaryTable.add(yLowBound).width(50).height(50).pad(10);
        boundaryTable.add(ySandboxBoundLabel).pad(10);
        boundaryTable.add(yHighBound).width(50).height(50).pad(10).row();
    
        // Add button in the next row
        boundaryTable.add(addSandbox).colspan(3).padTop(20).row();
    
        // Adding a new button for resetting the sandbox list
        TextButton resetSandboxList = new TextButton("Reset Sandbox List", skin);
        resetSandboxList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.resetSandboxes();
                sandboxInfoTable.clear();
                sandboxInfoTable.add(new Label("Sandbox List:", skin)).row();
            }
        });
        boundaryTable.add(resetSandboxList).colspan(3).padTop(10).row();
    
        // Adding sand friction controls
        Label sandFrictionKineticLabel = new Label("Sand Kinetic Friction:", skin);
        TextField sandFrictionKineticField = new TextField(""+game.getGolfGameScreen().getSandFrictionKinetic(), skin);
        Label sandFrictionStaticLabel = new Label("Sand Static Friction:", skin);
        TextField sandFrictionStaticField = new TextField(""+game.getGolfGameScreen().getSandFrictionStatic(), skin);
    
        sandFrictionKineticField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    game.getGolfGameScreen().setSandFrictionKinetic(Double.parseDouble(sandFrictionKineticField.getText()));
                    sandFrictionKineticLabel.setText("Sand Kinetic Friction:");
                } catch (Exception e) {
                    sandFrictionKineticLabel.setText("Invalid Kinetic Friction");
                }
            }
        });
    
        sandFrictionStaticField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    game.getGolfGameScreen().setSandFrictionStatic(Double.parseDouble(sandFrictionStaticField.getText()));
                    sandFrictionStaticLabel.setText("Sand Static Friction:");
                } catch (Exception e) {
                    sandFrictionStaticLabel.setText("Invalid Static Friction");
                }
            }
        });
    
        // Add sand friction controls to boundary table
        boundaryTable.add(sandFrictionKineticLabel).colspan(3).padTop(10).row();
        boundaryTable.add(sandFrictionKineticField).width(100).height(50).pad(10).colspan(3).row();
        boundaryTable.add(sandFrictionStaticLabel).colspan(3).padTop(10).row();
        boundaryTable.add(sandFrictionStaticField).width(100).height(50).pad(10).colspan(3).row();
    
        // Add the boundaryTable to the mainTable
        mainTable.add(boundaryTable).expand().center();
    
        // For existing sandboxes
        for (Sandbox sandbox : game.getSandboxes()) {
            sandboxInfoTable.add(new Label("Sandbox: " + sandbox.toString(), skin)).pad(10).row();
        }
    
        // Place the sandboxInfoTable at the bottom right
        mainTable.add(sandboxInfoTable).expand().bottom().right().pad(10);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen
        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // Implement if there's a need to handle the game pausing
    }

    @Override
    public void resume() {
        // Implement if there's a need to handle the game resuming
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
        skin.dispose();
    }
}
