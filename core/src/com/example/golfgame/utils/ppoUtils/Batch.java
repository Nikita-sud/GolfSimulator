package com.example.golfgame.utils.ppoUtils;

import java.util.List;

public class Batch {
    private List<Transition> batch;
    public Batch(List<Transition> batch){
        this.batch = batch;
    }
    public List<Transition> getBatch(){
        return batch;
    }
    public List<Transition> setBatch(){
        return batch;
    }
}
