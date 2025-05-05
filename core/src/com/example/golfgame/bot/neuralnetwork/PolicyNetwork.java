package com.example.golfgame.bot.neuralnetwork;

/**
 * A policy network that extends the NeuralNetwork class.
 * This network is used for policy-based reinforcement learning.
 */
public class PolicyNetwork extends NeuralNetwork {
    private double minProbability = 1e-10; // Minimum probability to avoid zero probabilities

    public PolicyNetwork(int[] sizes) {
        // Пример: ReLU для всех скрытых, Linear для выхода
        super(sizes, createActivationTypes(sizes.length, ActivationType.TANH, ActivationType.LINEAR));
    }

     /**
      * Constructs a PolicyNetwork with specified layer sizes and activations.
      *
      * @param sizes an array specifying the number of neurons in each layer
      * @param activationTypes array of activation types for hidden and output layers
      */
    public PolicyNetwork(int[] sizes, ActivationType[] activationTypes) {
        super(sizes, activationTypes);
    }

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
     * Computes the loss for the policy network using the PPO objective function.
     *
     * @param policyOutputs the outputs of the policy network
     * @param advantages the advantages computed for the actions
     * @param oldProbabilities the old probabilities of the actions
     * @param epsilon the clipping parameter for PPO
     * @param action the action taken
     * @return the computed loss value
     */
    public double computeLoss(double[][] policyOutputs, double advantage, double oldProbability, double epsilon, double[] action) {
        // Вычисляем текущую вероятность для данного действия
        double probability = computeProbability(policyOutputs, action);

        // Проверка на NaN или бесконечность перед делением
         if (Double.isNaN(probability) || Double.isInfinite(probability) ||
             Double.isNaN(oldProbability) || Double.isInfinite(oldProbability) || oldProbability == 0) {
              System.err.println("Warning: Invalid probability encountered in computeLoss. Prob: " + probability + ", OldProb: " + oldProbability);
              // Возвращаем 0 или выбрасываем исключение, чтобы избежать NaN в градиентах
              return 0.0;
         }


        // Отношение вероятностей
        double probabilityRatio = probability / oldProbability;

        // Проверка на NaN после деления
        if (Double.isNaN(probabilityRatio) || Double.isInfinite(probabilityRatio)) {
            System.err.println("Warning: Invalid probability ratio: " + probabilityRatio + " (prob=" + probability + ", oldProb=" + oldProbability + ")");
            return 0.0; // Избегаем NaN
        }

        // Ограниченное отношение (Clipped ratio)
        double clippedRatio = Math.max(Math.min(probabilityRatio, 1 + epsilon), 1 - epsilon);

        // Функция потерь PPO (минимизируем негатив от цели)
        // Цель: min(ratio * A, clip(ratio, 1-eps, 1+eps) * A)
        // Потеря: - Цель
        double loss = -Math.min(probabilityRatio * advantage, clippedRatio * advantage);

        // Проверка на NaN после вычисления потерь
         if (Double.isNaN(loss) || Double.isInfinite(loss)) {
             System.err.println("Warning: Loss calculation resulted in NaN/Infinity. Ratio: " + probabilityRatio + ", ClippedRatio: " + clippedRatio + ", Advantage: " + advantage);
             return 0.0;
         }


        // Мы не делим на advantages.length, так как это потеря для ОДНОГО перехода
        return loss;
    }

    /**
     * Computes the probability of taking a specific action given the policy output.
     *
     * @param policyOutput the output of the policy network
     * @param action the action for which the probability is computed
     * @return the computed probability value
     */
    public double computeProbability(double[][] policyOutput, double[] action) {
        double mu_theta = policyOutput[0][0];
        double sigma_theta_raw = policyOutput[1][0];
        double mu_force = policyOutput[2][0];
        double sigma_force_raw = policyOutput[3][0];

        // Softplus
        double sigma_theta = softplus(sigma_theta_raw);
        double sigma_force = softplus(sigma_force_raw);

        double theta = action[0];
        double force = action[1];

        double prob_theta = (1 / (Math.sqrt(2 * Math.PI) * sigma_theta)) 
                            * Math.exp(-Math.pow(theta - mu_theta, 2) / (2 * Math.pow(sigma_theta, 2)));
        
        double prob_force = (1 / (Math.sqrt(2 * Math.PI) * sigma_force)) 
                            * Math.exp(-Math.pow(force - mu_force, 2) / (2 * Math.pow(sigma_force, 2)));
        
        double probability = prob_theta * prob_force;

        return Math.max(probability, minProbability);
    }

    /**
     * Applies the softplus activation function to the input value.
     *
     * @param x the input value
     * @return the result of applying the softplus function to the input value
     */
    public double softplus(double x) {
        // Более стабильная реализация softplus
        if (x > 30) return x; // Приближение для больших x
        if (x < -30) return Math.exp(x); // Приближение для малых x
        return Math.log(1 + Math.exp(x));
    }
}