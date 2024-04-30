package com.example.golfgame.physics.ODE;

import java.util.List;
import java.util.Map;
import com.example.golfgame.utils.Function;

/**
 * The ODE interface defines a method for solving ordinary differential equations (ODEs) using numerical methods.
 * Implementations of this interface are expected to provide specific algorithms for approximating solutions to ODEs.
 */
public interface ODE {

    /**
     * Solves a system of ordinary differential equations using a numerical method specified by the implementing class.
     * The method calculates values over a specified range of the independent variable, starting from an initial state.
     *
     * @param differentials A map where keys represent the names of dependent variables and values are Function instances.
     *                      Each Function calculates the derivative of its corresponding variable based on the current state.
     * @param initial_state A map specifying the initial values for all variables in the system, including the independent variable.
     * @param step_size The increment by which the independent variable is increased on each step.
     *                  It must be a positive value to ensure forward progression in the calculations.
     * @param stopping_point The final value of the independent variable at which the calculation will cease.
     *                       This value determines the end of the simulation period.
     * @param independent_variable The name of the independent variable, which typically represents time or some form of progression.
     *                             This variable controls the iterations of the solving process.
     * @return A list of maps, each representing the state of all variables at a specific step of the computation.
     *         Each element in the list corresponds to an increment in the independent variable, beginning from the initial state and
     *         continuing until the stopping point.
     *
     * @throws IllegalArgumentException if the step_size is non-positive or if the initial_state does not contain the independent variable.
     *                                 Such checks ensure that the parameters are valid and meaningful for the numerical solution process.
     */
    public abstract List<Map<String, Double>> solve(Map<String, Function> differentials, Map<String, Double> initial_state, double step_size, double stopping_point, String independent_variable);
}
