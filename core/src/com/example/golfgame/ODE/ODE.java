package com.example.golfgame.ODE;

import java.util.List;
import java.util.Map;
import com.example.golfgame.Function;

/**
 * Represents an abstract base class for solving Ordinary Differential Equations (ODEs).
 * This class provides a framework that requires implementing classes to define a specific
 * numerical method for solving systems of ODEs based on initial conditions and step size.
 *
 * <p>The class is designed to handle systems of first-order ODEs that can be represented in
 * the form of derivatives with respect to an independent variable (often time).</p>
 */
public abstract class ODE {

    /**
     * Solves the system of differential equations provided in the form of derivatives.
     *
     * @param differentials A map of the dependent variables and their corresponding differential
     *                      equations expressed as functions. Each function should calculate the
     *                      derivative of the variable based on the current state of all variables.
     * @param initial_state A map containing the initial values for all variables, including
     *                      the independent variable.
     * @param step_size The increment of the independent variable for each step of the numerical method.
     *                  This value determines the resolution of the solution.
     * @param stopping_point The value of the independent variable at which the computation should stop.
     *                       This defines the range over which the solution is computed.
     * @param independent_variable The name of the independent variable in the system, which is often
     *                             time or a spatial dimension.
     * @return A list of maps, where each map represents the state of all variables at a specific
     *         increment of the independent variable from the initial state up to the stopping point.
     *         Each state is a snapshot of all variable values at that step.
     *
     * <p>Note: Implementing classes should ensure that the solution respects the step size and stopping
     * point, and properly handles any potential numerical stability issues specific to the method used.</p>
     */
    public abstract List<Map<String, Double>> solve(Map<String, Function> differentials, Map<String, Double> initial_state, double step_size, double stopping_point, String independent_variable);
}
