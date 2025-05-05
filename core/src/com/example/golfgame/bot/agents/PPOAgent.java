package com.example.golfgame.bot.agents;

import com.example.golfgame.bot.neuralnetwork.NeuralNetwork;
import com.example.golfgame.bot.neuralnetwork.PolicyNetwork;
import com.example.golfgame.bot.neuralnetwork.ValueNetwork;
import com.example.golfgame.utils.MatrixUtils;
import com.example.golfgame.utils.ppoUtils.Action;
import com.example.golfgame.utils.ppoUtils.BackPropResult;
import com.example.golfgame.utils.ppoUtils.State;
import com.example.golfgame.utils.ppoUtils.Transition;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Collections;

/**
 * The PPOAgent class implements a Proximal Policy Optimization (PPO) agent.
 * It uses policy and value networks to learn and select actions based on states.
 */
public class PPOAgent implements Serializable {
    private static final long serialVersionUID = 1L;
    private PolicyNetwork policyNetwork;
    private ValueNetwork valueNetwork;
    private List<Transition> memory; // Only used temporarily for computeAdvantages/OldProbs
    private double gamma; // Discount factor
    private double lambda; // GAE parameter
    private double epsilon; // Clipping parameter for PPO
    private Random random = new Random(System.currentTimeMillis());

    /**
     * Constructs a PPOAgent with the specified parameters.
     *
     * @param policyNetworkSizes sizes of the layers in the policy network
     * @param valueNetworkSizes sizes of the layers in the value network
     * @param gamma discount factor
     * @param lambda GAE parameter
     * @param epsilon clipping parameter for PPO
     */
    public PPOAgent(int[] policyNetworkSizes, int[] valueNetworkSizes, double gamma, double lambda, double epsilon) {
        // Ensure the networks use appropriate activation functions by default
        this.policyNetwork = new PolicyNetwork(policyNetworkSizes); // Uses ReLU hidden, Linear output by default
        this.valueNetwork = new ValueNetwork(valueNetworkSizes);   // Uses ReLU hidden, Linear output by default
        this.memory = new ArrayList<>(); // This memory is only used temporarily now
        this.gamma = gamma;
        this.lambda = lambda;
        this.epsilon = epsilon;
    }
    public double softplus(double x) {
        // Более стабильная реализация softplus
        if (x > 30) return x; // Приближение для больших x
        if (x < -30) return Math.exp(x); // Приближение для малых x
        return Math.log(1 + Math.exp(x));
    }

    /**
     * Stores a transition in the agent's memory.
     * Note: This is not used in the current PPO training loop,
     * data is passed directly to train(). Kept for potential other uses.
     *
     * @param transition the transition to store
     */
    public void storeTransition(Transition transition) {
        // If you need a persistent memory buffer elsewhere, you can use this.
        // For the current train() method, it's not directly used.
        // this.memory.add(transition);
    }

    public void train(List<Transition> batchData, int epochs, int miniBatchSize, double policyLr, double valueLr) {
        if (batchData == null || batchData.isEmpty()) {
            System.err.println("Warning: Trying to train on empty batch data.");
            return;
        }

        int batchSize = batchData.size();
        System.out.println("Starting training: Batch Size=" + batchSize + ", Epochs=" + epochs + ", MiniBatchSize=" + miniBatchSize);


        // 1. Compute Advantages and Old Probabilities ONCE for the entire batch.
        this.memory = batchData; // Temporarily assign for compute* methods
        List<Double> advantagesList = computeAdvantagesParallel();
        // --- Advantage Normalization (Optional but Recommended) ---
        double[] advantagesArray = normalizeAdvantages(advantagesList);
        // --- End Advantage Normalization ---
        double[] oldProbabilities = computeOldProbabilitiesParallel();
        this.memory = null; // Clear the temporary reference


        List<Integer> indices = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            indices.add(i);
        }

        // 2. Training Loop over Epochs
        for (int epoch = 0; epoch < epochs; epoch++) {
            Collections.shuffle(indices, random);

            double totalPolicyLossEpoch = 0;
            double totalValueLossEpoch = 0;
            int miniBatchCount = 0;

            // 3. Loop over Mini-batches
            for (int start = 0; start < batchSize; start += miniBatchSize) {
                
                int end = Math.min(start + miniBatchSize, batchSize);
                int currentMiniBatchSize = end - start;

                // Initialize gradient accumulators for the mini-batch
                double[][][] mb_nabla_w_policy = createZeroGradientsW(policyNetwork);
                double[][] mb_nabla_b_policy = createZeroGradientsB(policyNetwork);
                double[][][] mb_nabla_w_value = createZeroGradientsW(valueNetwork);
                double[][] mb_nabla_b_value = createZeroGradientsB(valueNetwork);

                // 4. Process each transition in the mini-batch
                for (int i = start; i < end; i++) {
                    int index = indices.get(i);
                    Transition transition = batchData.get(index);
                    double advantage = advantagesArray[index]; // Use potentially normalized advantage
                    double oldProbability = oldProbabilities[index];
                    double[] action = {transition.getAction().getAngle(), transition.getAction().getForce()};
                    double[] currentState = transition.getState1().getState();
                    double[] nextState = transition.getState2().getState();
                    double reward = transition.getReward();

                    // === Value Network Update ===
                    double nextValue = valueNetwork.forward(nextState)[0][0];
                    double targetValue = reward + gamma * nextValue;
                    NeuralNetwork.Pair<List<double[][]>, List<double[][]>> valueForwardResult = valueNetwork.forwardWithZs(currentState);
                    double predictedValue = valueForwardResult._1.get(valueForwardResult._1.size() - 1)[0][0];
                    double dL_dValue = predictedValue - targetValue;
                    double[][] value_initial_delta = new double[1][1];
                    value_initial_delta[0][0] = dL_dValue; // dL/dz = dL/d(output) * 1 (for linear output)
                    BackPropResult valueBackpropResult = valueNetwork.backprop(currentState, value_initial_delta);
                    accumulateGradients(mb_nabla_w_value, valueBackpropResult.getNablaW());
                    accumulateGradients(mb_nabla_b_value, valueBackpropResult.getNablaB());
                    totalValueLossEpoch += 0.5 * Math.pow(dL_dValue, 2); // Accumulate MSE loss for logging

                    // === Policy Network Update ===
                    NeuralNetwork.Pair<List<double[][]>, List<double[][]>> policyForwardResult = policyNetwork.forwardWithZs(currentState);
                    double[][] policyOutput = policyForwardResult._1.get(policyForwardResult._1.size() - 1);

                    // --- Calculate Policy Gradient dL/dz_L ---
                    double probability = policyNetwork.computeProbability(policyOutput, action);
                    double probabilityRatio = (oldProbability > 1e-9) ? (probability / oldProbability) : 0;

                    double dL_dprob = 0;
                    // Avoid calculating gradient if ratio is invalid or advantage is near zero
                    if (oldProbability > 1e-9 && !Double.isNaN(probabilityRatio) && !Double.isInfinite(probabilityRatio) && Math.abs(advantage) > 1e-9) {
                        double clippedRatio = Math.max(Math.min(probabilityRatio, 1 + epsilon), 1 - epsilon);
                        if (probabilityRatio * advantage <= clippedRatio * advantage) {
                            // If we are exactly on the boundary, gradient is technically undefined/zero
                            if (Math.abs(probabilityRatio - (1 + epsilon)) > 1e-9 && Math.abs(probabilityRatio - (1 - epsilon)) > 1e-9) {
                                dL_dprob = -advantage / oldProbability; // Unclipped case gradient
                            }
                        } // Else (clipped case active), dL/dprob remains 0
                         if (Double.isNaN(dL_dprob) || Double.isInfinite(dL_dprob)) {
                             System.err.println("Warning: dL_dprob is NaN/Infinity. Advantage: " + advantage + ", OldProb: " + oldProbability);
                             dL_dprob = 0;
                        }
                    } else {
                         // Log less frequently or conditionally if needed
                         // System.err.println("Warning: Skipping dL_dprob calculation. OldProb: " + oldProbability + ", Ratio: " + probabilityRatio + ", Adv: " + advantage);
                    }

                    double mu_theta = policyOutput[0][0];
                    double sigma_theta_raw = policyOutput[1][0];
                    double mu_force = policyOutput[2][0];
                    double sigma_force_raw = policyOutput[3][0];
                    double action_theta = action[0];
                    double action_force = action[1];

                    double sigma_theta = policyNetwork.softplus(sigma_theta_raw);
                    double sigma_force = policyNetwork.softplus(sigma_force_raw);
                    // Add small epsilon for stability, especially to sigma^2 and sigma^3
                    sigma_theta = Math.max(sigma_theta, 1e-6);
                    sigma_force = Math.max(sigma_force, 1e-6);
                    double sigma_theta_sq = sigma_theta * sigma_theta + 1e-9;
                    double sigma_force_sq = sigma_force * sigma_force + 1e-9;
                    double sigma_theta_cub = sigma_theta_sq * sigma_theta;
                    double sigma_force_cub = sigma_force_sq * sigma_force;


                    double prob_theta_term1 = (1 / (Math.sqrt(2 * Math.PI) * sigma_theta));
                    double prob_theta_exp = Math.exp(-Math.pow(action_theta - mu_theta, 2) / (2 * sigma_theta_sq));
                    double prob_theta = prob_theta_term1 * prob_theta_exp;

                    double prob_force_term1 = (1 / (Math.sqrt(2 * Math.PI) * sigma_force));
                    double prob_force_exp = Math.exp(-Math.pow(action_force - mu_force, 2) / (2 * sigma_force_sq));
                    double prob_force = prob_force_term1 * prob_force_exp;

                    double dProbTheta_dMuTheta = prob_theta * (action_theta - mu_theta) / sigma_theta_sq;
                    double dProbTheta_dSigmaTheta = prob_theta * (Math.pow(action_theta - mu_theta, 2) / sigma_theta_cub - 1 / sigma_theta);

                    double dProbForce_dMuForce = prob_force * (action_force - mu_force) / sigma_force_sq;
                    double dProbForce_dSigmaForce = prob_force * (Math.pow(action_force - mu_force, 2) / sigma_force_cub - 1 / sigma_force);

                    double dSoftplus_dRawTheta = MatrixUtils.sigmoid(sigma_theta_raw);
                    double dSoftplus_dRawForce = MatrixUtils.sigmoid(sigma_force_raw);

                    double dProb_dMuTheta = dProbTheta_dMuTheta * prob_force;
                    double dProb_dSigmaThetaRaw = (dProbTheta_dSigmaTheta * prob_force) * dSoftplus_dRawTheta;
                    double dProb_dMuForce = dProbForce_dMuForce * prob_theta;
                    double dProb_dSigmaForceRaw = (dProbForce_dSigmaForce * prob_theta) * dSoftplus_dRawForce;

                    // Clean potential NaNs from sub-calculations
                    dProb_dMuTheta = Double.isFinite(dProb_dMuTheta) ? dProb_dMuTheta : 0.0;
                    dProb_dSigmaThetaRaw = Double.isFinite(dProb_dSigmaThetaRaw) ? dProb_dSigmaThetaRaw : 0.0;
                    dProb_dMuForce = Double.isFinite(dProb_dMuForce) ? dProb_dMuForce : 0.0;
                    dProb_dSigmaForceRaw = Double.isFinite(dProb_dSigmaForceRaw) ? dProb_dSigmaForceRaw : 0.0;

                    double dL_dMuTheta = dL_dprob * dProb_dMuTheta;
                    double dL_dSigmaThetaRaw = dL_dprob * dProb_dSigmaThetaRaw;
                    double dL_dMuForce = dL_dprob * dProb_dMuForce;
                    double dL_dSigmaForceRaw = dL_dprob * dProb_dSigmaForceRaw;

                    double[][] policy_initial_delta = new double[4][1];
                    policy_initial_delta[0][0] = dL_dMuTheta;
                    policy_initial_delta[1][0] = dL_dSigmaThetaRaw;
                    policy_initial_delta[2][0] = dL_dMuForce;
                    policy_initial_delta[3][0] = dL_dSigmaForceRaw;

                    for (int r = 0; r < 4; r++) {
                        if (!Double.isFinite(policy_initial_delta[r][0])) {
                             System.err.println("Warning: Final policy initial delta component is NaN/Infinity at index " + r + " dL_dprob:" + dL_dprob);
                             policy_initial_delta[r][0] = 0.0; // Reset invalid gradient component
                        }
                        // --- Optional: Gradient Clipping ---
                        double clipVal = 1.0;
                        policy_initial_delta[r][0] = Math.max(-clipVal, Math.min(clipVal, policy_initial_delta[r][0]));
                        // --- End Optional: Gradient Clipping ---
                    }
                    // --- End Policy Gradient Calculation ---

                    BackPropResult policyBackpropResult = policyNetwork.backprop(currentState, policy_initial_delta);
                    accumulateGradients(mb_nabla_w_policy, policyBackpropResult.getNablaW());
                    accumulateGradients(mb_nabla_b_policy, policyBackpropResult.getNablaB());

                    // Accumulate actual PPO loss for logging
                    totalPolicyLossEpoch += policyNetwork.computeLoss(policyOutput, advantage, oldProbability, epsilon, action);

                } // End loop over mini-batch transitions

                // 5. Update Parameters after processing the mini-batch
                policyNetwork.updateParameters(mb_nabla_w_policy, mb_nabla_b_policy, policyLr, currentMiniBatchSize);
                valueNetwork.updateParameters(mb_nabla_w_value, mb_nabla_b_value, valueLr, currentMiniBatchSize);
                miniBatchCount++;

            } // End loop over mini-batches

            // Log average losses for the epoch
            if (miniBatchCount > 0) {
                 System.out.printf("Epoch %d/%d - Avg Policy Loss: %.4f, Avg Value Loss: %.4f%n",
                                   epoch + 1, epochs, totalPolicyLossEpoch / batchSize, totalValueLossEpoch / batchSize);
             } else {
                 System.out.println("Epoch " + (epoch + 1) + "/" + epochs + " - No mini-batches processed.");
             }

        } // End loop over epochs
        System.out.println("Training finished for the batch.");
    } // End train method


    // Helper method for advantage normalization (optional but recommended)
    private double[] normalizeAdvantages(List<Double> advantages) {
        int size = advantages.size();
        if (size <= 1) {
            return advantages.stream().mapToDouble(d -> d).toArray(); // Cannot normalize if size is 0 or 1
        }

        double sum = 0.0;
        for (double adv : advantages) {
            sum += adv;
        }
        double mean = sum / size;

        double sumSq = 0.0;
        for (double adv : advantages) {
            sumSq += (adv - mean) * (adv - mean);
        }
        // Use population standard deviation or sample standard deviation? Usually population for batch.
        // Add epsilon for numerical stability if std dev is close to zero
        double stdDev = Math.sqrt(sumSq / size) + 1e-8;

        double[] normalizedAdvantages = new double[size];
        for (int i = 0; i < size; i++) {
            normalizedAdvantages[i] = (advantages.get(i) - mean) / stdDev;
        }
        // Optional: Log mean and std dev before normalization for debugging
        // System.out.printf("Advantage stats: Mean=%.4f, StdDev=%.4f%n", mean, stdDev);
        return normalizedAdvantages;
    }


    // --- Вспомогательные методы для градиентов --- (без изменений)
    private double[][][] createZeroGradientsW(NeuralNetwork net) {
        double[][][] weights = net.getWeights();
        double[][][] zeroW = new double[weights.length][][];
        for (int i = 0; i < weights.length; i++) {
            zeroW[i] = new double[weights[i].length][];
            for (int j = 0; j < weights[i].length; j++) {
                zeroW[i][j] = new double[weights[i][j].length];
                Arrays.fill(zeroW[i][j], 0.0);
            }
        }
        return zeroW;
    }

    private double[][] createZeroGradientsB(NeuralNetwork net) {
        double[][] biases = net.getBiases();
        double[][] zeroB = new double[biases.length][];
        for (int i = 0; i < biases.length; i++) {
            zeroB[i] = new double[biases[i].length];
            Arrays.fill(zeroB[i], 0.0);
        }
        return zeroB;
    }

    private void accumulateGradients(double[][][] total_nabla_w, double[][][] nabla_w) {
         if (nabla_w == null || total_nabla_w.length != nabla_w.length) {
             System.err.println("Gradient accumulation error (W): structure mismatch or null.");
             return;
         }
        for (int i = 0; i < total_nabla_w.length; i++) {
             if (total_nabla_w[i] == null || nabla_w[i] == null || total_nabla_w[i].length != nabla_w[i].length) {
                 System.err.println("Gradient accumulation error (W): layer " + i + " structure mismatch or null.");
                 continue;
             }
            for (int j = 0; j < total_nabla_w[i].length; j++) {
                 if (total_nabla_w[i][j] == null || nabla_w[i][j] == null || total_nabla_w[i][j].length != nabla_w[i][j].length) {
                     System.err.println("Gradient accumulation error (W): layer " + i + " neuron " + j + " structure mismatch or null.");
                     continue;
                 }
                for (int k = 0; k < total_nabla_w[i][j].length; k++) {
                    total_nabla_w[i][j][k] += nabla_w[i][j][k];
                }
            }
        }
    }

     private void accumulateGradients(double[][] total_nabla_b, double[][] nabla_b) {
         if (nabla_b == null || total_nabla_b.length != nabla_b.length) {
             System.err.println("Gradient accumulation error (B): structure mismatch or null.");
             return;
         }
         for (int i = 0; i < total_nabla_b.length; i++) {
             if (total_nabla_b[i] == null || nabla_b[i] == null || total_nabla_b[i].length != nabla_b[i].length) {
                  System.err.println("Gradient accumulation error (B): layer " + i + " structure mismatch or null.");
                 continue;
             }
             for (int j = 0; j < total_nabla_b[i].length; j++) {
                 total_nabla_b[i][j] += nabla_b[i][j];
             }
         }
     }

    // --- Методы computeAdvantagesParallel, computeOldProbabilitiesParallel --- (без изменений)
     private double[] computeOldProbabilitiesParallel() {
         if (memory == null || memory.isEmpty()){
             System.err.println("Error: computeOldProbabilitiesParallel called with null or empty memory.");
             return new double[0];
         }
         return memory.parallelStream()
                      .mapToDouble(transition -> {
                          double[] state = transition.getState1().getState();
                          double[] action = {transition.getAction().getAngle(), transition.getAction().getForce()};
                          // Need to handle potential exceptions during forward pass
                          try {
                             double[][] policyOutput = policyNetwork.forward(state);
                             return policyNetwork.computeProbability(policyOutput, action);
                          } catch (Exception e) {
                              System.err.println("Error during forward pass in computeOldProbabilitiesParallel: " + e.getMessage());
                              // Return a default low probability or handle appropriately
                              return 1e-10;
                          }
                      })
                      .toArray();
     }

     private List<Double> computeAdvantagesParallel() {
         if (memory == null || memory.isEmpty()){
             System.err.println("Error: computeAdvantagesParallel called with null or empty memory.");
             return new ArrayList<>();
         }
         double[] values = memory.parallelStream()
                                 .mapToDouble(transition -> {
                                      try {
                                         return valueNetwork.forward(transition.getState1().getState())[0][0];
                                      } catch (Exception e) {
                                          System.err.println("Error during value forward pass (state1) in computeAdvantagesParallel: " + e.getMessage());
                                          return 0.0; // Or handle appropriately
                                      }
                                  })
                                 .toArray();
         double[] nextValues = memory.parallelStream()
                                     .mapToDouble(transition -> {
                                         try {
                                             return valueNetwork.forward(transition.getState2().getState())[0][0];
                                         } catch (Exception e) {
                                             System.err.println("Error during value forward pass (state2) in computeAdvantagesParallel: " + e.getMessage());
                                             return 0.0; // Or handle appropriately
                                         }
                                     })
                                     .toArray();
         double[] deltas = new double[memory.size()];
         for (int i = 0; i < memory.size(); i++) {
             deltas[i] = memory.get(i).getReward() + gamma * nextValues[i] - values[i];
         }

         List<Double> advantages = new ArrayList<>(Arrays.asList(new Double[memory.size()])); // Pre-allocate
         double advantage = 0.0;
         for (int i = memory.size() - 1; i >= 0; i--) {
             advantage = deltas[i] + gamma * lambda * advantage;
             advantages.set(i, advantage); // Use set instead of add(0, ...) for efficiency
         }
         return advantages;
     }


    // --- Методы selectAction, softplus, selectRandomAction --- (без изменений)
    public Action selectAction(State state) {
         double[][] policyOutput = policyNetwork.forward(state.getState());

         double mu_theta = policyOutput[0][0];
         double sigma_theta_raw = policyOutput[1][0];
         double mu_force = policyOutput[2][0];
         double sigma_force_raw = policyOutput[3][0];

         double sigma_theta = softplus(sigma_theta_raw);
         double sigma_force = softplus(sigma_force_raw);

         // Добавим ограничение на минимальную сигму для стабильности
         sigma_theta = Math.max(sigma_theta, 1e-6);
         sigma_force = Math.max(sigma_force, 1e-6);

         double theta = mu_theta + sigma_theta * random.nextGaussian();
         double force = mu_force + sigma_force * random.nextGaussian();

         // Ограничение силы в разумных пределах
         force = Math.max(1.0, Math.min(force, 5.0)); // Примерные границы, подберите под вашу игру

         // Опционально: Ограничение угла (например, в [-pi, pi] или [0, 2pi])
         // theta = Math.atan2(Math.sin(theta), Math.cos(theta)); // Нормализация в [-pi, pi]

         return new Action(theta, force);
    }

     public Action selectRandomAction() {
         double theta = random.nextDouble() * 2 * Math.PI;
         double force = random.nextDouble() * (5.0 - 1.0) + 1.0; // Границы силы
         return new Action(theta, force);
     }


    // --- Методы saveAgent, loadAgent --- (без изменений)
    public void saveAgent(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
        }
    }

    public static PPOAgent loadAgent(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (PPOAgent) ois.readObject();
        }
    }
    public PolicyNetwork getPolicyNetwork() {
        return policyNetwork;
    }

    public ValueNetwork getValueNetwork() {
        return valueNetwork;
    }
}