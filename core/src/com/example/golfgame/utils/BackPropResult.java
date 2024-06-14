package com.example.golfgame.utils;

public class BackPropResult {
    private double[][][] delta_nabla_w;
    private double[][] delta_nabla_b;
    public BackPropResult(double[][][] delta_nabla_w, double[][] delta_nabla_b) {
        this.delta_nabla_w = delta_nabla_w;
        this.delta_nabla_b = delta_nabla_b;
    }

    public void setNablaW(double[][][] delta_nabla_w){
        this.delta_nabla_w = delta_nabla_w;
    }

    public void setNablaB(double[][] delta_nabla_b){
        this.delta_nabla_b = delta_nabla_b;
    }

    public double[][][] getNablaW(){
        return delta_nabla_w;
    }

    public double[][] getNablaB(){
        return delta_nabla_b;
    }

}