# TSP Branch and Bound (Priority Queue) - Full Easy Explanation

This document explains the complete logic of `TSPBranchAndBoundPQ.java` in simple language:
- line by line (top to bottom)
- function to function
- loop to loop
- where backtracking happens
- example with state-space tree (text/graph style in Markdown)
- data structures used, why used, and alternatives with complexity impact

---

## 1) What this program solves

Problem: Traveling Salesman Problem (TSP)
- Start from one city
- Visit every city exactly once
- Return to the start city
- Total cost should be minimum

Method used: Branch and Bound + Min Heap (PriorityQueue)

Important: This is NOT greedy. It explores many possible routes, but prunes routes that cannot beat the current best answer.

---

## 2) Top-to-bottom code walkthrough

## Imports

- `import java.util.ArrayList;`
  - Dynamic list for storing path.
- `import java.util.Arrays;`
  - Used for filling matrix rows with default values.
- `import java.util.List;`
  - Interface type for path.
- `import java.util.PriorityQueue;`
  - Min heap for best-first expansion.
- `import java.util.Scanner;`
  - Input from user.

## Class declaration

- `public class TSPBranchAndBoundPQ {`
  - Main class containing all methods and data.

## Global constant

- `private static final int INF = Integer.MAX_VALUE / 4;`
  - A very large number used as infinity.
  - `/4` avoids overflow when values are added repeatedly.

## Inner class `State`

Each `State` object represents one node of the state-space tree.

Fields:
- `currentCity` -> where we are currently
- `visitedCount` -> how many cities visited so far
- `mask` -> bitmask representing visited cities
- `costSoFar` -> exact cost paid till now
- `bound` -> optimistic lower bound of full tour from this state
- `path` -> current partial route

Constructor just copies all values into the object.

## Global variables

- `private static int[][] cost;`
  - Distance matrix. `cost[i][j]` is distance city i to city j.
- `private static int n;`
  - Number of cities.

---

## 3) Function-by-function explanation

## A) `lowerBound(State s)`

Purpose:
- Estimate minimum possible final cost from this state.
- Used for pruning and ordering heap.

How it works:
1. Start with `lb = s.costSoFar`.
2. Add minimum edge from current city to one unvisited city.
   - If all cities already visited, add edge to return city (index 0 in this code's bound function logic).
3. For every unvisited city, add its cheapest outgoing edge.
4. Return the sum.

Why useful:
- This is optimistic estimate (usually <= actual final tour through that state).
- If this estimate is already bad, no need to explore that branch.

Loop details:
- Loop over all cities to find next candidate from current city.
- Loop over each unvisited city.
- Nested loop inside it to find minimum outgoing edge.

Complexity per call:
- Worst-case O(n^2).

---

## B) `solveUsingBranchAndBound(int startCity)`

Purpose:
- Main solver. Finds best TSP tour starting and ending at `startCity`.

Step-by-step:
1. Initialize `bestCost = INF` and `bestPath` empty.
2. Create `PriorityQueue<State>` with comparator:
   - smaller `bound` first
   - if tie, smaller `costSoFar` first
3. Create start state:
   - path = `[startCity]`
   - visitedCount = 1
   - mask has only start bit set
   - costSoFar = 0
   - compute bound and push to heap
4. While heap is not empty:
   1. Pop state with smallest bound.
   2. If `current.bound >= bestCost`, prune it (`continue`).
   3. If all cities visited:
      - compute return-to-start cost
      - update best solution if better
      - continue
   4. Otherwise expand children:
      - loop all cities `next = 0...n-1`
      - skip if already visited in mask
      - compute newCost
      - prune if `newCost >= bestCost`
      - create new path copy, add next city
      - create child state with updated values
      - compute child bound
      - if child.bound < bestCost, push child
5. After loop:
   - if no path found, return null
   - else return final state containing best path and best cost

Loop details:
- One main `while` loop over state expansion.
- One `for` loop for generating children.
- Inside pruning conditions, many branches are skipped fast.

Complexity (worst case):
- TSP worst-case remains exponential/factorial in nature: O(n!).
- Branch and Bound often much faster than brute force in practice due to pruning.
- Heap operations add `log M` factor (`M` = current heap size).

---

## C) `printStepByStepPath(List<Integer> fullPath)`

Purpose:
- Print route in incremental format with cumulative cost in brackets.

Example output style:
- `city 1[0] -> city 2[10]`
- `city 1[0] -> city 2[10] -> city 4[35]`

Logic:
1. Start with first city and cost 0.
2. For each next city in path:
   - add edge cost from previous city
   - append `-> city X[currentTotal]`
   - print current full prefix

Complexity:
- O(n).

---

## D) `printDistanceTable()`

Purpose:
- Terminal-only matrix visualization (row/column city table).

Logic:
- Print column headers C1..Cn
- For each row city:
  - print row label
  - print distances for all columns

Complexity:
- O(n^2).

---

## E) `main(String[] args)`

Purpose:
- Program flow control.

Step-by-step flow:
1. Create Scanner.
2. Read number of cities `n`.
3. Validate `n >= 2` and `n <= 30`.
4. Create `cost[n][n]` matrix, initialize with zero.
5. Input upper-triangular distances only:
   - city 1->2, 1->3, ...
   - then city 2->3, ...
   - mirror assign to keep symmetric matrix.
6. Print distance table.
7. Ask start city.
8. Validate start city range.
9. Run solver.
10. If no answer, print message.
11. Else print incremental route steps and final minimum cost.

Nested input loops:
- Outer loop `i = 0..n-1`
- Inner loop `j = i+1..n-1`
- This avoids duplicate input because `cost[i][j] == cost[j][i]`.

---

## 4) Where backtracking is used here

This implementation uses Branch and Bound with best-first expansion (heap), not recursive DFS backtracking.

But conceptually, backtracking still happens in the state-space search:
- A branch is expanded.
- If it cannot lead to better answer (`bound >= bestCost` or `newCost >= bestCost`), it is abandoned.
- Search then goes to another branch from heap.

So this is "implicit backtracking via pruning + branch switching", not "recursive pop-backtracking".

If you want classic explicit backtracking, that would usually look like recursion:
- choose city
- mark visited
- recurse
- unmark visited (backtrack step)

Your current code instead keeps each path in separate `State` objects, so no manual unmark/pop is needed.

---

## 5) Step-by-step example with graphical (text) representation

Sample input (4 cities):
- 1-2 = 10
- 1-3 = 15
- 1-4 = 20
- 2-3 = 35
- 2-4 = 25
- 3-4 = 30
- Start city = 1

State-space tree (partial) in Markdown text graph:

```text
Start: [1], cost=0
|
|-- [1 -> 2], cost=10
|   |
|   |-- [1 -> 2 -> 3], cost=45
|   |   |
|   |   |-- [1 -> 2 -> 3 -> 4 -> 1], final=95
|   |
|   |-- [1 -> 2 -> 4], cost=35
|       |
|       |-- [1 -> 2 -> 4 -> 3 -> 1], final=80   <-- BEST
|
|-- [1 -> 3], cost=15
|   |
|   |-- [1 -> 3 -> 2 -> 4 -> 1], final=95
|   |-- [1 -> 3 -> 4 -> 2 -> 1], final=80
|
|-- [1 -> 4], cost=20
    |
    |-- [1 -> 4 -> 2 -> 3 -> 1], final=95
    |-- [1 -> 4 -> 3 -> 2 -> 1], final=95
```

How pruning/backtracking idea appears:
- After bestCost becomes 80, any future branch with bound >= 80 is skipped.
- That means many expensive branches are not explored deeply.

---

## 6) Data structures used and why

## 1. `int[][] cost` (Adjacency Matrix)
Why used:
- Distance lookup `cost[u][v]` is O(1).
- TSP repeatedly asks edge costs, so O(1) lookup is very important.

If we used adjacency list instead:
- Lookup for exact pair could become O(deg) or require extra map.
- Not ideal for dense complete graph like TSP.

Complexity impact:
- Matrix memory O(n^2), lookup O(1).
- Good trade-off for TSP complete graph.

## 2. `PriorityQueue<State>` (Min Heap)
Why used:
- Always expand most promising state first (smallest bound).
- This is core of best-first Branch and Bound.

If we used Stack (DFS):
- Behaves like plain backtracking, less informed order.
- Might find good solution late, causing less pruning early.

If we used normal Queue (BFS):
- Level-order exploration, usually huge memory growth and weak pruning order.

Complexity impact:
- Insert/delete from heap: O(log M), where M is heap size.
- Better node order can reduce explored states a lot in practice.

## 3. `int mask` (Bitmask for visited cities)
Why used:
- Check visited city: O(1) with bit operation.
- Mark visited: O(1).
- Memory efficient compared to boolean array per state.

If we used `boolean[] visited` inside each state:
- Copying array for each child is O(n).
- More memory and slower child generation.

Complexity impact:
- Bit operations are constant time and fast.

## 4. `List<Integer> path` (ArrayList)
Why used:
- Needed to print final route.
- Easy append and copy.

If we stored only parent pointer:
- Memory can reduce per node.
- But route reconstruction at end becomes reverse traversal steps.

Complexity impact:
- Child path copy costs O(path length).
- This is extra overhead, but code remains simple and readable.

## 5. `Scanner`
Why used:
- Easy terminal input.

Alternative:
- BufferedReader is faster for very large input.
- For classroom size input, Scanner is simpler and enough.

---

## 7) Complexity discussion in detail

Let n = number of cities.

## Time complexity (overall)
- Worst case is still exponential/factorial like TSP: O(n!).
- Why? In worst case pruning is weak and many permutations are explored.

More detailed view:
- Each expanded state computes lower bound: O(n^2).
- Child generation loop: O(n) per state.
- Heap push/pop: O(log M).
- So practical runtime depends mainly on how many states survive pruning.

A practical rough model:
- O(S * (n^2 + log S)), where S = number of expanded states.
- S can be much smaller than n! when pruning works well.

## Space complexity
- Cost matrix: O(n^2)
- Heap states: can grow large, worst-case exponential in n
- Path storage in states increases memory further

So practical space can be large for big n.

---

## 8) If different data structures were used, what changes?

1. Adjacency List instead of Matrix
- Edge lookup slower for complete graph usage pattern
- Lower bound and transitions become less direct
- Time usually worse for this problem style

2. Stack instead of PriorityQueue
- Time in practice often worse (finds good solution later)
- Pruning less effective early
- Could still be correct but usually slower

3. Queue instead of PriorityQueue
- Can explode memory quickly due to broad levels
- Usually poor practical performance for TSP

4. boolean[] visited per state instead of bitmask
- Easier to understand conceptually
- More copy cost O(n) per child and more memory

5. Parent pointer tree node instead of full path list copy
- Better memory per node
- Slightly harder code for output reconstruction

---

## 9) Final summary in simple words

- Your code uses Branch and Bound correctly with a min heap.
- It is not greedy and not graph-visualization based.
- It takes user input in non-repeating pair style.
- It prints matrix table in terminal.
- It prints route step-by-step with cumulative cost.
- It returns the minimum tour cost.
- Backtracking idea is present through branch pruning and switching, not recursive unmark/pop style.

If you want, next I can add a second markdown file with:
- a short viva-ready version (2-3 pages)
- likely exam questions + answers based on this code.
