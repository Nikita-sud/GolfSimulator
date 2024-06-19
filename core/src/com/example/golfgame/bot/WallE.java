package com.example.golfgame.bot;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.botsbehaviors.AdvancedBot;
import com.example.golfgame.bot.botsbehaviors.HillClimbingBot;
import com.example.golfgame.bot.botsbehaviors.PPOBot;
import com.example.golfgame.bot.botsbehaviors.RuleBasedBot;
import com.example.golfgame.bot.agents.PPOAgent;

import java.io.IOException;

public class WallE {
    private volatile GolfGame game;
    private BotBehavior botBehavior;
    private RuleBasedBot ruleBasedBot;
    private HillClimbingBot hillClimbingBot;
    private AdvancedBot advancedBot;
    private PPOBot ppoBot;

    public WallE(GolfGame game) {
        this.game = game;
        this.ruleBasedBot = new RuleBasedBot();
        this.advancedBot = new AdvancedBot();
        this.hillClimbingBot = new HillClimbingBot();
        PPOAgent ppoAgent = null;

        try {
            ppoAgent = PPOAgent.loadAgent("savedAgent/savedAgent.dat");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (ppoAgent != null) {
            this.ppoBot = new PPOBot(ppoAgent);
        } else {
            // Handle the case where the PPO agent couldn't be loaded
            this.ppoBot = new PPOBot(new PPOAgent(new int[]{4, 64, 64, 4}, new int[]{4, 64, 64, 1}, 0.99, 0.95, 0.2));
        }

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
        setBotBehavior(hillClimbingBot);
    }

    public void switchToAdvanced() {
        setBotBehavior(advancedBot);
    }

    public void switchToPPO() {
        setBotBehavior(ppoBot);
    }

    public BotBehavior getBotBehavior() {
        return botBehavior;
    }

    public HillClimbingBot getHillClimbingBot(){
        return hillClimbingBot;
    }


}