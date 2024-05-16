package com.example.golfgame.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import java.util.ArrayList;
import java.util.List;

public class WaterSurfaceManager {
    private float width, depth;
    private int gridResolution;

    /**
     * Constructs a WaterSurfaceManager with specified dimensions for the water surface.
     *
     * @param width          the width of the water surface
     * @param depth          the depth of the water surface
     * @param gridResolution the number of segments along each axis of the water grid
     */
    public WaterSurfaceManager(float width, float depth, int gridResolution) {
        this.width = width;
        this.depth = depth;
        this.gridResolution = gridResolution;
    }

    /**
     * Creates a grid water surface model centered at specified coordinates in the 3D world.
     * The surface is partially transparent and blue, simulating water.
     *
     * @param centerX the X-coordinate of the center of the water surface
     * @param centerZ the Z-coordinate of the center of the water surface
     * @return a new {@link ModelInstance} representing the water surface, which can be rendered in a 3D scene
     */
    public List<ModelInstance> createWaterSurface(float centerX, float centerZ) {
        ModelBuilder modelBuilder = new ModelBuilder();
        List<ModelInstance> waterInstances = new ArrayList<>();

        int partWidth = (int) (width / gridResolution);
        int partDepth = (int) (depth / gridResolution);
        float halfTotalWidth = width * 0.5f;
        float halfTotalDepth = depth * 0.5f;

        for (int pz = 0; pz < gridResolution; pz++) {
            for (int px = 0; px < gridResolution; px++) {
                modelBuilder.begin();
                MeshPartBuilder meshBuilder = modelBuilder.part("water_part_" + pz + "_" + px, GL20.GL_TRIANGLES, Usage.Position | Usage.Normal,
                        new Material(ColorAttribute.createDiffuse(new Color(0, 0, 1, 0.5f)), new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.8f)));

                for (int z = 0; z <= partDepth; z++) {
                    for (int x = 0; x <= partWidth; x++) {
                        float worldX = centerX + (x + px * partWidth) - halfTotalWidth;
                        float worldZ = centerZ + (z + pz * partDepth) - halfTotalDepth;
                        float height = 0; // Initial height for the water surface

                        meshBuilder.vertex(new float[]{worldX, height, worldZ, 0, 1, 0});
                    }
                }

                for (int z = 0; z < partDepth; z++) {
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
                waterInstances.add(partInstance);
            }
        }

        return waterInstances;
    }
}
