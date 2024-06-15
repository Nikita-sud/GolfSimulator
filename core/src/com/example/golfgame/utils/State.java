package com.example.golfgame.utils;

import java.util.Arrays;

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
    @Override
    public String toString() {
        return "State{" +
                "state=" + Arrays.toString(state) +
                '}';
    }
}
