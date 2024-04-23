package com.example.golfgame;

import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
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
    private GolfGame game;  
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Model ballModel;
    private ModelInstance ballInstance;
    private List<ModelInstance> terrainInstances;
    private Environment environment;
    private CameraInputController camController;
    private DirectionalShadowLight shadowLight;
    private ModelBatch shadowBatch;
    private PhysicsEngine physicsEngine;
    private BallState ballState;
    private Texture grassTexture;
    private Function heightFunction;
    private float cameraDistance = 10;
    private float cameraAngle = 0; 
    private float speed;

    public GolfGameScreen(GolfGame game) {
        this.game = game;
        create();
    }
    private List<ModelInstance> createTerrainModels(Function heightFunction, int gridWidth, int gridHeight, float scale, int parts) {
        ModelBuilder modelBuilder = new ModelBuilder();
        List<ModelInstance> terrainInstances = new ArrayList<>();
    
        int partWidth = gridWidth / parts;
        int partHeight = gridHeight / parts;
    
        for (int pz = 0; pz < parts; pz++) {
            for (int px = 0; px < parts; px++) {
                modelBuilder.begin();
                MeshPartBuilder meshBuilder = modelBuilder.part("terrain_part_" + pz + "_" + px, GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, new Material(TextureAttribute.createDiffuse(grassTexture)));
    
                // Создание вершин для каждой части
                for (int z = 0; z <= partHeight; z++) {
                    for (int x = 0; x <= partWidth; x++) {
                        float worldX = (x + px * partWidth) * scale - gridWidth * scale * 0.5f;
                        float worldZ = (z + pz * partHeight) * scale - gridHeight * scale * 0.5f;
                        float height = getTerrainHeight(worldX, worldZ);
                        meshBuilder.vertex(new float[]{worldX, height, worldZ, 0, 1, 0, (x + px * partWidth) / (float) gridWidth, (z + pz * partHeight) / (float) gridHeight});
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
                terrainInstances.add(partInstance);
            }
        }
    
        return terrainInstances;
    }
    private float getTerrainHeight(float x, float z) {
        Map<String, Double> args = new HashMap<>();
        args.put("x", (double) x);
        args.put("y", (double) z);
        return (float) heightFunction.evaluate(args);
    }
    private void create() {
        modelBatch = new ModelBatch();
        heightFunction = new Function("sin(x) * cos(y)", "x", "y");
        ODE solver = new RungeKutta();
        ballState = new BallState(0, 0, 0.01, 0.01);
        physicsEngine = new PhysicsEngine(solver, heightFunction, 0.1);
        speed = 10;
        grassTexture = new Texture(Gdx.files.internal("textures/grassTexture.jpeg"));
        camera = new PerspectiveCamera(100, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(1f, 1f, 1f);
        camera.lookAt(0f, 0f, 0f);
        camera.near = 0.1f;
        camera.far = 300.0f;
        camera.update();
        shadowLight = new DirectionalShadowLight(1024, 1024, 500f, 500f, 0.1f, 100f);
        shadowLight.set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f);
        environment = new Environment();
        environment.add(shadowLight);
        environment.shadowMap = shadowLight;
        shadowBatch = new ModelBatch(new DepthShaderProvider());
        ObjLoader loader = new ObjLoader();
        ballModel = loader.loadModel(Gdx.files.internal("models/sphere.obj"));
        ballInstance = new ModelInstance(ballModel);
        terrainInstances = createTerrainModels(heightFunction, 200, 200, 1.0f, 4); 
        camController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(camController);
    }

    @Override
    public void show() {
        // Setup the environment, load models, etc.
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            ballState.setVx(-speed * Math.cos(cameraAngle));
            ballState.setVy(-speed * Math.sin(cameraAngle));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            cameraAngle += 0.05; // Rotate camera left
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            cameraAngle -= 0.05; // Rotate camera right
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
        ballState = physicsEngine.update(ballState, deltaTime);
        float radius = 1f;
        float ballZ = getTerrainHeight((float) ballState.getX(), (float) ballState.getY()) + radius;
        ballInstance.transform.setToTranslation((float) ballState.getX(), ballZ, (float) ballState.getY());
        float cameraX = (float) (ballState.getX() + cameraDistance * Math.cos(cameraAngle));
        float cameraY = (float) (ballState.getY() + cameraDistance * Math.sin(cameraAngle));
        camera.position.set(cameraX, ballZ + 5f, cameraY);
        camera.lookAt((float) ballState.getX(), ballZ + 1f, (float) ballState.getY());
        camera.up.set(Vector3.Y);
        camera.update();
        camera.update();
    }

    private void draw() {
        Gdx.gl.glClearColor(0.5f, 0.7f, 1.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        camController.update();
        shadowLight.begin(Vector3.Zero, camera.direction);
        shadowBatch.begin(shadowLight.getCamera());
        for (ModelInstance terrainInstance : terrainInstances) {
            shadowBatch.render(terrainInstance, environment);
        }
        shadowBatch.end();
        shadowLight.end();
        modelBatch.begin(camera);
        for (ModelInstance terrainInstance : terrainInstances) {
            modelBatch.render(terrainInstance, environment);
        }
        modelBatch.render(ballInstance, environment);
        modelBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
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
        modelBatch.dispose();
        shadowLight.dispose();
        shadowBatch.dispose();
        grassTexture.dispose();
    }

    public void setHeightFunction(Function heightFunction){
        this.heightFunction = heightFunction;
    }
}
