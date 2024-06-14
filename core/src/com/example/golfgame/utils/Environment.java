package com.example.golfgame.utils;

import java.util.List;

public class Environment {

    private Function heightFunction;
    private BallState goal;
    private List<Sandbox> sandboxes;

    /**
     * Generates Environment object with specified parameters
     * @param heightFunction
     * @param goal
     * @param sandboxes
     */
    public Environment(Function heightFunction, BallState goal, List<Sandbox> sandboxes){
        this.heightFunction = heightFunction;
        this.goal = goal;
        this.sandboxes = sandboxes;
    }
    /**
     * Generates Environment with random 5-degree polynomial as height function, random goal position within some radius of [0,0] 
     * and random sandboxes with some restrictions
     * TODO: Specify restrections on random generations
     */
    public Environment(){
        //TODO: Implement Method
    }
}
