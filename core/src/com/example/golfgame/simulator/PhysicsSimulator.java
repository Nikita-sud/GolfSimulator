package com.example.golfgame.simulator;

import com.example.golfgame.bot.agents.PPOAgent;
import com.example.golfgame.utils.BallState;
import com.example.golfgame.utils.Function;
import com.example.golfgame.utils.MatrixUtils;
import com.example.golfgame.utils.Transition;
import com.example.golfgame.utils.Action;
import com.example.golfgame.utils.State;
import com.example.golfgame.utils.TerrainManager;
import com.example.golfgame.physics.PhysicsEngine;
import com.example.golfgame.physics.ODE.RungeKutta;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Map;

public class PhysicsSimulator {
    private PhysicsEngine engine;
    private BallState ball;
    private BallState goal;
    private static Random random = new Random(2024);
    private PPOAgent agent;
    private boolean inWater = false;
    TerrainManager terrainManager;

    private static final double GOAL_RADIUS = 1; // Радиус цели для вознаграждения
    private static final double PENALTY_HIT = -1; // Наказание за каждый удар
    private static final double PENALTY_WATER = -3; // Наказание за попадание в воду
    private static final double REWARD_GOAL = 5; // Вознаграждение за попадание в цель

    public PhysicsSimulator(Function heightFunction, PPOAgent agent, BallState goal) {
        engine = new PhysicsEngine(new RungeKutta(), heightFunction);
        ball = new BallState(0, 0, 0, 0);
        terrainManager =  new TerrainManager(heightFunction, 200, 200, 1, 4);
        this.agent = agent;
        this.goal = goal;
    }

    /**
     * performs hit simulation
     * @param velocityMagnitude
     * @param angle
     * @return
     */
    public BallState hit(float velocityMagnitude, float angle) {
        System.out.printf("Hitting with force: %.2f and angle: %.2f\n", velocityMagnitude, angle);
        ball.setVx(velocityMagnitude * Math.cos(angle));
        ball.setVy(velocityMagnitude * Math.sin(angle));
        Map<String, Double> functionVals = new HashMap<>();
        while (!engine.isAtRest(ball)) {
            functionVals.put("x", ball.getX());
            functionVals.put("y", ball.getY());
            if (engine.getSurfaceFunction().evaluate(functionVals) < 0) { // Water
                System.out.println("Ball in water!");
                return ball;
            }
            engine.update(ball, 0.01);
        }
        System.out.printf("New ball position: (%.2f, %.2f)\n", ball.getX(), ball.getY());
        return ball;
    }

    public double getReward(BallState ball) {
        double distanceToGoal = ball.distanceTo(goal);
        System.out.println(distanceToGoal);
        if (distanceToGoal < GOAL_RADIUS) {
            System.out.println("");
            System.out.println("Goal Reached");
            System.out.println("");
            return REWARD_GOAL;
        } else if (inWater) { // Ball is in water
            return PENALTY_WATER;
        } else {
            return PENALTY_HIT;
        }
    }

    public List<Transition> play() {
        List<Transition> transitions = new ArrayList<>();
        State initialState = new State(new double[]{ball.getX(), ball.getY(), ball.getVx(), ball.getVy()});
        for (int step = 0; step < 10; step++) {
            Action action = agent.selectRandomAction();
            BallState nextBallState = hit((float) action.getForce(), (float) action.getAngle());
            State nextState = new State(MatrixUtils.flattenArray(terrainManager.getNormalizedMarkedHeightMap((float) nextBallState.getX(), (float) nextBallState.getY(), 10, 10)));
            double reward = getReward(nextBallState);
            Transition transition = new Transition(initialState, action, reward, nextState);
            System.out.println(transition);
            transitions.add(transition);
            initialState = nextState;
            if (reward == REWARD_GOAL) {
                break;
            }
        }
        System.out.println("Done");
        return transitions;
    }

    public void runSimulation(int episodes) {
        for (int episode = 0; episode < episodes; episode++) {
            List<Transition> transitions = play();
            for (Transition transition : transitions) {
                agent.storeTransition(transition);
            }
            agent.train();
        }
    }

    /**
     * performs number of hit simulations from [0,0] or position set by setPosition method
     * @param velocityMagnitudes
     * @param angles
     * @return
     */
    public BallState[] hit(float[] velocityMagnitudes, float[] angles) {
        BallState[] res = new BallState[velocityMagnitudes.length];
        for (int i = 0; i < velocityMagnitudes.length; i++) {
            res[i] = hit(velocityMagnitudes[i], angles[i]);
            ball.setX(0);
            ball.setY(0);
        }
        return res;
    }

    /**
     * performs number of hit simulations within a certain unit radius of the goal
     * @param velocityMagnitudes
     * @param angles
     * @return
     */
    public BallState[] randomHits(int n, BallState goal, float radius) {
        BallState[] res = new BallState[n];
        for (int i = 0; i < n; i++) {
            float ballX = random.nextFloat(-radius, radius);
            float ballY = random.nextBoolean() ? (float) Math.sqrt(radius * radius - ballX * ballX) : -(float) Math.sqrt(radius * radius - ballX * ballX);
            ballX += goal.getX();
            ballY += goal.getY();
            ball.setX(ballX);
            ball.setY(ballY);
            float velocityMagnitude = random.nextFloat(1, 5);
            float angle = random.nextFloat(0f, (float) (2 * Math.PI));
            res[i] = hit(velocityMagnitude, angle);
        }
        return res;
    }

     /**
     * performs number of hit simulations within a certain unit radius of the goal
     * @param n number of simulations
     * @param goal target BallState
     * @param radius search radius
     * @return array of BallState results
     */
    public BallState[] parallelRandomHits(int n, BallState goal, float radius) throws InterruptedException, ExecutionException {
        BallState[] res = new BallState[n];
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<BallState>> futures = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            futures.add(executor.submit(() -> {
                float ballX = random.nextFloat() * (2 * radius) - radius;
                float ballY = random.nextBoolean() ? (float) Math.sqrt(radius * radius - ballX * ballX) : -(float) Math.sqrt(radius * radius - ballX * ballX);
                ballX += goal.getX();
                ballY += goal.getY();
                ball.setX(ballX);
                ball.setY(ballY);
                float velocityMagnitude = random.nextFloat() * (5 - 1) + 1;
                float angle = random.nextFloat() * (2 * (float) Math.PI);
                return hit(velocityMagnitude, angle);
            }));
        }

        for (int i = 0; i < n; i++) {
            res[i] = futures.get(i).get();
        }

        executor.shutdown();
        return res;
    }

    /**
     * sets ball Position
     * @param x
     * @param y
     */
    public void setPosition(float x, float y) {
        ball.setX(x);
        ball.setY(y);
    }

    public void checkIfInWater(BallState ball){
        this.inWater = false;
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Function h = new Function("cos(0.3x)*sin(0.5y)+10", "x", "y");
        int[] policyNetworkSizes = {40000, 64, 64, 4}; // Input size, hidden layers, and output size
        int[] valueNetworkSizes = {40000, 64, 64, 1};  // Input size, hidden layers, and output size
        double gamma = 0.99;
        double lambda = 0.95;
        double epsilon = 0.2;

        TerrainManager terrainManager =  new TerrainManager(h, 200, 200, 1, 4);
    
        PPOAgent agent = new PPOAgent(policyNetworkSizes, valueNetworkSizes, gamma, lambda, epsilon);
        BallState goal = new BallState(10, 10, 0, 0);
    
        PhysicsSimulator sim = new PhysicsSimulator(h, agent, goal);
    
        // Run simulation for training the agent
        sim.runSimulation(1000);
    
        System.out.println("Simulation complete");
    
        // Test the agent with one game
        BallState initialBallState = new BallState(0, 0, 0, 0);
        State initialState = new State(MatrixUtils.flattenArray(terrainManager.getNormalizedMarkedHeightMap(0, 0, 10, 10)));
        BallState ball = initialBallState;
    
        for (int step = 0; step < 200; step++) {
            Action action = agent.selectAction(initialState);
            ball = sim.hit((float) action.getForce(), (float) action.getAngle());
            State nextState = new State(new double[]{ball.getX(), ball.getY(), ball.getVx(), ball.getVy()});
    
            double distanceToGoal = Math.sqrt(Math.pow(ball.getX() - goal.getX(), 2) + Math.pow(ball.getY() - goal.getY(), 2));
            System.out.printf("Step %d: Ball at (%.2f, %.2f), Distance to goal: %.2f\n", step, ball.getX(), ball.getY(), distanceToGoal);
    
            if (distanceToGoal < 0.5) {
                System.out.println("Goal reached!");
                break;
            }
    
            initialState = nextState;
        }
    }
}