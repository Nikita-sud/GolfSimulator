package com.example.golfgame.bot.neuralnetwork;

public class ValueNetwork extends NeuralNetwork {

    /**
     * Constructs a ValueNetwork with specified layer sizes.
     * Assumes ReLU for hidden layers and Linear for the output layer.
     *
     * @param sizes an array specifying the number of neurons in each layer
     */
    public ValueNetwork(int[] sizes) {
         // Пример: ReLU для всех скрытых, Linear для выхода
        super(sizes, createActivationTypes(sizes.length, ActivationType.RELU, ActivationType.LINEAR));
    }

    /**
     * Constructs a ValueNetwork with specified layer sizes and activations.
     *
     * @param sizes an array specifying the number of neurons in each layer
     * @param activationTypes array of activation types for hidden and output layers
     */
    public ValueNetwork(int[] sizes, ActivationType[] activationTypes) {
        super(sizes, activationTypes);
    }

     // Вспомогательный метод для создания массива активаций (можно вынести в утилиты)
     private static ActivationType[] createActivationTypes(int numLayers, ActivationType hiddenType, ActivationType outputType) {
         if (numLayers < 2) {
             throw new IllegalArgumentException("Network must have at least 2 layers (input and output).");
         }
         ActivationType[] types = new ActivationType[numLayers - 1];
         for (int i = 0; i < types.length - 1; i++) {
             types[i] = hiddenType; // Скрытые слои
         }
         types[types.length - 1] = outputType; // Выходной слой
         return types;
     }

    /**
     * Computes the loss for the value network using mean squared error.
     *
     * @param output the output of the value network
     * @param target the target values
     * @return the computed loss value
     */
    public double computeLoss(double[][] output, double[] target) {
        double loss = 0.0;
        for (int i = 0; i < output.length; i++) {
            loss += Math.pow(output[i][0] - target[i], 2);
        }
        return loss / output.length;
    }
}