package com.example.golfgame.bot;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.botsbehaviors.AdvancedBot;
import com.example.golfgame.bot.botsbehaviors.PPOBot;
import com.example.golfgame.bot.botsbehaviors.RuleBasedBot;

public class WallE {

    private volatile GolfGame game;
    private BotBehavior botBehavior;
    private RuleBasedBot ruleBasedBot;
    private AdvancedBot advancedBot;
    private PPOBot ppoBot;

    public WallE(GolfGame game) {
        this.game = game;
        this.ruleBasedBot = new RuleBasedBot();
        this.advancedBot = new AdvancedBot();
        this.botBehavior = ruleBasedBot; // Default behavior
    }

    public void setDirection() {
        float adjustedAngle = botBehavior.setDirection(game);
        game.getGolfGameScreen().setCameraAngle(adjustedAngle);
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

    public void switchToAdvanced() {
        setBotBehavior(advancedBot);
    }

    public void switchToPPO() {
        setBotBehavior(ppoBot); 
    }

    public BotBehavior getBotBehavior(){
        return botBehavior;
    }
}