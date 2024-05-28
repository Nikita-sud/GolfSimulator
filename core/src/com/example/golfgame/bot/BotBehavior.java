package com.example.golfgame.bot;

import com.example.golfgame.GolfGame;

/**
 * An interface defining the behavior of a bot in the golf game.
 * Implementing classes should define how the bot sets its direction and hits the ball.
 */
public interface BotBehavior {

    /**
     * Sets the direction for the bot based on the current game state.
     *
     * @param game The current instance of the GolfGame.
     * @return The direction angle set for the bot.
     */
    float setDirection(GolfGame game);

    /**
     * Executes a hit action for the bot based on the current game state.
     *
     * @param game The current instance of the GolfGame.
     */
    void hit(GolfGame game);
}
