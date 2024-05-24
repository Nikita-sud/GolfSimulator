package com.example.golfgame.bot;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.botsbehavior.HillClimbingBot;
import com.example.golfgame.bot.botsbehavior.RuleBasedBot;

public class WallE {

    private volatile GolfGame game;
    private BotBehavior botBehavior;
    private RuleBasedBot ruleBasedBot;
    private HillClimbingBot hillClimbingBot;

    public WallE(GolfGame game) {
        this.game = game;
        this.ruleBasedBot = new RuleBasedBot();
        this.hillClimbingBot = new HillClimbingBot();
        this.botBehavior = ruleBasedBot; // Default behavior
    }

    public void setDirection() {
        float adjustedAngle = botBehavior.setDirection(game);
        game.getGolfGameScreen().setCameraAngel(adjustedAngle);
    }

    public synchronized void hit() {
        botBehavior.hit(game);
    }

    public void setBotBehavior(BotBehavior botBehavior) {
        this.botBehavior = botBehavior;
    }

    public void switchToRuleBased() {
        setBotBehavior(ruleBasedBot);
    }

    public void switchToHillClimbing() {
        setBotBehavior(hillClimbingBot);
    }
}
