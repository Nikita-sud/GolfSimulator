package com.example.golfgame.physics.ODE;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.example.golfgame.utils.Function;

/**
 * This class implements the ODE interface to solve ordinary differential equations using the Euler method.
 * The Euler method is a numerical procedure for approximating solutions to a particular kind of initial value problem.
 * It approximates the continuous function by using its derivative at several points.
 *
 * @see com.example.golfgame.physics.ODE.ODE
 */
public class Euler implements ODE {

    /**
     * Solves differential equations using the Euler method, given the differential equations,
     * initial state, step size, stopping point, and the independent variable.
     * 
     * @param differentials A map of the differential equations, where each key is a string representing
     *                      the dependent variable and each value is a Function object that computes the derivative.
     * @param initial_state A map representing the initial state of the system. The keys are the variables names and
     *                      the values are their respective initial values.
     * @param step_size The size of each step to take in the independent variable (often time).
     * @param stopping_point The value of the independent variable at which to stop the simulation.
     * @param independent_variable The name of the independent variable in the equations (often time).
     * @return A list of maps, each representing the state of the dependent variables at each step up to the stopping point.
     *         Each map is similar to the initial_state format, with an additional key for the independent variable.
     * 
     * @throws IllegalArgumentException if step_size is non-positive or if the initial_state does not contain
     *                                  the independent variable.
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
