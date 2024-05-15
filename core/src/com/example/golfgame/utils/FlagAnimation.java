package com.example.golfgame.utils;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.utils.Array;

public class FlagAnimation {
    private ModelInstance flagInstance;
    private float elapsedTime = 0;
    private float waveSpeed = 1.0f;
    private float waveAmplitude = 0.2f;
    private float crossWaveSpeed = 0.8f; // Speed for cross wave
    private float crossWaveAmplitude = 0.1f; // Amplitude for cross wave
    private Array<Vector3> originalVertices;

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
