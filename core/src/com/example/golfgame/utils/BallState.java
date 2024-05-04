package com.example.golfgame.utils;

/**
 * Represents the state of a golf ball, including its position and velocity components.
 * This class provides methods to set and retrieve the ball's position (x, y) and velocity (vx, vy)
 * in a two-dimensional space.
 */
public class BallState {
    private double x = 0;
    private double y = 0;
    private double vx = 0;
    private double vy = 0;

    /**
     * Constructs a new BallState with specified initial position and velocity.
     *
     * @param x the initial x-coordinate of the ball.
     * @param y the initial y-coordinate of the ball.
     * @param vx the initial velocity of the ball along the x-axis.
     * @param vy the initial velocity of the ball along the y-axis.
     */
    public BallState(double x, double y, double vx, double vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }

    /**
     * Sets all components of the ball's state.
     *
     * @param x the new x-coordinate of the ball.
     * @param y the new y-coordinate of the ball.
     * @param vx the new velocity of the ball along the x-axis.
     * @param vy the new velocity of the ball along the y-axis.
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
     * @param x the new x-coordinate of the ball.
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Sets the y-coordinate of the ball.
     *
     * @param y the new y-coordinate of the ball.
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Sets the velocity of the ball along the x-axis.
     *
     * @param vx the new velocity along the x-axis.
     */
    public void setVx(double vx) {
        this.vx = vx;
    }

    /**
     * Sets the velocity of the ball along the y-axis.
     *
     * @param vy the new velocity along the y-axis.
     */
    public void setVy(double vy) {
        this.vy = vy;
    }

    /**
     * Returns the x-coordinate of the ball.
     *
     * @return the current x-coordinate of the ball.
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of the ball.
     *
     * @return the current y-coordinate of the ball.
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the velocity of the ball along the x-axis.
     *
     * @return the current velocity along the x-axis.
     */
    public double getVx() {
        return vx;
    }

    /**
     * Returns the velocity of the ball along the y-axis.
     *
     * @return the current velocity along the y-axis.
     */
    public double getVy() {
        return vy;
    }
}