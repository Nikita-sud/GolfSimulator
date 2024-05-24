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

    /**
     * Constructs a PhysicsEngine with a specific ODE solver, a surface function, and a coefficient of friction.
     *
     * @param solver the differential equation solver to use
     * @param surfaceFunction the function representing the surface's height as a function of x and y
     * @param mu_k the coefficient of kinetic friction
     */

    public PhysicsEngine(ODE solver, Function surfaceFunction) {
        this.solver = solver;
        this.surfaceFunction = surfaceFunction;
    }

    public PhysicsEngine(ODE solver, Function surfaceFunction, double mu_k, double mu_s) {
        this.solver = solver;
        this.surfaceFunction = surfaceFunction;
        this.mu_k = mu_k;
        this.mu_s = mu_s;
    }

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
        
        double h = deltaX; // Assuming deltaX is your small step for the derivative
    
        // Setting values for function evaluation at x + h and x - h
        valuesOneStepAhead.put("x", x + h);
        valuesOneStepAhead.put("y", y);
        valuesOneStepBehind.put("x", x - h);
        valuesOneStepBehind.put("y", y);
    
        // Setting values for function evaluation at x + 2h and x - 2h
        valuesTwoStepsAhead.put("x", x + 2 * h);
        valuesTwoStepsAhead.put("y", y);
        valuesTwoStepsBehind.put("x", x - 2 * h);
        valuesTwoStepsBehind.put("y", y);
    
        // Five-point central difference formula:
        // f'(x) ≈ (−f(x+2h) + 8f(x+h) − 8f(x−h) + f(x−2h)) / 12h
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
        
        double h = deltaY; // Assuming deltaY is your small step for the derivative
    
        // Setting values for function evaluation at y + h and y - h
        valuesOneStepAhead.put("x", x);
        valuesOneStepAhead.put("y", y + h);
        valuesOneStepBehind.put("x", x);
        valuesOneStepBehind.put("y", y - h);
    
        // Setting values for function evaluation at y + 2h and y - 2h
        valuesTwoStepsAhead.put("x", x);
        valuesTwoStepsAhead.put("y", y + 2 * h);
        valuesTwoStepsBehind.put("x", x);
        valuesTwoStepsBehind.put("y", y - 2 * h);
    
        // Five-point central difference formula for the derivative with respect to y:
        // f'(y) ≈ (−f(y+2h) + 8f(y+h) − 8f(y−h) + f(y−2h)) / 12h
        double derivative = (-surfaceFunction.evaluate(valuesTwoStepsAhead) 
                             + 8 * surfaceFunction.evaluate(valuesOneStepAhead)
                             - 8 * surfaceFunction.evaluate(valuesOneStepBehind)
                             + surfaceFunction.evaluate(valuesTwoStepsBehind)) 
                            / (12 * h);
    
        return derivative;
    }
    

    /**
     * Generates a map of differential equations representing the dynamics of the ball based on its current state.
     *
     * @param ballState the current state of the ball including position and velocity
     * @return a map of differential equations for each state variable
     */
    public Map<String, Function> getDifferentialEquations(BallState ballState) {
        Map<String, Double> vars = new HashMap<>();
        double dx = derivativeX(ballState.getX(), ballState.getY());
        double dy = derivativeY(ballState.getX(), ballState.getY());
        vars.put("vx", ballState.getVx());
        vars.put("vy", ballState.getVy());

        String expressionVx = ((-g*dx)/(1+Math.pow(dx, 2)+Math.pow(dy, 2)))+"-"+((mu_k*g)/(Math.sqrt(1+Math.pow(dx, 2)+Math.pow(dy, 2))))+"*(vx/sqrt(vx^2 + vy^2 + ("+dx+"*vx"+"+"+dy+"*vy)^2))";
        String expressionVy = ((-g*dy)/(1+Math.pow(dx, 2)+Math.pow(dy, 2))+"-"+(mu_k*g)/(Math.sqrt(1+Math.pow(dx, 2)+Math.pow(dy, 2))))+"*(vy/sqrt(vx^2 + vy^2 + ("+dx+"*vx"+"+"+dy+"*vy)^2))";

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
     * @param duration the total time duration for the simulation
     * @return the final state of the ball after simulation
     */

    public BallState update(BallState ballState, double stepSize) {
        if (isAtRest(ballState)) {
            // Check if the force exceeds static friction threshold to start moving
            if (canOvercomeStaticFriction(ballState)) {
                return updateWithKineticFriction(ballState, stepSize);
            }
            // No movement, return current state
            return ballState;
        } else {
            return updateWithKineticFriction(ballState, stepSize);
        }
    }

    public BallState updateToCertaintTime(BallState ballState, double stepSize, double time) {
        if (isAtRest(ballState)) {
            // Check if the force exceeds static friction threshold to start moving
            if (canOvercomeStaticFriction(ballState)) {
                return updateWithKineticFriction(ballState, stepSize, time);
            }
            // No movement, return current state
            return ballState;
        } else {
            return updateWithKineticFriction(ballState, stepSize, time);
        }
    }


    private boolean isAtRest(BallState ballState) {
        return Math.abs(ballState.getVx()) < 0.001 && Math.abs(ballState.getVy()) < 0.001;
    }
    
    private boolean canOvercomeStaticFriction(BallState ballState) {
        double dx = derivativeX(ballState.getX(), ballState.getY());
        double dy = derivativeY(ballState.getX(), ballState.getY());
        double normalForce = g * (1 + Math.pow(dx, 2) + Math.pow(dy, 2));
        double staticFrictionForce = mu_s * normalForce;
        double gravitationalComponent = g * Math.sqrt(dx*dx + dy*dy);
    
        return gravitationalComponent > staticFrictionForce;
    }

    private BallState updateWithKineticFriction(BallState ballState, double stepSize) {
        Map<String, Function> differentials = getDifferentialEquations(ballState);
        Map<String, Double> initialState = new HashMap<>();
        initialState.put("x", ballState.getX());
        initialState.put("y", ballState.getY());
        initialState.put("vx", ballState.getVx());
        initialState.put("vy", ballState.getVy());
        initialState.put("t", 0.0);
    
        List<Map<String, Double>> results = solver.solve(differentials, initialState, stepSize, stepSize,"t");
    
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

    private BallState updateWithKineticFriction(BallState ballState, double stepSize, double time) {
        Map<String, Function> differentials = getDifferentialEquations(ballState);
        Map<String, Double> initialState = new HashMap<>();
        initialState.put("x", ballState.getX());
        initialState.put("y", ballState.getY());
        initialState.put("vx", ballState.getVx());
        initialState.put("vy", ballState.getVy());
        initialState.put("t", 0.0);
    
        List<Map<String, Double>> results = solver.solve(differentials, initialState, stepSize, time,"t");
    
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

}
