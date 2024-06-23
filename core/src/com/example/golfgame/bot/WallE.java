package com.example.golfgame.bot;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.botsbehaviors.HillClimbingBot;
import com.example.golfgame.bot.botsbehaviors.RuleBasedBot;

/**
 * WallE class represents an intelligent agent for playing the golf game.
 * It can switch between different bot behaviors and control the game actions.
 */
public class WallE {
    private volatile GolfGame game;
    private BotBehavior botBehavior;
    private RuleBasedBot ruleBasedBot;
    private HillClimbingBot hillClimbingBot;

    /**
     * Constructs a WallE bot for the given game.
     *
     * @param game the game instance to control
     */
    public WallE(GolfGame game) {
        this.game = game;
        this.ruleBasedBot = new RuleBasedBot();
        this.hillClimbingBot = new HillClimbingBot();
        this.botBehavior = ruleBasedBot; // Default behavior
    }

    /**
     * Sets the direction for the bot based on its behavior.
     */
    public void setDirection() {
        float adjustedAngle = botBehavior.setDirection(game);
        game.getGolfGameScreen().setCameraAngle(adjustedAngle);
    }

    /**
     * Performs a hit action in the game based on the bot's behavior.
     */
    public synchronized void hit() {
        botBehavior.hit(game);
    }

    /**
     * Sets the bot's behavior to the specified behavior.
     *
     * @param botBehavior the new bot behavior to set
     */
    public void setBotBehavior(BotBehavior botBehavior) {
        this.botBehavior = botBehavior;
    }

    /**
     * Switches the bot's behavior to rule-based.
     */
    public void switchToRuleBased() {
        setBotBehavior(ruleBasedBot);
    }

    /**
     * Switches the bot's behavior to advanced.
     */
    public void switchToAdvanced() {
        setBotBehavior(hillClimbingBot);
    }

    /**
     * Gets the current bot behavior.
     *
     * @return the current bot behavior
     */
    public BotBehavior getBotBehavior() {
        return botBehavior;
    }

    /**
     * Gets the hill climbing bot instance.
     *
     * @return the hill climbing bot instance
     */
    public HillClimbingBot getHillClimbingBot() {
        return hillClimbingBot;
    }
}