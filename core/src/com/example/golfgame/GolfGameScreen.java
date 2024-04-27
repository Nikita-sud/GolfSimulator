package com.example.golfgame;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.example.golfgame.ODE.*;

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
    private Environment gameEnvironment;
    private CameraInputController cameraController;
    private DirectionalShadowLight mainShadowLight;
    private ModelBatch shadowModelBatch;
    private PhysicsEngine gamePhysicsEngine;
    private BallState currentBallState;
    private Texture grassTexture;
    private Function terrainHeightFunction;
    private float cameraDistance = 10;
    private float cameraViewAngle = 0;
    private float ballSpeed = 10;
    private AssetManager assetManager;
    private float terrainCenterX = 0;
    private float terrainCenterZ = 0;
    private Weather weather = new Weather(0);
    private Stage stage;
    private TextButton button;
    private Label facingLabel;

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
        assetManager.load("models/sphere.obj", Model.class);
        assetManager.finishLoading();
    }

    /**
     * Initializes game components such as models, environment, and camera settings.
     */
    protected void initializeComponents() {
        mainModelBatch = new ModelBatch();
        grassTexture = assetManager.get("textures/grassTexture.jpeg", Texture.class);
        golfBallModel = assetManager.get("models/sphere.obj", Model.class);
        terrainHeightFunction = mainGame.getSettingsScreen().getCurHeightFunction();
        ODE solver = new RungeKutta();
        currentBallState = new BallState(0, 0, 1000, 1000);
        gamePhysicsEngine = new PhysicsEngine(solver, terrainHeightFunction, 0.1);
        mainCamera = new PerspectiveCamera(100, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mainCamera.position.set(1f, 1f, 1f);
        mainCamera.lookAt(0f, 0f, 0f);
        mainCamera.near = 0.1f;
        mainCamera.far = 300.0f;
        mainCamera.update();
        mainShadowLight = new DirectionalShadowLight(2048*2, 2048*2, 500f, 500f, 0.01f, 1000f);
        Float sunlight = (float)mainGame.getGolfGameScreen().getWeather().getSun();
        mainShadowLight.set(0.8f*sunlight, 0.8f*sunlight, 0.8f*sunlight, -1f, -0.8f, -0.2f);
        gameEnvironment = new Environment();
        gameEnvironment.add(mainShadowLight);
        gameEnvironment.shadowMap = mainShadowLight;
        shadowModelBatch = new ModelBatch(new DepthShaderProvider());
        golfBallInstance = new ModelInstance(golfBallModel);
        golfCourseInstances = createTerrainModels(terrainHeightFunction, 200, 200, 1.0f, 4, 0, 0);
        cameraController = new CameraInputController(mainCamera);
        Gdx.input.setInputProcessor(cameraController);
    }

    /**
     * Creates terrain models based on a specified height function to simulate varying terrain elevations.
     *
     * @param heightFunction the mathematical function used to determine terrain elevation at any point
     * @param gridWidth the number of horizontal divisions in the terrain mesh
     * @param gridHeight the number of vertical divisions in the terrain mesh
     * @param scale the scale factor for the size of the terrain
     * @param parts the number of parts the terrain is divided into for rendering
     * @param centerX the X coordinate at the center of the terrain grid
     * @param centerZ the Z coordinate at the center of the terrain grid
     * @return a list of model instances representing the terrain
     */
    private List<ModelInstance> createTerrainModels(Function heightFunction, int gridWidth, int gridHeight, float scale, int parts, float centerX, float centerZ) {
        ModelBuilder modelBuilder = new ModelBuilder();
        List<ModelInstance> golfCourseInstances = new ArrayList<>();
    
        int partWidth = gridWidth / parts;
        int partHeight = gridHeight / parts;
    
        float halfTotalWidth = gridWidth * scale * 0.5f;
        float halfTotalHeight = gridHeight * scale * 0.5f;
    
        for (int pz = 0; pz < parts; pz++) {
            for (int px = 0; px < parts; px++) {
                modelBuilder.begin();
                MeshPartBuilder meshBuilder = modelBuilder.part("terrain_part_" + pz + "_" + px, GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, new Material(TextureAttribute.createDiffuse(grassTexture)));
                MeshPartBuilder lineMeshBuilder = modelBuilder.part("terrain_lines_" + pz + "_" + px, GL20.GL_LINES, Usage.Position | Usage.Normal, new Material(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE)));
    
                for (int z = 0; z <= partHeight; z++) {
                    for (int x = 0; x <= partWidth; x++) {
                        float worldX = centerX + (x + px * partWidth) * scale - halfTotalWidth;
                        float worldZ = centerZ + (z + pz * partHeight) * scale - halfTotalHeight;
                        float height = getTerrainHeight(worldX, worldZ);
                        float textureU = (worldX + gridWidth * scale / 2) / (gridWidth * scale);
                        float textureV = (worldZ + gridHeight * scale / 2) / (gridHeight * scale);
    
                        meshBuilder.vertex(new float[]{worldX, height, worldZ, 0, 1, 0, textureU, textureV});
                        lineMeshBuilder.vertex(new float[]{worldX, height, worldZ, 0, 1, 0});
                    }
                }
    
                for (int z = 0; z < partHeight; z++) {
                    for (int x = 0; x < partWidth; x++) {
                        int base = (partWidth + 1) * z + x;
                        short index1 = (short) (base);
                        short index2 = (short) (base + 1);
                        short index3 = (short) (base + partWidth + 1);
                        short index4 = (short) (base + partWidth + 2);
    
                        meshBuilder.index(index1, index3, index2);
                        meshBuilder.index(index2, index3, index4);
                        // Add line indices for wireframe
                        lineMeshBuilder.index(index1, index2);
                        lineMeshBuilder.index(index2, index4);
                        lineMeshBuilder.index(index4, index3);
                        lineMeshBuilder.index(index3, index1);
                    }
                }
    
                Model partModel = modelBuilder.end();
                ModelInstance partInstance = new ModelInstance(partModel);
                golfCourseInstances.add(partInstance);
            }
        }
    
        return golfCourseInstances;
    }
    

    /**
     * Calculates the height of the terrain at a given coordinate based on the defined height function.
     *
     * @param x the x-coordinate of the location
     * @param z the z-coordinate of the location
     * @return the height at the given location
     */
    private float getTerrainHeight(float x, float z) {
        Map<String, Double> args = new HashMap<>();
        args.put("x", (double) x);
        args.put("y", (double) z);
        return (float) terrainHeightFunction.evaluate(args);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(cameraController);
        if (golfCourseInstances == null || golfCourseInstances.isEmpty()) {
            golfCourseInstances = createTerrainModels(terrainHeightFunction, 200, 200, 1.0f, 4, 0, 0);
        }
        resetGameState();

        // Stage for other elements
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        Table table = new Table();
        table.setPosition(900, 430);
        table.setFillParent(true);
        stage.addActor(table);
        Skin skin = new Skin(Gdx.files.internal("assets/uiskin.json"));  // Load the UI skin
        button = new TextButton("Settings", skin);

        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){

                mainGame.setScreen(mainGame.getSettingsScreen());
            }
        });

        Label windLabel = new Label("vxWind="+String.format("%.4f", mainGame.getGolfGameScreen().getWeather().getWind()[0])+"\nvyWind="+String.format("%.4f", mainGame.getGolfGameScreen().getWeather().getWind()[1])+"\nvzWind="+String.format("%.4f", mainGame.getGolfGameScreen().getWeather().getWind()[2]), skin);
        facingLabel = new Label("Facing(x,y,z): "+String.format("%.2f", mainCamera.direction.x)+", "+String.format("%.2f", mainCamera.direction.z)+", "+String.format("%.2f", mainCamera.direction.y), skin);
        table.add(button).pad(10).row();
        table.add(windLabel).row();
        facingLabel.setPosition(20, 910);
        stage.addActor(facingLabel);

        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Resets the game state to initial conditions, setting the position and velocity of the golf ball,
     * camera position, and view angles to their starting values.
     */
    private void resetGameState() {
        currentBallState.setAllComponents(0,0,0.001,0.001);
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
            pause();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            currentBallState.setVx(-ballSpeed * Math.cos(cameraViewAngle));
            currentBallState.setVy(-ballSpeed * Math.sin(cameraViewAngle));
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
        // With a certain probability, wind changes from time to time
        if (currentBallState.getVx()>0.01||currentBallState.getVy()>0.01){
            currentBallState.setVx(weather.getWind()[0]+currentBallState.getVx());
            currentBallState.setVy(weather.getWind()[1]+currentBallState.getVy());
        }
    
        currentBallState = gamePhysicsEngine.update(currentBallState, deltaTime);
        checkAndReloadTerrainIfNeeded();
        float radius = 1f;
        float ballZ = getTerrainHeight((float) currentBallState.getX(), (float) currentBallState.getY()) + radius;
        golfBallInstance.transform.setToTranslation((float) currentBallState.getX(), ballZ, (float) currentBallState.getY());
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
    private void checkAndReloadTerrainIfNeeded() {
        Vector3 ballPosition = new Vector3((float) currentBallState.getX(), (float) currentBallState.getY(), 0);
        Vector3 terrainCenter = new Vector3(terrainCenterX, terrainCenterZ, 0);

        if (ballPosition.dst(terrainCenter) > 50) { // Threshold distance to trigger terrain reload
            reloadTerrain(ballPosition.x, ballPosition.y);
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
        golfCourseInstances = createTerrainModels(terrainHeightFunction, 200, 200, 1.0f, 4, x, y);
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
        shadowModelBatch.render(golfBallInstance, gameEnvironment);

        shadowModelBatch.begin(mainShadowLight.getCamera());
        shadowModelBatch.end();
        mainShadowLight.end();
        mainModelBatch.begin(mainCamera);

        for (ModelInstance terrainInstance : golfCourseInstances) {
            shadowModelBatch.render(terrainInstance, gameEnvironment);
            mainModelBatch.render(terrainInstance, gameEnvironment);
        }
        mainModelBatch.render(golfBallInstance, gameEnvironment);
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
        mainModelBatch.dispose();
        mainShadowLight.dispose();
        shadowModelBatch.dispose();
        grassTexture.dispose();
    }

}
