package com.example.golfgame.bot.botsbehaviors;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.BotBehavior;
import com.example.golfgame.bot.agents.PPOAgent;
import com.example.golfgame.utils.BallState;
import com.example.golfgame.utils.Function;
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
    private TerrainManager stateTerrainManager; // 10x10 grid matching training dimensions
    private Action lastAction; // Cache the action between setDirection and hit

    /**
     * Constructs a PPOBot with a specified PPOAgent and height function.
     *
     * @param agent the PPOAgent used to determine actions
     * @param heightFunction the terrain height function (used to create a 10x10 state grid)
     */
    public PPOBot(PPOAgent agent, Function heightFunction) {
        this.agent = agent;
        // Use 10x10 grid with scale=5 to cover [-25, 25] around ball (state = 102 elements)
        this.stateTerrainManager = new TerrainManager(heightFunction, 10, 10, 5, 1);
    }

    /**
     * Updates the height function used for state extraction.
     *
     * @param heightFunction the new terrain height function
     */
    public void updateHeightFunction(Function heightFunction) {
        this.stateTerrainManager = new TerrainManager(heightFunction, 10, 10, 5, 1);
    }

    /**
     * Sets the direction for the bot by using the PPO agent to select an action based on the current state.
     *
     * @param game the GolfGame instance containing the game state and settings
     * @return the target angle for the camera
     */
    @Override
    public float setDirection(GolfGame game) {
        BallState ball = game.getGolfGameScreen().getBallState();
        BallState goal = game.getGolfGameScreen().getGoalState();
        double[] stateArray = stateTerrainManager.getState(
            (float) ball.getX(), (float) ball.getY(),
            (float) goal.getX(), (float) goal.getY());
        State state = new State(stateArray);

        // Select action once and cache it for hit()
        lastAction = agent.selectAction(state);

        return (float) lastAction.getAngle();
    }

    /**
     * Hits the ball using the cached action from setDirection.
     *
     * @param game the GolfGame instance containing the game state and settings
     */
    @Override
    public void hit(GolfGame game) {
        if (lastAction != null) {
            game.getGolfGameScreen().performHit((float) lastAction.getForce());
            lastAction = null;
        }
    }
}