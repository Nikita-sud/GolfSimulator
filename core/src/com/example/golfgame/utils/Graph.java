package com.example.golfgame.utils;

import com.example.golfgame.utils.ppoUtils.Action;
import java.util.*;

public class Graph {
    private Map<BallState, List<Edge>> adjVertices;

    public Graph() {
        this.adjVertices = new HashMap<>();
    }

    public void addVertex(BallState state) {
        adjVertices.putIfAbsent(state, new ArrayList<>());
    }

    public void addEdge(BallState from, BallState to, Action action) {
        adjVertices.get(from).add(new Edge(to, action));
    }

    public List<Action> dijkstra(BallState start, BallState goal) {
        Map<BallState, Double> distances = new HashMap<>();
        Map<BallState, BallState> previous = new HashMap<>();
        Map<BallState, Action> actions = new HashMap<>();
        PriorityQueue<BallState> pq = new PriorityQueue<>(Comparator.comparingDouble(distances::get));
        List<BallState> vertices = new ArrayList<>(adjVertices.keySet());

        for (BallState vertex : vertices) {
            if (vertex.equals(start)) {
                distances.put(vertex, 0.0);
            } else {
                distances.put(vertex, Double.MAX_VALUE);
            }
            pq.add(vertex);
        }

        while (!pq.isEmpty()) {
            BallState current = pq.poll();

            if (current.equals(goal)) {
                List<Action> path = new ArrayList<>();
                BallState step = goal;
                while (previous.containsKey(step)) {
                    path.add(actions.get(step));
                    step = previous.get(step);
                }
                Collections.reverse(path);
                return path;
            }

            for (Edge edge : adjVertices.get(current)) {
                BallState neighbor = edge.to;
                double newDist = distances.get(current) + current.distanceTo(neighbor);

                if (newDist < distances.get(neighbor)) {
                    pq.remove(neighbor);
                    distances.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    actions.put(neighbor, edge.action);
                    pq.add(neighbor);
                }
            }
        }

        return null;
    }

    static class Edge {
        BallState to;
        Action action;

        Edge(BallState to, Action action) {
            this.to = to;
            this.action = action;
        }
    }
}