package com.example.golfgame.bot.botsbehaviors;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.BotBehavior;
import com.example.golfgame.bot.agents.PPOAgent;
import com.example.golfgame.utils.BallState;
import com.example.golfgame.utils.gameUtils.TerrainManager;
import com.example.golfgame.utils.ppoUtils.Action;
import com.example.golfgame.utils.ppoUtils.State;

/**
 * The PPOBot class implements the BotBehavior interface and defines
 * a behavior for the bot using Proximal Policy Optimization (PPO) agent to decide
 * the direction and force for hitting the ball.
 */
public class PPOBot implements BotBehavior {
    private PPOAgent agent;

    /**
     * Constructs a PPOBot with a specified PPOAgent.
     *
     * @param agent the PPOAgent used to determine actions
     */
    public PPOBot(PPOAgent agent) {
        this.agent = agent;
    }

    /**
     * Sets the direction for the bot by using the PPO agent to select an action based on the current state.
     *
     * @param game the GolfGame instance containing the game state and settings
     * @return the target angle for the camera
     */
    @Override
    public float setDirection(GolfGame game) {
        // Get the current state of the game
        BallState ball = game.getGolfGameScreen().getBallState();
        BallState goal = game.getGolfGameScreen().getGoalState();
        TerrainManager terrainManager = game.getGolfGameScreen().getTerrainManager();
        double[] stateArray = terrainManager.getState((float) ball.getX(), (float) ball.getY(), (float) goal.getX(), (float) goal.getY());
        State state = new State(stateArray);

        // Use the PPO agent to select an action
        Action action = agent.selectAction(state);

        // Get the angle from the action
        float targetAngle = (float) action.getAngle();

        return targetAngle;
    }

    /**
     * Hits the ball by using the PPO agent to select an action based on the current state.
     *
     * @param game the GolfGame instance containing the game state and settings
     */
    @Override
    public void hit(GolfGame game) {
        // Get the current state of the game
        BallState ball = game.getGolfGameScreen().getBallState();
        BallState goal = game.getGolfGameScreen().getGoalState();
        TerrainManager terrainManager = game.getGolfGameScreen().getTerrainManager();
        double[] stateArray = terrainManager.getState((float) ball.getX(), (float) ball.getY(), (float) goal.getX(), (float) goal.getY());
        State state = new State(stateArray);

        // Use the PPO agent to select an action
        Action action = agent.selectAction(state);

        // Perform the hit action
        game.getGolfGameScreen().performHit((float) action.getForce());
    }
}