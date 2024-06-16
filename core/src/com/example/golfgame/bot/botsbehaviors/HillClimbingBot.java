package com.example.golfgame.bot.botsbehaviors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.BotBehavior;
import com.example.golfgame.simulator.PhysicsSimulator;
import com.example.golfgame.utils.BallState;
import com.example.golfgame.utils.Function;

public class HillClimbingBot implements BotBehavior {

    private volatile float hitPower;
    private volatile float angle;

    private static final float DELTAHITPOWER = 0.2f;
    private static final float DELTAANGLE = 0.05f;
    private static final float ANGLE_TOLERANCE = 0.1f;

    public HillClimbingBot() {
        hitPower = 3;
        angle = 0;
    }

    @Override
    public float setDirection(GolfGame game) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<?> future = executorService.submit(() -> climb(game));

        try {
            future.get(); // Wait for the climb method to finish
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }

        return angle;
    }

    @Override
    public void hit(GolfGame game) {
        if (Math.abs(game.getGolfGameScreen().getCameraAngle() - angle) < ANGLE_TOLERANCE) {
            game.getGolfGameScreen().performHit(hitPower);
        }
    }

    private void climb(GolfGame game) {
        BallState goal = game.getGolfGameScreen().getGoalState();
        PhysicsSimulator simulator = new PhysicsSimulator(game.getGolfGameScreen().getHeightFunction(), goal);

        while (true) {
            BallState curSimResult = simulator.singleHit(hitPower, angle, game.getGolfGameScreen().getBallState());

            // Try climbing in 4 directions: increasing/decreasing angle/hitPower
            BallState doubleIncreaseResult = simulator.singleHit(hitPower + DELTAHITPOWER, angle + DELTAANGLE, game.getGolfGameScreen().getBallState());
            BallState angleIncreaseResult = simulator.singleHit(hitPower - DELTAHITPOWER, angle + DELTAANGLE,game.getGolfGameScreen().getBallState());
            BallState hitPowerIncreaseResult = simulator.singleHit(hitPower + DELTAHITPOWER, angle - DELTAANGLE,game.getGolfGameScreen().getBallState());
            BallState doubleDecreaseResult = simulator.singleHit(hitPower - DELTAHITPOWER, angle - DELTAANGLE,game.getGolfGameScreen().getBallState());

            BallState bestState = bestState(new BallState[]{doubleDecreaseResult, doubleIncreaseResult, angleIncreaseResult, hitPowerIncreaseResult, curSimResult}, goal);

            if (bestState.equals(curSimResult)) {
                break; // No improvement found
            }

            if (bestState.epsilonPositionEquals(doubleIncreaseResult, 0.001)) {
                angle += DELTAANGLE;
                hitPower += DELTAHITPOWER;
            } else if (bestState.epsilonPositionEquals(doubleDecreaseResult, 0.001)) {
                angle -= DELTAANGLE;
                hitPower -= DELTAHITPOWER;
            } else if (bestState.epsilonPositionEquals(angleIncreaseResult, 0.001)) {
                angle += DELTAANGLE;
                hitPower -= DELTAHITPOWER;
            } else if (bestState.epsilonPositionEquals(hitPowerIncreaseResult, 0.001)) {
                angle -= DELTAANGLE;
                hitPower += DELTAHITPOWER;
            }

            // Ensure we have a termination condition to avoid infinite loop
            if (Math.abs(bestState.getX() - goal.getX()) < 0.01 && Math.abs(bestState.getY() - goal.getY()) < 0.01) {
                break;
            }
        }
    }

    private BallState bestState(BallState[] states, BallState goal) {
        double bestCloseness = Double.MIN_VALUE;
        BallState best = null;
        for (BallState state : states) {
            double closeness = closeness(state, goal);
            if (closeness > bestCloseness) {
                bestCloseness = closeness;
                best = state;
            }
        }
        return best;
    }

    private float closeness(BallState simResult, BallState goal) {
        return (float) (1 / simResult.distanceTo(goal));
    }

    public static void main(String[] args) {
        Function h = new Function("cos(0.3x)*sin(0.5y)+10", "x", "y");
        PhysicsSimulator sim = new PhysicsSimulator(h, new BallState(5, 5, 0, 0));
        HillClimbingBot hc = new HillClimbingBot();
    }
}
