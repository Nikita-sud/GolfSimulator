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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.Camera;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.WallE;
import com.example.golfgame.bot.botsbehaviors.HillClimbingBot;
import com.example.golfgame.utils.*;
import com.example.golfgame.utils.animations.FlagAnimation;
import com.example.golfgame.utils.animations.WaterAnimation;
import com.example.golfgame.utils.gameUtils.Sandbox;
import com.example.golfgame.utils.gameUtils.TerrainManager;
import com.example.golfgame.utils.gameUtils.WaterSurfaceManager;
import com.example.golfgame.utils.gameUtils.Weather;
import com.example.golfgame.physics.*;
import com.example.golfgame.physics.ODE.*;
import com.example.golfgame.simulator.PhysicsSimulator;

/**
 * Represents the main game screen where the golf game takes place.
 * Handles the rendering of game models, user input, game state updates,
 * and interactions.
 */
public class GolfGameScreen implements Screen, Disposable {
    @SuppressWarnings("unused")
    // Constants
    private static final float CAMERA_FOV = 100;
    private static final float MIN_CAMERA_DISTANCE = 5;
    private static final float MAX_CAMERA_DISTANCE = 15;
    private static final float DEFAULT_CAMERA_DISTANCE = 10;
    private static final float CAMERA_HEIGHT = 5f;
    private static final float BALL_HEIGHT_OFFSET = 1f;
    private static final float LOW_SPEED_THRESHOLD_GRASS = 0.008f;
    private static final float LOW_SPEED_THRESHOLD_SAND = 1.0f;
    private static final float MIN_SPEED = 1f;
    private static final float MAX_SPEED = 10f;

    // Core game objects
    private final GolfGame mainGame;
    private final AssetManager assetManager;
    private final Stage stage;

    // Camera and environment
    private PerspectiveCamera mainCamera;
    private CameraInputController cameraController;
    private DirectionalShadowLight mainShadowLight;
    private ModelBatch mainModelBatch;
    private ModelBatch shadowModelBatch;
    private Environment gameEnvironment;

    // Models and instances
    private Model golfBallModel, flagModel, flagStemModel;
    private ModelInstance golfBallInstance, flagInstance, flagStemInstance, lineInstance;
    private List<ModelInstance> golfCourseInstances, sandInstances, waterSurfaces;
    private ModelInstance holeInstance;

    // Physics and terrain
    private PhysicsEngine gamePhysicsEngine;
    private TerrainManager terrainManager;
    private WaterSurfaceManager waterSurfaceManager;
    private Function terrainHeightFunction;
    private BallState currentBallState, lastValidState, goalState = new BallState(-20, 20, 0, 0);
    private static float GOAL_TOLERANCE = 1.5f;
    private double grassFrictionKinetic, grassFrictionStatic;
    private double sandFrictionKinetic = 0.7;
    private double sandFrictionStatic = 1;
    private float lowSpeedThreshold = LOW_SPEED_THRESHOLD_GRASS;
    private List<BallState> ballPositionsWhenSlow;

    // UI components
    private Skin skin;
    private Label facingLabel, scoreLabel, lastScoreLabel, ballMovementLabel;
    private ProgressBar speedProgressBar;
    private Dialog pauseDialog;
    private List<Vector2> path;

    // Animations
    private FlagAnimation flagAnimation;
    private List<WaterAnimation> waterAnimations;

    // Game state
    // private GolfEnvironment environment;
    private boolean isPaused = false;
    private boolean isAdjustingSpeed = false;
    private boolean isBallAllowedToMove = false;
    private boolean isBallInWater = false;
    private float currentSpeed = MIN_SPEED;
    private float speedAdjustmentRate = 10.0f;
    private int score = 0, lastScore = -1;
    private float cameraDistance = DEFAULT_CAMERA_DISTANCE;
    private float cameraViewAngle = 0;
    private boolean ruleBasedBotActive = false;
    private boolean hillClimbingBotActive = false;
    private float ballRotationAngleX = 0f;
    private float ballRotationAngleY = 0f;
    private final float ballRadius = 1f; // радиус мяча в метрах

    // Light settings
    private float sunlight;

    // Weather
    private Weather weather = new Weather(0);

    // Music
    private Music music;

    // Bots
    private WallE wallE;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> ruleBasedBotFuture = null;

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
        this.currentBallState = new BallState(0,0, 0.001, 0.001);
        // this.environment = new GolfEnvironment(terrainManager, currentBallState, goalState);
        loadAssets();
    }

    /**
     * Loads necessary textures and models from assets, ensuring all graphical assets are ready before use.
     */
    private void loadAssets() {
        assetManager.load("textures/grassTexture.jpeg", Texture.class);
        assetManager.load("textures/sandTexture.jpeg", Texture.class);
        assetManager.load("textures/holeTexture.png", Texture.class);
        assetManager.load("textures/redLine.png", Texture.class);
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

        // Initialize Skin and Dialog
        initializeSkinAndDialog();

        // Initialize Models and Assets
        initializeModelsAndAssets();

        // Initialize Physics Engine and Game State
        initializePhysicsAndGameState();

        // Initialize Terrain
        initializeTerrain();

        initializeBot();

        // Initialize Camera and Light
        initializeCameraAndLight();

        // Initialize Game Environment
        initializeGameEnvironment();

        // Set Input Processor
        setInputProcessor();
    }

    /**
     * Initializes the bot for the game.
     */
    private void initializeBot(){
        this.wallE = new WallE(mainGame);
    }

    /**
     * Initializes the skin and dialog for the UI.
     */
    private void initializeSkinAndDialog() {
        skin = new Skin(Gdx.files.internal("assets/uiskin.json"));
        pauseDialog = new Dialog("", skin, "dialog") {
            public void result(Object obj) {
                if ((Boolean) obj) {
                    pauseGame();
                } else {
                    isBallAllowedToMove = false;
                    mainGame.switchToMenu();
                }
            }
        };
        pauseDialog.text("Game Paused");
        pauseDialog.button("Resume", true);
        pauseDialog.button("Back to Main Menu", false);
        pauseDialog.hide();
    }

    /**
     * Initializes the models and assets for the game.
     */
    private void initializeModelsAndAssets() {
        mainModelBatch = new ModelBatch();
        golfBallModel = assetManager.get("models/sphere.obj", Model.class);
        flagModel = assetManager.get("models/flag.obj", Model.class);
        flagStemModel = assetManager.get("models/flagStem.obj", Model.class);
        music = assetManager.get("assets/music/game-screen.mp3");
    }

    /**
     * Initializes the physics engine and game state.
     */
    private void initializePhysicsAndGameState() {
        terrainHeightFunction = mainGame.getSettingsScreen().getCurHeightFunction();
        ODE solver = new RungeKutta();
        gamePhysicsEngine = new PhysicsEngine(solver, terrainHeightFunction);
        score = 0;
        lastScore = -1;
        ballPositionsWhenSlow = new ArrayList<>();
        ballPositionsWhenSlow.clear();
        lastValidState = currentBallState.copy();
        grassFrictionKinetic = 0.1;
        grassFrictionStatic = 0.2;
        ballRotationAngleX = 0f;
        ballRotationAngleY = 0f;
    }

    /**
     * Initializes the terrain for the game.
     */
    private void initializeTerrain() {
        terrainManager = new TerrainManager(terrainHeightFunction,
            assetManager.get("textures/grassTexture.jpeg", Texture.class),
            assetManager.get("textures/sandTexture.jpeg", Texture.class),
            assetManager.get("textures/holeTexture.png", Texture.class),
            assetManager.get("textures/redLine.png", Texture.class),
            200, 200, 1.0f, 4);
        waterSurfaceManager = new WaterSurfaceManager(200.0f, 200.0f, 50);
    }

    /**
     * Initializes the camera and lighting for the game.
     */
    private void initializeCameraAndLight() {
        mainCamera = new PerspectiveCamera(CAMERA_FOV, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mainCamera.position.set(1f, 1f, 1f);
        mainCamera.lookAt(0f, 0f, 0f);
        mainCamera.near = 0.1f;
        mainCamera.far = 300.0f;
        mainCamera.update();

        mainShadowLight = new DirectionalShadowLight(2048 * 2, 2048 * 2, 500f, 500f, 0.01f, 1000f);
        sunlight = (float) mainGame.getGolfGameScreen().getWeather().getSun();
        mainShadowLight.set(0.8f * sunlight, 0.8f * sunlight, 0.8f * sunlight, -1f, -0.8f, -0.2f);
    }

    /**
     * Initializes the game environment.
     */
    private void initializeGameEnvironment() {
        for (Sandbox box : mainGame.getSandboxes()) {
            terrainManager.addSandArea(new float[]{box.getXLowBound(), box.getXLowBound(), box.getXHighBound(), box.getYHighBound()});
        }
        terrainManager.setHoleArea(new float[]{(float) goalState.getX(), (float) goalState.getY()});

        gameEnvironment = new Environment();
        gameEnvironment.add(mainShadowLight);
        gameEnvironment.shadowMap = mainShadowLight;

        shadowModelBatch = new ModelBatch(new DepthShaderProvider());
        golfBallInstance = new ModelInstance(golfBallModel);
        initializeModelInstance(golfBallInstance, Color.WHITE);

        flagInstance = new ModelInstance(flagModel);
        initializeModelInstance(flagInstance, Color.RED, true);

        flagStemInstance = new ModelInstance(flagStemModel);
        initializeModelInstance(flagStemInstance, Color.WHITE);

        setPositionForFlagAndStemInstances();

        flagAnimation = new FlagAnimation(flagInstance);
        golfCourseInstances = terrainManager.createGrassTerrainModels(0, 0);
        sandInstances = terrainManager.createSandTerrainModels(0, 0);
        holeInstance = terrainManager.createHoleTerrainModel(0, 0);
        lineInstance = terrainManager.createRedLineModel(new PhysicsSimulator(terrainHeightFunction, goalState).hitWithPath(10, 0).getValue());
        waterSurfaces = waterSurfaceManager.createWaterSurface(0, 0);
        waterAnimations = new ArrayList<>();
        for (ModelInstance waterSurface : waterSurfaces) {
            waterAnimations.add(new WaterAnimation(waterSurface));
        }
    }

    /**
     * Sets the input processor for the game.
     */
    private void setInputProcessor() {
        cameraController = new CameraInputController(mainCamera);
        Gdx.input.setInputProcessor(cameraController);
    }

    /**
     * Initializes a model instance with a specific color.
     *
     * @param instance the model instance to initialize
     * @param color the color to apply to the model instance
     */
    private void initializeModelInstance(ModelInstance instance, Color color) {
        initializeModelInstance(instance, color, false);
    }

    /**
     * Initializes a model instance with a specific color and culling settings.
     *
     * @param instance the model instance to initialize
     * @param color the color to apply to the model instance
     * @param noCullFace whether to disable face culling
     */
    private void initializeModelInstance(ModelInstance instance, Color color, boolean noCullFace) {
        for (Material material : instance.materials) {
            material.clear();
            if (noCullFace) {
                material.set(new IntAttribute(IntAttribute.CullFace, GL20.GL_NONE));
            }
            material.set(ColorAttribute.createDiffuse(color));
        }
    }

    /**
     * Sets the position for the flag and stem instances.
     */
    private void setPositionForFlagAndStemInstances() {
        flagInstance.transform.setToTranslation((float) goalState.getX(), (float) terrainHeightFunction.evaluate(new HashMap<String, Double>() {{
            put("x", goalState.getX());
            put("y", goalState.getY());
        }}), (float) goalState.getY());

        flagStemInstance.transform.setToTranslation((float) goalState.getX(), (float) terrainHeightFunction.evaluate(new HashMap<String, Double>() {{
            put("x", goalState.getX());
            put("y", goalState.getY());
        }}), (float) goalState.getY());
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

    /**
     * Initializes the UI components for the game.
     */
    private void initializeUIComponents() {
        lineInstance = terrainManager.createRedLineModel(new PhysicsSimulator(terrainHeightFunction, goalState).hitWithPath(10, 0).getValue());
        initializeButtons();
        initializeLabels();
        initializeProgressBar();
    }

    /**
     * Initializes the buttons for the UI.
     */
    private void initializeButtons() {
        Table buttonTable = new Table();
        buttonTable.setFillParent(true);
        buttonTable.top().right();

        TextButton settingsButton = createSettingsButton();
        TextButton pauseButton = createPauseButton();
        TextButton pathButton = createPathButton();

        buttonTable.add(settingsButton).width(100).height(50).pad(10);
        buttonTable.add(pauseButton).width(100).height(50).pad(10);
        buttonTable.add(pathButton).width(100).height(50).pad(10);
        stage.addActor(buttonTable);
    }

    /**
     * Creates the settings button for the UI.
     *
     * @return the created settings button
     */
    private TextButton createSettingsButton() {
        TextButton settingsButton = new TextButton("Settings", skin);
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isPaused) {
                    pauseGame();
                }
                isBallAllowedToMove = false;
                mainGame.switchToSettings();
            }
        });
        return settingsButton;
    }

    /**
     * Creates the pause button for the UI.
     *
     * @return the created pause button
     */
    private TextButton createPauseButton() {
        TextButton pauseButton = new TextButton("Pause", skin);
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                pauseGame();
            }
        });
        return pauseButton;
    }

    /**
     * Creates the path button for the UI
     * 
     * @return the created oath button
     */
    private TextButton createPathButton(){
        TextButton pathButton = new TextButton("Path", skin);
        pathButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                PhysicsSimulator simulator = new PhysicsSimulator(terrainHeightFunction, goalState);
                simulator.setPosition((float)currentBallState.getX(), (float)currentBallState.getY());
                List<Vector2> result = simulator.hitWithPath(10, cameraViewAngle).getValue();
                lineInstance = terrainManager.createRedLineModel(result);
            }
        });
        return pathButton;
    }

    /**
     * Initializes the labels for the UI.
     */
    private void initializeLabels() {
        Table labelTable = new Table();
        labelTable.setFillParent(true);
        labelTable.top().left();

        Label windLabel = createWindLabel();
        facingLabel = createFacingLabel();
        scoreLabel = createScoreLabel();
        lastScoreLabel = createLastScoreLabel();

        ballMovementLabel = new Label("Ball can move: " + isBallAllowedToMove, skin);

        labelTable.add(scoreLabel).pad(10);
        labelTable.add(lastScoreLabel).pad(10);
        labelTable.add(facingLabel).pad(10).top().left();
        labelTable.row();
        labelTable.add(windLabel).pad(10).bottom().left().expandY();
        labelTable.add(ballMovementLabel).pad(10).bottom().left().expandY();

        stage.addActor(labelTable);
    }

    /**
     * Creates the wind label for the UI.
     *
     * @return the created wind label
     */
    private Label createWindLabel() {
        return new Label("Wind: vx=" + String.format("%.4f", mainGame.getGolfGameScreen().getWeather().getWind()[0]) +
                ", vy=" + String.format("%.4f", mainGame.getGolfGameScreen().getWeather().getWind()[1]) +
                ", vz=" + String.format("%.4f", mainGame.getGolfGameScreen().getWeather().getWind()[2]), skin);
    }

    /**
     * Creates the facing label for the UI.
     *
     * @return the created facing label
     */
    private Label createFacingLabel() {
        return new Label("Facing(x,y,z): " + String.format("%.2f", mainCamera.direction.x) + ", " +
                String.format("%.2f", mainCamera.direction.y) + ", " + String.format("%.2f", mainCamera.direction.z), skin);
    }

    /**
     * Creates the score label for the UI.
     *
     * @return the created score label
     */
    private Label createScoreLabel() {
        return new Label("Score: " + score, skin);
    }

    /**
     * Creates the last score label for the UI.
     *
     * @return the created last score label
     */
    private Label createLastScoreLabel() {
        return new Label("Last Score: " + lastScore, skin);
    }

    /**
     * Initializes the progress bar for the UI.
     */
    private void initializeProgressBar() {
        speedProgressBar = new ProgressBar(MIN_SPEED, MAX_SPEED, 0.1f, true, skin, "progress-bar");
        speedProgressBar.setValue(currentSpeed);
        speedProgressBar.setVisible(true);

        Table uiTable = new Table();
        uiTable.setFillParent(true);
        uiTable.right().padRight(20);
        uiTable.add(speedProgressBar).width(40).height(600).pad(10);

        stage.addActor(uiTable);
    }

    /**
     * Reloads the terrain and resets the game state.
     */
    private void reloadTerrainAndResetState() {
        reloadTerrain();
        resetWaterAnimations();
        resetGameState();
    }

    /**
     * Reloads the terrain around the current position.
     */
    private void reloadTerrain() {
        golfCourseInstances = terrainManager.createGrassTerrainModels(0, 0);
        sandInstances = terrainManager.createSandTerrainModels(0, 0);
        holeInstance = terrainManager.createHoleTerrainModel(0, 0);
        lineInstance = terrainManager.createRedLineModel(new PhysicsSimulator(terrainHeightFunction, goalState).hitWithPath(10, 0).getValue());
        waterSurfaces = waterSurfaceManager.createWaterSurface(0, 0);
    }

    /**
     * Resets the water animations.
     */
    private void resetWaterAnimations() {
        waterAnimations.clear();
        for (ModelInstance waterSurface : waterSurfaces) {
            waterAnimations.add(new WaterAnimation(waterSurface));
        }
    }

    /**
     * Resets the game state to initial conditions.
     */
    public void resetGameState() {
        currentBallState.setAllComponents(0, 0, 0.001, 0.001);
        reloadTerrain(0, 0);
        ballRotationAngleX = 0f;
        ballRotationAngleY = 0f;
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
            handleCameraInput();
            handleBallSpeedAdjustment();
        }
    }

    /**
     * Handles camera input to adjust the camera view angle and distance.
     */
    private void handleCameraInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            cameraViewAngle += 0.05;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            cameraViewAngle -= 0.05;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            cameraDistance = Math.max(MIN_CAMERA_DISTANCE, cameraDistance - 0.1f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            cameraDistance = Math.min(MAX_CAMERA_DISTANCE, cameraDistance + 0.1f);
        }
        facingLabel.setText(getFacingLabelText());
    }

    /**
     * Returns the current camera facing direction as a formatted string.
     *
     * @return the facing direction text
     */
    private String getFacingLabelText() {
        return "Facing(x,y,z): " +
                String.format("%.2f", mainCamera.direction.x) + ", " +
                String.format("%.2f", mainCamera.direction.y) + ", " +
                String.format("%.2f", mainCamera.direction.z);
    }

    /**
     * Handles the ball speed adjustment when the space key is pressed.
     */
    private void handleBallSpeedAdjustment() {
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            isAdjustingSpeed = true;
        } else if (isAdjustingSpeed) {
            isAdjustingSpeed = false;
            if (!isBallAllowedToMove) {
                performHit(currentSpeed);
            }
        }
    }

    /**
     * Performs a hit on the ball with the specified speed.
     *
     * @param speed the speed to hit the ball with
     */
    public void performHit(float speed) {
        isBallAllowedToMove = true;
        currentBallState.setVx(-speed * Math.cos(cameraViewAngle));
        currentBallState.setVy(-speed * Math.sin(cameraViewAngle));
        isBallInWater = false;
    }

    /**
     * Performs a hit on the ball with the specified velocity.
     *
     * @param vx the x velocity to hit the ball with
     * @param vy the y velocity to hit the ball with
     */
    public void performHitWithVelocity(double vx, double vy) {
        isBallAllowedToMove = true;
        currentBallState.setVx(-vx);
        currentBallState.setVy(-vy);
        isBallInWater = false;
    }

    /**
     * Checks if the ball is allowed to move.
     *
     * @return true if the ball is allowed to move, false otherwise
     */
    public boolean getIsBallAllowedToMove() {
        return isBallAllowedToMove;
    }

    @Override
    public void render(float delta) {
        handleInput();
        if (!isPaused) {
            update(delta);
        }
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
        // Adjust ball speed if necessary
        if (isAdjustingSpeed) {
            adjustBallSpeed(deltaTime);
        }
    
        updateBotBehavior();
    
        // Check if the ball has reached the goal
        if (currentBallState.epsilonPositionEquals(goalState, GOAL_TOLERANCE)) {
            handleGoalReached();
        }
    
        // Update ball state if allowed to move
        if (isBallAllowedToMove) {
            currentBallState = gamePhysicsEngine.update(currentBallState, deltaTime);
            updateBallRotation(deltaTime);
        }
    
        ballMovementLabel.setText("Ball can move: " + isBallAllowedToMove);
    
        setPositionForFlagAndStemInstances();
    
        // Apply wind effects to the ball's velocity
        applyWindEffect();
    
        // Update the ball's position in the world
        updateBallPosition();
    
        // Update animations
        updateAnimations(deltaTime);
    
        // Update the camera position
        updateCameraPosition(deltaTime);
    
        // Handle ball movement when on low speed
        handleLowSpeedBallMovement();
    
        // Handle ball falling below ground level
        handleBallFallingBelowGround();
    
        // Set friction based on the terrain type
        setTerrainFriction();
    
        // Check and handle if the ball is out of bounds
        checkAndHandleBallOutOfBounds();
    }

    private void updateBotBehavior() {
        if (hillClimbingBotActive) {
            advancedBotPlay();
        } else if (ruleBasedBotActive) {
            if (ruleBasedBotFuture == null || ruleBasedBotFuture.isDone()) {
                ruleBasedBotFuture = executorService.submit(this::ruleBasedbotPlay);
            }
        }   
    }

    private void updateBallRotation(float deltaTime) {
        float velocityX = (float) currentBallState.getVx();
        float velocityY = (float) currentBallState.getVy();
        
        float angularVelocityX = (velocityX / ballRadius) * (180f / (float) Math.PI); 
        float angularVelocityY = (velocityY / ballRadius) * (180f / (float) Math.PI); 
    
        ballRotationAngleX -= angularVelocityX * deltaTime;
        ballRotationAngleY += angularVelocityY * deltaTime;
    
        ballRotationAngleX %= 360;
        ballRotationAngleY %= 360;
    }

    /**
     * Handles the event when the goal is reached.
     */
    private void handleGoalReached() {
        if (!validGoal(currentBallState, goalState)){
            return;
        }
        lastScore = score;
        score = 0;
        scoreLabel.clear();
        scoreChange();
        lastScoreLabel.setText("Last Score: " + lastScore);
        resetGameState();
    }

    public static boolean validGoal(BallState ball, BallState goal){
        return ball.epsilonPositionEquals(goal, GOAL_TOLERANCE)&&Math.abs(ball.getVx())<1&&Math.abs(ball.getVy())<2;
    }

    private void checkAndHandleBallOutOfBounds() {
        if (isBallOutOfBounds(currentBallState)) {
            // Увеличение счета
            scoreChange();
    
            // Возврат мяча на последнее корректное положение
            currentBallState.setAllComponents(lastValidState.getX(), lastValidState.getY(), lastValidState.getVx(), lastValidState.getVy());
            System.out.println("Ball is out of bounds. Returning to last valid position.");
        }
    }

    /**
     * Applies wind effect to the ball's velocity.
     */
    private void applyWindEffect() {
        if (Math.abs(currentBallState.getVx()) > 0.01 || Math.abs(currentBallState.getVy()) > 0.01) {
            currentBallState.setVx(weather.getWind()[0] + currentBallState.getVx());
            currentBallState.setVy(weather.getWind()[1] + currentBallState.getVy());
        }
    }

    /**
     * Updates the animations for the game.
     *
     * @param deltaTime the time elapsed since the last frame
     */
    private void updateAnimations(float deltaTime) {
        flagAnimation.update(deltaTime);
        for (WaterAnimation waterAnimation : waterAnimations) {
            waterAnimation.update(deltaTime);
        }
    }

    /**
     * Updates the ball's position in the world.
     */
    private void updateBallPosition() {
        float ballZ = terrainManager.getTerrainHeight((float) currentBallState.getX(), (float) currentBallState.getY()) + BALL_HEIGHT_OFFSET;
        golfBallInstance.transform.setToTranslation((float) currentBallState.getX(), ballZ, (float) currentBallState.getY());
        
        golfBallInstance.transform.rotate(Vector3.X, ballRotationAngleY);
        golfBallInstance.transform.rotate(Vector3.Z, ballRotationAngleX);
    }

    /**
     * Handles the ball movement when it is moving slowly.
     */
    private void handleLowSpeedBallMovement() {
        Vector3 ballPosition = new Vector3((float) currentBallState.getX(), (float) currentBallState.getY(), 0);
        ballPosition.z = terrainManager.getTerrainHeight(ballPosition.x, ballPosition.y);
        boolean onSand = terrainManager.isBallOnSand(ballPosition);

        lowSpeedThreshold = onSand ? LOW_SPEED_THRESHOLD_SAND : LOW_SPEED_THRESHOLD_GRASS;

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
                scoreChange();
            }
        }
    }

    /**
     * Handles the event when the ball falls below ground level.
     */
    private void handleBallFallingBelowGround() {
        float ballZ = terrainManager.getTerrainHeight((float) currentBallState.getX(), (float) currentBallState.getY()) + BALL_HEIGHT_OFFSET;
        if (ballZ - BALL_HEIGHT_OFFSET < 0) {
            scoreChange();
            isBallInWater = true;
            currentBallState.setAllComponents(lastValidState.getX(), lastValidState.getY(), lastValidState.getVx(), lastValidState.getVy());
            ballRotationAngleX = 0f;
            ballRotationAngleY = 0f;
            System.out.println("Ball has fallen below ground level. Resetting to last valid position.");
        } else {
            updateLastValidState();
        }
    }

    /**
     * Updates the last valid state of the ball.
     */
    private void updateLastValidState() {
        if (!ballPositionsWhenSlow.isEmpty()) {
            BallState lastPosition = ballPositionsWhenSlow.get(ballPositionsWhenSlow.size() - 1);
            lastValidState.setAllComponents(lastPosition.getX(), lastPosition.getY(), lastPosition.getVx(), lastPosition.getVy());
        } else {
            lastValidState.setAllComponents(currentBallState.getX(), currentBallState.getY(), currentBallState.getVx(), currentBallState.getVy());
        }
    }

    /**
     * Sets the terrain friction based on the ball's position.
     */
    private void setTerrainFriction() {
        Vector3 ballPosition = new Vector3((float) currentBallState.getX(), (float) currentBallState.getY(), 0);
        boolean onSand = terrainManager.isBallOnSand(ballPosition);

        if (onSand) {
            gamePhysicsEngine.setFriction(sandFrictionKinetic, sandFrictionStatic);
        } else {
            gamePhysicsEngine.setFriction(grassFrictionKinetic, grassFrictionStatic);
        }
    }

    /**
     * Executes the rule-based bot logic for playing the game.
     */
    private void ruleBasedbotPlay() {
        if (!isBallAllowedToMove) {
            wallE.switchToRuleBased();
            System.out.println("About to call setDirection");
            wallE.setDirection();
            waitForHillClimbDirectionSetAndHit();
        }
    }

    private void waitForHillClimbDirectionSetAndHit(){
        // Run a separate thread to periodically check if the direction is set
        new Thread(() -> {
            while (wallE.getHillClimbingBot().isDirectionSet()) {
                try {
                    Thread.sleep(10); // 10 milliseconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            wallE.hit();
        }).start();
    }

    /**
     * Executes the advanced bot logic for playing the game.
     */
    private void advancedBotPlay() {
        if (!isBallAllowedToMove) {
            wallE.switchToPPO();    
            wallE.setDirection();
            wallE.hit();
        }
    }

    /**
     * Changes the score and updates the score label.
     */
    private void scoreChange() {
        scoreLabel.setText("Score: " + score++);
        isBallAllowedToMove = false;
    }

    /**
     * Updates the camera position based on the ball's position and view angle.
     *
     * @param delta the time elapsed since the last frame
     */
    public void updateCameraPosition(float delta) {
        float ballZ = terrainManager.getTerrainHeight((float) currentBallState.getX(), (float) currentBallState.getY()) + BALL_HEIGHT_OFFSET;
        float cameraX = (float) (currentBallState.getX() + cameraDistance * Math.cos(cameraViewAngle));
        float cameraY = (float) (currentBallState.getY() + cameraDistance * Math.sin(cameraViewAngle));
        mainCamera.position.set(cameraX, ballZ + CAMERA_HEIGHT, cameraY);
        mainCamera.lookAt((float) currentBallState.getX(), ballZ + 1f, (float) currentBallState.getY());
        mainCamera.up.set(Vector3.Y);
        mainCamera.update();
    }

    /**
     * Adjusts the ball's speed based on the delta time.
     *
     * @param deltaTime the time elapsed since the last frame
     */
    private void adjustBallSpeed(float deltaTime) {
        currentSpeed += speedAdjustmentRate * deltaTime;
        if (currentSpeed > MAX_SPEED) {
            currentSpeed = MAX_SPEED;
            speedAdjustmentRate = -speedAdjustmentRate; 
        } else if (currentSpeed < MIN_SPEED) {
            currentSpeed = MIN_SPEED;
            speedAdjustmentRate = -speedAdjustmentRate; 
        }
        speedProgressBar.setValue(currentSpeed);
    }

    /**
     * Reloads the terrain around a new center position.
     *
     * @param x the new x-coordinate center for the terrain
     * @param y the new y-coordinate center for the terrain
     */
    private void reloadTerrain(float x, float y) {
        golfCourseInstances.clear();
        golfCourseInstances = terrainManager.createGrassTerrainModels(x, y);
        sandInstances = terrainManager.createSandTerrainModels(x, y);
        System.out.println("Terrain reloaded around position: " + x + ", " + y);
    }

    /**
     * Draws the game scene, including the golf ball and terrain, using the appropriate shaders and lighting.
     */
    private void draw() {
        // Clear the screen with a light blue color
        Gdx.gl.glClearColor(0.5f, 0.7f, 1.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Update camera controller
        cameraController.update();

        // Begin rendering shadows
        mainShadowLight.begin(Vector3.Zero, mainCamera.direction);
        shadowModelBatch.begin(mainShadowLight.getCamera());

        // Render terrain instances for shadows
        for (ModelInstance terrainInstance : golfCourseInstances) {
            shadowModelBatch.render(terrainInstance, gameEnvironment);
        }
        // Render sand instances for shadows
        for (ModelInstance sandInstance : sandInstances) {
            shadowModelBatch.render(sandInstance, gameEnvironment);
        }
        // Render other game elements for shadows
        shadowModelBatch.render(lineInstance, gameEnvironment);
        shadowModelBatch.render(holeInstance, gameEnvironment);
        shadowModelBatch.render(golfBallInstance, gameEnvironment);
        shadowModelBatch.render(flagStemInstance, gameEnvironment);
        shadowModelBatch.render(flagInstance, gameEnvironment);

        // End rendering shadows
        shadowModelBatch.end();
        mainShadowLight.end();

        // Begin rendering main scene
        mainModelBatch.begin(mainCamera);

        // Render terrain instances for main scene
        for (ModelInstance terrainInstance : golfCourseInstances) {
            mainModelBatch.render(terrainInstance, gameEnvironment);
        }
        // Render sand instances for main scene
        for (ModelInstance sandInstance : sandInstances) {
            mainModelBatch.render(sandInstance, gameEnvironment);
        }

        // Render other game elements for main scene
        mainModelBatch.render(lineInstance, gameEnvironment);
        mainModelBatch.render(holeInstance, gameEnvironment);
        mainModelBatch.render(golfBallInstance, gameEnvironment);
        mainModelBatch.render(flagStemInstance, gameEnvironment);
        mainModelBatch.render(flagInstance, gameEnvironment);

        // Render water surfaces
        for (ModelInstance waterSurface : waterSurfaces) {
            mainModelBatch.render(waterSurface, gameEnvironment);
        }
        // End rendering main scene
        mainModelBatch.end();
    }











    @Override
    public void resize(int width, int height) {
        mainCamera.viewportWidth = width;
        mainCamera.viewportHeight = height;
        mainCamera.update();
        speedProgressBar.setWidth(width * 0.001f); 
        speedProgressBar.setHeight(height * 0.05f);
        stage.getViewport().update(width, height, true);
    }

    /**
     * Sets the height function for the terrain.
     *
     * @param newHeightFunction the new height function to use
     */
    public void setHeightFunction(Function newHeightFunction) {
        this.terrainHeightFunction = newHeightFunction;
    }

    /**
     * Sets the weather for the game.
     *
     * @param newWeather the new weather to use
     */
    public void setWeather(Weather newWeather) {
        weather = newWeather;
    }

    /**
     * Gets the current weather.
     *
     * @return the current weather
     */
    public Weather getWeather() {
        return weather;
    }

    /**
     * Sets the goal coordinates for the game.
     *
     * @param coords the new goal coordinates
     */
    public void setGoalCoords(float[] coords) {
        goalState.setX(coords[0]);
        goalState.setY(coords[1]);
        setPositionForFlagAndStemInstances();
    }

    /**
     * Sets the ball coordinates.
     *
     * @param coords the new ball coordinates
     */
    public void setBallCoords(float[] coords) {
        currentBallState.setX(coords[0]);
        currentBallState.setY(coords[1]);
    }

    /**
     * Sets the goal radius.
     *
     * @param radius the new goal radius
     */
    public void setGoalRadius(float radius) {
        GOAL_TOLERANCE = radius;
    }

    /**
     * Sets the camera angle for the game.
     *
     * @param newCameraAngle the new camera angle to use
     */
    public void setCameraAngle(float newCameraAngle) {
        cameraViewAngle = newCameraAngle;
    }

    public double getSandFrictionKinetic(){
        return sandFrictionKinetic;
    }

    public double getSandFrictionStatic(){
        return sandFrictionStatic;
    }

    /**
     * Gets the current camera angle.
     *
     * @return the current camera angle
     */
    public float getCameraAngle() {
        return cameraViewAngle;
    }

    /**
     * Gets the goal tolerance.
     *
     * @return the goal tolerance
     */
    public static float getGoalTolerance() {
        return GOAL_TOLERANCE;
    }

    /**
     * Gets the current ball state.
     *
     * @return the current ball state
     */
    public BallState getBallState() {
        return currentBallState;
    }

    /**
     * Gets the goal state.
     *
     * @return the goal state
     */
    public BallState getGoalState() {
        return goalState;
    }

    /**
     * Gets the main camera.
     *
     * @return the main camera
     */
    public Camera getMainCamera() {
        return mainCamera;
    }

    /**
     * Gets the current speed of the progress bar.
     *
     * @return the current speed of the progress bar
     */
    public float getCurrentSpeedBar() {
        return currentSpeed;
    }

    /**
     * Gets the physics engine.
     *
     * @return the physics engine
     */
    public PhysicsEngine getPhysicsEngine() {
        return gamePhysicsEngine;
    }

    /**
     * Gets the WallE bot.
     *
     * @return the WallE bot
     */
    public WallE getWallE() {
        return wallE;
    }

    /**
     * Gets the terrain manager.
     *
     * @return the terrain manager
     */
    public TerrainManager getTerrainManager() {
        return terrainManager;
    }

    /**
     * Gets the friction value for a specific position.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return the friction value
     */
    public float getFriction(float x, float y) {
        Vector3 position = new Vector3(x, y, terrainManager.getTerrainHeight(x, y));
        return terrainManager.isBallOnSand(position) ? (float) sandFrictionKinetic : (float) grassFrictionKinetic;
    }

    /**
     * Checks if the camera is correctly positioned.
     *
     * @return true if the camera is correctly positioned, false otherwise
     */
    public boolean cameraCorrectlyPut() {
        if (currentBallState.getVx() > 0.01 || currentBallState.getVy() > 0.01) {
            return true;
        }
        Vector2 ballToGoal = new Vector2((float) (goalState.getX() - currentBallState.getX()), (float) (goalState.getY() - currentBallState.getY())).nor();
        Vector2 camVector2 = new Vector2(mainCamera.direction.x, mainCamera.direction.z).nor();
        return (Math.abs(ballToGoal.x - camVector2.x) < 0.001) && (Math.abs(ballToGoal.y - camVector2.y) < 0.001);
    }

    public void setPath(List<Vector2> path) {
        this.path = path;
    }

    public boolean isBallInWater(BallState ballState) {
        return isBallInWater;
    }

    public boolean isBallOutOfBounds(BallState ballState) {
        float x = (float) ballState.getX();
        float y = (float) ballState.getY();
        return x > terrainManager.getTerrainWidth()/2  || y > terrainManager.getTerrainHeight()/2;
    }

    /**
     * Toggles the pause state of the game.
     */
    private void pauseGame() {
        isPaused = !isPaused;
        if (isPaused) {
            pauseDialog.show(stage);
        } else {
            pauseDialog.hide();
        }
    }

    /**
     * Toggles the rule-based bot activeness.
     */
    public void toggleRuleBasedBotActiveness() {
        ruleBasedBotActive = !ruleBasedBotActive;
    }

    /**
     * Sets the rule-based bot activeness.
     *
     * @param activeness the activeness state to set
     */
    public void setRuleBasedBotActive(boolean activeness) {
        ruleBasedBotActive = activeness;
    }

    /**
     * Toggles the hill-climbing bot activeness.
     */
    public void toggleHillClimbingBotActiveness() {
        hillClimbingBotActive = !hillClimbingBotActive;
    }

    public void setSandFrictionKinetic(double kineticFriction) {
        this.sandFrictionKinetic = kineticFriction;
    }
    
    public void setSandFrictionStatic(double staticFriction) {
        this.sandFrictionStatic = staticFriction;
    }

    public void setLineInstance(ModelInstance line){
        lineInstance = line;
    }
    /**
     * Sets the hill-climbing bot activeness.
     *
     * @param activeness the activeness state to set
     */
    public void setHillClimbingBotActive(boolean activeness) {
        hillClimbingBotActive = activeness;
    }

    public Function getHeightFunction(){
        return terrainHeightFunction;
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
