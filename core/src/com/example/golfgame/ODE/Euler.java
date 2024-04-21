package com.example.golfgame.ODE;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.example.golfgame.Function;

/**
 * Implements the Euler method for solving ordinary differential equations (ODEs).
 * The Euler method is a simple numerical procedure for solving initial value problems
 * of the form dy/dt = f(t, y), with a given initial condition y(t0) = y0.
 *
 * <p>This class extends the abstract ODE class and provides an implementation of the
 * Euler method to approximate the solution over a specified range.
 */
public class Euler extends ODE {

    /**
     * Solves the differential equations using the Euler method.
     *
     * @param differentials a map where keys are the names of dependent variables, and values are
     *                      functions defining the derivatives of these variables.
     * @param initial_state a map where keys are the names of all variables (including the independent
     *                      variable), and values are the initial values for these variables.
     * @param step_size the increment in the independent variable for each step of the Euler method.
     * @param stopping_point the final value of the independent variable at which the computation should stop.
     * @param independent_variable the name of the independent variable in the differential equations, typically time 't'.
     * @return a list of maps, each representing the state of all variables at a given step.
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
            for (String var : dependent_variables) {
                if (!var.equals(independent_variable)) {
                    double newValue = current_state.get(var) + step_size * differentials.get(var).evaluate(current_state);
                    newState.put(var, newValue);
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
