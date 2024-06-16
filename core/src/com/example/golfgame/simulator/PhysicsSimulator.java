package com.example.golfgame.simulator;

import com.example.golfgame.bot.agents.PPOAgent;
import com.example.golfgame.utils.*;
import com.example.golfgame.physics.PhysicsEngine;
import com.example.golfgame.physics.ODE.RungeKutta;

import java.io.IOException;
import java.util.*;

public class PhysicsSimulator {
    private PhysicsEngine engine;
    private BallState ball;
    private BallState goal;
    private static Random random = new Random(2024);
    private PPOAgent agent;
    private boolean inWater = false;
    private TerrainManager terrainManager;

    private static final double GOAL_RADIUS = 2; // Radius for goal reward
    // private static final double PENALTY_WATER = -3; // Penalty for hitting water
    private static final double REWARD_GOAL = 5; // Reward for reaching the goal

    public PhysicsSimulator(Function heightFunction, PPOAgent agent, BallState goal) {
        this.engine = new PhysicsEngine(new RungeKutta(), heightFunction);
        this.ball = new BallState(0, 0, 0, 0);
        this.terrainManager = new TerrainManager(heightFunction, 200, 200, 1, 4);
        this.agent = agent;
        this.goal = goal;
    }
    
    public PhysicsSimulator(Function heightFunction, BallState goal) {
        this.engine = new PhysicsEngine(new RungeKutta(), heightFunction);
        this.ball = new BallState(0, 0, 0, 0);
        this.terrainManager = new TerrainManager(heightFunction, 200, 200, 1, 4);
        this.goal = goal;
    }

    /**
     * Performs a hit simulation.
     * @param velocityMagnitude the magnitude of the velocity
     * @param angle the angle of the hit
     * @return the new ball state
     */
    public BallState hit(float velocityMagnitude, float angle) {
        BallState ballCopy = new BallState(ball.getX(), ball.getY(), ball.getVx(), ball.getVy());
        System.out.printf("Hitting with force: %.2f and angle: %.2f\n", velocityMagnitude, angle);
        ballCopy.setVx(-velocityMagnitude * Math.cos(angle));
        ballCopy.setVy(-velocityMagnitude * Math.sin(angle));
        Map<String, Double> functionVals = new HashMap<>();

        while (!engine.isAtRest(ballCopy)) {
            functionVals.put("x", ballCopy.getX());
            functionVals.put("y", ballCopy.getY());
            if (engine.getSurfaceFunction().evaluate(functionVals) < 0) { // Water
                System.out.println("Ball in water!");
                inWater = true;
                return ballCopy;
            }
            engine.update(ballCopy, 0.001);
        }
        System.out.printf("New ball position: (%.2f, %.2f)\n", ballCopy.getX(), ballCopy.getY());
        return ballCopy;
    }

    public BallState singleHit(float velocityMagnitude, float angle, BallState ballPosition){
        resetBallPosition();
        return hit(velocityMagnitude, angle);
    }

    /**
     * Computes the reward based on the ball state.
     * @param currentBall the current ball state
     * @param lastPosition the last position of the ball
     * @param win whether the goal is reached
     * @param isBallInWater whether the ball is in water
     * @return the computed reward
     */
    public double getReward(BallState currentBall, BallState lastPosition, boolean win, boolean isBallInWater) {
        double distanceToGoal = currentBall.distanceTo(goal);
        double lastDistanceToGoal = lastPosition.distanceTo(goal);

        // Reward calculation
        double reward = lastDistanceToGoal - distanceToGoal;

        if (distanceToGoal < GOAL_RADIUS) {
            System.out.println("");
            System.out.println("Goal Reached");
            System.out.println("");
            return REWARD_GOAL;
        } else {
            return reward;
        }
    }

    /**
     * Performs multiple hit simulations.
     * @param velocityMagnitudes array of velocity magnitudes
     * @param angles array of angles
     * @return array of resulting ball states
     */
    public BallState[] hit(float[] velocityMagnitudes, float[] angles) {
        BallState[] res = new BallState[velocityMagnitudes.length];
        for (int i = 0; i < velocityMagnitudes.length; i++) {
            res[i] = hit(velocityMagnitudes[i], angles[i]);
            resetBallPosition();
        }
        return res;
    }

    /**
     * Resets the ball position to the initial state.
     */
    private void resetBallPosition() {
        ball.setX(0);
        ball.setY(0);
    }

    /**
     * Performs random hit simulations within a certain radius of the goal.
     * @param n number of simulations
     * @param goal target goal state
     * @param radius radius around the goal
     * @return array of resulting ball states
     */
    public BallState[] randomHits(int n, BallState goal, float radius) {
        BallState[] res = new BallState[n];
        for (int i = 0; i < n; i++) {
            float ballX = random.nextFloat() * (2 * radius) - radius;
            float ballY = random.nextBoolean() ? (float) Math.sqrt(radius * radius - ballX * ballX) : -(float) Math.sqrt(radius * radius - ballX * ballX);
            ballX += goal.getX();
            ballY += goal.getY();
            ball.setX(ballX);
            ball.setY(ballY);
            float velocityMagnitude = random.nextFloat() * (5 - 1) + 1;
            float angle = random.nextFloat() * (2 * (float) Math.PI);
            res[i] = hit(velocityMagnitude, angle);
        }
        return res;
    }

    /**
     * Sets the ball position.
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void setPosition(float x, float y) {
        ball.setX(x);
        ball.setY(y);
    }

    public double[] getState() {
        return MatrixUtils.flattenArray(terrainManager.getNormalizedMarkedHeightMap((float) ball.getX(), (float) ball.getY(), (float) goal.getX(), (float) goal.getY()));
    }
    @SuppressWarnings("static-access")
    public void image(){
        terrainManager.saveHeightMapAsImage(terrainManager.getNormalizedMarkedHeightMap((float) ball.getX(), (float) ball.getY(), (float) goal.getX(), (float) goal.getY()), "height_map", "png");
    }

    public void runSimulation(int episodes, float radius) {
        for (int episode = 0; episode < episodes; episode++) {
            runSingleEpisode(radius);
            System.out.printf("Episode %d:", episode);
        }
    }

    private double runSingleEpisode(float radius) {
        resetBallPosition();
        float ballX = random.nextFloat() * (2 * radius) - radius;
        float ballY = random.nextBoolean() ? (float) Math.sqrt(radius * radius - ballX * ballX) : -(float) Math.sqrt(radius * radius - ballX * ballX);
        ballX += goal.getX();
        ballY += goal.getY();
        ball.setX(ballX);
        ball.setY(ballY);
        double totalReward = 0;
        BallState lastPosition = new BallState(ball.getX(), ball.getY(), ball.getVx(), ball.getVy());

        for (int step = 0; step < 50; step++) {
            double[] stateArray = getState();
            State state = new State(stateArray);
            Action action = agent.selectRandomAction();
            BallState newBallState = hit((float) action.getForce(), (float) action.getAngle());
            boolean win = newBallState.distanceTo(goal) < GOAL_RADIUS;
            double reward = getReward(newBallState, lastPosition, win, inWater);
            totalReward += reward;
            double[] newStateArray = getState();
            State newState = new State(newStateArray);
            Transition transition = new Transition(state, action, reward, newState);
            agent.storeTransition(transition);
            lastPosition = new BallState(newBallState.getX(), newBallState.getY(), newBallState.getVx(), newBallState.getVy());
            if (win) {
                break;
            }
        }
        agent.train();
        for (int step = 0; step < 50; step++) {
            double[] stateArray = getState();
            State state = new State(stateArray);
            Action action = agent.selectAction(state);
            BallState newBallState = hit((float) action.getForce(), (float) action.getAngle());
            boolean win = newBallState.distanceTo(goal) < GOAL_RADIUS;
            double reward = getReward(newBallState, lastPosition, win, inWater);
            totalReward += reward;
            double[] newStateArray = getState();
            State newState = new State(newStateArray);
            Transition transition = new Transition(state, action, reward, newState);
            agent.storeTransition(transition);
            lastPosition = new BallState(newBallState.getX(), newBallState.getY(), newBallState.getVx(), newBallState.getVy());
            if (win) {
                break;
            }
        }
        agent.train();
        return totalReward;
    }

    public static void main(String[] args) throws IOException {
        Function h = new Function("2", "x", "y");
        int[] policyNetworkSizes = {40000, 64, 64, 4}; // Input size, hidden layers, and output size
        int[] valueNetworkSizes = {40000, 64, 64, 1};  // Input size, hidden layers, and output size
        double gamma = 0.99;
        double lambda = 0.95;
        double epsilon = 0.2;

        PPOAgent agent = new PPOAgent(policyNetworkSizes, valueNetworkSizes, gamma, lambda, epsilon);
        BallState goal = new BallState(5, 5, 0, 0);

        PhysicsSimulator simulator = new PhysicsSimulator(h, agent, goal);
        // simulator.image();
        simulator.runSimulation(100, 3); // Run for 10 episodes
        agent.saveAgent("savedAgent/savedAgent.dat");
    }
}
