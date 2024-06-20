package com.example.golfgame.bot.botsbehaviors;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.BotBehavior;
import com.example.golfgame.screens.GolfGameScreen;
import com.example.golfgame.simulator.PhysicsSimulator;
import com.example.golfgame.utils.BallState;
import com.example.golfgame.utils.ApproximateStateComparator;

import java.util.*;

public class AStarBot implements BotBehavior {
    private static final float SHOT_ANGLE_STEP = 15f;
    private static final float SPEED_STEP = 1f;
    private static final float MIN_SPEED = 1;
    private static final float MAX_SPEED = 5;

    private PriorityQueue<Node> openList;
    private TreeSet<Node> closedList;
    private GolfGame game;
    private PhysicsSimulator simulator;
    private List<Node> path;
    private int currentStep;
    private boolean isDirectionSet = false;

    public AStarBot(GolfGame game) {
        this.openList = new PriorityQueue<>(Comparator.comparingDouble(Node::getCost));
        this.closedList = new TreeSet<>(Comparator.comparing(Node::getState, new ApproximateStateComparator(GolfGameScreen.getGoalTolerance())));
        this.path = new ArrayList<>();
        this.currentStep = 0;
        initializePath(game);
    }

    public void initializePath(GolfGame game) {
        this.game = game;
        if (simulator == null) {
            BallState goalState = game.getGolfGameScreen().getGoalState();
            simulator = new PhysicsSimulator(game.getGolfGameScreen().getHeightFunction(), goalState);
        }

        BallState startState = game.getGolfGameScreen().getBallState();
        BallState goalState = game.getGolfGameScreen().getGoalState();

        // Строим маршрут
        this.path = buildPath(startState, goalState);

        if (path != null && path.size() > 1) {
            currentStep = 1; // Начинаем с первого шага после начального состояния
            isDirectionSet = true;
        }
    }

    @Override
    public float setDirection(GolfGame game) {
        isDirectionSet = false;
        if (currentStep < path.size()) {
            Node nextNode = path.get(currentStep);
            float targetAngle = nextNode.getAngle();
            isDirectionSet = true;
            return targetAngle;
        }
        return 0;
    }

    @Override
    public void hit(GolfGame game) {
        if (isDirectionSet && currentStep < path.size()) {
            Node currentNode = path.get(currentStep);
            float speed = currentNode.getSpeed();
            game.getGolfGameScreen().performHit(speed);

            currentStep++; // Переходим к следующему шагу

            if (currentStep >= path.size()) {
                isDirectionSet = false; // Завершили выполнение всех шагов
            }
        }
    }

    private List<Node> buildPath(BallState startState, BallState goalState) {
        openList.clear();
        closedList.clear();
        List<Node> newPath = new ArrayList<>();

        Node startNode = new Node(startState, null, 0, heuristic(startState, goalState), 0, 0);
        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();

            if (currentNode.getState().epsilonPositionEquals(goalState, GolfGameScreen.getGoalTolerance())) {
                newPath = reconstructPath(currentNode);
                break;
            }

            closedList.add(currentNode);

            for (Node neighbor : getNeighbors(currentNode)) {
                boolean inClosedList = closedList.stream().anyMatch(n -> n.getState().epsilonPositionEquals(neighbor.getState(), GolfGameScreen.getGoalTolerance()));
                if (inClosedList) {
                    continue;
                }

                double tentativeGCost = currentNode.getGCost() + distance(currentNode.getState(), neighbor.getState());

                boolean inOpenList = openList.stream().anyMatch(n -> n.getState().epsilonPositionEquals(neighbor.getState(), GolfGameScreen.getGoalTolerance()));

                if (!inOpenList || tentativeGCost < neighbor.getGCost()) {
                    neighbor.setParent(currentNode);
                    neighbor.setGCost(tentativeGCost);
                    neighbor.setFCost(tentativeGCost + neighbor.getHCost());

                    if (!inOpenList) {
                        openList.add(neighbor);
                    }
                }
            }
        }

        return newPath;
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
                neighbors.add(new Node(newState, node, node.getGCost(), heuristic(newState, game.getGolfGameScreen().getGoalState()), speed, angle));
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

    public boolean isDirectionSet() {
        return isDirectionSet;
    }

    private static class Node {
        private BallState state;
        private Node parent;
        private double gCost;
        private double hCost;
        private float speed;
        private float angle;

        public Node(BallState state, Node parent, double gCost, double hCost, float speed, float angle) {
            this.state = state;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.speed = speed;
            this.angle = angle;
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

        public float getSpeed() {
            return speed;
        }

        public float getAngle() {
            return angle;
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