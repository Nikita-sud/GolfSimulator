package com.example.golfgame.utils;

import com.badlogic.gdx.graphics.g3d.ModelInstance;

public class FlagAnimation {
    private ModelInstance flagInstance;
    private float elapsedTime = 0;
    private float waveSpeed = 1.0f;
    private float waveAmplitude = 0.2f;
    private float goalStateX;
    private float goalStateY;

    public FlagAnimation(ModelInstance flagInstance,double goalStateX,double goalStateY) {
        this.flagInstance = flagInstance;
        this.goalStateX = (float) goalStateX;
        this.goalStateY = (float) goalStateY;
    }

    public void update(float deltaTime) {
        elapsedTime += deltaTime;
        float waveOffset = waveAmplitude * (float) Math.sin(waveSpeed * elapsedTime)+0.5f;
        flagInstance.transform.setToTranslation(goalStateX, waveOffset, goalStateY);
    }
}
