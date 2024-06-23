package com.example.golfgame.utils;

import java.util.Comparator;

public class ApproximateStateComparator implements Comparator<BallState> {
    private final double epsilon;

    public ApproximateStateComparator(double epsilon) {
        this.epsilon = epsilon;
    }

    @Override
    public int compare(BallState state1, BallState state2) {
        // Если позиции равны с учетом epsilon, объекты считаются равными
        if (state1.epsilonPositionEquals(state2, epsilon)) {
            return 0;
        }

        // Сравниваем по координате X
        int xCompare = Double.compare(state1.getX(), state2.getX());
        if (xCompare != 0) {
            return xCompare;
        }

        // Если X равны, сравниваем по координате Y
        return Double.compare(state1.getY(), state2.getY());
    }
}