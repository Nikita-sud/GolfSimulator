package com.example.golfgame.bot.botsbehaviors;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.BotBehavior;
import com.example.golfgame.bot.agents.PPOAgent;
import com.example.golfgame.utils.Action;
import com.example.golfgame.utils.BallState;
import com.example.golfgame.utils.State;
import com.example.golfgame.utils.TerrainManager;

public class PPOBot implements BotBehavior {
    private PPOAgent agent;

    public PPOBot(PPOAgent agent) {
        this.agent = agent;
    }

    @Override
    public float setDirection(GolfGame game) {
        // Get the current state of the game
        BallState ball = game.getGolfGameScreen().getBallState();
        BallState goal = game.getGolfGameScreen().getGoalState();
        TerrainManager terrainManager = game.getGolfGameScreen().getTerrainManager();
        double[] stateArray = terrainManager.getState((float) ball.getX(),(float) ball.getY(),(float) goal.getX(),(float) goal.getY());
        State state = new State(stateArray);

        // Use the PPO agent to select an action
        Action action = agent.selectAction(state);

        // Get the angle from the action
        float targetAngle = (float) action.getAngle();

        return targetAngle;
    }


    @Override
    public void hit(GolfGame game) {
        // Get the current state of the game
        BallState ball = game.getGolfGameScreen().getBallState();
        BallState goal = game.getGolfGameScreen().getGoalState();
        TerrainManager terrainManager = game.getGolfGameScreen().getTerrainManager();
        double[] stateArray = terrainManager.getState((float) ball.getX(),(float) ball.getY(),(float) goal.getX(),(float) goal.getY());
        State state = new State(stateArray);

        // Use the PPO agent to select an action
        Action action = agent.selectAction(state);

        // Perform the hit action
        game.getGolfGameScreen().performHit((float) action.getForce());
    }
}