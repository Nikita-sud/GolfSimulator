package com.example.golfgame.bot.neuralnetwork;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.graph.MergeVertex;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class MultiModalNetwork {

    public static ComputationGraph createMultiModalNetwork(int height, int width, int channels, int numNumericFeatures, int outputSize) {
        ComputationGraphConfiguration graph = new NeuralNetConfiguration.Builder()
            .seed(123)
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .updater(new Adam())
            .weightInit(WeightInit.XAVIER)
            .graphBuilder()
            .addInputs("imageInput", "numericInput")
            .setInputTypes(InputType.convolutional(height, width, channels), InputType.feedForward(numNumericFeatures))
            .addLayer("cnn1", new ConvolutionLayer.Builder(5, 5)
                    .nIn(channels)
                    .stride(1, 1)
                    .nOut(20)
                    .activation(Activation.RELU)
                    .build(), "imageInput")
            .addLayer("maxpool1", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                    .kernelSize(2, 2)
                    .stride(2, 2)
                    .build(), "cnn1")
            .addLayer("cnn2", new ConvolutionLayer.Builder(5, 5)
                    .stride(1, 1)
                    .nOut(50)
                    .activation(Activation.RELU)
                    .build(), "maxpool1")
            .addLayer("maxpool2", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                    .kernelSize(2, 2)
                    .stride(2, 2)
                    .build(), "cnn2")
            .addLayer("ffn1", new DenseLayer.Builder()
                    .nOut(512)
                    .activation(Activation.RELU)
                    .build(), "maxpool2")
            .addLayer("ffn2", new DenseLayer.Builder()
                    .nOut(256)
                    .activation(Activation.RELU)
                    .build(), "ffn1")
            .addLayer("numericDense", new DenseLayer.Builder()
                    .nIn(numNumericFeatures)
                    .nOut(256)
                    .activation(Activation.RELU)
                    .build(), "numericInput")
            .addVertex("merge", new MergeVertex(), "ffn2", "numericDense")
            .addLayer("output", new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                    .nOut(outputSize)
                    .activation(Activation.IDENTITY)
                    .build(), "merge")
            .setOutputs("output")
            .build();

        ComputationGraph net = new ComputationGraph(graph);
        net.init();
        return net;
    }
}