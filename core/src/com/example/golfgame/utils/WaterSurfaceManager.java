package com.example.golfgame.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

/**
 * Manages the creation of water surface models for graphical representation in a 3D environment.
 * This class uses libGDX's ModelBuilder to dynamically generate water surfaces based on specified dimensions.
 */
public class WaterSurfaceManager {
    private float width, depth;

    /**
     * Constructs a WaterSurfaceManager with specified dimensions for the water surface.
     *
     * @param width  the width of the water surface
     * @param depth  the depth of the water surface
     */
    public WaterSurfaceManager(float width, float depth) {
        this.width = width;
        this.depth = depth;
    }

    /**
     * Creates a rectangular water surface model centered at specified coordinates in the 3D world.
     * The surface is partially transparent and blue, simulating water.
     *
     * @param centerX the X-coordinate of the center of the water surface
     * @param centerZ the Z-coordinate of the center of the water surface
     * @return a new {@link ModelInstance} representing the water surface, which can be rendered in a 3D scene
     */
    public ModelInstance createWaterSurface(float centerX, float centerZ) {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("water", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal,
                new Material(ColorAttribute.createDiffuse(new Color(0, 0, 1, 0.5f))));
        float halfWidth = width / 2;
        float halfDepth = depth / 2;
        builder.rect(
            centerX - halfWidth, 0, centerZ + halfDepth,
            centerX + halfWidth, 0, centerZ + halfDepth,
            centerX + halfWidth, 0, centerZ - halfDepth,
            centerX - halfWidth, 0, centerZ - halfDepth,
            0, 1, 0
        );
        Model waterModel = modelBuilder.end();
        return new ModelInstance(waterModel);
    }
}
