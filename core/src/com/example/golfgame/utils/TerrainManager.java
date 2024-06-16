package com.example.golfgame.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;


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
import com.badlogic.gdx.math.Vector3;
import com.example.golfgame.screens.GolfGameScreen;

/**
 * Manages the terrain generation and properties in the golf game.
 * This includes creating grass, sand, and hole terrain models,
 * as well as determining terrain heights and sand areas.
 */
public class TerrainManager {
    private Function heightFunction;
    private Texture grassTexture, sandTexture, holeTexture;
    private int gridWidth, gridHeight;
    private List<float[]> sandAreas;
    private float[] holeArea;
    private float scale;
    private int parts;

    /**
     * Constructs a TerrainManager with specified parameters.
     *
     * @param heightFunction The function defining the terrain height.
     * @param grassTexture   The texture for grass areas.
     * @param sandTexture    The texture for sand areas.
     * @param holeTexture    The texture for the hole area.
     * @param gridWidth      The width of the terrain grid.
     * @param gridHeight     The height of the terrain grid.
     * @param scale          The scale factor for the terrain.
     * @param parts          The number of parts the terrain is divided into.
     */
    public TerrainManager(Function heightFunction, Texture grassTexture, Texture sandTexture, Texture holeTexture, int gridWidth, int gridHeight, float scale, int parts) {
        this.heightFunction = heightFunction;
        this.grassTexture = grassTexture;
        this.sandTexture = sandTexture;
        this.holeTexture = holeTexture;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.scale = scale;
        this.parts = parts;
        sandAreas = new ArrayList<>();
    }
    public TerrainManager(Function heightFunction, int gridWidth, int gridHeight, float scale, int parts) {
        this.heightFunction = heightFunction;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.scale = scale;
        this.parts = parts;
    }

    /**
     * Creates grass terrain models around a specified center position.
     *
     * @param centerX The x-coordinate of the terrain center.
     * @param centerZ The z-coordinate of the terrain center.
     * @return A list of ModelInstance objects representing the grass terrain.
     */
    public List<ModelInstance> createGrassTerrainModels(float centerX, float centerZ) {
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
     * Creates a model instance for the hole area.
     *
     * @param centerX The x-coordinate of the hole center.
     * @param centerZ The z-coordinate of the hole center.
     * @return A ModelInstance representing the hole terrain.
     */
    public ModelInstance createHoleTerrainModel(float centerX, float centerZ){
        ModelBuilder modelBuilder = new ModelBuilder();

        // Define terrain boundaries
        float minX = centerX - gridWidth * scale * 0.5f;
        float maxX = centerX + gridWidth * scale * 0.5f;
        float minZ = centerZ - gridHeight * scale * 0.5f;
        float maxZ = centerZ + gridHeight * scale * 0.5f;

        // Clipping the hole area to terrain boundaries (drawing it bigger than 1, to rescale for a small picture)
        float x1 = Math.max(minX, (float)(holeArea[0] - GolfGameScreen.getGoalTolerance()));
        float z1 = Math.max(minZ, (float)(holeArea[1] - GolfGameScreen.getGoalTolerance()));
        float x2 = Math.min(maxX, (float)(holeArea[0] + GolfGameScreen.getGoalTolerance()));
        float z2 = Math.min(maxZ, (float)(holeArea[1] + GolfGameScreen.getGoalTolerance()));
        
        modelBuilder.begin();
        holeTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        Material holeMaterial = new Material(TextureAttribute.createDiffuse(holeTexture), ColorAttribute.createSpecular(1, 1, 1, 1));
        MeshPartBuilder meshBuilder = modelBuilder.part("hole_terrain", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, holeMaterial);

        // Calculate grid dimensions based on clipped coordinates
        int numX = (int)((x2 - x1) / scale) + 1;
        int numZ = (int)((z2 - z1) / scale) + 1;

        // Generate vertices and store heights for indexing
        for (int ix = 0; ix < numX; ix++) {
            for (int iz = 0; iz < numZ; iz++) {
                float x = x1 + ix * scale;
                float z = z1 + iz * scale;
                float height = getTerrainHeight(x, z) + 0.1f;
                float u = (float)(x - x1) / (x2 - x1);  // Adjusted to use relative position within clipped area
                float v = (float)(z - z1) / (z2 - z1);
                meshBuilder.vertex(new float[]{x, height, z, 0, 1, 0, u, v});
            }
        }

        // Add indices to form triangles
        for (int ix = 0; ix < numX - 1; ix++) {
            for (int iz = 0; iz < numZ - 1; iz++) {
                int bl = ix * numZ + iz;
                int br = bl + 1;
                int tl = bl + numZ;
                int tr = tl + 1;
                meshBuilder.index((short)tl, (short)bl, (short)br); // Triangle 1
                meshBuilder.index((short)tl, (short)br, (short)tr); // Triangle 2
            }
        }

        Model holeModel = modelBuilder.end();
        return new ModelInstance(holeModel);
    }

    /**
     * Creates sand terrain models around a specified center position.
     *
     * @param centerX The x-coordinate of the terrain center.
     * @param centerZ The z-coordinate of the terrain center.
     * @return A list of ModelInstance objects representing the sand terrain.
     */
    public List<ModelInstance> createSandTerrainModels(float centerX, float centerZ) {
        ModelBuilder modelBuilder = new ModelBuilder();
        List<ModelInstance> sandInstances = new ArrayList<>();
    
        float scale = this.scale;
    
        // Define terrain boundaries
        float minX = centerX - gridWidth * scale * 0.5f;
        float maxX = centerX + gridWidth * scale * 0.5f;
        float minZ = centerZ - gridHeight * scale * 0.5f;
        float maxZ = centerZ + gridHeight * scale * 0.5f;
    
        for (float[] area : sandAreas) {
            // Clipping the sand area to terrain boundaries
            float x1 = Math.max(minX, area[0]);
            float z1 = Math.max(minZ, area[1]);
            float x2 = Math.min(maxX, area[2]);
            float z2 = Math.min(maxZ, area[3]);
    
            // Skip this sand area if it is completely outside the terrain boundaries
            if (x1 >= x2 || z1 >= z2) {
                continue;
            }
    
            modelBuilder.begin();
            sandTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            Material sandMaterial = new Material(TextureAttribute.createDiffuse(sandTexture), ColorAttribute.createSpecular(1, 1, 1, 1));
            MeshPartBuilder meshBuilder = modelBuilder.part("sand_terrain", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, sandMaterial);
    
            // Calculate grid dimensions based on clipped coordinates
            int numX = (int)((x2 - x1) / scale) + 1;
            int numZ = (int)((z2 - z1) / scale) + 1;
    
            // Generate vertices and store heights for indexing
            for (int ix = 0; ix < numX; ix++) {
                for (int iz = 0; iz < numZ; iz++) {
                    float x = x1 + ix * scale;
                    float z = z1 + iz * scale;
                    float height = getTerrainHeight(x, z) + 0.1f;
                    float u = (float)(x - x1) / (x2 - x1);  // Adjusted to use relative position within clipped area
                    float v = (float)(z - z1) / (z2 - z1);
                    meshBuilder.vertex(new float[]{x, height, z, 0, 1, 0, u, v});
                }
            }
    
            // Add indices to form triangles
            for (int ix = 0; ix < numX - 1; ix++) {
                for (int iz = 0; iz < numZ - 1; iz++) {
                    int bl = ix * numZ + iz;
                    int br = bl + 1;
                    int tl = bl + numZ;
                    int tr = tl + 1;
                    meshBuilder.index((short)tl, (short)bl, (short)br); // Triangle 1
                    meshBuilder.index((short)tl, (short)br, (short)tr); // Triangle 2
                }
            }
    
            Model sandModel = modelBuilder.end();
            ModelInstance sandInstance = new ModelInstance(sandModel);
            sandInstances.add(sandInstance);
        }
    
        return sandInstances;
    }

    /**
     * Checks if a given position is on a sand area.
     *
     * @param ballPosition The position of the ball.
     * @return True if the position is on sand, false otherwise.
     */
    public boolean isBallOnSand(Vector3 ballPosition) {
        boolean onSand = false;
        for (float[] area : sandAreas) {
            float minX = area[0];
            float maxX = area[2];
            float minY = area[1];
            float maxY = area[3];
    
            if (ballPosition.x >= minX && ballPosition.x <= maxX &&
                ballPosition.y >= minY && ballPosition.y <= maxY) {
                onSand = true;
                break;
            }
        }
        return onSand;
    }

    /**
     * Determines if the given position is water.
     *
     * @param x The x-coordinate of the position.
     * @param y The y-coordinate of the position.
     * @return True if the position is water, false otherwise.
     */
    public boolean isWater(float x, float y) {
        // Logic to determine if the position (x, y) is water
        // This is a placeholder. Replace with actual implementation.
        // Example: check if the height at (x, y) is below water level.
        float height = getTerrainHeight(x, y);
        return height < 0; // Assuming waterLevel is defined
    }

    /**
     * Calculates the height of the terrain at the specified coordinates.
     *
     * @param x The x-coordinate.
     * @param z The z-coordinate.
     * @return The height of the terrain at the specified coordinates.
     */
    public float getTerrainHeight(float x, float z) {
        Map<String, Double> args = new HashMap<>();
        args.put("x", (double) x);
        args.put("y", (double) z);
        return (float) heightFunction.evaluate(args);
    }

    /**
     * Adds a sand area to the terrain.
     *
     * @param sandArea An array representing the boundaries of the sand area (minX, minY, maxX, maxY).
     */
    public void addSandArea(float[] sandArea) {
        sandAreas.add(sandArea);
    }

    /**
     * Returns the list of sand areas.
     *
     * @return The list of sand areas.
     */
    public List<float[]> getSandAreasList() {
        return sandAreas;
    }

    /**
     * Sets the hole area on the terrain.
     *
     * @param newHoleArea An array representing the boundaries of the hole area.
     */
    public void setHoleArea(float[] newHoleArea) {
        holeArea = newHoleArea;
    }

    /**
     * Generates a normalized heightmap of the terrain and marks the positions of the ball, the goal, and sand areas.
     *
     * @param ballX The x-coordinate of the ball.
     * @param ballY The y-coordinate of the ball.
     * @param goalX The x-coordinate of the goal.
     * @param goalY The y-coordinate of the goal.
     * @return A 2D array representing the normalized heightmap with marked ball, goal, and sand positions.
     */
    public double[][] getNormalizedMarkedHeightMap(float ballX, float ballY, float goalX, float goalY) {
        double[][] heightMap = new double[gridWidth][gridHeight];

        // Step 1: Calculate the height map and find min and max heights
        float minHeight = Float.MAX_VALUE;
        float maxHeight = Float.MIN_VALUE;

        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                float worldX = (x - gridWidth / 2) * scale; 
                float worldZ = (y - gridHeight / 2) * scale;
                float height = getTerrainHeight(worldX, worldZ);
                heightMap[x][y] = height;
                if (height < minHeight) {
                    minHeight = height;
                }
                if (height > maxHeight) {
                    maxHeight = height;
                }
            }
        }

        // Step 2: Normalize the height map to the range [-1, 1] and set heights below 0 to -1
        if (minHeight == maxHeight) {
            for (int x = 0; x < gridWidth; x++) {
                for (int y = 0; y < gridHeight; y++) {
                    heightMap[x][y] = 0; // Все точки будут на одной высоте, нормализованное значение 0
                }
            }
        } else {
            for (int x = 0; x < gridWidth; x++) {
                for (int y = 0; y < gridHeight; y++) {
                    if (heightMap[x][y] < 0) {
                        heightMap[x][y] = -1;
                    } else {
                        heightMap[x][y] = 2 * ((heightMap[x][y] - minHeight) / (maxHeight - minHeight)) - 1;
                    }
                }
            }
        }

        // Step 3: Mark the sand areas if sandAreas is not empty
        if (sandAreas!=null) {
            for (float[] area : sandAreas) {
                int startX = (int)((area[0] / scale) + gridWidth / 2);
                int startY = (int)((area[1] / scale) + gridHeight / 2);
                int endX = (int)((area[2] / scale) + gridWidth / 2);
                int endY = (int)((area[3] / scale) + gridHeight / 2);

                for (int x = startX; x <= endX; x++) {
                    for (int y = startY; y <= endY; y++) {
                        if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                            heightMap[x][y] = 1; // Assuming 1 for sand areas to mark on the map
                        }
                    }
                }
            }
        }

        // Step 4: Mark the ball and goal positions
        int ballPosX = (int) ((ballX / scale) + gridWidth / 2);
        int ballPosY = (int) ((ballY / scale) + gridHeight / 2);
        int goalPosX = (int) ((goalX / scale) + gridWidth / 2);
        int goalPosY = (int) ((goalY / scale) + gridHeight / 2);

        // Assuming 3 for ball and 5 for goal to mark on the map
        if (ballPosX >= 0 && ballPosX < gridWidth && ballPosY >= 0 && ballPosY < gridHeight) {
            heightMap[ballPosX][ballPosY] = 3;
        }

        if (goalPosX >= 0 && goalPosX < gridWidth && goalPosY >= 0 && goalPosY < gridHeight) {
            heightMap[goalPosX][goalPosY] = 5;
        }

        return heightMap;
    }

    /**
     * Converts the height map to an image and saves it as a PNG or JPEG file.
     *
     * @param heightMap The height map to be converted to an image.
     * @param fileName  The name of the file to save the image.
     * @param format    The format of the file (PNG or JPEG).
     */
    public static void saveHeightMapAsImage(double[][] heightMap, String fileName, String format) {
        int width = heightMap.length;
        int height = heightMap[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int value = (int) ((heightMap[x][y] + 1) * 127.5); // Normalize to [0, 255]
                int color = (value << 16) | (value << 8) | value; // Grayscale
                image.setRGB(x, y, color);
            }
        }

        try {
            File outputFile = new File(fileName + "." + format);
            ImageIO.write(image, format, outputFile);
            System.out.println("Image saved successfully: " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error saving the image: " + e.getMessage());
        }
    }

    public double[] getState(float ballX, float ballY, float goalX, float goalY) {
        return MatrixUtils.flattenArray(getNormalizedMarkedHeightMap(ballX, ballY, goalX, goalX));
    }

    public int getTerrainWidth(){
        return gridWidth;
    }

    public int getTerrainHeight(){
        return gridHeight;
    }
    public static void main(String[] args) {
        // Example usage:
        double[][] exampleHeightMap = {
            {-1, -0.5, 0, 0.5, 1},
            {-1, -0.5, 0, 0.5, 1},
            {-1, -0.5, 0, 0.5, 1},
            {-1, -0.5, 0, 0.5, 1},
            {-1, -0.5, 0, 0.5, 1}
        };
        saveHeightMapAsImage(exampleHeightMap, "height_map", "png");
    }
}
