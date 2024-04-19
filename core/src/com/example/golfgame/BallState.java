package com.example.golfgame;

/**
 * Represents the state of a ball in a golf simulation game, including its position and velocity.
 * The class stores four properties: x and y coordinates for position, and vx and vy for velocity components.
 */
public class BallState {
    private double x = 0, y = 0;
    private double vx = 0, vy = 0;

    /**
     * Constructs a new BallState with specified position and velocity.
     *
     * @param x  the initial x-coordinate of the ball.
     * @param y  the initial y-coordinate of the ball.
     * @param vx the initial x-component of the velocity of the ball.
     * @param vy the initial y-component of the velocity of the ball.
     */
    public BallState(double x, double y, double vx, double vy) {
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
     * Sets the x-component of the velocity of the ball.
     *
     * @param vx the new x-component of the velocity.
     */
    public void setVx(double vx) {
        this.vx = vx;
    }

    /**
     * Sets the y-component of the velocity of the ball.
     *
     * @param vy the new y-component of the velocity.
     */
    public void setVy(double vy) {
        this.vy = vy;
    }

    /**
     * Returns the x-coordinate of the ball.
     *
     * @return the x-coordinate of the ball.
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of the ball.
     *
     * @return the y-coordinate of the ball.
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the x-component of the velocity of the ball.
     *
     * @return the x-component of the velocity.
     */
    public double getVx() {
        return vx;
    }

    /**
     * Returns the y-component of the velocity of the ball.
     *
     * @return the y-component of the velocity.
     */
    public double getVy() {
        return vy;
    }
}
