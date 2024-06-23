package com.example.golfgame.physics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.golfgame.physics.ODE.*;
import com.example.golfgame.utils.BallState;
import com.example.golfgame.utils.Function;

/**
 * The PhysicsEngine class simulates the motion of a ball on a surface using
 * differential equations to model the physics involved, including gravity
 * and kinetic friction.
 */
public class PhysicsEngine {
    private ODE solver;
    private Function surfaceFunction;
    private double g = 9.81; // Acceleration due to gravity, m/s^2
    private double mu_k = 0.1; // Coefficient of kinetic friction
    private double mu_s = 0.2; // Coefficient of static friction
    private double deltaX = 0.01; // Increment for numerical derivative in x-direction
    private double deltaY = 0.01; // Increment for numerical derivative in y-direction
    private double deltaDirection = 0.01; // Increment for numerical derivative in given direction

    /**
     * Constructs a PhysicsEngine with a specific ODE solver and a surface function.
     *
     * @param solver the differential equation solver to use
     * @param surfaceFunction the function representing the surface's height as a function of x and y
     */
    public PhysicsEngine(ODE solver, Function surfaceFunction) {
        this.solver = solver;
        this.surfaceFunction = surfaceFunction;
    }

    /**
     * Constructs a PhysicsEngine with a specific ODE solver, a surface function, and coefficients of friction.
     *
     * @param solver the differential equation solver to use
     * @param surfaceFunction the function representing the surface's height as a function of x and y
     * @param mu_k the coefficient of kinetic friction
     * @param mu_s the coefficient of static friction
     */
    public PhysicsEngine(ODE solver, Function surfaceFunction, double mu_k, double mu_s) {
        this.solver = solver;
        this.surfaceFunction = surfaceFunction;
        this.mu_k = mu_k;
        this.mu_s = mu_s;
    }

    /**
     * Sets the coefficients of friction.
     *
     * @param mu_k the coefficient of kinetic friction
     * @param mu_s the coefficient of static friction
     */
    public void setFriction(double mu_k, double mu_s) {
        this.mu_k = mu_k;
        this.mu_s = mu_s;
    }

    /**
     * Calculates the derivative of the surface function along the x-axis at a given point.
     *
     * @param x the x-coordinate at which to calculate the derivative
     * @param y the y-coordinate at which to calculate the derivative
     * @return the derivative value along the x-axis
     */
    private double derivativeX(double x, double y) {
        Map<String, Double> valuesOneStepAhead = new HashMap<>();
        Map<String, Double> valuesOneStepBehind = new HashMap<>();
        Map<String, Double> valuesTwoStepsAhead = new HashMap<>();
        Map<String, Double> valuesTwoStepsBehind = new HashMap<>();
        
        double h = deltaX;
    
        valuesOneStepAhead.put("x", x + h);
        valuesOneStepAhead.put("y", y);
        valuesOneStepBehind.put("x", x - h);
        valuesOneStepBehind.put("y", y);
        valuesTwoStepsAhead.put("x", x + 2 * h);
        valuesTwoStepsAhead.put("y", y);
        valuesTwoStepsBehind.put("x", x - 2 * h);
        valuesTwoStepsBehind.put("y", y);
    
        double derivative = (-surfaceFunction.evaluate(valuesTwoStepsAhead) 
                             + 8 * surfaceFunction.evaluate(valuesOneStepAhead)
                             - 8 * surfaceFunction.evaluate(valuesOneStepBehind)
                             + surfaceFunction.evaluate(valuesTwoStepsBehind)) 
                            / (12 * h);
    
        return derivative;
    }

    /**
     * Calculates the derivative of the surface function along the y-axis at a given point.
     *
     * @param x the x-coordinate at which to calculate the derivative
     * @param y the y-coordinate at which to calculate the derivative
     * @return the derivative value along the y-axis
     */
    private double derivativeY(double x, double y) {
        Map<String, Double> valuesOneStepAhead = new HashMap<>();
        Map<String, Double> valuesOneStepBehind = new HashMap<>();
        Map<String, Double> valuesTwoStepsAhead = new HashMap<>();
        Map<String, Double> valuesTwoStepsBehind = new HashMap<>();
        
        double h = deltaY;
    
        valuesOneStepAhead.put("x", x);
        valuesOneStepAhead.put("y", y + h);
        valuesOneStepBehind.put("x", x);
        valuesOneStepBehind.put("y", y - h);
        valuesTwoStepsAhead.put("x", x);
        valuesTwoStepsAhead.put("y", y + 2 * h);
        valuesTwoStepsBehind.put("x", x);
        valuesTwoStepsBehind.put("y", y - 2 * h);
    
        double derivative = (-surfaceFunction.evaluate(valuesTwoStepsAhead) 
                             + 8 * surfaceFunction.evaluate(valuesOneStepAhead)
                             - 8 * surfaceFunction.evaluate(valuesOneStepBehind)
                             + surfaceFunction.evaluate(valuesTwoStepsBehind)) 
                            / (12 * h);
    
        return derivative;
    }

    /**
     * Calculates the derivative of the surface function along the direction vector at a given point.
     * 
     * @param x the x-coordinate at which to calculate the derivative
     * @param y the y-coordinate at which to calculate the derivative
     * @param xDirection x-component of the direction
     * @param yDirection y-component of the direction
     * @return the derivative along the direction axis
     */
    public double derivative(double x, double y, double xDirection, double yDirection){
        Map<String, Double> valuesOneStepAhead = new HashMap<>();
        Map<String, Double> valuesOneStepBehind = new HashMap<>();
        Map<String, Double> valuesTwoStepsAhead = new HashMap<>();
        Map<String, Double> valuesTwoStepsBehind = new HashMap<>();
        
        double h = deltaDirection;
        valuesOneStepAhead.put("x", x + xDirection * h);
        valuesOneStepAhead.put("y", y + yDirection * h);
        valuesOneStepBehind.put("x", x - xDirection * h);
        valuesOneStepBehind.put("y", y - yDirection * h);
        valuesTwoStepsAhead.put("x", x + xDirection * 2 * h);
        valuesTwoStepsAhead.put("y", y + yDirection * 2 * h);
        valuesTwoStepsBehind.put("x", x - xDirection * 2 * h);
        valuesTwoStepsBehind.put("y", y - yDirection * 2 * h);

        double derivative = (-surfaceFunction.evaluate(valuesTwoStepsAhead) 
                             + 8 * surfaceFunction.evaluate(valuesOneStepAhead)
                             - 8 * surfaceFunction.evaluate(valuesOneStepBehind)
                             + surfaceFunction.evaluate(valuesTwoStepsBehind)) 
                            / (12 * h);

        return derivative;
    }

    /**
     * Calculates the second derivative of the surface function along the direction vector at a given point.
     * 
     * @param x the x-coordinate at which to calculate the second derivative
     * @param y the y-coordinate at which to calculate the second derivative
     * @param xDirection x-component of the direction
     * @param yDirection y-component of the direction
     * @return the second derivative along the direction axis
     */
    public double secondDerivative(double x, double y, double xDirection, double yDirection){
        Map<String, Double> valuesOneStepAhead = new HashMap<>();
        Map<String, Double> valuesOneStepBehind = new HashMap<>();
        Map<String, Double> valuesTwoStepsAhead = new HashMap<>();
        Map<String, Double> valuesTwoStepsBehind = new HashMap<>();
        Map<String, Double> currentValues = new HashMap<>();
        
        double h = deltaDirection;
        valuesOneStepAhead.put("x", x + xDirection * h);
        valuesOneStepAhead.put("y", y + yDirection * h);
        valuesOneStepBehind.put("x", x - xDirection * h);
        valuesOneStepBehind.put("y", y - yDirection * h);
        valuesTwoStepsAhead.put("x", x + xDirection * 2 * h);
        valuesTwoStepsAhead.put("y", y + yDirection * 2 * h);
        valuesTwoStepsBehind.put("x", x - xDirection * 2 * h);
        valuesTwoStepsBehind.put("y", y - yDirection * 2 * h);
        currentValues.put("x", x);
        currentValues.put("y", y);

        double derivative = (-surfaceFunction.evaluate(valuesTwoStepsAhead) 
                             + 16 * surfaceFunction.evaluate(valuesOneStepAhead)
                             - 30 * surfaceFunction.evaluate(currentValues)
                             + 16 * surfaceFunction.evaluate(valuesOneStepBehind)
                             - surfaceFunction.evaluate(valuesTwoStepsBehind)) 
                            / (12 * Math.pow(h, 2));

        return derivative;
    }

    /**
     * Generates a map of differential equations representing the dynamics of the ball based on its current state.
     *
     * @param ballState the current state of the ball including position and velocity
     * @return a map of differential equations for each state variable
     */
    public Map<String, Function> getDifferentialEquations(BallState ballState) {
        double dx = derivativeX(ballState.getX(), ballState.getY());
        double dy = derivativeY(ballState.getX(), ballState.getY());

        String expressionVx = ((-g * dx) / (1 + Math.pow(dx, 2) + Math.pow(dy, 2))) + "-"
                + ((mu_k * g) / (Math.sqrt(1 + Math.pow(dx, 2) + Math.pow(dy, 2)))) + "*(vx/sqrt(vx^2 + vy^2 + (" + dx
                + "*vx" + "+" + dy + "*vy)^2))";
        String expressionVy = ((-g * dy) / (1 + Math.pow(dx, 2) + Math.pow(dy, 2)) + "-" + (mu_k * g)
                / (Math.sqrt(1 + Math.pow(dx, 2) + Math.pow(dy, 2)))) + "*(vy/sqrt(vx^2 + vy^2 + (" + dx + "*vx" + "+"
                + dy + "*vy)^2))";

        Map<String, Function> differentials = new HashMap<>();
        differentials.put("x", new Function("vx", "vx"));
        differentials.put("y", new Function("vy", "vy"));
        differentials.put("vx", new Function(expressionVx, "vx", "vy"));
        differentials.put("vy", new Function(expressionVy, "vx", "vy"));

        return differentials;
    }

    /**
     * Updates the state of the ball over a given duration using the specified step size.
     *
     * @param ballState the initial state of the ball
     * @param stepSize the time step size for the simulation
     * @return the final state of the ball after simulation
     */
    public BallState update(BallState ballState, double stepSize) {
        if (isAtRest(ballState)) {
            if (canOvercomeStaticFriction(ballState)) {
                return updateWithKineticFriction(ballState, stepSize);
            }
            return ballState;
        } else {
            return updateWithKineticFriction(ballState, stepSize);
        }
    }

    /**
     * Updates the state of the ball to a certain time using the specified step size.
     *
     * @param ballState the initial state of the ball
     * @param stepSize the time step size for the simulation
     * @param time the total time duration for the simulation
     * @return the final state of the ball after simulation
     */
    public BallState updateToCertaintTime(BallState ballState, double stepSize, double time) {
        if (isAtRest(ballState)) {
            if (canOvercomeStaticFriction(ballState)) {
                return updateWithKineticFriction(ballState, stepSize, time);
            }
            return ballState;
        } else {
            return updateWithKineticFriction(ballState, stepSize, time);
        }
    }

    /**
     * Checks if the ball is at rest based on its velocity.
     *
     * @param ballState the current state of the ball
     * @return true if the ball is at rest, false otherwise
     */
    public boolean isAtRest(BallState ballState) {
        return Math.abs(ballState.getVx()) < 0.001 && Math.abs(ballState.getVy()) < 0.001;
    }

    /**
     * Checks if the ball can overcome static friction to start moving.
     *
     * @param ballState the current state of the ball
     * @return true if the ball can overcome static friction, false otherwise
     */
    private boolean canOvercomeStaticFriction(BallState ballState) {
        double dx = derivativeX(ballState.getX(), ballState.getY());
        double dy = derivativeY(ballState.getX(), ballState.getY());
        double normalForce = g * (1 + Math.pow(dx, 2) + Math.pow(dy, 2));
        double staticFrictionForce = mu_s * normalForce;
        double gravitationalComponent = g * Math.sqrt(dx * dx + dy * dy);

        return gravitationalComponent > staticFrictionForce;
    }

    /**
     * Updates the state of the ball with kinetic friction over a given duration using the specified step size.
     *
     * @param ballState the initial state of the ball
     * @param stepSize the time step size for the simulation
     * @return the final state of the ball after simulation
     */
    private BallState updateWithKineticFriction(BallState ballState, double stepSize) {
        Map<String, Function> differentials = getDifferentialEquations(ballState);
        Map<String, Double> initialState = new HashMap<>();
        initialState.put("x", ballState.getX());
        initialState.put("y", ballState.getY());
        initialState.put("vx", ballState.getVx());
        initialState.put("vy", ballState.getVy());
        initialState.put("t", 0.0);

        List<Map<String, Double>> results = solver.solve(differentials, initialState, stepSize, stepSize, "t");

        if (results.isEmpty()) {
            System.err.println("No states were returned by the ODE solver.");
            return ballState;
        }

        Map<String, Double> finalState = results.get(results.size() - 1);
        ballState.setX(finalState.get("x"));
        ballState.setY(finalState.get("y"));
        ballState.setVx(finalState.get("vx"));
        ballState.setVy(finalState.get("vy"));
        return ballState;
    }

    /**
     * Updates the state of the ball with kinetic friction to a certain time using the specified step size.
     *
     * @param ballState the initial state of the ball
     * @param stepSize the time step size for the simulation
     * @param time the total time duration for the simulation
     * @return the final state of the ball after simulation
     */
    private BallState updateWithKineticFriction(BallState ballState, double stepSize, double time) {
        Map<String, Function> differentials = getDifferentialEquations(ballState);
        Map<String, Double> initialState = new HashMap<>();
        initialState.put("x", ballState.getX());
        initialState.put("y", ballState.getY());
        initialState.put("vx", ballState.getVx());
        initialState.put("vy", ballState.getVy());
        initialState.put("t", 0.0);

        List<Map<String, Double>> results = solver.solve(differentials, initialState, stepSize, time, "t");

        if (results.isEmpty()) {
            System.err.println("No states were returned by the ODE solver.");
            return ballState;
        }

        Map<String, Double> finalState = results.get(results.size() - 1);
        ballState.setX(finalState.get("x"));
        ballState.setY(finalState.get("y"));
        ballState.setVx(finalState.get("vx"));
        ballState.setVy(finalState.get("vy"));
        return ballState;
    }

    /**
     * Returns the slope of the surface along the x-axis at a given point.
     *
     * @param x the x-coordinate at which to calculate the slope
     * @param y the y-coordinate at which to calculate the slope
     * @return the slope along the x-axis
     */
    public float getSlopeX(float x, float y) {
        return (float) derivativeX(x, y);
    }

    /**
     * Returns the slope of the surface along the y-axis at a given point.
     *
     * @param x the x-coordinate at which to calculate the slope
     * @param y the y-coordinate at which to calculate the slope
     * @return the slope along the y-axis
     */
    public float getSlopeY(float x, float y) {
        return (float) derivativeY(x, y);
    }

    /**
     * Returns the surface function.
     *
     * @return the surface function
     */
    public Function getSurfaceFunction() {
        return surfaceFunction;
    }
}
