package com.example.golfgame.bot.botsbehavior;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.BotBehavior;
import com.example.golfgame.utils.BallState;

public class HillClimbingBot implements BotBehavior {

    private static final int MAX_ITERATIONS = 100;
    private static final float STEP_SIZE = 0.05f;

    @Override
    public float setDirection(GolfGame game) {
        BallState ballState = game.getGolfGameScreen().getBallState().copy();
        BallState goalState = game.getGolfGameScreen().getGoalState().copy();

        float currentAngle = game.getGolfGameScreen().getCameraAngel();
        float bestAngle = currentAngle;
        float bestDistance = Float.MAX_VALUE;

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            float testAngle = currentAngle + (i - MAX_ITERATIONS / 2) * STEP_SIZE;
            BallState testState = simulateHit(ballState, testAngle);

            float distanceToGoal = distance(testState, goalState);
            if (distanceToGoal < bestDistance) {
                bestDistance = distanceToGoal;
                bestAngle = testAngle;
            }
        }

        return bestAngle;
    }

    private BallState simulateHit(BallState ballState, float angle) {
        // Simulate the ball state after a hit with the given angle
        // This is a placeholder logic, replace it with the actual simulation logic
        BallState simulatedState = ballState.copy();
        simulatedState.setX(ballState.getX() + (float) Math.cos(angle));
        simulatedState.setY(ballState.getY() + (float) Math.sin(angle));
        return simulatedState;
    }

    private float distance(BallState state1, BallState state2) {
        float dx = (float) (state1.getX() - state2.getX());
        float dy = (float) (state1.getY() - state2.getY());
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public void hit(GolfGame game) {
        float currentAngle = game.getGolfGameScreen().getCameraAngel();
        game.getGolfGameScreen().setCameraAngel(currentAngle);
        game.getGolfGameScreen().setBotHitTriggered(true);
    }
}
