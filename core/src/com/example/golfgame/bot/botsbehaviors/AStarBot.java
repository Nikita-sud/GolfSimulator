package com.example.golfgame.bot.botsbehaviors;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.BotBehavior;
import com.example.golfgame.screens.GolfGameScreen;
import com.example.golfgame.simulator.PhysicsSimulator;
import com.example.golfgame.utils.BallState;

import java.util.*;

public class AStarBot implements BotBehavior {
    private static final float SHOT_ANGLE_STEP = 15f;
    private static final float SPEED_STEP = 1f;
    private static final float MIN_SPEED = 1;
    private static final float MAX_SPEED = 5;
    private float deltaAngle;

    private PriorityQueue<Node> openList;
    private Set<Node> closedList;
    private GolfGame game;
    private PhysicsSimulator simulator;
    private List<Node> path;
    private BallState nextState;

    public AStarBot() {
        this.openList = new PriorityQueue<>(Comparator.comparingDouble(Node::getCost));
        this.closedList = new HashSet<>();
        this.path = new ArrayList<>();
    }

    @Override
    public float setDirection(GolfGame game) {
        this.game = game;
        if (simulator == null) {
            BallState goalState = game.getGolfGameScreen().getGoalState();
            simulator = new PhysicsSimulator(game.getGolfGameScreen().getHeightFunction(), goalState);
        }

        BallState startState = game.getGolfGameScreen().getBallState();
        BallState goalState = game.getGolfGameScreen().getGoalState();

        Node startNode = new Node(startState, null, 0, heuristic(startState, goalState));
        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();

            if (currentNode.getState().epsilonPositionEquals(goalState, GolfGameScreen.getGoalTolerance())) {
                path = reconstructPath(currentNode);
                nextState = path.get(1).getState(); 
                break;
            }

            closedList.add(currentNode);

            for (Node neighbor : getNeighbors(currentNode)) {
                if (closedList.contains(neighbor)) {
                    continue;
                }

                double tentativeGCost = currentNode.getGCost() + distance(currentNode.getState(), neighbor.getState());

                if (!openList.contains(neighbor) || tentativeGCost < neighbor.getGCost()) {
                    neighbor.setParent(currentNode);
                    neighbor.setGCost(tentativeGCost);
                    neighbor.setFCost(tentativeGCost + neighbor.getHCost());

                    if (!openList.contains(neighbor)) {
                        openList.add(neighbor);
                    }
                }
            }
        }

        if (nextState != null) {
            float targetAngle = (float) Math.atan2(nextState.getVy(), nextState.getVx());
            return targetAngle;
        }

        return 0;
    }

    @Override
    public void hit(GolfGame game) {
        if (Math.abs(deltaAngle) < 0.005 && nextState != null) {
            double velocityMagnitude = Math.sqrt(nextState.getVx() * nextState.getVx() + nextState.getVy() * nextState.getVy());
            game.getGolfGameScreen().performHit((float) velocityMagnitude);
        }
    }

    private double heuristic(BallState from, BallState to) {
        return Math.sqrt(Math.pow(from.getX() - to.getX(), 2) + Math.pow(from.getY() - to.getY(), 2));
    }

    private double distance(BallState from, BallState to) {
        return Math.sqrt(Math.pow(from.getX() - to.getX(), 2) + Math.pow(from.getY() - to.getY(), 2));
    }

    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        BallState currentState = node.getState();

        for (float angle = 0; angle < 360; angle += SHOT_ANGLE_STEP) {
            for (float speed = MIN_SPEED; speed <= MAX_SPEED; speed += SPEED_STEP) {
                BallState newState = simulator.singleHit(speed, angle, currentState);
                neighbors.add(new Node(newState, node, node.getGCost(), heuristic(newState, game.getGolfGameScreen().getGoalState())));
            }
        }
        return neighbors;
    }

    private List<Node> reconstructPath(Node node) {
        List<Node> path = new ArrayList<>();
        while (node != null) {
            path.add(node);
            node = node.getParent();
        }

        Collections.reverse(path);
        return path;
    }

    private static class Node {
        private BallState state;
        private Node parent;
        private double gCost;
        private double hCost;

        public Node(BallState state, Node parent, double gCost, double hCost) {
            this.state = state;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
        }

        public BallState getState() {
            return state;
        }

        public Node getParent() {
            return parent;
        }

        public double getGCost() {
            return gCost;
        }

        public double getHCost() {
            return hCost;
        }

        public double getCost() {
            return gCost + hCost;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public void setGCost(double gCost) {
            this.gCost = gCost;
        }

        public void setFCost(double fCost) {
            this.hCost = fCost;
        }
    }
}