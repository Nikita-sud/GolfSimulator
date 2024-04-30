package com.example.golfgame.physics.ODE;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.example.golfgame.utils.Function;

/**
 * This class implements the ODE interface to solve ordinary differential equations using the Runge-Kutta method.
 * The Runge-Kutta method is a fourth-order method that provides high accuracy for numerical solutions of ODEs
 * by computing four intermediate slopes (k1, k2, k3, k4) to estimate the next value of the dependent variable.
 */
public class RungeKutta implements ODE {

    /**
     * Solves the differential equations using the fourth-order Runge-Kutta method. This method iteratively calculates
     * the next state of the system based on the weighted average of slopes at several points within each step.
     *
     * @param differentials A map of functions representing the differential equations for each dependent variable.
     * @param initial_state Initial values for all variables including the independent variable.
     * @param step_size The change in the independent variable for each step; should be a positive number.
     * @param stopping_point The value of the independent variable at which to stop the calculations.
     * @param independent_variable The variable considered as independent, commonly time.
     * @return A list of maps, each representing the state of the system at successive time steps,
     *         showing updated values for each dependent and independent variable.
     *
     * @throws IllegalArgumentException if step_size is zero or negative, or if the initial state does not contain the
     *                                  independent variable, to ensure correct calculations.
     */
    @Override
    public List<Map<String, Double>> solve(Map<String, Function> differentials, Map<String, Double> initial_state, double step_size, double stopping_point, String independent_variable) {
        if (step_size <= 0) {
            throw new IllegalArgumentException("Step size must be positive to progress the simulation.");
        }
        if (!initial_state.containsKey(independent_variable)) {
            throw new IllegalArgumentException("Initial state must include the independent variable.");
        }

        int steps = (int) ((stopping_point - initial_state.get(independent_variable)) / step_size);
        List<Map<String, Double>> values = new ArrayList<>();

        String[] dependent_variables = differentials.keySet().toArray(new String[0]);
        Map<String, Double> current_state = new HashMap<>(initial_state);
        double current_time = current_state.get(independent_variable);

        for (int i = 0; i < steps; i++) {
            Map<String, Double> newState = new HashMap<>();
            Map<String, Double> k1 = calculateSlope(differentials, current_state);
            Map<String, Double> k2 = calculateSlope(differentials, calculateIntermediateState(current_state, k1, step_size * 0.5, current_time + step_size * 0.5, independent_variable));
            Map<String, Double> k3 = calculateSlope(differentials, calculateIntermediateState(current_state, k2, step_size * 0.5, current_time + step_size * 0.5, independent_variable));
            Map<String, Double> k4 = calculateSlope(differentials, calculateIntermediateState(current_state, k3, step_size, current_time + step_size, independent_variable));

            // Combine slopes to calculate the next state
            for (String var : dependent_variables) {
                if (!var.equals(independent_variable)) {
                    double nextValue = current_state.get(var) + (step_size / 6.0) * (k1.get(var) + 2*k2.get(var) + 2*k3.get(var) + k4.get(var));
                    newState.put(var, nextValue);
                }
            }
            current_time += step_size;
            newState.put(independent_variable, current_time);

            values.add(newState);
            current_state = new HashMap<>(newState);
        }
        return values;
    }

    /**
     * Helper method to compute an intermediate state for the Runge-Kutta calculations.
     * This state is used to calculate the slope at the midpoint of each step or at the end of the step.
     *
     * @param currentState The current state of all variables.
     * @param slope The slopes at the current state, calculated from the differential equations.
     * @param stepFraction The fraction of the full step size to be used for this intermediate state.
     * @param newTime The new value of the independent variable for this intermediate state.
     * @param independent_variable The independent variable identifier.
     * @return A new state map for the next slope calculation, adjusted by the intermediate slopes and time step.
     */
    private Map<String, Double> calculateIntermediateState(Map<String, Double> currentState, Map<String, Double> slope, double stepFraction, double newTime, String independent_variable) {
        Map<String, Double> newState = new HashMap<>(currentState);
        newState.forEach((var, value) -> {
            if (!var.equals(independent_variable)) {
                newState.put(var, value + stepFraction * slope.get(var));
            }
        });
        newState.put(independent_variable, newTime);
        return newState;
    }

    /**
     * Calculates the slope for each variable using the differential equations provided.
     * This slope is essential for the intermediate state calculations in the Runge-Kutta method.
     *
     * @param differentials The differential equations as functions.
     * @param state The current state from which to calculate the slopes.
     * @return A map containing the calculated slopes for each variable, used in subsequent state updates.
     */
    private Map<String, Double> calculateSlope(Map<String, Function> differentials, Map<String, Double> state) {
        Map<String, Double> slope = new HashMap<>();
        differentials.forEach((var, func) -> slope.put(var, func.evaluate(state)));
        return slope;
    }
}
