package com.example.golfgame;

/**
 * Represents the state of a golf ball in a simulation game, encapsulating its position and velocity.
 * The state includes the ball's coordinates (x, y) and the components of its velocity (vx, vy).
 */
public class BallState {
    private double x = 0;
    private double y = 0;
    private double vx = 0;
    private double vy = 0;

    /**
     * Initializes a new instance of {@code BallState} with specified position and velocity.
     *
     * @param x  the initial x-coordinate of the ball in meters.
     * @param y  the initial y-coordinate of the ball in meters.
     * @param vx the initial horizontal component of the ball's velocity in meters per second.
     * @param vy the initial vertical component of the ball's velocity in meters per second.
     */
    public BallState(double x, double y, double vx, double vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }

    /**
     * Sets all state components of the ball at once.
     *
     * @param x  the new x-coordinate of the ball in meters.
     * @param y  the new y-coordinate of the ball in meters.
     * @param vx the new horizontal component of the ball's velocity in meters per second.
     * @param vy the new vertical component of the ball's velocity in meters per second.
     */
    public void setAllComponents(double x, double y, double vx, double vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }

    /**
     * Sets the x-coordinate of the ball.
     *
     * @param x the new x-coordinate of the ball in meters.
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Sets the y-coordinate of the ball.
     *
     * @param y the new y-coordinate of the ball in meters.
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Sets the horizontal component of the ball's velocity.
     *
     * @param vx the new horizontal velocity in meters per second.
     */
    public void setVx(double vx) {
        this.vx = vx;
    }

    /**
     * Sets the vertical component of the ball's velocity.
     *
     * @param vy the new vertical velocity in meters per second.
     */
    public void setVy(double vy) {
        this.vy = vy;
    }

    /**
     * Returns the x-coordinate of the ball.
     *
     * @return the x-coordinate of the ball in meters.
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of the ball.
     *
     * @return the y-coordinate of the ball in meters.
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the horizontal component of the ball's velocity.
     *
     * @return the horizontal velocity component in meters per second.
     */
    public double getVx() {
        return vx;
    }

    /**
     * Returns the vertical component of the ball's velocity.
     *
     * @return the vertical velocity component in meters per second.
     */
    public double getVy() {
        return vy;
    }
}
