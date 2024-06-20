package com.example.golfgame.bot.botsbehaviors;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Random;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.BotBehavior;
import com.example.golfgame.screens.GolfGameScreen;
import com.example.golfgame.simulator.PhysicsSimulator;
import com.example.golfgame.utils.BallState;
import com.example.golfgame.utils.Function;

public class HillClimbingBot implements BotBehavior {

    private volatile float hitPower;
    private volatile float angle;

    private static final float DELTAHITPOWER = 0.2f;
    private static final float DELTAANGLE = 0.1f;
    private static final float ANGLE_TOLERANCE = 0.1f;

    private static final float MAX_FORCE = 10.0f; // Maximum force
    private static final float MIN_FORCE = 1.0f;  // Minimum force

    private static final float GOAL_TOLERANCE = 1.5f;
    
    private boolean isDirectionSet = false;

    private RuleBasedBot helper;

    private ExecutorService executorService = Executors. newSingleThreadExecutor();

    public HillClimbingBot() {
        hitPower = 3;
        angle = 0;

        helper = new RuleBasedBot();
    }

    @Override
    public float setDirection(GolfGame game) {
        System.out.println("setDirection happened");

        Future<Float> future = executorService.submit(new Callable<Float>() {
            @Override
            public Float call() {
                System.out.println("Thread running");
                initializeAngle(game); initializeForce(game); // Initial guesses for force and angle
                climb(game);
                return angle;
            }
        });

        try {
            return future.get(); // This will block until the callable is done
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return angle;
    }

    private void initializeForce(GolfGame game) {
        BallState ballState = game.getGolfGameScreen().getBallState();
        BallState goal = game.getGolfGameScreen().getGoalState();

        // Calculate the distance to the goal
        double distance = Math.sqrt(Math.pow(goal.getX() - ballState.getX(), 2) + Math.pow(goal.getY() - ballState.getY(), 2));

        // Linearly interpolate force based on the distance
        hitPower = (float) Math.min(MAX_FORCE, Math.max(MIN_FORCE, distance / 4));
        System.out.printf("Initial guess for force based on distance (%.2f) is: %.2f\n", distance, hitPower);
    }

    private void initializeAngle(GolfGame game){
        // Initial guess based on rule-based estimate
        angle =(float) (helper.findTargetAngle(game)%(2*Math.PI));
        System.out.printf("Initial guess for angle is: %.2f\n", angle);
    }



    @Override
    public void hit(GolfGame game) {
        if (Math.abs(game.getGolfGameScreen().getCameraAngle() - angle) < ANGLE_TOLERANCE) {
            game.getGolfGameScreen().performHit(hitPower);
            isDirectionSet = false;
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

            // Check if the current result is within the goal tolerance
            if (GolfGameScreen.validGoal(curSimResult, goal)) {
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
                System.out.printf("Neighbor %d: (%.2f, %.2f) ", i, neighbors[i].getX(), neighbors[i].getY());
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

        // Introduce random jumps to escape local minima
        for (int i = 0; i < 5; i++) { // Try 5 random jumps
            float randomHitPower = Math.max(0.1f, originalHitPower + (random.nextFloat() - 0.5f) * 4 * DELTAHITPOWER);
            float randomAngle = originalAngle + (random.nextFloat() - 0.5f) * 4 * DELTAANGLE;

            BallState randomState = simulator.singleHit(randomHitPower, randomAngle, game.getGolfGameScreen().getBallState());
            if (randomState.distanceTo(goal) < simulator.singleHit(hitPower, angle, game.getGolfGameScreen().getBallState()).distanceTo(goal)) {
                hitPower = randomHitPower;
                angle = randomAngle;
            }
        }
    }

    public boolean isDirectionSet(){
        return isDirectionSet;
    }

    private BallState bestState(BallState[] states, BallState goal) {
        double smallestDistance = Integer.MAX_VALUE;
        BallState best = null;
        for (BallState state : states) {
            if (state.distanceTo(goal)<smallestDistance) {
                smallestDistance = state.distanceTo(goal);
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
