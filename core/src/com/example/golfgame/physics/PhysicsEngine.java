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
    private double deltaX = 0.01; // Increment for numerical derivative in x-direction
    private double deltaY = 0.01; // Increment for numerical derivative in y-direction

    /**
     * Constructs a PhysicsEngine with a specific ODE solver, a surface function, and a coefficient of friction.
     *
     * @param solver the differential equation solver to use
     * @param surfaceFunction the function representing the surface's height as a function of x and y
     * @param mu_k the coefficient of kinetic friction
     */
    public PhysicsEngine(ODE solver, Function surfaceFunction, double mu_k) {
        this.solver = solver;
        this.surfaceFunction = surfaceFunction;
        this.mu_k = mu_k;
    }

    /**
     * Calculates the derivative of the surface function along the x-axis at a given point.
     *
     * @param x the x-coordinate at which to calculate the derivative
     * @param y the y-coordinate at which to calculate the derivative
     * @return the derivative value along the x-axis
     */
    private double derivativeX(double x, double y) {
        Map<String, Double> valuesPlus = new HashMap<>();
        Map<String, Double> valuesMinus = new HashMap<>();
        
        valuesPlus.put("x", x + deltaX);
        valuesPlus.put("y", y);
        valuesMinus.put("x", x - deltaX);
        valuesMinus.put("y", y);

        return (surfaceFunction.evaluate(valuesPlus) - surfaceFunction.evaluate(valuesMinus)) / (2 * deltaX);
    }

    /**
     * Calculates the derivative of the surface function along the y-axis at a given point.
     *
     * @param x the x-coordinate at which to calculate the derivative
     * @param y the y-coordinate at which to calculate the derivative
     * @return the derivative value along the y-axis
     */
    private double derivativeY(double x, double y) {
        Map<String, Double> valuesPlus = new HashMap<>();
        Map<String, Double> valuesMinus = new HashMap<>();
        
        valuesPlus.put("x", x);
        valuesPlus.put("y", y + deltaY);
        valuesMinus.put("x", x);
        valuesMinus.put("y", y - deltaY);

        return (surfaceFunction.evaluate(valuesPlus) - surfaceFunction.evaluate(valuesMinus)) / (2 * deltaY);
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
}
