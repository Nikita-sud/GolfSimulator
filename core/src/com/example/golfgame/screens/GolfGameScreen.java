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
import java.util.HashMap;
import java.util.List;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.WallE;
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
    // Constants
    private static final float GOAL_TOLERANCE = 1f;
    private static final float CAMERA_FOV = 100;
    private static final float MIN_CAMERA_DISTANCE = 5;
    private static final float MAX_CAMERA_DISTANCE = 15;
    private static final float DEFAULT_CAMERA_DISTANCE = 10;
    private static final float CAMERA_HEIGHT = 5f;
    private static final float BALL_HEIGHT_OFFSET = 1f;
    private static final float LOW_SPEED_THRESHOLD_GRASS = 0.005f;
    private static final float LOW_SPEED_THRESHOLD_SAND = 1.0f;
    private static final float MIN_SPEED = 0.01f;
    private static final float MAX_SPEED = 10.0f;

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
    private ModelInstance golfBallInstance, flagInstance, flagStemInstance;
    private List<ModelInstance> golfCourseInstances, sandInstances, waterSurfaces;
    private ModelInstance holeInstance;

    // Physics and terrain
    private PhysicsEngine gamePhysicsEngine;
    private TerrainManager terrainManager;
    private WaterSurfaceManager waterSurfaceManager;
    private Function terrainHeightFunction;
    private BallState currentBallState, lastValidState, goalState = new BallState(-20, 20, 0, 0);;
    private double grassFrictionKinetic, grassFrictionStatic, sandFrictionKinetic, sandFrictionStatic;
    private float lowSpeedThreshold = LOW_SPEED_THRESHOLD_GRASS;
    private List<BallState> ballPositionsWhenSlow;

    // UI components
    private Skin skin;
    private Label facingLabel, scoreLabel, lastScoreLabel;
    private ProgressBar speedProgressBar;
    private Dialog pauseDialog;

    // Animations
    private FlagAnimation flagAnimation;
    private List<WaterAnimation> waterAnimations;

    // Game state
    private boolean isPaused = false;
    private boolean isAdjustingSpeed = false;
    private boolean isBallAllowedToMove = false;
    private float currentSpeed = MIN_SPEED;
    private float speedAdjustmentRate = 10.0f;
    private int score = 0, lastScore = -1;
    private float cameraDistance = DEFAULT_CAMERA_DISTANCE;
    private float cameraViewAngle = 0;
    private float terrainCenterX = 0, terrainCenterZ = 0;
    private boolean ruleBasedBotActive = false;
    private boolean hillClimbingBotActive = false;

    // Light settings
    private float sunlight;

    // Weather
    private Weather weather = new Weather(0);

    // Music
    private Music music;

    // Bots
    private WallE wallE;

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

        initializeBot();

        // Initialize Skin and Dialog
        initializeSkinAndDialog();

        // Initialize Models and Assets
        initializeModelsAndAssets();

        // Initialize Physics Engine and Game State
        initializePhysicsAndGameState();

        // Initialize Terrain
        initializeTerrain();

        // Initialize Camera and Light
        initializeCameraAndLight();

        // Initialize Game Environment
        initializeGameEnvironment();

        // Set Input Processor
        setInputProcessor();
    }

    private void initializeBot(){
        this.wallE = new WallE(mainGame);
    }

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

    private void initializeModelsAndAssets() {
        mainModelBatch = new ModelBatch();
        golfBallModel = assetManager.get("models/sphere.obj", Model.class);
        flagModel = assetManager.get("models/flag.obj", Model.class);
        flagStemModel = assetManager.get("models/flagStem.obj", Model.class);
        music = assetManager.get("assets/music/game-screen.mp3");
    }

    private void initializePhysicsAndGameState() {
        terrainHeightFunction = mainGame.getSettingsScreen().getCurHeightFunction();
        ODE solver = new RungeKutta();
        currentBallState = new BallState(0, 0, 0.001, 0.001);
        gamePhysicsEngine = new PhysicsEngine(solver, terrainHeightFunction);
        score = 0;
        lastScore = -1;
        ballPositionsWhenSlow = new ArrayList<>();
        ballPositionsWhenSlow.clear();
        lastValidState = currentBallState.copy();
        grassFrictionKinetic = 0.1;
        grassFrictionStatic = 0.2;
        sandFrictionKinetic = 0.7;
        sandFrictionStatic = 1;
    }

    private void initializeTerrain() {
        terrainManager = new TerrainManager(terrainHeightFunction,
            assetManager.get("textures/grassTexture.jpeg", Texture.class),
            assetManager.get("textures/sandTexture.jpeg", Texture.class),
            assetManager.get("textures/holeTexture.png", Texture.class),
            200, 200, 1.0f, 4);
        waterSurfaceManager = new WaterSurfaceManager(200.0f, 200.0f, 50);
    }

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

        waterSurfaces = waterSurfaceManager.createWaterSurface(0, 0);
        waterAnimations = new ArrayList<>();
        for (ModelInstance waterSurface : waterSurfaces) {
            waterAnimations.add(new WaterAnimation(waterSurface));
        }
    }

    private void setInputProcessor() {
        cameraController = new CameraInputController(mainCamera);
        Gdx.input.setInputProcessor(cameraController);
    }

    private void initializeModelInstance(ModelInstance instance, Color color) {
        initializeModelInstance(instance, color, false);
    }

    private void initializeModelInstance(ModelInstance instance, Color color, boolean noCullFace) {
        for (Material material : instance.materials) {
            material.clear();
            if (noCullFace) {
                material.set(new IntAttribute(IntAttribute.CullFace, GL20.GL_NONE));
            }
            material.set(ColorAttribute.createDiffuse(color));
        }
    }

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

    private void initializeUIComponents() {
        initializeButtons();
        initializeLabels();
        initializeProgressBar();
    }

    private void initializeButtons() {
        Table buttonTable = new Table();
        buttonTable.setFillParent(true);
        buttonTable.top().right();

        TextButton settingsButton = createSettingsButton();
        TextButton pauseButton = createPauseButton();

        buttonTable.add(settingsButton).width(100).height(50).pad(10);
        buttonTable.add(pauseButton).width(100).height(50).pad(10);

        stage.addActor(buttonTable);
    }

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

    private void initializeLabels() {
        Table labelTable = new Table();
        labelTable.setFillParent(true);
        labelTable.top().left();

        Label windLabel = createWindLabel();
        facingLabel = createFacingLabel();
        scoreLabel = createScoreLabel();
        lastScoreLabel = createLastScoreLabel();

        labelTable.add(scoreLabel).pad(10);
        labelTable.add(lastScoreLabel).pad(10);
        labelTable.add(facingLabel).pad(10).top().left();
        labelTable.row();
        labelTable.add(windLabel).pad(10).bottom().left().expandY();

        stage.addActor(labelTable);
    }

    private Label createWindLabel() {
        return new Label("Wind: vx=" + String.format("%.4f", mainGame.getGolfGameScreen().getWeather().getWind()[0]) +
                ", vy=" + String.format("%.4f", mainGame.getGolfGameScreen().getWeather().getWind()[1]) +
                ", vz=" + String.format("%.4f", mainGame.getGolfGameScreen().getWeather().getWind()[2]), skin);
    }

    private Label createFacingLabel() {
        return new Label("Facing(x,y,z): " + String.format("%.2f", mainCamera.direction.x) + ", " +
                String.format("%.2f", mainCamera.direction.y) + ", " + String.format("%.2f", mainCamera.direction.z), skin);
    }

    private Label createScoreLabel() {
        return new Label("Score: " + score, skin);
    }

    private Label createLastScoreLabel() {
        return new Label("Last Score: " + lastScore, skin);
    }

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
    
    private void reloadTerrainAndResetState() {
        reloadTerrain();
        resetWaterAnimations();
        resetGameState();
    }
    
    private void reloadTerrain() {
        golfCourseInstances = terrainManager.createGrassTerrainModels(0, 0);
        sandInstances = terrainManager.createSandTerrainModels(0, 0);
        holeInstance = terrainManager.createHoleTerrainModel(0, 0);
        waterSurfaces = waterSurfaceManager.createWaterSurface(0, 0);
    }
    
    private void resetWaterAnimations() {
        waterAnimations.clear();
        for (ModelInstance waterSurface : waterSurfaces) {
            waterAnimations.add(new WaterAnimation(waterSurface));
        }
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
            handleCameraInput();
            handleBallSpeedAdjustment();
        }
    }

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

    private String getFacingLabelText() {
        return "Facing(x,y,z): " +
                String.format("%.2f", mainCamera.direction.x) + ", " +
                String.format("%.2f", mainCamera.direction.y) + ", " +
                String.format("%.2f", mainCamera.direction.z);
    }

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

    public void performHit(float speed) {
        // Logic to hit the ball
        isBallAllowedToMove = true;
        currentBallState.setVx(-speed * Math.cos(cameraViewAngle));
        currentBallState.setVy(-speed * Math.sin(cameraViewAngle));
    }
    public void performHitWithVelocity(double vx,double vy) {
        isBallAllowedToMove = true;
        currentBallState.setVx(-vx);
        currentBallState.setVy(-vy);
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

        if (hillClimbingBotActive) {
            hillClimbingBotPlay();
        } else if (ruleBasedBotActive) {
            ruleBasedPlay();
        }
    
        // Check if the ball has reached the goal
        if (currentBallState.epsilonPositionEquals(goalState, GOAL_TOLERANCE)) {
            handleGoalReached();
        }
    
        // Update ball state if allowed to move
        if (isBallAllowedToMove) {
            currentBallState = gamePhysicsEngine.update(currentBallState, deltaTime);
        }
        setPositionForFlagAndStemInstances();
        // Apply wind effects to the ball's velocity
        applyWindEffect();
    
        // Reload terrain and water surfaces if needed
        checkAndReloadTerrainAndWaterSurfaceIfNeeded();
    
        // Update animations
        updateAnimations(deltaTime);
    
        // Update the ball's position in the world
        updateBallPosition();
    
        // Update the camera position
        updateCameraPosition(deltaTime);
    
        // Handle ball movement when on low speed
        handleLowSpeedBallMovement();
    
        // Handle ball falling below ground level
        handleBallFallingBelowGround();
    
        // Set friction based on the terrain type
        setTerrainFriction();
        // Reset the bot hit trigger flag after processing
    }
    
    private void handleGoalReached() {
        lastScore = score;
        score = 0;
        scoreLabel.clear();
        scoreChange();
        lastScoreLabel.setText("Last Score: " + lastScore);
        isBallAllowedToMove = false;
        resetGameState();
    }
    
    private void applyWindEffect() {
        if (Math.abs(currentBallState.getVx()) > 0.01 || Math.abs(currentBallState.getVy()) > 0.01) {
            currentBallState.setVx(weather.getWind()[0] + currentBallState.getVx());
            currentBallState.setVy(weather.getWind()[1] + currentBallState.getVy());
        }
    }
    
    private void updateAnimations(float deltaTime) {
        flagAnimation.update(deltaTime);
        for (WaterAnimation waterAnimation : waterAnimations) {
            waterAnimation.update(deltaTime);
        }
    }
    
    private void updateBallPosition() {
        float ballZ = terrainManager.getTerrainHeight((float) currentBallState.getX(), (float) currentBallState.getY()) + BALL_HEIGHT_OFFSET;
        golfBallInstance.transform.setToTranslation((float) currentBallState.getX(), ballZ, (float) currentBallState.getY());
    }
    
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
                isBallAllowedToMove = false;
            }
        }
    }
    
    private void handleBallFallingBelowGround() {
        float ballZ = terrainManager.getTerrainHeight((float) currentBallState.getX(), (float) currentBallState.getY()) + BALL_HEIGHT_OFFSET;
        if (ballZ - BALL_HEIGHT_OFFSET < 0) {
            scoreChange();
            isBallAllowedToMove = false;
            currentBallState.setAllComponents(lastValidState.getX(), lastValidState.getY(), lastValidState.getVx(), lastValidState.getVy());
            System.out.println("Ball has fallen below ground level. Resetting to last valid position.");
        } else {
            updateLastValidState();
        }
    }
    
    private void updateLastValidState() {
        if (!ballPositionsWhenSlow.isEmpty()) {
            BallState lastPosition = ballPositionsWhenSlow.get(ballPositionsWhenSlow.size() - 1);
            lastValidState.setAllComponents(lastPosition.getX(), lastPosition.getY(), lastPosition.getVx(), lastPosition.getVy());
        } else {
            lastValidState.setAllComponents(currentBallState.getX(), currentBallState.getY(), currentBallState.getVx(), currentBallState.getVy());
        }
    }
    
    private void setTerrainFriction() {
        Vector3 ballPosition = new Vector3((float) currentBallState.getX(), (float) currentBallState.getY(), 0);
        boolean onSand = terrainManager.isBallOnSand(ballPosition);
    
        if (onSand) {
            gamePhysicsEngine.setFriction(sandFrictionKinetic, sandFrictionStatic);
        } else {
            gamePhysicsEngine.setFriction(grassFrictionKinetic, grassFrictionStatic);
        }
    }
    
    private void ruleBasedPlay(){
        if(!cameraCorrectlyPut()){
            wallE.switchToRuleBased();
            wallE.setDirection();
        }
        if(!isBallAllowedToMove){
            wallE.hit();
        }
    }

    private void hillClimbingBotPlay() {
        if (!cameraCorrectlyPut()) {
            wallE.switchToHillClimbing();
            wallE.setDirection();
        }
        if (!isBallAllowedToMove) {
            wallE.hit();
        }
    }
    
    private void scoreChange(){
        scoreLabel.setText("Score: " + score++);
    }

    private void updateCameraPosition(float delta) {
        float ballZ = terrainManager.getTerrainHeight((float) currentBallState.getX(), (float) currentBallState.getY()) + BALL_HEIGHT_OFFSET;
        float cameraX = (float) (currentBallState.getX() + cameraDistance * Math.cos(cameraViewAngle));
        float cameraY = (float) (currentBallState.getY() + cameraDistance * Math.sin(cameraViewAngle));
        mainCamera.position.set(cameraX, ballZ + CAMERA_HEIGHT, cameraY);
        mainCamera.lookAt((float) currentBallState.getX(), ballZ + 1f, (float) currentBallState.getY());
        mainCamera.up.set(Vector3.Y);
        mainCamera.update();
    }

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

    public void setHeightFunction(Function newHeightFunction) {
        this.terrainHeightFunction = newHeightFunction;
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
        setPositionForFlagAndStemInstances();
    }

    public void setCameraAngel(float newCameraAngel){
        cameraViewAngle = newCameraAngel;
    }

    public float getCameraAngel(){
        return cameraViewAngle;
    }
    
    public static float getGoalTolerance() {
        return GOAL_TOLERANCE;
    }

    public BallState getBallState(){
        return currentBallState;
    }

    public BallState getGoalState(){
        return goalState;
    }

    public Camera getMainCamera(){
        return mainCamera;
    }
    
    public float getCurrentSpeedBar(){
        return currentSpeed;
    }
    
    public PhysicsEngine getPhysicsEngine(){
        return gamePhysicsEngine;
    }
    
    public WallE wallE(){
        return wallE;
    }

    public TerrainManager getTerrainManager(){
        return terrainManager;
    }

    public float getFriction(float x, float y) {
        Vector3 position = new Vector3(x, y, terrainManager.getTerrainHeight(x, y));
        return terrainManager.isBallOnSand(position) ? (float) sandFrictionKinetic : (float) grassFrictionKinetic;
    }
    
    
    public boolean cameraCorrectlyPut(){
        // if the ball is rolling, camera position does not matter
        if (currentBallState.getVx()>0.01||currentBallState.getVy()>0.01){
            return true;
        }
        Vector2 ballToGoal = new Vector2((float)(goalState.getX()-currentBallState.getX()), (float)(goalState.getY()-currentBallState.getY())).nor();
        Vector2 camVector2 = new Vector2(mainCamera.direction.x, mainCamera.direction.z).nor();
        return (Math.abs(ballToGoal.x-camVector2.x)<0.001)&&(Math.abs(ballToGoal.y-camVector2.y)<0.001);
    }
    
    private void pauseGame() {
        isPaused = !isPaused;
        if (isPaused) {
            pauseDialog.show(stage);
        } else {
            pauseDialog.hide();
        }
    }
    public void toggleRuleBasedBotActiveness(){
        ruleBasedBotActive = !ruleBasedBotActive;
    }

    public void setRuleBasedBotActive(boolean activeness){
        ruleBasedBotActive = activeness;
    }
    
    public void toggleHillClimbingBotActiveness(){
        hillClimbingBotActive= !hillClimbingBotActive;
    }

    public void setHillClimbingBotActive(boolean activeness){
        hillClimbingBotActive = activeness;
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
