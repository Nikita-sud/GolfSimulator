package com.example.golfgame.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

import java.util.ArrayList;
import java.util.List;

import com.example.golfgame.GolfGame;
import com.example.golfgame.utils.*;
import com.example.golfgame.physics.*;
import com.example.golfgame.physics.ODE.*;



/**
 * The GolfGameScreen class implements the game screen for a 3D golf game,
 * handling rendering, input, and game state management. It integrates a physics engine,
 * dynamic lighting, and user input to create an interactive game experience.
 */
public class GolfGameScreen implements Screen, Disposable {
    @SuppressWarnings("unused")
    private Function heightFunction;
    private GolfGame mainGame;
    private PerspectiveCamera mainCamera;
    private ModelBatch mainModelBatch;
    private Model golfBallModel;
    private ModelInstance golfBallInstance;
    private List<ModelInstance> golfCourseInstances;
    private List<ModelInstance> sandInstances;
    private ModelInstance waterSurface;
    private TerrainManager terrainManager;
    private WaterSurfaceManager waterSurfaceManager;
    private Environment gameEnvironment;
    private CameraInputController cameraController;
    private DirectionalShadowLight mainShadowLight;
    private ModelBatch shadowModelBatch;
    private PhysicsEngine gamePhysicsEngine;
    private BallState currentBallState;
    private Texture grassTexture;
    private Texture sandTexture;
    private Function terrainHeightFunction;
    private float cameraDistance = 10;
    private float cameraViewAngle = 0;
    private float ballSpeed = 10;
    private AssetManager assetManager;
    private float terrainCenterX = 0;
    private float terrainCenterZ = 0;
    private Weather weather = new Weather(0);
    private Stage stage;
    private Label facingLabel;
    private Float sunlight;
    private boolean isPaused = false;
    private Dialog pauseDialog;
    private Skin skin;
    private double grassFrictionKinetic;
    private double grassFrictionStatic;
    private double sandFrictionKinetic;
    private double sandFrictionStatic;
    private float lowSpeedThreshold = 0.005f;
    private List<BallState> ballPositionsWhenSlow = new ArrayList<BallState>();
    private Label scoreLabel;
    private BallState lastValidState;
    private int score;
    private boolean isBallAllowedToMove = false;



    /**
     * Constructs a new GolfGameScreen with necessary dependencies.
     *
     * @param game the main game control object, handling overall game state and logic
     * @param assetManager the asset manager to load and manage game assets
     * @throws IllegalArgumentException if assetManager is null
     */
    public GolfGameScreen(GolfGame game, AssetManager assetManager) {
        if (assetManager == null) {
            throw new IllegalArgumentException("AssetManager must not be null");
        }
        this.mainGame = game;
        this.assetManager = assetManager;
        loadAssets();
    }

    /**
     * Loads necessary textures and models from assets, ensuring all graphical assets are ready before use.
     */
    private void loadAssets() {
        assetManager.load("textures/grassTexture.jpeg", Texture.class);
        assetManager.load("textures/sandTexture.jpeg", Texture.class);
        assetManager.load("models/sphere.obj", Model.class);
        assetManager.finishLoading();
    }

    /**
     * Initializes game components such as models, environment, and camera settings.
     */
    public void initializeComponents() {
        if(isPaused){
            pauseGame();
        }
        skin = new Skin(Gdx.files.internal("assets/uiskin.json")); // Initialize skin first
    
        pauseDialog = new Dialog("", skin, "dialog") { // Now use skin to initialize Dialog
            public void result(Object obj) {
                if ((Boolean) obj) {
                    pauseGame(); // Toggle pause
                } else {
                    mainGame.setScreen(mainGame.getMenuScreen());
                }
            }
        };
        pauseDialog.text("Game Paused");
        pauseDialog.button("Resume", true); // Resume button
        pauseDialog.button("Back to Main Menu", false); // Go to main menu button
        pauseDialog.hide(); // Hide the dialog initially

        mainModelBatch = new ModelBatch();
        grassTexture = assetManager.get("textures/grassTexture.jpeg", Texture.class);
        sandTexture = assetManager.get("textures/sandTexture.jpeg", Texture.class);
        golfBallModel = assetManager.get("models/sphere.obj", Model.class);
        terrainHeightFunction = mainGame.getSettingsScreen().getCurHeightFunction();
        ODE solver = new RungeKutta();
        currentBallState = new BallState(0, 0, 0.001, 0.001);
        gamePhysicsEngine = new PhysicsEngine(solver, terrainHeightFunction);

        score =0;
        scoreLabel = new Label("Score: "+score, skin);
        ballPositionsWhenSlow.clear();
        lastValidState = currentBallState.copy();

        grassFrictionKinetic = 0.1;
        grassFrictionStatic = 0.2;
        sandFrictionKinetic = 0.7;
        sandFrictionStatic = 1;
    
        terrainManager = new TerrainManager(terrainHeightFunction, grassTexture,sandTexture, 200, 200, 1.0f, 4);
        waterSurfaceManager = new WaterSurfaceManager(200, 200);

        mainCamera = new PerspectiveCamera(100, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mainCamera.position.set(1f, 1f, 1f);
        mainCamera.lookAt(0f, 0f, 0f);
        mainCamera.near = 0.1f;
        mainCamera.far = 300.0f;
        mainCamera.update();
    
        mainShadowLight = new DirectionalShadowLight(2048*2, 2048*2, 500f, 500f, 0.01f, 1000f);
        sunlight = (float)mainGame.getGolfGameScreen().getWeather().getSun();
        mainShadowLight.set(0.8f*sunlight, 0.8f*sunlight, 0.8f*sunlight, -1f, -0.8f, -0.2f);

        // Put sandboxes to specified locations
        for (Sandbox box: mainGame.getSandboxes()){
            terrainManager.addSandArea(new float[]{box.getXLowBound(), box.getXLowBound(), box.getXHighBound(), box.getYHighBound()});
        }
    
        gameEnvironment = new Environment();
        gameEnvironment.add(mainShadowLight);
        gameEnvironment.shadowMap = mainShadowLight;
    
        shadowModelBatch = new ModelBatch(new DepthShaderProvider());
        golfBallInstance = new ModelInstance(golfBallModel);
        for (Material material : golfBallInstance.materials) {
            material.clear();
            material.set(ColorAttribute.createDiffuse(Color.WHITE));
        }
        
        golfCourseInstances = terrainManager.createGrassTerrainModels(0, 0);
        sandInstances = terrainManager.createSandTerrainModels(0, 0);
        waterSurface = waterSurfaceManager.createWaterSurface(0, 0);
    
        cameraController = new CameraInputController(mainCamera);
        Gdx.input.setInputProcessor(cameraController);
    }

    @Override
    public void show() {
        // Create the stage and skin for UI elements
        stage = new Stage(new ScreenViewport());

        // Set up a table for organizing UI elements
        Table table = new Table();
        table.setFillParent(true); // Makes the table fill the parent container
        table.top().right(); // Aligns the contents of the table to the top right corner
        // Create the settings button
        TextButton settingsButton = new TextButton("Settings", skin);
        settingsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(isPaused){
                    pauseGame();
                }
                mainGame.setScreen(mainGame.getSettingsScreen()); // Switch to settings screen
            }
        });

        // Create the pause button
        TextButton pauseButton = new TextButton("Pause", skin);
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                pauseGame(); // This method should handle game pausing
            }
        });

        // Add buttons to the table
        table.add(settingsButton).width(100).height(50).pad(10);
        table.add(pauseButton).width(100).height(50).pad(10);

        // Add the table to the stage
        stage.addActor(table);

        // Create labels for displaying wind information and camera direction
        Label windLabel = new Label("Wind: vx=" + String.format("%.4f", mainGame.getGolfGameScreen().getWeather().getWind()[0]) +
                                    ", vy=" + String.format("%.4f", mainGame.getGolfGameScreen().getWeather().getWind()[1]) +
                                    ", vz=" + String.format("%.4f", mainGame.getGolfGameScreen().getWeather().getWind()[2]), skin);

        facingLabel = new Label("Facing(x,y,z): " + String.format("%.2f", mainCamera.direction.x) + ", " +
                                String.format("%.2f", mainCamera.direction.y) + ", " + String.format("%.2f", mainCamera.direction.z), skin);

        // Add labels to the stage
        Table labelTable = new Table();
        labelTable.setFillParent(true); // Make the table fill the parent container
        labelTable.top().left(); // Align the table content to the top left corner for the facing label

        labelTable.add(scoreLabel).pad(10);

        // Add the facing label in the top left
        labelTable.add(facingLabel).pad(10).top().left();
        labelTable.row(); // Move to the next row in the table

        // Position the wind label at the bottom left
        labelTable.add(windLabel).pad(10).bottom().left().expandY(); // Use expandY to push this label to the bottom

        stage.addActor(labelTable);


        // Set up input processing for the stage and the camera controller
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(cameraController);
        Gdx.input.setInputProcessor(multiplexer);

        // Check and initialize game components
        if (golfCourseInstances == null || golfCourseInstances.isEmpty()) {
            golfCourseInstances = terrainManager.createGrassTerrainModels(0, 0);
            sandInstances = terrainManager.createSandTerrainModels(0, 0);
        }
        resetGameState();
    }


    /**
    * Resets the game state to initial conditions, setting the position and velocity of the golf ball,
    * camera position, and view angles to their starting values.
    */
    protected void resetGameState() {
        currentBallState.setAllComponents(0,0,0.001,0.001);
        reloadTerrain(terrainCenterX, terrainCenterZ); 
        cameraViewAngle = 0;
        cameraDistance = 10;
        mainCamera.position.set(1f, 1f, 1f);
        mainCamera.lookAt(0f, 0f, 0f);
        mainCamera.update();
    }

    /**
     * Processes user inputs to control game elements such as camera angle and zoom.
     */
    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            pauseGame();
        }
        if(!isPaused){
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if (!isBallAllowedToMove) {
                    isBallAllowedToMove = true; // Allow the ball to move
                    currentBallState.setVx(-ballSpeed * Math.cos(cameraViewAngle));
                    currentBallState.setVy(-ballSpeed * Math.sin(cameraViewAngle));
                } else {
                    isBallAllowedToMove = false; // Prevent the ball from moving
                    currentBallState.setVx(0);
                    currentBallState.setVy(0);
                }
            }
            
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                cameraViewAngle += 0.05; // Rotate camera left
                facingLabel.setText("Facing(x,y,z): "+String.format("%.2f", mainCamera.direction.x)+", "+String.format("%.2f", mainCamera.direction.z)+", "+String.format("%.2f", mainCamera.direction.y));
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                cameraViewAngle -= 0.05; // Rotate camera right
                facingLabel.setText("Facing(x,y,z): "+String.format("%.2f", mainCamera.direction.x)+", "+String.format("%.2f", mainCamera.direction.z)+", "+String.format("%.2f", mainCamera.direction.y));
            }
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                cameraDistance = Math.max(5, cameraDistance - 0.1f); // Zoom in
                facingLabel.setText("Facing(x,y,z): "+String.format("%.2f", mainCamera.direction.x)+", "+String.format("%.2f", mainCamera.direction.z)+", "+String.format("%.2f", mainCamera.direction.y));
            }
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                cameraDistance = Math.min(15, cameraDistance + 0.1f); // Zoom out
                facingLabel.setText("Facing(x,y,z): "+String.format("%.2f", mainCamera.direction.x)+", "+String.format("%.2f", mainCamera.direction.z)+", "+String.format("%.2f", mainCamera.direction.y));
            }
        }
        
    }

    @Override
    public void render(float delta) {
        handleInput();
        update(delta);
        draw();
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    /**
     * Updates the state of the game including the physics engine calculations and camera updates.
     *
     * @param deltaTime The time elapsed since the last frame, used for smooth animations and physics calculations.
     */
    private void update(float deltaTime) {
        if (isPaused) return;

        // Only allow ball physics update if it is allowed to move
        if (isBallAllowedToMove) {
            // Update physics engine state
            currentBallState = gamePhysicsEngine.update(currentBallState, deltaTime);
        }
    
        // Update wind effects
        if (Math.abs(currentBallState.getVx()) > 0.01 || Math.abs(currentBallState.getVy()) > 0.01) {
            currentBallState.setVx(weather.getWind()[0] + currentBallState.getVx());
            currentBallState.setVy(weather.getWind()[1] + currentBallState.getVy());
        }
    
        checkAndReloadTerrainAndWaterSurafceIfNeeded();
    
        // Update ball's graphical position
        float ballZ = terrainManager.getTerrainHeight((float) currentBallState.getX(), (float) currentBallState.getY()) + 1f;
        golfBallInstance.transform.setToTranslation((float) currentBallState.getX(), ballZ, (float) currentBallState.getY());
        updateCameraPosition(deltaTime);

        // Check if the ball is on sand
        Vector3 ballPosition = new Vector3((float) currentBallState.getX(), (float) currentBallState.getY(), 0);
        ballPosition.z = terrainManager.getTerrainHeight(ballPosition.x, ballPosition.y);  // Correcting the use of Y as Z
        boolean onSand = terrainManager.isBallOnSand(ballPosition);

        // Check if the ball is not moving significantly
        if (Math.abs(currentBallState.getVx()) <= lowSpeedThreshold && Math.abs(currentBallState.getVy()) <= lowSpeedThreshold) {
            boolean shouldAdd = true;

            // Check if the last position is the same as the current position within a small tolerance
            if (!ballPositionsWhenSlow.isEmpty()) {
                BallState lastPosition = ballPositionsWhenSlow.get(ballPositionsWhenSlow.size() - 1);
                if (onSand? currentBallState.epsilonEquals(lastPosition, 1):currentBallState.epsilonEquals(lastPosition, 0.1)) {
                    shouldAdd = false; // Do not add if positions are the same within the tolerance
                }
            }

            // Add the current state if it is distinct enough to be considered a stop
            if (shouldAdd) {
                ballPositionsWhenSlow.add(new BallState(currentBallState.getX(), currentBallState.getY(), currentBallState.getVx(), currentBallState.getVy()));
                scoreLabel.setText("Score: " + score++);
                isBallAllowedToMove = false;
            }
        }

        // Check if the ball has fallen below ground level
        if (ballZ-1 < 0) {
            // Reset to the last valid position
            currentBallState.setAllComponents(lastValidState.getX(), lastValidState.getY(), lastValidState.getVx(), lastValidState.getVy());
            System.out.println("Ball has fallen below ground level. Resetting to last valid position.");
        } else {
            if (!ballPositionsWhenSlow.isEmpty()) {
                BallState lastPosition = ballPositionsWhenSlow.get(ballPositionsWhenSlow.size() - 1);
                lastValidState.setAllComponents(lastPosition.getX(), lastPosition.getY(), lastPosition.getVx(), lastPosition.getVy());
            }else{
                lastValidState.setAllComponents(currentBallState.getX(), currentBallState.getY(), currentBallState.getVx(), currentBallState.getVy());
            }
        }

    
        // Adjust friction based on terrain
        if (onSand) {
            gamePhysicsEngine.setFriction(sandFrictionKinetic,sandFrictionStatic);
        } else {
            gamePhysicsEngine.setFriction(grassFrictionKinetic,grassFrictionStatic);
        }
    }
    

    private void updateCameraPosition(float delta) {
        float ballZ = terrainManager.getTerrainHeight((float) currentBallState.getX(), (float) currentBallState.getY()) + 1f;
        float cameraX = (float) (currentBallState.getX() + cameraDistance * Math.cos(cameraViewAngle));
        float cameraY = (float) (currentBallState.getY() + cameraDistance * Math.sin(cameraViewAngle));
        mainCamera.position.set(cameraX, ballZ + 5f, cameraY);
        mainCamera.lookAt((float) currentBallState.getX(), ballZ + 1f, (float) currentBallState.getY());
        mainCamera.up.set(Vector3.Y);
        mainCamera.update();
    }

    /**
     * Checks if the terrain needs to be reloaded based on the ball's position to ensure the playing area remains centered on the ball.
     */
    private void checkAndReloadTerrainAndWaterSurafceIfNeeded() {
        Vector3 ballPosition = new Vector3((float) currentBallState.getX(), (float) currentBallState.getY(), 0);
        Vector3 terrainCenter = new Vector3(terrainCenterX, terrainCenterZ, 0);

        if (ballPosition.dst(terrainCenter) > 20) { // Threshold distance to trigger terrain reload
            reloadTerrain(ballPosition.x, ballPosition.y);
            waterSurface = waterSurfaceManager.createWaterSurface(terrainCenterX, terrainCenterZ);
        }
    }

    /**
     * Reloads the terrain around a new center position when the ball moves beyond a certain threshold from the current center.
     *
     * @param x the new x-coordinate center for the terrain
     * @param y the new y-coordinate center for the terrain
     */
    private void reloadTerrain(float x, float y) {
        terrainCenterX = x;
        terrainCenterZ = y;
        golfCourseInstances.clear();
        golfCourseInstances = terrainManager.createGrassTerrainModels(x, y);
        sandInstances = terrainManager.createSandTerrainModels(x, y);
        System.out.println("Terrain reloaded around position: " + x + ", " + y);
    }

    /**
     * Draws the game scene, including the golf ball and terrain, using the appropriate shaders and lighting.
     */
    private void draw() {
        Gdx.gl.glClearColor(0.5f, 0.7f, 1.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        cameraController.update();
        
        mainShadowLight.begin(Vector3.Zero, mainCamera.direction);
        shadowModelBatch.begin(mainShadowLight.getCamera());
        
        // Render all shadows here
        for (ModelInstance terrainInstance : golfCourseInstances) {
            shadowModelBatch.render(terrainInstance, gameEnvironment);
        }
        for (ModelInstance sandInstance : sandInstances) {
            shadowModelBatch.render(sandInstance, gameEnvironment);
        }
        shadowModelBatch.render(golfBallInstance, gameEnvironment);
        
        shadowModelBatch.end();
        mainShadowLight.end();
    
        // Render all visible models here
        mainModelBatch.begin(mainCamera);
        for (ModelInstance terrainInstance : golfCourseInstances) {
            mainModelBatch.render(terrainInstance, gameEnvironment);
        }
        for (ModelInstance sandInstance : sandInstances) {
            mainModelBatch.render(sandInstance, gameEnvironment);
        }
        mainModelBatch.render(golfBallInstance, gameEnvironment);
        mainModelBatch.render(waterSurface, gameEnvironment);
        mainModelBatch.end();
    }
    

    @Override
    public void resize(int width, int height) {
        mainCamera.viewportWidth = width;
        mainCamera.viewportHeight = height;
        mainCamera.update();
        stage.getViewport().update(width, height, true);
    }

    public void setHeightFunction(Function newHeightFunction){
        this.heightFunction = newHeightFunction;
    }

    public void setWeather(Weather newWeather){
        weather = newWeather;
    }

    public Weather getWeather(){
        return weather;
    }

    private void pauseGame() {
        isPaused = !isPaused; 
        if (isPaused) {
            pauseDialog.show(stage);
        } else {
            pauseDialog.hide();
        }
    }
    
    
    @Override
    public void pause() {
        // This method would contain logic to handle the game pausing.
    }

    @Override
    public void resume() {
        // This method would handle logic to resume the game after pausing.
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        Gdx.app.log("GolfGameScreen", "Disposing screen");
        mainModelBatch.dispose();
        mainShadowLight.dispose();
        shadowModelBatch.dispose();
    }

}
