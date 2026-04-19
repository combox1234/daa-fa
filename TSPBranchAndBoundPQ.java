import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

public class TSPBranchAndBoundPQ {
    private static final int INF = Integer.MAX_VALUE / 4;
    private static class State {
        int currentCity;
        int visitedCount;
        int mask;
        int costSoFar;
        int bound;
        List<Integer> path;
        State(int currentCity, int visitedCount, int mask, int costSoFar, int bound, List<Integer> path) {
            this.currentCity = currentCity;
            this.visitedCount = visitedCount;
            this.mask = mask;
            this.costSoFar = costSoFar;
            this.bound = bound;
            this.path = path;
        }
    }
    private static int[][] cost;
    private static int n;
    private static int lowerBound(State s) {
        int lb = s.costSoFar;
        int minFromCurrent = INF;
        if (s.visitedCount == n) {
            minFromCurrent = cost[s.currentCity][0];
        } else {
            for (int next = 0; next < n; next++) {
                if ((s.mask & (1 << next)) == 0) {
                    minFromCurrent = Math.min(minFromCurrent, cost[s.currentCity][next]);
                }
            }
        }
        lb += minFromCurrent;
        for (int city = 0; city < n; city++) {
            if ((s.mask & (1 << city)) == 0) {
                int minOut = INF;
                for (int to = 0; to < n; to++) {
                    if (city != to) {
                        minOut = Math.min(minOut, cost[city][to]);
                    }
                }
                lb += minOut;
            }
        }
        return lb;
    }
    private static State solveUsingBranchAndBound(int startCity) {
        int bestCost = INF;
        List<Integer> bestPath = new ArrayList<>();
        PriorityQueue<State> minHeap = new PriorityQueue<>((a, b) -> {
            if (a.bound != b.bound) {
                return Integer.compare(a.bound, b.bound);
            }
            return Integer.compare(a.costSoFar, b.costSoFar);
        });
        List<Integer> startPath = new ArrayList<>();
        startPath.add(startCity);
        State start = new State(startCity, 1, (1 << startCity), 0, 0, startPath);
        start.bound = lowerBound(start);
        minHeap.add(start);
        while (!minHeap.isEmpty()) {
            State current = minHeap.poll();
            if (current.bound >= bestCost) {
                continue;
            }
            if (current.visitedCount == n) {
                int tourCost = current.costSoFar + cost[current.currentCity][startCity];
                if (tourCost < bestCost) {
                    bestCost = tourCost;
                    bestPath = new ArrayList<>(current.path);
                    bestPath.add(startCity);
                }
                continue;
            }
            for (int next = 0; next < n; next++) {
                if ((current.mask & (1 << next)) != 0) {
                    continue;
                }
                int newCost = current.costSoFar + cost[current.currentCity][next];
                if (newCost >= bestCost) {
                    continue;
                }
                List<Integer> newPath = new ArrayList<>(current.path);
                newPath.add(next);
                State child = new State(
                    next,
                    current.visitedCount + 1,
                    current.mask | (1 << next),
                    newCost,
                    0,
                    newPath
                );
                child.bound = lowerBound(child);
                if (child.bound < bestCost) {
                    minHeap.add(child);
                }
            }
        }
        if (bestPath.isEmpty()) {
            return null;
        }
        return new State(startCity, n + 1, (1 << n) - 1, bestCost, bestCost, bestPath);
    }
    private static void printStepByStepPath(List<Integer> fullPath) {
        StringBuilder routeBuilder = new StringBuilder();
        int cumulativeCost = 0;
        routeBuilder.append("city ").append(fullPath.get(0) + 1).append("[").append(cumulativeCost).append("]");
        for (int i = 1; i < fullPath.size(); i++) {
            int from = fullPath.get(i - 1);
            int to = fullPath.get(i);
            cumulativeCost += cost[from][to];
            routeBuilder.append(" -> city ").append(to + 1).append("[").append(cumulativeCost).append("]");
            System.out.println(routeBuilder);
        }
    }

    private static void printDistanceTable() {
        int cellWidth = 9;

        System.out.println("\nDistance table:");
        System.out.printf("%" + cellWidth + "s", " ");
        for (int col = 0; col < n; col++) {
            System.out.printf("%" + cellWidth + "s", "C" + (col + 1));
        }
        System.out.println();

        for (int row = 0; row < n; row++) {
            System.out.printf("%" + cellWidth + "s", "C" + (row + 1));
            for (int col = 0; col < n; col++) {
                System.out.printf("%" + cellWidth + "d", cost[row][col]);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of cities: ");
        n = sc.nextInt();
        if (n < 2) {
            System.out.println("At least 2 cities are required.");
            return;
        }
        if (n > 30) {
            System.out.println("Please use 30 or fewer cities for this implementation.");
            return;
        }
        cost = new int[n][n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(cost[i], 0);
        }
        System.out.println("Enter distances between city pairs:");
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                System.out.print("Enter distance from city " + (i + 1) + " to city " + (j + 1) + ": ");
                int d = sc.nextInt();
                if (d <= 0) {
                    System.out.println("Distance must be positive.");
                    return;
                }
                cost[i][j] = d;
                cost[j][i] = d;
            }
        }
        printDistanceTable();
        System.out.print("Enter start city (1 to " + n + "): ");
        int startCity = sc.nextInt() - 1;
        if (startCity < 0 || startCity >= n) {
            System.out.println("Invalid city number.");
            return;
        }
        State answer = solveUsingBranchAndBound(startCity);
        if (answer == null) {
            System.out.println("No valid tour found.");
            return;
        }
        System.out.println("\nFinal tour building steps:");
        printStepByStepPath(answer.path);
        System.out.println("\nFinal minimum cost: " + answer.costSoFar);
        sc.close();
    }
}
