package com.example.golfgame.bot;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.botsbehaviors.AdvancedBot;
import com.example.golfgame.bot.botsbehaviors.PPOBot;
import com.example.golfgame.bot.botsbehaviors.RuleBasedBot;
import com.example.golfgame.utils.TerrainManager;

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

        TerrainManager terrainManager = game.getGolfGameScreen().getTerrainManager();
        int height = terrainManager.getTerrainHeight();
        int width = terrainManager.getTerrainWidth();

        int channels = 1; // Replace with actual width
        int numNumericFeatures = 4;
        int outputSize = 2;
        int memoryCapacity = 1000;
        double epsilon = 1.0;
        double gamma = 0.99;
        int batchSize = 32;
        int updateSteps = 10;
        double clipValue = 0.2;

        this.ppoBot = new PPOBot(height, width, channels, numNumericFeatures, outputSize, memoryCapacity, epsilon, gamma, batchSize, updateSteps, clipValue);
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