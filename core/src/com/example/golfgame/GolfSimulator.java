package com.example.golfgame;

import com.badlogic.gdx.ApplicationListener;
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
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
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
import java.util.Map;
import com.example.golfgame.ODE.*;

/**
 * This class serves as the main simulator for the golf game, handling all rendering,
 * input processing, and physics updates for the game.
 */
public class GolfSimulator implements ApplicationListener, Disposable {
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Model model;
    private Model ballModel;
    private ModelInstance ballInstance;
    private ModelInstance modelInstance;
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

    /**
     * Creates the terrain model for the golf game based on a height function.
     *
     * @param heightFunction The function used to determine terrain height at any point.
     * @param width The width of the terrain.
     * @param depth The depth of the terrain.
     * @param widthSegments The number of horizontal segments for the mesh.
     * @param depthSegments The number of vertical segments for the mesh.
     * @return The model representing the terrain.
     */
    private Model createTerrainModel(Function heightFunction, float width, float depth, int widthSegments, int depthSegments) {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        Material material = new Material(TextureAttribute.createDiffuse(grassTexture), ColorAttribute.createSpecular(1, 1, 1, 1), FloatAttribute.createShininess(8f));
        material.set(IntAttribute.createCullFace(GL20.GL_NONE));
        MeshPartBuilder builder = modelBuilder.part("terrain", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, material);
        float halfWidth = width / 2;
        float halfDepth = depth / 2;
        float deltaX = width / widthSegments;
        float deltaY = depth / depthSegments;
        builder.setUVRange(0, 0, 1, 1);
        for (int z = 0; z < depthSegments; z++) {
            for (int x = 0; x < widthSegments; x++) {
                float x1 = -halfWidth + x * deltaX;
                float x2 = x1 + deltaX;
                float z1 = -halfDepth + z * deltaY;
                float z2 = z1 + deltaY;
                float y1 = getTerrainHeight(x1, z1);
                float y2 = getTerrainHeight(x2, z1);
                float y3 = getTerrainHeight(x2, z2);
                float y4 = getTerrainHeight(x1, z2);
                builder.rect(new Vector3(x1, y1, z1), new Vector3(x2, y2, z1), new Vector3(x2, y3, z2), new Vector3(x1, y4, z2), new Vector3(0, 1, 0));
            }
        }
        return modelBuilder.end();
    }

    /**
     * Calculates the terrain height at a given location.
     *
     * @param x The x-coordinate at which to calculate the height.
     * @param z The z-coordinate at which to calculate the height.
     * @return The height of the terrain at the given coordinates.
     */
    private float getTerrainHeight(float x, float z) {
        Map<String, Double> args = new HashMap<>();
        args.put("x", (double) x);
        args.put("y", (double) z);
        return (float) heightFunction.evaluate(args);
    }

    @Override
    public void create() {
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
        model = createTerrainModel(heightFunction, 100, 100, 110, 10);
        modelInstance = new ModelInstance(model);
        camController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(camController);
    }

    @Override
    public void render() {
        handleInput();
        float deltaTime = Gdx.graphics.getDeltaTime();
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
        Gdx.gl.glClearColor(0.5f, 0.7f, 1.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        camController.update();
        shadowLight.begin(Vector3.Zero, camera.direction);
        shadowBatch.begin(shadowLight.getCamera());
        shadowBatch.render(modelInstance, environment);
        shadowBatch.end();
        shadowLight.end();
        modelBatch.begin(camera);
        modelBatch.render(modelInstance, environment);
        modelBatch.render(ballInstance, environment);
        modelBatch.end();
    }

    /**
     * Processes user input to adjust the game state.
     */
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
    public void dispose() {
        modelBatch.dispose();
        model.dispose();
        shadowLight.dispose();
        shadowBatch.dispose();
        grassTexture.dispose();
    }
}
