package com.example.golfgame.ODE;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.example.golfgame.Function;

/**
 * Implements the Ralston's method for solving ordinary differential equations (ODEs).
 * This class is a part of the ODE solver family, using a specific kind of Runge-Kutta
 * method that provides a good balance between accuracy and computational efficiency.
 *
 * <p>Ralston's method is particularly known for its second-order accuracy with fewer
 * function evaluations compared to other methods.</p>
 */
public class Ralston extends ODE {

    /**
     * Solves a system of differential equations using Ralston's method.
     *
     * @param differentials The map of dependent variables and their corresponding
     *                      differential equations as functions. These functions should
     *                      calculate the derivatives based on the current state.
     * @param initial_state The initial conditions of the dependent variables, including
     *                      the initial value of the independent variable.
     * @param step_size The increment by which the independent variable is increased on each step.
     * @param stopping_point The final value of the independent variable at which the integration
     *                       should stop.
     * @param independent_variable The name of the independent variable in the differential equations,
     *                             often time or space.
     * @return A list of state maps, each representing the state of all variables at a given
     *         increment of the independent variable.
     *
     * <p>The method utilizes a two-step approach to estimate the next value of the dependent
     * variables. The integration process calculates intermediate slopes (k1, k2) to determine
     * the best next approximation of the solution.</p>
     *
     * <p>It's recommended to use consistent units across all variables to avoid scale mismatches
     * and ensure numerical stability.</p>
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
            Map<String, Double> midState = new HashMap<>(current_state);

            // Calculate k1
            Map<String, Double> k1 = new HashMap<>();
            for (String var : dependent_variables) {
                if (!var.equals(independent_variable)) {
                    k1.put(var, differentials.get(var).evaluate(current_state));
                }
            }

            // Calculate k2
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

            // Update new state using weighted average of k1 and k2
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
