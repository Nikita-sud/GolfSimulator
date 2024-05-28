package com.example.golfgame.utils;

/**
 * Represents an abstract representation of a sandbox.
 * The boundaries of the sandbox are read-only after initialization.
 */
public class Sandbox {

    private final float xLowBound;
    private final float xHighBound;
    private final float yLowBound;
    private final float yHighBound;

    /**
     * Constructs a Sandbox with specified boundaries.
     *
     * @param xLowBound  The starting position of the sandbox on the spatial x-axis.
     * @param xHighBound The ending position of the sandbox on the spatial x-axis.
     * @param yLowBound  The starting position of the sandbox on the spatial y-axis.
     * @param yHighBound The ending position of the sandbox on the spatial y-axis.
     */
    public Sandbox(float xLowBound, float xHighBound, float yLowBound, float yHighBound) {
        this.xLowBound = xLowBound;
        this.xHighBound = xHighBound;
        this.yLowBound = yLowBound;
        this.yHighBound = yHighBound;
    }

    /**
     * Returns the lower boundary on the x-axis.
     *
     * @return The lower boundary on the x-axis.
     */
    public float getXLowBound() {
        return xLowBound;
    }

    /**
     * Returns the upper boundary on the x-axis.
     *
     * @return The upper boundary on the x-axis.
     */
    public float getXHighBound() {
        return xHighBound;
    }

    /**
     * Returns the lower boundary on the y-axis.
     *
     * @return The lower boundary on the y-axis.
     */
    public float getYLowBound() {
        return yLowBound;
    }

    /**
     * Returns the upper boundary on the y-axis.
     *
     * @return The upper boundary on the y-axis.
     */
    public float getYHighBound() {
        return yHighBound;
    }

    /**
     * Returns a string representation of the sandbox boundaries.
     *
     * @return A string in the format "Sandbox from [xLow, yLow] to [xHigh, yHigh]".
     */
    @Override
    public String toString() {
        return String.format("Sandbox from [%.2f, %.2f] to [%.2f, %.2f]", xLowBound, yLowBound, xHighBound, yHighBound);
    }
}
