package com.example.golfgame.utils;

public class State {
    private double[] state;
    public State(double[] state){
        this.state = state;
    }
    
    public void setState(double[] state){
        this.state = state;
    }

    public double[] getState(){
        return state;
    }
}
