package com.example.golfgame;
public class PhysicsEngine {
    private double deltaX = 0.00001;
    private double deltaY = 0.00001;
    private Function h;
    private double muK; // friction coefficient

    public PhysicsEngine(Function heightFunction, double muK) {
        this.h = heightFunction;
        this.muK = muK;
    }

    // Method for updating ball state
    public BallState updateState(BallState currentState, double deltaTime) {
        double gradX = computeGradientX(currentState.getX(), currentState.getY(), deltaX);
        double gradY = computeGradientY(currentState.getX(), currentState.getY(), deltaY);
    
        // Initializing acceleration
        double ax = -9.81 * gradX;
        double ay = -9.81 * gradY;
    
        // Calculating speed magnitude
        double speedMagnitude = Math.sqrt(Math.pow(currentState.getVx(), 2) + Math.pow(currentState.getVy(), 2));
    
        // Adding friction if speed is not equal to 0
        if (speedMagnitude > 0) {
            ax -= muK * 9.81 * (currentState.getVx()) / speedMagnitude;
            ay -= muK * 9.81 * (currentState.getVy()) / speedMagnitude;
        }
    
        // Updating velocities and position
        double newVx = currentState.getVx() + ax * deltaTime;
        double newVy = currentState.getVy() + ay * deltaTime;
        double newX = currentState.getX() + newVx * deltaTime;
        double newY = currentState.getY() + newVy * deltaTime;
    
        return new BallState(newX, newY, newVx, newVy);
    }
    

    public double computeGradientX(double x, double y, double deltaX) {
        double h1 = h.evaluate(x+deltaX,y);
        double h2 = h.evaluate(x,y);
        return (h1 - h2) / deltaX;
    }
    
    public double computeGradientY(double x, double y, double deltaY) {
        double h1 = h.evaluate(x,y+deltaY);
        double h2 = h.evaluate(x,y);
        return (h1 - h2) / deltaY;
    }
    
    
}
