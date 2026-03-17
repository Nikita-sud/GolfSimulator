package com.example.golfgame.simulator;

import com.badlogic.gdx.math.Vector2;
import com.example.golfgame.bot.agents.PPOAgent;
import com.example.golfgame.utils.*;
import com.example.golfgame.utils.gameUtils.TerrainManager;
import com.example.golfgame.utils.ppoUtils.Action;
import com.example.golfgame.utils.ppoUtils.State;
import com.example.golfgame.utils.ppoUtils.Transition;
import com.example.golfgame.physics.PhysicsEngine;
import com.example.golfgame.physics.ODE.ODE;
import com.example.golfgame.physics.ODE.RungeKutta;
import com.example.golfgame.screens.GolfGameScreen;
import java.util.*;

public class PhysicsSimulator {
    private PhysicsEngine engine;
    private BallState ball;
    private BallState goal;
    private static Random random = new Random(2024);
    private PPOAgent agent;
    private boolean inWater = false;
    private TerrainManager terrainManager;
    private List<Function> functions = new ArrayList<>();

    private static final double GOAL_RADIUS = 1.5; // Radius for goal reward
    private static final double PENALTY_WATER = -3; // Penalty for hitting water
    private static final double PENALTY_SAND = -1; // Penalty for being on sand
    private static final double REWARD_GOAL = 5; // Reward for reaching the goal

    private static final float engineStepSize = 0.001f;

    /**
     * Constructs a PhysicsSimulator with specified height function and agent.
     *
     * @param heightFunction the function defining the terrain height.
     * @param agent the PPOAgent used for the simulation.
     */
    public PhysicsSimulator(String heightFunction, PPOAgent agent) {
        addFunction(heightFunction);
        Function fheightFunction = new Function(heightFunction, "x","y");
        this.engine = new PhysicsEngine(new RungeKutta(), fheightFunction);
        this.ball = new BallState(0, 0, 0, 0);
        // ИЗМЕНИТЕ ЭТУ СТРОКУ: Используйте конструктор с размерами
        // Например, для карты 10x10 (stateDim=100):
        this.terrainManager = new TerrainManager(fheightFunction, 10, 10, 5, 1); // 10x10 grid, scale=5
        // Или для карты 20x20 (stateDim=400):
        // this.terrainManager = new TerrainManager(fheightFunction, 20, 20, 1, 1);
        this.agent = agent;
        this.goal = new BallState(-20, 20, 0, 0); // Match game's default goal position
    }
    
    /**
     * Constructs a PhysicsSimulator with specified height function and goal state.
     *
     * @param heightFunction the function defining the terrain height.
     * @param goal the target goal state.
     */
    public PhysicsSimulator(Function heightFunction, BallState goal) {
        this.engine = new PhysicsEngine(new RungeKutta(), heightFunction);
        this.ball = new BallState(0, 0, 0, 0);
        this.terrainManager = new TerrainManager(heightFunction);
        this.goal = goal;
    }

    /**
     * Constructs a PhysicsSimulator with specified height function, goal state, and solver.
     *
     * @param heightFunction the function defining the terrain height.
     * @param goal the target goal state.
     * @param solver the ODE solver used for the simulation.
     */
    public PhysicsSimulator(Function heightFunction, BallState goal, ODE solver){
        this.engine = new PhysicsEngine(solver, heightFunction);
        this.ball = new BallState(0, 0, 0.001, 0.001);
        this.terrainManager = new TerrainManager(heightFunction);
        this.goal = goal;
    }

    /**
     * Changes the height function used in the simulation.
     *
     * @param heightFunction the new function defining the terrain height.
     */
    public void changeHeightFunction(Function heightFunction){
        this.engine = new PhysicsEngine(new RungeKutta(), heightFunction);
        this.terrainManager = new TerrainManager(heightFunction);
    }

    /**
     * Performs a hit simulation.
     *
     * @param velocityMagnitude the magnitude of the velocity
     * @param angle the angle of the hit
     * @return the new ball state
     */
    public BallState hit(float velocityMagnitude, float angle) {
        inWater = false;
        BallState ballCopy = ball.deepCopy();
        // System.out.printf("Hitting with force: %.2f and angle: %.2f\n", velocityMagnitude, angle);
        ballCopy.setVx(-velocityMagnitude * Math.cos(angle));
        ballCopy.setVy(-velocityMagnitude * Math.sin(angle));

        Map<String, Double> functionVals = new HashMap<>();
        BallState lastBallState = ballCopy.deepCopy(); // Use ballCopy directly

        while (true) {
            functionVals.put("x", ballCopy.getX());
            functionVals.put("y", ballCopy.getY());

            // Check if the ball is in water
            if (terrainManager.isWater((float) ballCopy.getX(), (float) ballCopy.getY())) {
                System.out.println("Ball in water!");
                inWater = true;
                ballCopy.setX(lastBallState.getX());
                ballCopy.setY(lastBallState.getY());
                return ballCopy;
            }

            // Check if the ball has reached the goal
            if (GolfGameScreen.validSimulatorGoal(ballCopy, goal)) {
                System.out.println("Goal reached in simulator!");
                return ballCopy;
            }

            // Update the last ball state before updating the current ball state
            lastBallState.set(ballCopy.getX(), ballCopy.getY(), ballCopy.getVx(), ballCopy.getVy());

            // Update the ball state
            engine.update(ballCopy, engineStepSize);

            // Check if the ball is at rest
            if (engine.isAtRest(ballCopy)) {
                break;
            }
        }

        // Check if the ball is on sand
        if (terrainManager.isBallOnSand((float) ballCopy.getX(), (float) ballCopy.getY())) {
            System.out.println("Ball on sand!");
        }

        // System.out.printf("New ball position: (%.2f, %.2f)\n", ballCopy.getX(), ballCopy.getY());
        return ballCopy;
    }

    /**
     * Performs a hit simulation and returns the path.
     *
     * @param velocityMagnitude the magnitude of the velocity
     * @param angle the angle of the hit
     * @return a Pair containing the final BallState and the path of the ball as a list of Vector2 points
     */
    public Pair<BallState, List<Vector2>> hitWithPath(float velocityMagnitude, float angle) {
        inWater = false;
        BallState lastPosition = ball.deepCopy();
        BallState ballCopy = ball.deepCopy();
        // System.out.printf("Hitting with force: %.2f and angle: %.2f\n", velocityMagnitude, angle);
        ballCopy.setVx(-velocityMagnitude * Math.cos(angle));
        ballCopy.setVy(-velocityMagnitude * Math.sin(angle));
        List<Vector2> path = new ArrayList<>();
        path.add(new Vector2((float)ballCopy.getX(), (float)ballCopy.getY()));

        BallState lastBallState = null;
        do {
            if (terrainManager.isWater((float) ballCopy.getX(), (float) ballCopy.getY())) { // Water
                System.out.println("Ball in water!");
                inWater = true;
                ballCopy.setX(lastPosition.getX());
                ballCopy.setY(lastPosition.getY());
                return new Pair<>(ballCopy, path);
            }
            lastBallState = new BallState(ballCopy.getX(), ballCopy.getY(), ballCopy.getVx(), ballCopy.getVy());
            engine.update(ballCopy, engineStepSize);
            path.add(new Vector2((float)ballCopy.getX(), (float)ballCopy.getY()));
        } while (!ballCopy.epsilonEquals(lastBallState, 0));

        if (terrainManager.isBallOnSand((float) ballCopy.getX(), (float) ballCopy.getY())) { // Sand
            System.out.println("Ball on sand!");
        }

        // System.out.printf("New ball position: (%.2f, %.2f)\n", ballCopy.getX(), ballCopy.getY());
        return new Pair<>(ballCopy, path);
    }

    /**
     * Performs a single hit simulation at a specified position.
     *
     * @param velocityMagnitude the magnitude of the velocity
     * @param angle the angle of the hit
     * @param ballPosition the position of the ball
     * @return the new ball state
     */
    public BallState singleHit(float velocityMagnitude, float angle, BallState ballPosition){
        resetBallPosition(ballPosition);
        return hit(velocityMagnitude, angle);
    }

    /**
     * Computes the reward based on the ball state.
     *
     * @param currentBall the current ball state
     * @param lastPosition the last position of the ball
     * @param win whether the goal is reached
     * @param isBallInWater whether the ball is in water
     * @return the computed reward
     */
    public double getReward(BallState currentBall, BallState lastPosition, boolean win, boolean isBallInWater) {
        double distanceToGoal = currentBall.distanceTo(goal);
        double lastDistanceToGoal = lastPosition.distanceTo(goal);

        if (distanceToGoal < GOAL_RADIUS) {
            System.out.println("Goal Reached!");
            return REWARD_GOAL;
        }
        if (isBallInWater) {
            return PENALTY_WATER;
        }

        // Reward = how much closer we got to the goal (positive = closer, negative = farther)
        double reward = lastDistanceToGoal - distanceToGoal;

        if (terrainManager.isBallOnSand((float) currentBall.getX(), (float) currentBall.getY())) {
            reward += PENALTY_SAND;
        }
        return reward;
    }

    /**
     * Performs multiple hit simulations.
     *
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
     * Resets the ball position to a specified state.
     *
     * @param ballPosition the position to reset the ball to
     */
    private void resetBallPosition(BallState ballPosition){
        ball.setX(ballPosition.getX());
        ball.setY(ballPosition.getY());
    }
    
    /**
     * Performs random hit simulations within a certain radius of the goal.
     *
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
     *
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void setPosition(float x, float y) {
        ball.setX(x);
        ball.setY(y);
    }

    /**
     * Returns the current state of the terrain.
     *
     * @return a flattened array representing the normalized height map with marked ball, goal, and sand positions
     */
    public double[] getState() {
        return terrainManager.getState(
            (float) ball.getX(), (float) ball.getY(), (float) goal.getX(), (float) goal.getY()
        );
    }

    /**
     * Saves the height map as an image.
     */
    @SuppressWarnings("static-access")
    public void image(){
        terrainManager.saveHeightMapAsImage(terrainManager.getNormalizedMarkedHeightMap((float) ball.getX(), (float) ball.getY(), (float) goal.getX(), (float) goal.getY()), "height_map", "png");
    }

    /**
     * Adds a function to the list of functions.
     *
     * @param function the function to add
     */
    public void addFunction(String function){
        functions.add(new Function(function, "x","y"));
    }
    public List<Transition> collectTransitions(int n_steps) {
        List<Transition> collectedData = new ArrayList<>();
        // Сбрасываем состояние симулятора (позиция мяча и т.д.)
        resetSimulationState(); // Вам нужно будет реализовать этот метод
    
        int current_step = 0;
        while (current_step < n_steps) {
            // Логика одного шага симуляции:
            // 1. Получить текущее состояние (state)
            // 2. Выбрать действие (action) с помощью agent.selectAction(state)
            // 3. Выполнить действие в симуляторе, получить новое состояние (newState), награду (reward)
            // 4. Проверить, не закончился ли эпизод (win, inWater, outOfBounds)
    
            double[] stateArray = getState();
            State state = new State(stateArray);
            Action action = agent.selectAction(state);
            BallState lastBallStateBeforeHit = ball.deepCopy();
            BallState newBallStateAfterHit = hit((float) action.getForce(), (float) action.getAngle());

            boolean win = GolfGameScreen.validSimulatorGoal(newBallStateAfterHit, goal);
            double reward = getReward(newBallStateAfterHit, lastBallStateBeforeHit, win, inWater);

            // Update ball position BEFORE getting new state so getState() reflects the new position
            if (win || inWater) {
                resetSimulationState();
            } else {
                ball.set(newBallStateAfterHit.getX(), newBallStateAfterHit.getY(),
                         newBallStateAfterHit.getVx(), newBallStateAfterHit.getVy());
            }

            double[] newStateArray = getState();
            State newState = new State(newStateArray);

            Transition transition = new Transition(state, action, reward, newState);
            collectedData.add(transition);
            current_step++;
        }
        return collectedData;
    }

    private void resetSimulationState() {
        // Spawn ball at random distance from goal, simulating realistic game starts
        float radius = 5 + random.nextFloat() * 25; // 5 to 30 units from goal
        float angle = random.nextFloat() * 2 * (float) Math.PI;
        float ballX = (float) goal.getX() + radius * (float) Math.cos(angle);
        float ballY = (float) goal.getY() + radius * (float) Math.sin(angle);
        ball.set(ballX, ballY, 0, 0);
        inWater = false;
    }
    
    public void runSimulation(int total_timesteps, int n_steps_per_batch, int epochs_per_batch, int mini_batch_size) {
        int current_total_steps = 0;
        int batch_num = 0;

        // --- НАЧАЛО ЦИКЛА WHILE ---
        while(current_total_steps < total_timesteps) { // Условие проверяется здесь
            batch_num++;
            System.out.println("--------------------");
            System.out.println("Starting Batch " + batch_num);
            // --- ВЫВОД СООБЩЕНИЯ ---
            // Вот это сообщение выводится ПЕРЕД сбором данных для ТЕКУЩЕГО батча
            System.out.println("Collecting data... Timestep " + current_total_steps + "/" + total_timesteps);

            List<Transition> batchData = collectTransitions(n_steps_per_batch);

            // --- Логирование Наград (без изменений) ---
            double totalRewardInBatch = 0;
            if (batchData != null && !batchData.isEmpty()) {
                for(Transition t : batchData) {
                    totalRewardInBatch += t.getReward();
                }
                double averageReward = totalRewardInBatch / batchData.size();
                System.out.printf("Batch %d finished collecting. Steps: %d, Total Reward: %.4f, Average Reward: %.6f%n",
                                batch_num, batchData.size(), totalRewardInBatch, averageReward);
            } else {
                System.out.println("Batch " + batch_num + " finished collecting. No data collected.");
                if (n_steps_per_batch > 0) {
                    System.err.println("Warning: No data collected, potentially stuck. Check simulator logic.");
                    break;
                }
            }

            // --- Обучение (без изменений) ---
            if (batchData != null && !batchData.isEmpty()) {
                System.out.println("Training on collected data (" + batchData.size() + " transitions)...");
                agent.train(batchData, epochs_per_batch, mini_batch_size, 0.0001, 0.0003);
            }

            // --- Обновление счетчика ---
            // Счетчик обновляется ПОСЛЕ обучения на батче
            current_total_steps += (batchData != null ? batchData.size() : 0);

        } // --- КОНЕЦ ЦИКЛА WHILE ---
        // Условие current_total_steps < total_timesteps проверяется снова

        System.out.println("--------------------");
        // Этот вывод происходит ПОСЛЕ выхода из цикла while
        System.out.println("Simulation finished after " + current_total_steps + " timesteps.");
    }
}