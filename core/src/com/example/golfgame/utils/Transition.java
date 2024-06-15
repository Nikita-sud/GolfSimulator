package com.example.golfgame.utils;

public class Transition {
    private State state1;
    private Action action;
    private double reward;
    private State state2;

    public Transition(State state1, Action action, double reward, State state2){
        this.state1 = state1;
        this.action = action;
        this.reward = reward;
        this.state2 = state2;
    }

    public void setState1(State state1){
        this.state1 = state1;
    }

    public void setAction(Action action){
        this.action = action;
    }

    public void setReward(double reward){
        this.reward = reward;
    }

    public void setState2(State state2){
        this.state2 = state2;
    }

    public State getState1(){
        return state1;
    }

    public Action getAction(){
        return action;
    }

    public double getReward(){
        return reward;
    }

    public State getState2(){
        return state2;
    }

    @Override
    public String toString() {
        return "Transition{" +
                "state1=" + state1 +
                ", action=" + action +
                ", reward=" + reward +
                ", state2=" + state2 +
                '}';
    }
}