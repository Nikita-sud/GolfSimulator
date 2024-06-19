package com.example.golfgame.bot.botsbehaviors;

import java.util.concurrent.*;
import java.util.Random;
import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.BotBehavior;
import com.example.golfgame.simulator.PhysicsSimulator;
import com.example.golfgame.utils.BallState;
import com.example.golfgame.utils.Function;

public class HillClimbingBot implements BotBehavior {

    private volatile float hitPower;
    private volatile float angle;

    private static final float DELTAHITPOWER = 0.5f;
    private static final float DELTAANGLE = 0.2f;
    private static final float ANGLE_TOLERANCE = 0.1f;
    private static final float GOAL_TOLERANCE = 1.5f;

    private boolean isDirectionSet = false;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public HillClimbingBot() {
        hitPower = 3;
        angle = 0;
    }

    @Override
    public float setDirection(GolfGame game) {
        System.out.println("setDirection happened");

        CompletableFuture<Float> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("Thread running");
            climb(game);
            isDirectionSet = true;  // Set the flag to true after climbing
            return angle;
        }, executorService);

        try {
            return future.get(); // This will block until the callable is done
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            shutdownExecutor(); // Ensure the executor is properly shut down
        }

        return angle;
    }

    public void shutdownExecutor() {
        executorService.shutdown();
    }
    

    @Override
    public void hit(GolfGame game) {
        if (Math.abs(game.getGolfGameScreen().getCameraAngle() - angle) < ANGLE_TOLERANCE) {
            game.getGolfGameScreen().performHit(hitPower);
            isDirectionSet = false; // Reset the flag after hitting
        }
    }

    private void climb(GolfGame game) {
        BallState goal = game.getGolfGameScreen().getGoalState();
        PhysicsSimulator simulator = new PhysicsSimulator(game.getGolfGameScreen().getHeightFunction(), goal);
        Random random = new Random();

        if (hillClimb(simulator, game, goal)) return;
        expandSearchRange(simulator, game, goal, random);
        hillClimb(simulator, game, goal);
    }

    private boolean hillClimb(PhysicsSimulator simulator, GolfGame game, BallState goal) {
        boolean improved = true;
        while (improved) {
            improved = false;

            BallState curSimResult = simulator.singleHit(hitPower, angle, game.getGolfGameScreen().getBallState());
            System.out.printf("Current Sim Result: (%.2f, %.2f) with force %.2f and angle %.2f\n", curSimResult.getX(), curSimResult.getY(), hitPower, angle);

            if (curSimResult.epsilonPositionEquals(goal, GOAL_TOLERANCE)) {
                System.out.println("Goal reached within tolerance!");
                return true;
            }

            BallState[] neighbors = {
                simulator.singleHit(hitPower + DELTAHITPOWER, angle, game.getGolfGameScreen().getBallState()),
                simulator.singleHit(Math.max(0.1f, hitPower - DELTAHITPOWER), angle, game.getGolfGameScreen().getBallState()),
                simulator.singleHit(hitPower, angle + DELTAANGLE, game.getGolfGameScreen().getBallState()),
                simulator.singleHit(hitPower, angle - DELTAANGLE, game.getGolfGameScreen().getBallState()),
                curSimResult
            };

            for (int i = 0; i < neighbors.length; i++) {
                System.out.printf("Neighbor %d: (%.2f, %.2f) with force %.2f and angle %.2f\n", i, neighbors[i].getX(), neighbors[i].getY(), neighbors[i].getVx(), neighbors[i].getVy());
            }

            BallState bestState = bestState(neighbors, goal);

            if (!bestState.equals(curSimResult)) {
                improved = true;
                if (bestState.equals(neighbors[0])) {
                    hitPower += DELTAHITPOWER;
                } else if (bestState.equals(neighbors[1])) {
                    hitPower = Math.max(0.1f, hitPower - DELTAHITPOWER);
                } else if (bestState.equals(neighbors[2])) {
                    angle += DELTAANGLE;
                } else if (bestState.equals(neighbors[3])) {
                    angle -= DELTAANGLE;
                }
                System.out.printf("Improved to: (%.2f, %.2f) with force %.2f and angle %.2f\n", bestState.getX(), bestState.getY(), hitPower, angle);
            }

            if (Math.abs(bestState.getX() - goal.getX()) < 0.01 && Math.abs(bestState.getY() - goal.getY()) < 0.01) {
                break;
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void expandSearchRange(PhysicsSimulator simulator, GolfGame game, BallState goal, Random random) {
        float originalHitPower = hitPower;
        float originalAngle = angle;

        for (float deltaPower = -2 * DELTAHITPOWER; deltaPower <= 2 * DELTAHITPOWER; deltaPower += DELTAHITPOWER) {
            for (float deltaAngle = -2 * DELTAANGLE; deltaAngle <= 2 * DELTAANGLE; deltaAngle += DELTAANGLE) {
                if (deltaPower == 0 && deltaAngle == 0) continue;

                BallState newState = simulator.singleHit(Math.max(0.1f, originalHitPower + deltaPower), originalAngle + deltaAngle, game.getGolfGameScreen().getBallState());
                if (newState.distanceTo(goal) < simulator.singleHit(hitPower, angle, game.getGolfGameScreen().getBallState()).distanceTo(goal)) {
                    hitPower = Math.max(0.1f, originalHitPower + deltaPower);
                    angle = originalAngle + deltaAngle;
                }
            }
        }

        for (int i = 0; i < 5; i++) {
            float randomHitPower = Math.max(0.1f, originalHitPower + (random.nextFloat() - 0.5f) * 4 * DELTAHITPOWER);
            float randomAngle = originalAngle + (random.nextFloat() - 0.5f) * 4 * DELTAANGLE;

            BallState randomState = simulator.singleHit(randomHitPower, randomAngle, game.getGolfGameScreen().getBallState());
            if (randomState.distanceTo(goal) < simulator.singleHit(hitPower, angle, game.getGolfGameScreen().getBallState()).distanceTo(goal)) {
                hitPower = randomHitPower;
                angle = randomAngle;
            }
        }
    }

    public boolean isDirectionSet() {
        return isDirectionSet;
    }

    private BallState bestState(BallState[] states, BallState goal) {
        double smallestDistance = Double.MAX_VALUE;
        BallState best = null;
        for (BallState state : states) {
            double distance = state.distanceTo(goal);
            if (distance < smallestDistance) {
                smallestDistance = distance;
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