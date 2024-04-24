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
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import com.example.golfgame.ODE.*;

public class GolfGameScreen implements Screen, Disposable {
    @SuppressWarnings("unused")
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


    public GolfGameScreen(GolfGame game, AssetManager assetManager) {
        if (assetManager == null) {
            throw new IllegalArgumentException("AssetManager must not be null");
        }
        this.mainGame = game;
        this.assetManager = assetManager;
        loadAssets();
        initializeComponents();
    }

    private void loadAssets() {
        assetManager.load("textures/grassTexture.jpeg", Texture.class);
        assetManager.load("models/sphere.obj", Model.class);
        assetManager.finishLoading();
    }

    private void initializeComponents() {
        mainModelBatch = new ModelBatch();
        grassTexture = assetManager.get("textures/grassTexture.jpeg", Texture.class);
        golfBallModel = assetManager.get("models/sphere.obj", Model.class);
        terrainHeightFunction = new Function("sin(x) * cos(y)", "x", "y");
        ODE solver = new RungeKutta();
        currentBallState = new BallState(0, 0, 0.01, 0.01);
        gamePhysicsEngine = new PhysicsEngine(solver, terrainHeightFunction, 0.1);
        mainCamera = new PerspectiveCamera(100, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mainCamera.position.set(1f, 1f, 1f);
        mainCamera.lookAt(0f, 0f, 0f);
        mainCamera.near = 0.1f;
        mainCamera.far = 300.0f;
        mainCamera.update();
        mainShadowLight = new DirectionalShadowLight(2048*3, 2048*3, 500f, 500f, 0.01f, 1000f);
        mainShadowLight.set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f);
        gameEnvironment = new Environment();
        gameEnvironment.add(mainShadowLight);
        gameEnvironment.shadowMap = mainShadowLight;
        shadowModelBatch = new ModelBatch(new DepthShaderProvider());
        golfBallInstance = new ModelInstance(golfBallModel);
        golfCourseInstances = createTerrainModels(terrainHeightFunction, 200, 200, 1.0f, 4, 0, 0); 
        cameraController = new CameraInputController(mainCamera);
        Gdx.input.setInputProcessor(cameraController);
    }
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

            // Создание вершин для каждой части
            for (int z = 0; z <= partHeight; z++) {
                for (int x = 0; x <= partWidth; x++) {
                    float worldX = centerX + (x + px * partWidth) * scale - halfTotalWidth;
                    float worldZ = centerZ + (z + pz * partHeight) * scale - halfTotalHeight;
                    float height = getTerrainHeight(worldX, worldZ);

                    // Рассчитываем глобальные текстурные координаты
                    float textureU = (worldX + gridWidth * scale / 2) / (gridWidth * scale);
                    float textureV = (worldZ + gridHeight * scale / 2) / (gridHeight * scale);

                    meshBuilder.vertex(new float[]{worldX, height, worldZ, 0, 1, 0, textureU, textureV});
                }
            }

            // Создание индексов для каждой части
            for (int z = 0; z < partHeight; z++) {
                for (int x = 0; x < partWidth; x++) {
                    int base = (partWidth + 1) * z + x;
                    short index1 = (short) (base);
                    short index2 = (short) (base + 1);
                    short index3 = (short) (base + partWidth + 1);
                    short index4 = (short) (base + partWidth + 2);

                    meshBuilder.index(index1, index3, index2);
                    meshBuilder.index(index2, index3, index4);
                }
            }

            Model partModel = modelBuilder.end();
            ModelInstance partInstance = new ModelInstance(partModel);
            golfCourseInstances.add(partInstance);
        }
    }

    return golfCourseInstances;
}

    
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
    }

    private void resetGameState() {
        currentBallState.setAllComponents(0,0,0.001,0.001);
        cameraViewAngle = 0;              
        cameraDistance = 10;               
        mainCamera.position.set(1f, 1f, 1f);
        mainCamera.lookAt(0f, 0f, 0f);
        mainCamera.update();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            currentBallState.setVx(-ballSpeed * Math.cos(cameraViewAngle));
            currentBallState.setVy(-ballSpeed * Math.sin(cameraViewAngle));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            cameraViewAngle += 0.05; // Rotate camera left
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            cameraViewAngle -= 0.05; // Rotate camera right
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            cameraDistance = Math.max(5, cameraDistance - 0.1f); // Zoom in
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            cameraDistance = Math.min(15, cameraDistance + 0.1f); // Zoom out
        }
    }

    @Override
    public void render(float delta) {
        handleInput();
        update(delta);
        draw();
    }

    private void update(float deltaTime) {
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

    private void checkAndReloadTerrainIfNeeded() {
        Vector3 ballPosition = new Vector3((float) currentBallState.getX(), (float) currentBallState.getY(), 0);
        Vector3 terrainCenter = new Vector3(terrainCenterX, terrainCenterZ, 0);
    
        // Проверяем, находится ли мяч в пределах безопасной зоны относительно центра террейна
        if (ballPosition.dst(terrainCenter) > 50) { // Пороговое значение можно настроить
            reloadTerrain(ballPosition.x, ballPosition.y);
        }
    }

    private void reloadTerrain(float x, float y) {
        // Обновляем координаты центра террейна
        terrainCenterX = x;
        terrainCenterZ = y;
        // Перегенерируем террейн с новым центром
        golfCourseInstances.clear();
        golfCourseInstances = createTerrainModels(terrainHeightFunction, 200, 200, 1.0f, 4, x, y);
        System.out.println("Terrain reloaded around position: " + x + ", " + y);
    }

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
    }

    @Override
    public void dispose() {
        mainModelBatch.dispose();
        mainShadowLight.dispose();
        shadowModelBatch.dispose();
        grassTexture.dispose();
    }
}
