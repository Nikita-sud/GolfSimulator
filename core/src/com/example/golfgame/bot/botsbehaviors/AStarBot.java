package com.example.golfgame.bot.botsbehaviors;

import com.example.golfgame.GolfGame;
import com.example.golfgame.bot.BotBehavior;
import com.example.golfgame.screens.GolfGameScreen;
import com.example.golfgame.simulator.PhysicsSimulator;
import com.example.golfgame.utils.BallState;
import com.example.golfgame.utils.ApproximateStateComparator;

import java.util.*;
import java.util.stream.IntStream;

public class AStarBot implements BotBehavior {
    private static final float SHOT_ANGLE_STEP = 15f;
    private static final float SPEED_STEP = 1f;
    private static final float MIN_SPEED = 1;
    private static final float MAX_SPEED = 5;

    private PriorityQueue<Node> openList;
    private Set<Node> openSet;
    private TreeSet<Node> closedList;
    private GolfGame game;
    private PhysicsSimulator simulator;
    private List<Node> path;
    private int currentStep;
    private boolean isDirectionSet = false;
    private boolean pathFound = false;

    public AStarBot(GolfGame game) {
        this.openList = new PriorityQueue<>(
            Comparator.<Node>comparingDouble(Node::getCost)
                .thenComparingDouble(Node::getHCost));
        this.openSet = new HashSet<>();
        this.closedList = new TreeSet<>(Comparator.comparing(Node::getState, new ApproximateStateComparator(GolfGameScreen.getGoalTolerance())));
        this.path = new ArrayList<>();
        this.currentStep = 0;
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

            currentStep++; 

            if (currentStep >= path.size()) {
                isDirectionSet = false;
            }
        }
    }

    private List<Node> buildPath(BallState startState, BallState goalState) {
        openList.clear();
        openSet.clear();
        closedList.clear();
        List<Node> newPath = new ArrayList<>();
    
        Node startNode = new Node(startState, null, 0, heuristic(startState, goalState), 0, 0);
        openList.add(startNode);
        openSet.add(startNode);
    
        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();
            openSet.remove(currentNode);
    
            if (currentNode.getState().epsilonPositionEquals(goalState, GolfGameScreen.getGoalTolerance())) {
                newPath = reconstructPath(currentNode);
                pathFound = true;
                break;
            }
    
            closedList.add(currentNode);
            boolean win = false;
            Node winNode = currentNode;
            for (Node neighbor : getNeighbors(currentNode)) {
                if (isNodeCloseToSet(neighbor, closedList)) {
                    continue;
                }
    
                double tentativeGCost = currentNode.getGCost() + currentNode.getState().distanceTo(neighbor.getState());
    
                if (!isNodeCloseToSet(neighbor, openSet) || tentativeGCost < neighbor.getGCost()) {
                    neighbor.setParent(currentNode);
                    neighbor.setGCost(tentativeGCost);
                    neighbor.setHCost(heuristic(neighbor.getState(), goalState));
    
                    if (!isNodeCloseToSet(neighbor, openSet)) {
                        openList.add(neighbor);
                        openSet.add(neighbor);
                    }
                }
                if (neighbor.getState().epsilonPositionEquals(goalState, GolfGameScreen.getGoalTolerance())) {
                    winNode = neighbor;
                    win = true;
                    break;
                }
            }
            if (win) {
                newPath = reconstructPath(winNode);
                pathFound = true;
                break;
            }
        }
    
        return newPath;
    }

    private boolean isNodeCloseToSet(Node node, Set<Node> nodeSet) {
        for (Node existingNode : nodeSet) {
            if (node.equals(existingNode)) {
                return true;
            }
        }
        return false;
    }

    private double heuristic(BallState from, BallState to) {
        return Math.sqrt(Math.pow(from.getX() - to.getX(), 2) + Math.pow(from.getY() - to.getY(), 2));
    }

    public void resetSteps(){
        this.currentStep=1;
        this.isDirectionSet = false;
    }

    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = Collections.synchronizedList(new ArrayList<>());
        BallState currentState = node.getState();
    
        IntStream.range(0, 360 / (int) SHOT_ANGLE_STEP).parallel().forEach(i -> {
            float angle = i * SHOT_ANGLE_STEP;
            for (float speed = MIN_SPEED; speed <= MAX_SPEED; speed += SPEED_STEP) {
                BallState newState = simulator.singleHit(speed, angle, currentState);
                if (!newState.equals(currentState)) { 
                    Node neighbor = new Node(newState.copy(), node, node.getGCost(), heuristic(newState, game.getGolfGameScreen().getGoalState()), speed, angle);
                    neighbors.add(neighbor);
                }
            }
        });
    
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

    public boolean isPathFound(){
        return pathFound;
    }

    private static class Node {
        private BallState state;
        private Node parent;
        private double gCost;
        private double hCost;
        private double fCost;
        private float speed;
        private float angle;
    
        private static final double DISTANCE_THRESHOLD = 2; 
    
        public Node(BallState state, Node parent, double gCost, double hCost, float speed, float angle) {
            this.state = state;
            this.parent = parent;
            this.gCost = roundToHundredths(gCost);
            this.hCost = roundToHundredths(hCost);
            this.fCost = this.gCost + this.hCost;
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
            return fCost;
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
            this.gCost = roundToHundredths(gCost);
            this.fCost = this.gCost + this.hCost;
        }
    
        public void setHCost(double hCost) {
            this.hCost = roundToHundredths(hCost);
            this.fCost = this.gCost + this.hCost;
        }
    
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Math.abs(state.getX() - node.state.getX()) < DISTANCE_THRESHOLD &&
                   Math.abs(state.getY() - node.state.getY()) < DISTANCE_THRESHOLD;
        }
    
        @Override
        public int hashCode() {
            return Objects.hash(Math.round(state.getX() / DISTANCE_THRESHOLD), Math.round(state.getY() / DISTANCE_THRESHOLD));
        }
    
        private double roundToHundredths(double value) {
            return Math.round(value * 100.0) / 100.0;
        }
    }
}
