package com.example.golfgame.utils;

import java.util.Random;

/**
 * Represents weather conditions affecting a golf game, including wind, rain, and sun levels.
 * This class models wind as a three-dimensional vector and provides methods to adjust and retrieve weather conditions.
 */
public class Weather {

    private float[] wind;
    private float rain;
    private float sun;
    private float windMagnitude;
    private static final Random windRandom = new Random(2024);

    /**
     * Constructs a new Weather instance with specified wind magnitude, rain intensity, and sunlight level.
     *
     * @param wind the initial magnitude of the wind in any direction.
     * @param rain the initial rain intensity, where higher values indicate heavier rain.
     * @param sun the initial level of sunlight, where higher values indicate brighter sunlight.
     */
    public Weather(float wind, float rain, float sun) {
        this.rain = rain;
        this.wind = generateWind(wind);
        this.sun = sun;
        this.windMagnitude = wind;
    }

    /**
     * Constructs a new Weather instance with specified wind magnitude and defaults for rain and sun.
     * By default, rain is set to 0 (no rain) and sun is set to 1 (full sunlight).
     *
     * @param wind the initial magnitude of the wind in any direction.
     */
    public Weather(float wind) {
        this(wind, 0, 1);
    }

    /**
     * Generates a three-dimensional wind vector based on a specified magnitude.
     * The direction is randomized and normalized to distribute the total wind magnitude evenly across the x, y, and z components.
     *
     * @param magnitude the desired magnitude of the wind.
     * @return a float array representing the wind vector with components [x, y, z].
     */
    private float[] generateWind(float magnitude) {
        float x = windRandom.nextFloat() * 2 - 1; // Generate a random float between -1 and 1
        float y = windRandom.nextFloat() * 2 - 1; // Generate a random float between -1 and 1
        float z = windRandom.nextFloat() * 2 - 1; // Generate a random float between -1 and 1
        float total = Math.abs(x) + Math.abs(y) + Math.abs(z);
        x *= (magnitude / total);
        y *= (magnitude / total);
        z *= (magnitude / total);
        return new float[]{x, y, z};
    }

    /**
     * Retrieves the current wind vector.
     *
     * @return the current wind vector as a float array [x, y, z].
     */
    public float[] getWind() {
        return wind;
    }

    /**
     * Retrieves the current rain intensity.
     *
     * @return the current rain intensity as a float.
     */
    public float getRain() {
        return rain;
    }

    /**
     * Retrieves the current level of sunlight.
     *
     * @return the current level of sunlight as a float.
     */
    public float getSun() {
        return sun;
    }

    /**
     * Retrieves the magnitude of the wind.
     *
     * @return the magnitude of the wind as a float.
     */
    public float getWindMagnitude() {
        return windMagnitude;
    }

    /**
     * Provides access to the random number generator used for wind calculations.
     *
     * @return the Random object used for wind generation.
     */
    public Random getRandom() {
        return windRandom;
    }

    /**
     * Updates the wind magnitude and recalculates the wind vector.
     *
     * @param newWind the new wind magnitude.
     */
    public void setWind(float newWind) {
        wind = generateWind(newWind);
    }

    /**
     * Updates the rain intensity.
     *
     * @param newRain the new rain intensity.
     */
    public void setRain(float newRain) {
        rain = newRain;
    }

    /**
     * Updates the sunlight level.
     *
     * @param newSun the new level of sunlight.
     */
    public void setSun(float newSun) {
        sun = newSun;
    }
}
