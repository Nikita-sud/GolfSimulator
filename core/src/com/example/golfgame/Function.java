package com.example.golfgame;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class Function implements DifferentialEquation {
    private Expression expression;

    public Function(String expressionString) {
        this.expression = new ExpressionBuilder(expressionString)
                .variables("x", "y", "t") 
                .build();
    }

    public void setExpression(String expressionString){
        this.expression = new ExpressionBuilder(expressionString)
                .variables("x", "y", "t")
                .build();
    }

    @Override
    public double evaluate(double x, double y) {

        expression.setVariable("x", x);
        expression.setVariable("y", y);
        expression.setVariable("t", 0);

        return expression.evaluate();
    }
}
