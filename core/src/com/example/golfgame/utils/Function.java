package com.example.golfgame.utils;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import java.util.Map;

/**
 * Represents a mathematical function that can be evaluated dynamically.
 * This class utilizes the exp4j library to parse and evaluate string expressions
 * based on the values provided for the variables involved.
 */
public class Function {
    private Expression expression;
    private String[] variables;

    /**
     * Constructs a new {@code Function} object from a given mathematical expression
     * as a string and a list of its variables.
     *
     * @param expressionString the string representation of the mathematical expression
     *                         (e.g., "sin(x) * cos(y) + z").
     * @param variables an array of strings representing the names of variables used
     *                  in the expression.
     *
     * <p>Example usage:</p>
     * <pre>{@code
     * Function f = new Function("sin(x) * cos(y) + z", "x", "y", "z");
     * Map<String, Double> values = Map.of("x", 1.0, "y", 2.0, "z", 3.0);
     * double result = f.evaluate(values); // evaluates sin(1) * cos(2) + 3
     * }</pre>
     */
    public Function(String expressionString, String... variables) {
        this.variables = variables;
        this.expression = new ExpressionBuilder(expressionString)
                .variables(variables)  // Declare all variables used in the expression
                .build();
    }

    /**
     * Evaluates the function based on the values provided for its variables.
     *
     * @param values a map where keys are the names of the variables and values
     *               are their corresponding numerical values.
     * @return the computed result of the function as a double.
     * @throws IllegalArgumentException if any variable value is missing in the input map.
     * 
     * <p>The {@code values} map must include entries for all variables used in the function.
     * If any variable is omitted, an {@code IllegalArgumentException} will be thrown.</p>
     */
    public double evaluate(Map<String, Double> values) {
        for (String variable : variables) {
            if (!values.containsKey(variable)) {
                throw new IllegalArgumentException("No value provided for variable: " + variable);
            }
            expression.setVariable(variable, values.get(variable));
        }
        return expression.evaluate();
    }
}
