package com.example.golfgame.simulator;

import com.example.golfgame.bot.agents.PPOAgent;
import com.example.golfgame.utils.*;
import com.example.golfgame.utils.gameUtils.TerrainManager;
import com.example.golfgame.utils.ppoUtils.Action;
import com.example.golfgame.utils.ppoUtils.Batch;
import com.example.golfgame.utils.ppoUtils.State;
import com.example.golfgame.utils.ppoUtils.Transition;
import com.example.golfgame.physics.PhysicsEngine;
import com.example.golfgame.physics.ODE.RungeKutta;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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
    private List<Batch> data = new ArrayList<>();
    private List<Function> functions = new ArrayList<>();

    private static final double GOAL_RADIUS = 1.5; // Radius for goal reward
    private static final double PENALTY_WATER = -3; // Penalty for hitting water
    private static final double PENALTY_SAND = -1; // Penalty for being on sand
    private static final double REWARD_GOAL = 5; // Reward for reaching the goal

    public PhysicsSimulator(String heightFunction, PPOAgent agent) {
        addFunction(heightFunction);
        Function fheightFunction = new Function(heightFunction, "x","y");
        this.engine = new PhysicsEngine(new RungeKutta(), fheightFunction);
        this.ball = new BallState(0, 0, 0, 0);
        this.terrainManager = new TerrainManager(fheightFunction);
        this.agent = agent;
        this.goal = new BallState(-7, 7, 0, 0);
    }
    
    public PhysicsSimulator(Function heightFunction, BallState goal) {
        this.engine = new PhysicsEngine(new RungeKutta(), heightFunction);
        this.ball = new BallState(0, 0, 0, 0);
        this.terrainManager = new TerrainManager(heightFunction);
        this.goal = goal;
    }

    public void changeHeightFunction(Function heightFunction){
        this.engine = new PhysicsEngine(new RungeKutta(), heightFunction);
        this.terrainManager = new TerrainManager(heightFunction);
    }

    /**
     * Performs a hit simulation.
     * @param velocityMagnitude the magnitude of the velocity
     * @param angle the angle of the hit
     * @return the new ball state
     */
    // Method to handle hitting the ball
    public BallState hit(float velocityMagnitude, float angle) {
        inWater = false;
        BallState lastPosition = new BallState(ball.getX(), ball.getY(), ball.getVx(), ball.getVy());
        BallState ballCopy = new BallState(ball.getX(), ball.getY(), ball.getVx(), ball.getVy());
        System.out.printf("Hitting with force: %.2f and angle: %.2f\n", velocityMagnitude, angle);
        ballCopy.setVx(-velocityMagnitude * Math.cos(angle));
        ballCopy.setVy(-velocityMagnitude * Math.sin(angle));
        Map<String, Double> functionVals = new HashMap<>();

        while (!engine.isAtRest(ballCopy)) {
            functionVals.put("x", ballCopy.getX());
            functionVals.put("y", ballCopy.getY());
            if (terrainManager.isWater((float) ballCopy.getX(), (float) ballCopy.getY())) { // Water
                System.out.println("Ball in water!");
                inWater = true;
                ballCopy.setX(lastPosition.getX());
                ballCopy.setY(lastPosition.getY());
                return ballCopy;
            }
            engine.update(ballCopy, 0.001);
        }

        if (terrainManager.isBallOnSand((float) ballCopy.getX(), (float)ballCopy.getY())) { // Sand
            System.out.println("Ball on sand!");
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
        }
        if (isBallInWater) {
            return reward + PENALTY_WATER;
        }
        if (terrainManager.isBallOnSand((float) currentBall.getX(), (float) currentBall.getY())) {
            return reward + PENALTY_SAND;
        }
        if (reward < 0) {
            double penaltyFactor = Math.exp(Math.abs(reward) / 10.0); // Exponential function
            reward -= penaltyFactor * 10; // Increase the penalty
        }
        return reward;
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

    public void addFunction(String function){
        functions.add(new Function(function, "x","y"));
    }

    public void runSimulation(int episodes, float radius,int steps) {
        for(Function function : functions){
            changeHeightFunction(function);
            for (int episode = 0; episode < episodes; episode++) {
                data.add(runSingleEpisode(radius,(int) Math.round(steps*0.2),true));
                data.add(runSingleEpisode(radius,(int) Math.round(steps*0.8),false));
                System.out.println("Episode: " + episode);
            }
        }
        agent.trainOnData(data);
    }
    public void runSimulationParallel(int episodes, float radius, int steps) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Batch>> futures = new ArrayList<>();
        
        for (Function function : functions) {
            changeHeightFunction(function);
            
            for (int episode = 0; episode < episodes; episode++) {
                final int ep = episode; // For lambda expression
                Callable<Batch> task = () -> {
                    if (ep % 2 == 0) {
                        return runSingleEpisode(radius, (int) Math.round(steps * 0.2), true);
                    } else {
                        return runSingleEpisode(radius, (int) Math.round(steps * 0.8), false);
                    }
                };
                futures.add(executor.submit(task));
            }
        }
        
        for (Future<Batch> future : futures) {
            try {
                data.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        
        executor.shutdown();
        agent.trainOnData(data);
    }

    private Batch runSingleEpisode(float radius, int steps, boolean randomAction) {
        List<Transition> batchTransitions = new ArrayList<>();
        resetBallPosition();
        float ballX = random.nextFloat() * (2 * radius) - radius;
        float ballY = random.nextBoolean() ? (float) Math.sqrt(radius * radius - ballX * ballX) : -(float) Math.sqrt(radius * radius - ballX * ballX);
        ballX += goal.getX();
        ballY += goal.getY();
        ball.setX(ballX);
        ball.setY(ballY);
        double totalReward = 0;
        BallState lastPosition = new BallState(ball.getX(), ball.getY(), ball.getVx(), ball.getVy());

        for (int step = 0; step < steps; step++) {
            double[] stateArray = getState();
            State state = new State(stateArray);
            Action action;
            if(randomAction){
                action = agent.selectRandomAction();
            }else{
                action = agent.selectAction(state);
            }
            BallState newBallState = hit((float) action.getForce(), (float) action.getAngle());
            boolean win = newBallState.distanceTo(goal) < GOAL_RADIUS;
            double reward = getReward(newBallState, lastPosition, win, inWater);
            totalReward += reward;
            double[] newStateArray = getState();
            State newState = new State(newStateArray);
            Transition transition = new Transition(state, action, reward, newState);
            batchTransitions.add(transition);
            lastPosition = new BallState(newBallState.getX(), newBallState.getY(), newBallState.getVx(), newBallState.getVy());
            if (win) {
                break;
            }
        }

        System.out.println("Total Reward: "+ totalReward);
        return new Batch(batchTransitions);
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String h = "2";
        @SuppressWarnings("unused")
        String[] functions = new String[]{"2(0.9-e^(-((x+10)^2+(y-10)^2)/200))-0.5",
                                        "2(0.9-e^(-((x+0)^2+(y-10)^6)/200))-1",
                                        "e^(-((x)^6+(y)^2)/500)-e^(-((x)^2+(y)^2)/500)+0.2"};
        // int[] policyNetworkSizes = {40000, 64, 64, 4}; // Input size, hidden layers, and output size
        // int[] valueNetworkSizes = {40000, 64, 64, 1};  // Input size, hidden layers, and output size
        // double gamma = 0.99;
        // double lambda = 0.95;
        // double epsilon = 0.2;

        PPOAgent agent = PPOAgent.loadAgent("savedAgent/savedAgent.dat");
        
        PhysicsSimulator simulator = new PhysicsSimulator(h, agent);
        // simulator.image();
        simulator.runSimulationParallel(500, 3, 30); // Run for 10 episodes
        agent.saveAgent("savedAgent/savedAgent.dat");
    }
}
