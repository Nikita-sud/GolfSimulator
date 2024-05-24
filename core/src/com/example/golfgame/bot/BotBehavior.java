package com.example.golfgame.bot;

import com.example.golfgame.GolfGame;

public interface BotBehavior {
    float setDirection(GolfGame game);
    void hit(GolfGame game);
}
