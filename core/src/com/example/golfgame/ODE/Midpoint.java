package com.example.golfgame.ODE;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.example.golfgame.Function;

/**
 * Implements the Midpoint method, also known as the second-order Runge-Kutta method,
 * for solving ordinary differential equations (ODEs). This method provides a balance
 * between simplicity and accuracy, offering a good alternative to the Euler method
 * with improved stability and convergence properties.
 */
public class Midpoint extends ODE {

    /**
     * Solves a system of ordinary differential equations using the Midpoint method.
     *
     * @param differentials A map containing the differential equations. Each entry
     *                      associates a dependent variable name with its derivative function.
     * @param initial_state A map specifying initial values for each variable, including
     *                      the independent variable (often time).
     * @param step_size The increment for the independent variable in each simulation step.
     * @param stopping_point The final value of the independent variable at which the simulation stops.
     * @param independent_variable The identifier for the independent variable.
     * @return A list of state maps, each representing the state of all variables at
     *         a given step of integration.
     *
     * <p>This method approximates the solution by using the derivative at the midpoint
     * of each interval to estimate the end point. Specifically, it calculates an
     * intermediate value using the initial slope (like Euler's method), then computes
     * the slope at this midpoint to project the final value.</p>
     *
     * <p>The method steps through the problem domain by incrementing the independent
     * variable by {@code step_size} until it reaches or exceeds {@code stopping_point}.</p>
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
            Map<String, Double> midPointState = new HashMap<>(current_state);

            // Calculate midpoint state
            for (String var : dependent_variables) {
                if (!var.equals(independent_variable)) {
                    double k1 = differentials.get(var).evaluate(current_state);
                    midPointState.put(var, current_state.get(var) + 0.5 * step_size * k1);
                }
            }
            midPointState.put(independent_variable, current_time + 0.5 * step_size);

            // Calculate the next state using the slope at the midpoint
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
