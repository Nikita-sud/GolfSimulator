package com.example.golfgame.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class TerrainManager {
    private Function heightFunction;
    private Texture grassTexture;
    private int gridWidth, gridHeight;
    private float scale;
    private int parts;

    public TerrainManager(Function heightFunction, Texture grassTexture, int gridWidth, int gridHeight, float scale, int parts) {
        this.heightFunction = heightFunction;
        this.grassTexture = grassTexture;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.scale = scale;
        this.parts = parts;
    }

    public List<ModelInstance> createTerrainModels(float centerX, float centerZ) {
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

    public float getTerrainHeight(float x, float z) {
        Map<String, Double> args = new HashMap<>();
        args.put("x", (double) x);
        args.put("y", (double) z);
        return (float) heightFunction.evaluate(args);
    }
}

