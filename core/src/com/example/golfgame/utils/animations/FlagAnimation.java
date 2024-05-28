package com.example.golfgame.utils.animations;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.utils.Array;

/**
 * A class to animate a flag in a 3D model instance.
 * This class applies a waving effect to the flag by modifying its vertex positions.
 */
public class FlagAnimation {
    private ModelInstance flagInstance;
    private float elapsedTime = 0;
    private float waveSpeed = 1.0f;
    private float waveAmplitude = 0.1f;
    private float crossWaveSpeed = 0.9f; // Speed for cross wave
    private float crossWaveAmplitude = 0.3f; // Amplitude for cross wave
    private Array<Vector3> originalVertices;

    /**
     * Constructs a FlagAnimation object with the specified flag model instance.
     *
     * @param flagInstance The ModelInstance representing the flag to be animated.
     */
    public FlagAnimation(ModelInstance flagInstance) {
        this.flagInstance = flagInstance;
        this.originalVertices = new Array<>();

        // Store the original vertices
        for (Mesh mesh : flagInstance.model.meshes) {
            float[] vertices = new float[mesh.getNumVertices() * mesh.getVertexAttributes().vertexSize / 4];
            mesh.getVertices(vertices);
            for (int i = 0; i < vertices.length; i += mesh.getVertexAttributes().vertexSize / 4) {
                originalVertices.add(new Vector3(vertices[i], vertices[i + 1], vertices[i + 2]));
            }
        }
    }

    /**
     * Updates the flag animation by modifying the vertex positions to create a waving effect.
     *
     * @param deltaTime The time elapsed since the last frame, used to update the animation.
     */
    public void update(float deltaTime) {
        elapsedTime += deltaTime;
        float[] vertices = new float[flagInstance.model.meshes.first().getNumVertices() * flagInstance.model.meshes.first().getVertexAttributes().vertexSize / 4];

        int vertexIndex = 0;
        for (Mesh mesh : flagInstance.model.meshes) {
            mesh.getVertices(vertices);

            for (int i = 0; i < vertices.length; i += mesh.getVertexAttributes().vertexSize / 4) {
                Vector3 originalVertex = originalVertices.get(vertexIndex++);
                float offsetY = waveAmplitude * (float) Math.sin(waveSpeed * elapsedTime + originalVertex.x);
                float offsetX = crossWaveAmplitude * (float) Math.sin(crossWaveSpeed * elapsedTime + originalVertex.y);

                vertices[i] = originalVertex.x + offsetX; // Modify x-coordinate for cross wave effect
                vertices[i + 1] = originalVertex.y + offsetY; // Modify y-coordinate for main wave effect
            }

            mesh.setVertices(vertices);
        }

        flagInstance.calculateTransforms();
    }
}
