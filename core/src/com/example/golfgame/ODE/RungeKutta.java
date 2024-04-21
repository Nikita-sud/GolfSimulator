package com.example.golfgame.ODE;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.example.golfgame.Function;

/**
 * Implements the classical fourth-order Runge-Kutta method for numerical solution of
 * ordinary differential equations. This method is known for its accuracy and stability,
 * making it one of the most widely used techniques for solving differential equations.
 */
public class RungeKutta extends ODE {

    /**
     * Solves differential equations using the fourth-order Runge-Kutta method.
     *
     * @param differentials A map where each key is a dependent variable and the value
     *                      is a Function that computes the derivative of that variable.
     * @param initial_state A map specifying the initial values of all variables,
     *                      including the independent variable.
     * @param step_size The step size to use for each iteration of the method.
     * @param stopping_point The value at which the independent variable should stop.
     * @param independent_variable The variable considered as the independent variable,
     *                             typically representing time or space.
     * @return A list of maps, each representing the state of all variables at successive
     *         steps of the independent variable.
     *
     * <p>Each iteration calculates four approximations (k1, k2, k3, k4) of the slope
     * using the differential equations provided, combining them to obtain a weighted
     * average that approximates the slope more accurately than a single-step method.</p>
     *
     * <p>This method provides a robust and accurate way to integrate systems of
     * differential equations but requires four evaluations of the derivative per step,
     * which may be computationally expensive for complex systems.</p>
     */
    @Override
    public List<Map<String, Double>> solve(Map<String, Function> differentials, Map<String, Double> initial_state, double step_size, double stopping_point, String independent_variable) {
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
            Map<String, Double> k4 = calculateSlope(differentials, calculateIntermediateState(current_state, k3, step_size, current_time + step_size,independent_variable));

            // Combine k1, k2, k3, and k4 to form the next state
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
     * Helper method to compute an intermediate state for the Runge-Kutta calculation.
     * @param currentState The current state of all variables.
     * @param slope The slopes at the current state.
     * @param stepFraction The fraction of the full step size to be used for this intermediate state.
     * @param newTime The new value of the independent variable.
     * @return A new state map for the next calculation.
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
     * @param differentials The differential equations as functions.
     * @param state The current state from which to calculate the slopes.
     * @return A map containing the calculated slopes for each variable.
     */
    private Map<String, Double> calculateSlope(Map<String, Function> differentials, Map<String, Double> state) {
        Map<String, Double> slope = new HashMap<>();
        differentials.forEach((var, func) -> slope.put(var, func.evaluate(state)));
        return slope;
    }
}
