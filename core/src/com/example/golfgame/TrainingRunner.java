// Создайте новый файл, например, src/com/example/golfgame/TrainingRunner.java
package com.example.golfgame; // Или ваш основной пакет

import com.example.golfgame.bot.agents.PPOAgent;
import com.example.golfgame.simulator.PhysicsSimulator;
import java.io.IOException;

public class TrainingRunner {

    public static void main(String[] args) {
        // --- Параметры ---
        // Размеры сетей (Пример: вход - 100 (10x10 карта). Добавьте скорость, если нужно!)
        // УБЕДИТЕСЬ, ЧТО РАЗМЕР СООТВЕТСТВУЕТ ВЫХОДУ simulator.getState()
        int stateDim = 100; // ЗАМЕНИТЕ на реальный размер вашего состояния (10x10 карта -> 100)
                            // Если вы добавили скорость, то stateDim = 102
        int[] policyNetworkSizes = {stateDim, 128, 128, 4}; // Пример: 2 скрытых слоя по 128 нейронов, 4 выхода (mu_t, sig_t, mu_f, sig_f)
        int[] valueNetworkSizes = {stateDim, 128, 128, 1};  // Пример: 2 скрытых слоя по 128 нейронов, 1 выход (ценность V(s))

        // Параметры PPO
        double gamma = 0.99;
        double lambda = 0.95;
        double epsilon = 0.2;

        // Параметры обучения
        int total_timesteps = 5*2048;
        int n_steps_per_batch = 2048;      // Сколько шагов собирать перед одним вызовом train()
        int epochs_per_batch = 5;         // Сколько раз проходить по собранному батчу
        int mini_batch_size = 64;          // Размер мини-батча внутри эпохи

        // Learning Rates (передаются в train)
        // double policyLr = 0.0003; // Сейчас не используется для реального обучения политики
        // double valueLr = 0.001;   // Используется для обучения Value Network

        // Путь для сохранения/загрузки агента (опционально)
        String agentFilePath = "ppo_agent.ser";

        PPOAgent agent = null;
        boolean loadedSuccessfully = false; // Флаг для отслеживания загрузки
        try {
            System.out.println("Attempting to load agent from: " + agentFilePath);
            agent = PPOAgent.loadAgent(agentFilePath);
            System.out.println("Agent loaded successfully.");
            loadedSuccessfully = true; // Устанавливаем флаг
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Could not load agent, creating a new one. Reason: " + e.getMessage());
            agent = new PPOAgent(policyNetworkSizes, valueNetworkSizes, gamma, lambda, epsilon);
        }

        if (loadedSuccessfully) {
            System.out.println("Explicitly resetting Adam state after loading...");
            // Нужен способ получить доступ к сетям из агента
            if (agent.getPolicyNetwork() != null) {
                 agent.getPolicyNetwork().resetAdamState();
            }
            if (agent.getValueNetwork() != null) {
                 agent.getValueNetwork().resetAdamState();
            }
        }

        // --- Создание Симулятора ---
        // Укажите вашу функцию высот или способ ее получения
        String heightFunctionString = "0"; // ПРОСТЕЙШАЯ ПЛОСКАЯ КАРТА ДЛЯ ТЕСТА! Замените на вашу
        System.out.println("Using height function: " + heightFunctionString);
        PhysicsSimulator simulator = new PhysicsSimulator(heightFunctionString, agent);
        // simulator.addFunction("0.1*sin(x)+0.05*cos(y)"); // Если хотите добавить другие позже

        System.out.println("Simulator and Agent created.");

        // --- Запуск Симуляции и Обучения ---
        System.out.println("Starting simulation loop...");
        try {
            // Запускаем симуляцию/обучение
            simulator.runSimulation(
                total_timesteps,
                n_steps_per_batch,
                epochs_per_batch,
                mini_batch_size
            );
        } catch (Exception e) {
             System.err.println("An error occurred during simulation/training:");
             e.printStackTrace(); // Печатаем стек ошибки для диагностики
        } finally { // Блок finally выполнится даже если была ошибка
             // --- Сохранение Агента (Опционально) ---
             try {
                 System.out.println("Attempting to save agent to: " + agentFilePath);
                 agent.saveAgent(agentFilePath);
                 System.out.println("Agent saved successfully.");
             } catch (IOException e) {
                 System.err.println("Failed to save agent: " + e.getMessage());
                 e.printStackTrace();
             }
        }
        System.out.println("Training process finished.");
    }
}