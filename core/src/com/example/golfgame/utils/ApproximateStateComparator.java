package com.example.golfgame.utils;

import java.util.Comparator;

public class ApproximateStateComparator implements Comparator<BallState> {
    private final double epsilon;

    public ApproximateStateComparator(double epsilon) {
        this.epsilon = epsilon;
    }

    @Override
    public int compare(BallState state1, BallState state2) {
        if (state1.epsilonPositionEquals(state2, epsilon)) {
            return 0;
        }
        return state1.hashCode() - state2.hashCode(); // Это упрощенный подход, вы можете использовать более сложный метод сравнения
    }
}