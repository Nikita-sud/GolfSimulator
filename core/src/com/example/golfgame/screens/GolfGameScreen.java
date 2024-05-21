package com.example.golfgame.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
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
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
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
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.golfgame.GolfGame;
import com.example.golfgame.utils.*;
import com.example.golfgame.utils.animations.FlagAnimation;
import com.example.golfgame.utils.animations.WaterAnimation;
import com.example.golfgame.physics.*;
import com.example.golfgame.physics.ODE.*;

/**
 * Represents the main game screen where the golf game takes place.
 * Handles the rendering of game models, user input, game state updates,
 * and interactions.
 */
public class GolfGameScreen implements Screen, Disposable {
    @SuppressWarnings("unused")
    private Function heightFunction;
    private GolfGame mainGame;
    private PerspectiveCamera mainCamera;
    private ModelBatch mainModelBatch;
    private Model golfBallModel;
    private Model flagModel;
    private Model flagStemModel;
    private ModelInstance golfBallInstance;
    private ModelInstance flagInstance;
    private ModelInstance flagStemInstance;
    private List<ModelInstance> golfCourseInstances;
    private List<ModelInstance> sandInstances;
    private ModelInstance holeInstance;
    private List<ModelInstance> waterSurfaces; // Change from a single instance to a list
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
    private Texture holeTexture;
    private Function terrainHeightFunction;
    private float cameraDistance = 10;
    private float cameraViewAngle = 0;
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
    private Label lastScoreLabel;
    private BallState lastValidState;
    private int score;
    private int lastScore;
    private boolean isBallAllowedToMove = false;
    private Music music;
    private BallState goalState = new BallState(-20, 20, 0, 0);
    private static final float GOAL_TOLERANCE = 1f;
    private FlagAnimation flagAnimation;
    private List<WaterAnimation> waterAnimations; // Change from a single instance to a list
    private boolean isAdjustingSpeed = false;
    private float minSpeed = 0.01f;
    private float maxSpeed = 10.0f;
    private float speedAdjustmentRate = 10.0f;
    private float currentSpeed = 0.01f;
    private ProgressBar speedProgressBar;

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
        this.stage = new Stage(new ScreenViewport()); 
        loadAssets();
    }

    /**
     * Loads necessary textures and models from assets, ensuring all graphical assets are ready before use.
     */
    private void loadAssets() {
        assetManager.load("textures/grassTexture.jpeg", Texture.class);
        assetManager.load("textures/sandTexture.jpeg", Texture.class);
        assetManager.load("textures/holeTexture.png", Texture.class);
        assetManager.load("models/sphere.obj", Model.class);
        assetManager.load("models/flag.obj", Model.class);
        assetManager.load("models/flagStem.obj", Model.class);
        assetManager.finishLoading();
    }

    /**
     * Initializes game components such as models, environment, and camera settings.
     */
    public void initializeComponents() {
        if (isPaused) {
            pauseGame();
        }
        skin = new Skin(Gdx.files.internal("assets/uiskin.json")); 
    
        pauseDialog = new Dialog("", skin, "dialog") { 
            public void result(Object obj) {
                if ((Boolean) obj) {
                    pauseGame(); 
                } else {
                    mainGame.setScreen(mainGame.getMenuScreen());
                }
            }
        };
        pauseDialog.text("Game Paused");
        pauseDialog.button("Resume", true); 
        pauseDialog.button("Back to Main Menu", false); 
        pauseDialog.hide(); 
    
        mainModelBatch = new ModelBatch();
        grassTexture = assetManager.get("textures/grassTexture.jpeg", Texture.class);
        sandTexture = assetManager.get("textures/sandTexture.jpeg", Texture.class);
        holeTexture = assetManager.get("textures/holeTexture.png", Texture.class);
        golfBallModel = assetManager.get("models/sphere.obj", Model.class);
        flagModel = assetManager.get("models/flag.obj", Model.class);
        flagStemModel = assetManager.get("models/flagStem.obj", Model.class);
        music = assetManager.get("assets/music/game-screen.mp3");
        terrainHeightFunction = mainGame.getSettingsScreen().getCurHeightFunction();
        ODE solver = new RungeKutta();
        currentBallState = new BallState(0, 0, 0.001, 0.001);
        gamePhysicsEngine = new PhysicsEngine(solver, terrainHeightFunction);
    
        score = 0;
        lastScore = -1;
        scoreLabel = new Label("Score: " + score, skin);
        lastScoreLabel = new Label("Last Score: " + lastScore, skin);
        ballPositionsWhenSlow.clear();
        lastValidState = currentBallState.copy();
    
        grassFrictionKinetic = 0.1;
        grassFrictionStatic = 0.2;
        sandFrictionKinetic = 0.7;
        sandFrictionStatic = 1;
    
        terrainManager = new TerrainManager(terrainHeightFunction, grassTexture, sandTexture, holeTexture, 200, 200, 1.0f, 4);
        waterSurfaceManager = new WaterSurfaceManager(200.0f, 200.0f, 50); 
    
        mainCamera = new PerspectiveCamera(100, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mainCamera.position.set(1f, 1f, 1f);
        mainCamera.lookAt(0f, 0f, 0f);
        mainCamera.near = 0.1f;
        mainCamera.far = 300.0f;
        mainCamera.update();
    
        mainShadowLight = new DirectionalShadowLight(2048 * 2, 2048 * 2, 500f, 500f, 0.01f, 1000f);
        sunlight = (float) mainGame.getGolfGameScreen().getWeather().getSun();
        mainShadowLight.set(0.8f * sunlight, 0.8f * sunlight, 0.8f * sunlight, -1f, -0.8f, -0.2f);
    
        for (Sandbox box : mainGame.getSandboxes()) {
            terrainManager.addSandArea(new float[]{box.getXLowBound(), box.getXLowBound(), box.getXHighBound(), box.getYHighBound()});
        }
    
        terrainManager.setHoleArea(new float[]{(float) goalState.getX(), (float) goalState.getY()});
    
        gameEnvironment = new Environment();
        gameEnvironment.add(mainShadowLight);
        gameEnvironment.shadowMap = mainShadowLight;
    
        shadowModelBatch = new ModelBatch(new DepthShaderProvider());
        golfBallInstance = new ModelInstance(golfBallModel);
        flagInstance = new ModelInstance(flagModel);
        flagStemInstance = new ModelInstance(flagStemModel);
    
        for (Material material : golfBallInstance.materials) {
            material.clear();
            material.set(ColorAttribute.createDiffuse(Color.WHITE));
        }
        IntAttribute cullFaceAttribute = new IntAttribute(IntAttribute.CullFace, GL20.GL_NONE);
    
        for (Material material : flagInstance.materials) {
            material.clear();
            material.set(cullFaceAttribute);
            material.set(ColorAttribute.createDiffuse(Color.RED));
        }
    
        flagInstance.transform.setToTranslation((float) goalState.getX(), (float) terrainHeightFunction.evaluate(new HashMap<String, Double>() {{
            put("x", goalState.getX());
            put("y", goalState.getY());
        }}), (float) goalState.getY());
        for (Material material : flagStemInstance.materials) {
            material.clear();
            material.set(ColorAttribute.createDiffuse(Color.WHITE));
        }
        flagStemInstance.transform.setToTranslation((float) goalState.getX(), (float) terrainHeightFunction.evaluate(new HashMap<String, Double>() {{
            put("x", goalState.getX());
            put("y", goalState.getY());
        }}), (float) goalState.getY());
    
        flagAnimation = new FlagAnimation(flagInstance);
        golfCourseInstances = terrainManager.createGrassTerrainModels(0, 0);
        sandInstances = terrainManager.createSandTerrainModels(0, 0);
        holeInstance = terrainManager.createHoleTerrainModel(0, 0);
    
        waterSurfaces = waterSurfaceManager.createWaterSurface(0, 0); 
        waterAnimations = new ArrayList<>();
        for (ModelInstance waterSurface : waterSurfaces) {
            waterAnimations.add(new WaterAnimation(waterSurface));
        }

        speedProgressBar = new ProgressBar(minSpeed, maxSpeed, 0.1f, true, skin,"progress-bar");
        speedProgressBar.setValue(currentSpeed);
        speedProgressBar.setVisible(true); 
    
        Table uiTable = new Table();
        uiTable.setFillParent(true);
        uiTable.right().padRight(20); 
        uiTable.add(speedProgressBar).width(40).height(600).pad(10);
        stage.addActor(uiTable);
    
        cameraController = new CameraInputController(mainCamera);
        Gdx.input.setInputProcessor(cameraController);
    }

    @Override
    public void show() {
        // Stop any previously playing music to prevent overlapping
        if (music.isPlaying()) {
            music.stop();
        }

        // Reset the stage to clear any previously added actors to avoid overlays
        stage.clear();

        // Initialize UI components
        initializeUIComponents();

        // Play background music
        music.setLooping(true);
        music.play();

        // Initialize input processors
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(cameraController);
        Gdx.input.setInputProcessor(multiplexer);

        // Reload terrain and reset game state if necessary
        if (golfCourseInstances == null || golfCourseInstances.isEmpty()) {
            reloadTerrainAndResetState();
        }
    }

    private void initializeUIComponents() {
        Table table = new Table();
        table.setFillParent(true);
        table.top().right();
        TextButton settingsButton = new TextButton("Settings", skin);
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isPaused) {
                    pauseGame();
                }
                mainGame.setScreen(mainGame.getSettingsScreen());
            }
        });

        TextButton pauseButton = new TextButton("Pause", skin);
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                pauseGame();
            }
        });

        table.add(settingsButton).width(100).height(50).pad(10);
        table.add(pauseButton).width(100).height(50).pad(10);
        stage.addActor(table);

        Label windLabel = new Label("Wind: vx=" + String.format("%.4f", mainGame.getGolfGameScreen().getWeather().getWind()[0]) +
                ", vy=" + String.format("%.4f", mainGame.getGolfGameScreen().getWeather().getWind()[1]) +
                ", vz=" + String.format("%.4f", mainGame.getGolfGameScreen().getWeather().getWind()[2]), skin);

        facingLabel = new Label("Facing(x,y,z): " + String.format("%.2f", mainCamera.direction.x) + ", " +
                String.format("%.2f", mainCamera.direction.y) + ", " + String.format("%.2f", mainCamera.direction.z), skin);

        scoreLabel = new Label("Score: " + score, skin);
        lastScoreLabel = new Label("Last Score: " + lastScore, skin);

        Table labelTable = new Table();
        labelTable.setFillParent(true);
        labelTable.top().left();
        labelTable.add(scoreLabel).pad(10);
        labelTable.add(lastScoreLabel).pad(10);
        labelTable.add(facingLabel).pad(10).top().left();
        labelTable.row();
        labelTable.add(windLabel).pad(10).bottom().left().expandY();
        stage.addActor(labelTable);

        Table uiTable = new Table();
        uiTable.setFillParent(true);
        uiTable.right().padRight(20);
        uiTable.add(speedProgressBar).width(40).height(600).pad(10);
        stage.addActor(uiTable);
    }

    private void reloadTerrainAndResetState() {
        golfCourseInstances = terrainManager.createGrassTerrainModels(0, 0);
        sandInstances = terrainManager.createSandTerrainModels(0, 0);
        holeInstance = terrainManager.createHoleTerrainModel(0, 0);
        waterSurfaces = waterSurfaceManager.createWaterSurface(0, 0);
        waterAnimations.clear();
        for (ModelInstance waterSurface : waterSurfaces) {
            waterAnimations.add(new WaterAnimation(waterSurface));
        }
        resetGameState();
    }


    /**
     * Resets the game state to initial conditions, setting the position and velocity of the golf ball,
     * camera position, and view angles to their starting values.
     */
    public void resetGameState() {
        currentBallState.setAllComponents(0, 0, 0.001, 0.001);
        reloadTerrain(terrainCenterX, terrainCenterZ);
        cameraViewAngle = 0;
        cameraDistance = 10;
        mainCamera.position.set(1f, 1f, 1f);
        mainCamera.lookAt(0f, 0f, 0f);
        mainCamera.update();
        score = 0;
        scoreLabel.clear();
        scoreLabel.setText("Score: " + score);
    }

    /**
     * Processes user inputs to control game elements such as camera angle and zoom.
     */
    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            pauseGame();
        }
        if (!isPaused) {
            if (!isBallAllowedToMove) {
                if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                    isAdjustingSpeed = true;
                } else {
                    if (isAdjustingSpeed) {
                        isAdjustingSpeed = false;
                        if (!isBallAllowedToMove) {
                            isBallAllowedToMove = true;
                            currentBallState.setVx(-currentSpeed * Math.cos(cameraViewAngle));
                            currentBallState.setVy(-currentSpeed * Math.sin(cameraViewAngle));
                        }
                    }
                }
            }

            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                cameraViewAngle += 0.05; 
                facingLabel.setText("Facing(x,y,z): " + String.format("%.2f", mainCamera.direction.x) + ", " + String.format("%.2f", mainCamera.direction.z) + ", " + String.format("%.2f", mainCamera.direction.y));
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                cameraViewAngle -= 0.05; 
                facingLabel.setText("Facing(x,y,z): " + String.format("%.2f", mainCamera.direction.x) + ", " + String.format("%.2f", mainCamera.direction.z) + ", " + String.format("%.2f", mainCamera.direction.y));
            }
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                cameraDistance = Math.max(5, cameraDistance - 0.1f); 
                facingLabel.setText("Facing(x,y,z): " + String.format("%.2f", mainCamera.direction.x) + ", " + String.format("%.2f", mainCamera.direction.z) + ", " + String.format("%.2f", mainCamera.direction.y));
            }
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                cameraDistance = Math.min(15, cameraDistance + 0.1f); 
                facingLabel.setText("Facing(x,y,z): " + String.format("%.2f", mainCamera.direction.x) + ", " + String.format("%.2f", mainCamera.direction.z) + ", " + String.format("%.2f", mainCamera.direction.y));
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


        if (isAdjustingSpeed) {
            currentSpeed += speedAdjustmentRate * deltaTime;
            if (currentSpeed > maxSpeed) {
                currentSpeed = maxSpeed;
                speedAdjustmentRate = -speedAdjustmentRate; 
            } else if (currentSpeed < minSpeed) {
                currentSpeed = minSpeed;
                speedAdjustmentRate = -speedAdjustmentRate; 
            }
            speedProgressBar.setValue(currentSpeed);
        }

        if (currentBallState.epsilonPositionEquals(goalState, GOAL_TOLERANCE)) {
            lastScore = score;
            score = 0;
            scoreLabel.clear();
            scoreLabel.setText("Score: " + score);
            lastScoreLabel.setText("Last Score: " + lastScore);
            isBallAllowedToMove = false;
            resetGameState();
        }
        if (isBallAllowedToMove) {
            currentBallState = gamePhysicsEngine.update(currentBallState, deltaTime);
        }

        if (Math.abs(currentBallState.getVx()) > 0.01 || Math.abs(currentBallState.getVy()) > 0.01) {
            currentBallState.setVx(weather.getWind()[0] + currentBallState.getVx());
            currentBallState.setVy(weather.getWind()[1] + currentBallState.getVy());
        }

        checkAndReloadTerrainAndWaterSurfaceIfNeeded();

        flagAnimation.update(deltaTime);
        for (WaterAnimation waterAnimation : waterAnimations) {
            waterAnimation.update(deltaTime);
        }

        float ballZ = terrainManager.getTerrainHeight((float) currentBallState.getX(), (float) currentBallState.getY()) + 1f;
        golfBallInstance.transform.setToTranslation((float) currentBallState.getX(), ballZ, (float) currentBallState.getY());
        updateCameraPosition(deltaTime);

        Vector3 ballPosition = new Vector3((float) currentBallState.getX(), (float) currentBallState.getY(), 0);
        ballPosition.z = terrainManager.getTerrainHeight(ballPosition.x, ballPosition.y);  
        boolean onSand = terrainManager.isBallOnSand(ballPosition);

        if (onSand) {
            lowSpeedThreshold = 0.05f;
        } else {
            lowSpeedThreshold = 0.005f;
        }

        if (Math.abs(currentBallState.getVx()) <= lowSpeedThreshold && Math.abs(currentBallState.getVy()) <= lowSpeedThreshold) {
            boolean shouldAdd = true;

            if (!ballPositionsWhenSlow.isEmpty()) {
                BallState lastPosition = ballPositionsWhenSlow.get(ballPositionsWhenSlow.size() - 1);
                if (onSand ? currentBallState.epsilonEquals(lastPosition, 0.5) : currentBallState.epsilonEquals(lastPosition, 0.1)) {
                    shouldAdd = false; 
                }
            }

            if (shouldAdd) {
                ballPositionsWhenSlow.add(new BallState(currentBallState.getX(), currentBallState.getY(), currentBallState.getVx(), currentBallState.getVy()));
                scoreLabel.clear();
                scoreLabel.setText("Score: " + score++);
                isBallAllowedToMove = false;
            }
        }

        if (ballZ - 1 < 0) {
            scoreLabel.setText("Score: " + score++);
            isBallAllowedToMove = false;
            currentBallState.setAllComponents(lastValidState.getX(), lastValidState.getY(), lastValidState.getVx(), lastValidState.getVy());
            System.out.println("Ball has fallen below ground level. Resetting to last valid position.");
        } else {
            if (!ballPositionsWhenSlow.isEmpty()) {
                BallState lastPosition = ballPositionsWhenSlow.get(ballPositionsWhenSlow.size() - 1);
                lastValidState.setAllComponents(lastPosition.getX(), lastPosition.getY(), lastPosition.getVx(), lastPosition.getVy());
            } else {
                lastValidState.setAllComponents(currentBallState.getX(), currentBallState.getY(), currentBallState.getVx(), currentBallState.getVy());
            }
        }

        if (onSand) {
            gamePhysicsEngine.setFriction(sandFrictionKinetic, sandFrictionStatic);
        } else {
            gamePhysicsEngine.setFriction(grassFrictionKinetic, grassFrictionStatic);
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
    private void checkAndReloadTerrainAndWaterSurfaceIfNeeded() {
        Vector3 ballPosition = new Vector3((float) currentBallState.getX(), (float) currentBallState.getY(), 0);
        Vector3 terrainCenter = new Vector3(terrainCenterX, terrainCenterZ, 0);

        if (ballPosition.dst(terrainCenter) > 20) { 
            reloadTerrain(ballPosition.x, ballPosition.y);
            waterSurfaces = waterSurfaceManager.createWaterSurface(terrainCenterX, terrainCenterZ); 
            waterAnimations.clear();
            for (ModelInstance waterSurface : waterSurfaces) {
                waterAnimations.add(new WaterAnimation(waterSurface));
            }
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

        for (ModelInstance terrainInstance : golfCourseInstances) {
            shadowModelBatch.render(terrainInstance, gameEnvironment);
        }
        for (ModelInstance sandInstance : sandInstances) {
            shadowModelBatch.render(sandInstance, gameEnvironment);
        }
        shadowModelBatch.render(holeInstance, gameEnvironment);
        shadowModelBatch.render(golfBallInstance, gameEnvironment);
        shadowModelBatch.render(flagStemInstance, gameEnvironment);
        shadowModelBatch.render(flagInstance, gameEnvironment);
        shadowModelBatch.end();
        mainShadowLight.end();

        mainModelBatch.begin(mainCamera);
        for (ModelInstance terrainInstance : golfCourseInstances) {
            mainModelBatch.render(terrainInstance, gameEnvironment);
        }
        for (ModelInstance sandInstance : sandInstances) {
            mainModelBatch.render(sandInstance, gameEnvironment);
        }
        mainModelBatch.render(holeInstance, gameEnvironment);
        mainModelBatch.render(golfBallInstance, gameEnvironment);
        mainModelBatch.render(flagStemInstance, gameEnvironment);
        mainModelBatch.render(flagInstance, gameEnvironment);
        for (ModelInstance waterSurface : waterSurfaces) {
            mainModelBatch.render(waterSurface, gameEnvironment);
        }
        mainModelBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        mainCamera.viewportWidth = width;
        mainCamera.viewportHeight = height;
        mainCamera.update();
        stage.getViewport().update(width, height, true);

        speedProgressBar.setWidth(width * 0.001f); 
        speedProgressBar.setHeight(height * 0.05f);
    }

    public void setHeightFunction(Function newHeightFunction) {
        this.heightFunction = newHeightFunction;
    }

    public void setWeather(Weather newWeather) {
        weather = newWeather;
    }

    public Weather getWeather() {
        return weather;
    }

    public void setGoalCoords(float[] coords) {
        goalState.setX(coords[0]);
        goalState.setY(coords[1]);
        flagInstance.transform.setToTranslation(coords[0], (float) terrainHeightFunction.evaluate(new HashMap<String, Double>() {{
            put("x", (double) coords[0]);
            put("y", (double) coords[1]);
        }}), coords[1]);
        flagStemInstance.transform.setToTranslation(coords[0], (float) terrainHeightFunction.evaluate(new HashMap<String, Double>() {{
            put("x", (double) coords[0]);
            put("y", (double) coords[1]);
        }}), coords[1]);
    }

    public static float getGoalTolerance() {
        return GOAL_TOLERANCE;
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
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        dispose();
        music.stop();
    }

    @Override
    public void dispose() {
        Gdx.app.log("GolfGameScreen", "Disposing screen");
        mainModelBatch.dispose();
        mainShadowLight.dispose();
        shadowModelBatch.dispose();
    }
}
