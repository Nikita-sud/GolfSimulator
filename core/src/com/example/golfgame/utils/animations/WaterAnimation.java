package com.example.golfgame.utils.animations;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.utils.Array;

/**
 * A class to animate water in a 3D model instance.
 * This class applies a wave effect to the water by modifying its vertex positions.
 */
public class WaterAnimation {
    private ModelInstance waterInstance;
    private float elapsedTime = 0;
    private float waveSpeed = 1.0f;
    private float waveAmplitude = 0.01f;
    private float crossWaveSpeed = 0.8f; // Speed for cross wave
    private float crossWaveAmplitude = 5.5f; // Amplitude for cross wave
    private float spatialFrequencyMultiplier = 5.0f; // Higher value means shorter wavelength
    private float crossWaveSpatialFrequencyMultiplier = 5.0f; // Higher value means shorter wavelength for cross waves
    private Array<Vector3> originalVertices;

    /**
     * Constructs a WaterAnimation object with the specified water model instance.
     *
     * @param waterInstance The ModelInstance representing the water to be animated.
     */
    public WaterAnimation(ModelInstance waterInstance) {
        this.waterInstance = waterInstance;
        this.originalVertices = new Array<>();

        // Store the original vertices
        for (Mesh mesh : waterInstance.model.meshes) {
            float[] vertices = new float[mesh.getNumVertices() * mesh.getVertexAttributes().vertexSize / 4];
            mesh.getVertices(vertices);
            for (int i = 0; i < vertices.length; i += mesh.getVertexAttributes().vertexSize / 4) {
                originalVertices.add(new Vector3(vertices[i], vertices[i + 1], vertices[i + 2]));
            }
        }
    }

    /**
     * Updates the water animation by modifying the vertex positions to create a wave effect.
     *
     * @param deltaTime The time elapsed since the last frame, used to update the animation.
     */
    public void update(float deltaTime) {
        elapsedTime += deltaTime;
        float[] vertices = new float[waterInstance.model.meshes.first().getNumVertices() * waterInstance.model.meshes.first().getVertexAttributes().vertexSize / 4];

        int vertexIndex = 0;
        for (Mesh mesh : waterInstance.model.meshes) {
            mesh.getVertices(vertices);

            for (int i = 0; i < vertices.length; i += mesh.getVertexAttributes().vertexSize / 4) {
                Vector3 originalVertex = originalVertices.get(vertexIndex++);
                float offsetY = waveAmplitude * (float) Math.sin(spatialFrequencyMultiplier * originalVertex.x - waveSpeed * elapsedTime);
                float offsetX = crossWaveAmplitude * (float) Math.sin(crossWaveSpatialFrequencyMultiplier * originalVertex.z - crossWaveSpeed * elapsedTime);

                vertices[i] = originalVertex.x + offsetX; // Modify x-coordinate for cross wave effect
                vertices[i + 1] = originalVertex.y + offsetY; // Modify y-coordinate for main wave effect
                vertices[i + 2] = originalVertex.z; // Keep z-coordinate unchanged
            }

            mesh.setVertices(vertices);
        }

        waterInstance.calculateTransforms();
    }
}
