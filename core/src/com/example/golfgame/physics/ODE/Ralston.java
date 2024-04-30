package com.example.golfgame.physics.ODE;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.example.golfgame.utils.Function;

/**
 * This class implements the ODE interface to solve ordinary differential equations using the Ralston method.
 * The Ralston method is a specific type of Runge-Kutta method that uses a weighted average of two slopes
 * (k1 and k2) to achieve a second-order accurate numerical solution. It is particularly known for its
 * accuracy and stability in solving stiff differential equations.
 */
public class Ralston implements ODE {

    /**
     * Solves the differential equations using the Ralston method, calculating the state of the system
     * at each step from the initial state to the stopping point.
     *
     * @param differentials A map where each key is a string representing the dependent variable, and each
     *                      value is a Function object that computes the derivative based on the current state.
     * @param initial_state A map specifying the initial values for all variables in the system, including the
     *                      independent variable which typically represents time.
     * @param step_size The step size to increment the independent variable, must be positive to ensure progression.
     * @param stopping_point The final value of the independent variable where the computation stops.
     * @param independent_variable The string identifier of the independent variable in the differential equations.
     * @return A list of maps, each representing the state of the system at a specific increment in the independent
     *         variable. Each map contains values for all dependent and independent variables updated to that point.
     * 
     * @throws IllegalArgumentException if the step_size is zero or negative, or if the initial_state does not include
     *                                  the independent variable. These checks ensure valid inputs for reliable simulation.
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
            Map<String, Double> midState = new HashMap<>(current_state);

            // Compute k1 for each dependent variable
            Map<String, Double> k1 = new HashMap<>();
            for (String var : dependent_variables) {
                if (!var.equals(independent_variable)) {
                    k1.put(var, differentials.get(var).evaluate(current_state));
                }
            }

            // Calculate the midState for computing k2
            for (String var : dependent_variables) {
                if (!var.equals(independent_variable)) {
                    midState.put(var, current_state.get(var) + 0.75 * step_size * k1.get(var));
                }
            }
            midState.put(independent_variable, current_time + 0.75 * step_size);
            Map<String, Double> k2 = new HashMap<>();
            for (String var : dependent_variables) {
                if (!var.equals(independent_variable)) {
                    k2.put(var, differentials.get(var).evaluate(midState));
                }
            }

            // Update the state using a weighted average of k1 and k2
            for (String var : dependent_variables) {
                if (!var.equals(independent_variable)) {
                    double nextValue = current_state.get(var) + step_size * ((1.0 / 3.0) * k1.get(var) + (2.0 / 3.0) * k2.get(var));
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
}
