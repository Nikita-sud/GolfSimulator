package com.example.golfgame.physics.ODE;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.example.golfgame.utils.Function;

/**
 * This class implements the ODE interface to solve ordinary differential equations using the Midpoint method.
 * The Midpoint method, also known as the second-order Runge-Kutta method, provides a balance between accuracy
 * and computational efficiency, improving upon the Euler method by using an intermediate step to calculate
 * the slope.
 */
public class Midpoint implements ODE {

    /**
     * Solves the system of differential equations using the Midpoint method given the set of differentials,
     * initial conditions, step size, and stopping point.
     *
     * @param differentials A map where each key is a string representing the dependent variable and
     *                      each value is a Function object to evaluate the derivative at a given state.
     * @param initial_state The initial values for all variables in the system, including the independent variable.
     * @param step_size The interval size to use for advancing the independent variable, must be a positive number.
     * @param stopping_point The final value of the independent variable where the computation will stop.
     * @param independent_variable The variable considered as independent, typically time.
     * @return A list of maps, each containing the state of all dependent variables after each step, including
     *         the independent variable updated to the corresponding time point.
     *
     * @throws IllegalArgumentException if step_size is non-positive, or if initial_state does not contain
     *                                  the independent variable, ensuring correct and meaningful input values.
     */
    @Override
    public List<Map<String, Double>> solve(Map<String, Function> differentials, Map<String, Double> initial_state, double step_size, double stopping_point, String independent_variable) {
        if (step_size <= 0) {
            throw new IllegalArgumentException("Step size must be positive.");
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
            Map<String, Double> midPointState = new HashMap<>(current_state);

            // Compute midpoint values for each dependent variable
            for (String var : dependent_variables) {
                if (!var.equals(independent_variable)) {
                    double k1 = differentials.get(var).evaluate(current_state);
                    midPointState.put(var, current_state.get(var) + 0.5 * step_size * k1);
                }
            }
            midPointState.put(independent_variable, current_time + 0.5 * step_size);

            // Use midpoint values to compute the next state
            for (String var : dependent_variables) {
                if (!var.equals(independent_variable)) {
                    double k2 = differentials.get(var).evaluate(midPointState);
                    newState.put(var, current_state.get(var) + step_size * k2);
                }
            }
            current_time += step_size;
            newState.put(independent_variable, current_time);

            values.add(newState);
            current_state = new HashMap<>(newState);
        }

        return values;
    }
}
