# Traveling Salesman Problem: Approaches Comparison

This note summarizes the main TSP approaches discussed in class:
- Brute Force
- Greedy (Nearest Neighbor)
- Backtracking
- Branch and Bound
- Dynamic Programming (Held-Karp Algorithm)

It includes the basic algorithm, the data structures used, why those data structures are suitable, why other common choices are not as good, pseudocode, and a complexity comparison.

---

## 1) Problem Definition

Given `n` cities and the distance between every pair of cities, find a minimum-cost tour that:
- starts from one city,
- visits every city exactly once,
- returns to the starting city.

We usually represent the problem using a distance matrix `cost[n][n]`.

---

## 2) Common Representation

Most TSP algorithms use these basic data structures:
- `cost[][]` distance matrix to store edge weights.
- `visited[]` array or bitmask to track which cities are already included.
- `path[]` or list to store the current route.
- `bestCost` to store the best answer found so far.

Why the distance matrix is used:
- It gives constant-time access to any pairwise distance.
- TSP needs repeated edge lookups, so `O(1)` access is important.

Why not an adjacency list for the main solution logic:
- TSP usually considers every city as a possible next step.
- For dense graphs like complete graphs, a matrix is simpler and faster to query.
- An adjacency list is more useful when the graph is sparse, but classic TSP is normally complete or nearly complete.

---

## 3) Brute Force

### Basic Idea

Try every possible tour, compute its total cost, and choose the minimum.
Here, permutation means the exact order of visiting the unvisited cities, not a lower bound estimate.

### Steps
1. Fix a starting city.
2. Generate all permutations of the remaining `n-1` cities.
3. For each permutation, form a full tour by returning to the start.
4. Compute the total cost of that tour.
5. Keep the minimum-cost tour.
6. This is complete enumeration, so no pruning is done.

### Data Structures Used
- `cost[][]`: stores distances.
- `path[]` or permutation list: stores one candidate tour.
- `bestCost`: stores minimum cost found.

### Why These Data Structures
- `cost[][]` gives direct edge access.
- `path[]` is enough because brute force only needs to test one permutation at a time.

### Why Not Other Data Structures
- Priority queue is unnecessary because brute force does not rank partial solutions.
- DP table is unnecessary because brute force does not reuse subproblem results.
- Bitmask is optional, but for pure brute force a permutation generator or recursion with `visited[]` is simpler.

### Pseudocode

```text
BRUTE_FORCE_TSP(cost, n, start):
    bestCost = infinity
    bestPath = empty

    generate all permutations of cities except start

    for each permutation p:
        tourCost = cost[start][p[0]]
        for i = 0 to n-3:
            tourCost += cost[p[i]][p[i+1]]
        tourCost += cost[p[n-2]][start]

        if tourCost < bestCost:
            bestCost = tourCost
            bestPath = [start] + p + [start]

    return bestPath, bestCost
```

### Complexity
- Time: `O(n!)`
- Space: `O(n)` for recursion/path, or `O(n)` to `O(n^2)` depending on permutation generation method

---

## 4) Greedy (Nearest Neighbor)

### Basic Idea

Always move to the nearest unvisited city from the current city.

### Steps
1. Choose a start city.
2. Mark it visited.
3. From the current city, select the closest unvisited city.
4. Move to that city and mark it visited.
5. Repeat until all cities are visited.
6. Return to the starting city.

### Data Structures Used
- `cost[][]`: distance matrix.
- `visited[]`: boolean array to mark cities.
- `path[]`: stores the final route.

### Why These Data Structures
- `visited[]` gives fast membership checking.
- `path[]` is needed to record the constructed route.
- `cost[][]` is the natural structure for nearest-distance lookup.

### Why Not Other Data Structures
- A priority queue is not necessary because the choice is local at every step, not global across many partial routes.
- A bitmask works too, but `visited[]` is simpler for the greedy method.
- DP is overkill because greedy does not solve overlapping subproblems.

### Pseudocode

```text
NEAREST_NEIGHBOR_TSP(cost, n, start):
    visited[start] = true
    path = [start]
    current = start
    totalCost = 0

    repeat n-1 times:
        nextCity = -1
        minDist = infinity

        for city = 0 to n-1:
            if not visited[city] and cost[current][city] < minDist:
                minDist = cost[current][city]
                nextCity = city

        visited[nextCity] = true
        path.append(nextCity)
        totalCost += minDist
        current = nextCity

    totalCost += cost[current][start]
    path.append(start)

    return path, totalCost
```

### Complexity
- Time: `O(n^2)`
- Space: `O(n)`

### Important Note
- Greedy is fast, but it does not guarantee an optimal tour.
- It can get trapped in a locally best choice that makes the final route expensive.

---

## 5) Backtracking

### Basic Idea

Build the tour one city at a time. If a partial path cannot possibly lead to a better answer, stop exploring that path.

### Steps
1. Start from one city.
2. Add an unvisited city to the current path.
3. Mark it visited.
4. Recurse to continue the path.
5. When all cities are included, return to the start and evaluate the cost.
6. Remove the city from the path and unmark it while returning from recursion.

### Data Structures Used
- `visited[]` or bitmask: tracks chosen cities.
- `path[]`: stores the current partial tour.
- recursion stack: stores function state automatically.

### Why These Data Structures
- Recursion naturally represents decision making and undoing choices.
- `visited[]` is quick for checking whether a city can be included next.

### Why Not Other Data Structures
- A priority queue is not needed in pure backtracking because the search is depth-first, not best-first.
- A DP table is not the goal here because backtracking focuses on search and pruning, not memoization.
- An adjacency list is not necessary for a complete graph TSP because every city is a candidate next move.

### Pseudocode

```text
BACKTRACK_TSP(cost, n, start):
    visited[start] = true
    path = [start]
    bestCost = infinity

    DFS(current=start, count=1, costSoFar=0)

    return bestPath, bestCost

DFS(current, count, costSoFar):
    if count == n:
        total = costSoFar + cost[current][start]
        update bestCost and bestPath if total is smaller
        return

    for city = 0 to n-1:
        if not visited[city]:
            visited[city] = true
            path.append(city)
            DFS(city, count + 1, costSoFar + cost[current][city])
            path.removeLast()
            visited[city] = false
```

### Complexity
- Time: `O(n!)`
- Space: `O(n)`

### Important Note
- Backtracking is better than brute force only because it can stop exploring some paths earlier.
- In the worst case, it is still exponential.

---

## 6) Branch and Bound

### Basic Idea

Explore partial tours, but compute a lower bound for each one. If the lower bound is already worse than the best known solution, prune that branch.
This lower bound is only an estimate used to cut branches early; it is different from brute-force permutation generation.

### Steps
1. Start with one city and create the initial state.
2. Compute a lower bound for the partial tour.
3. Store live states in a priority queue ordered by smallest bound.
4. Repeatedly take the state with the smallest bound.
5. If the bound is already worse than the best solution, discard it.
6. Otherwise expand it by adding unvisited cities.
7. Compute bounds for children and push only promising states.
8. When a complete tour is formed, update the best answer.

### Data Structures Used
- `PriorityQueue` / min-heap: chooses the most promising state first.
- `State` object: stores current city, cost so far, bound, visited information, and path.
- `visited[]` or bitmask: checks whether a city was already taken.
- `path[]` or list: stores the partial route.

### Why These Data Structures
- `PriorityQueue` is ideal because branch and bound wants best-first exploration.
- The `State` object keeps all information needed for pruning and reconstruction.
- A bitmask gives compact and fast visited-state tracking.

### Why Not Other Data Structures
- A simple stack would make it depth-first, which is backtracking, not best-first branch and bound.
- A plain queue would explore states in generation order, which is not efficient for pruning.
- DP table is not enough by itself because branch and bound focuses on search space pruning.

### Pseudocode

```text
BRANCH_AND_BOUND_TSP(cost, n, start):
    bestCost = infinity
    bestPath = empty
    pq = min priority queue ordered by bound

    startState = (start, visited={start}, path=[start], costSoFar=0)
    startState.bound = LOWER_BOUND(startState)
    pq.push(startState)

    while pq is not empty:
        state = pq.pop()

        if state.bound >= bestCost:
            continue

        if all cities visited:
            total = state.costSoFar + cost[state.currentCity][start]
            if total < bestCost:
                bestCost = total
                bestPath = state.path + [start]
            continue

        for each unvisited city next:
            child = state with next added
            child.bound = LOWER_BOUND(child)
            if child.bound < bestCost:
                pq.push(child)

    return bestPath, bestCost
```

### Complexity
- Time: worst-case `O(n!)`
- Space: worst-case exponential because many states can be stored in the queue

### Important Note
- Branch and Bound can be much faster than brute force in practice.
- It is still exact, so it always finds the optimal answer if allowed to finish.

---

## 7) Dynamic Programming: Held-Karp Algorithm

### Basic Idea

Use DP to store the best cost for every subset of cities and the last city in that subset.

This is the classic exact DP solution for TSP.

### State Definition

Let `dp[mask][i]` be the minimum cost to:
- start from the source city,
- visit all cities in `mask`,
- and end at city `i`.

Here `mask` is a bitmask of visited cities.

### Steps
1. Fix a start city, usually city `0`.
2. Initialize `dp[1<<start][start] = 0`.
3. For each subset `mask` that contains the start city:
   - for each ending city `i` in the subset:
     - try extending to every city `j` not in the subset.
4. Update `dp[mask | (1<<j)][j]` with the minimum possible cost.
5. After processing all subsets, close the tour by returning to the start city.
6. Take the minimum over all possible last cities.

### Data Structures Used
- `dp[mask][i]` 2D table: stores subproblem answers.
- bitmask `mask`: represents the set of visited cities.
- optional `parent[mask][i]`: reconstructs the final tour.

### Why These Data Structures
- Bitmask is perfect for representing subsets of cities.
- The DP table avoids recomputing the same subproblem again and again.
- A parent table is useful if we want the actual tour, not just the cost.

### Why Not Other Data Structures
- `visited[]` alone is not enough, because DP needs to distinguish many different subsets, not just one current path.
- A priority queue is not required because the algorithm is based on recurrence and memoization, not best-first search.
- A list-based path representation is not enough for efficient repeated subproblem lookup.

### Pseudocode

```text
HELD_KARP_TSP(cost, n, start):
    dp[1<<n][n] = infinity
    dp[1<<start][start] = 0

    for mask from 0 to (1<<n) - 1:
        if mask does not contain start:
            continue

        for last = 0 to n-1:
            if dp[mask][last] == infinity:
                continue
            if last not in mask:
                continue

            for next = 0 to n-1:
                if next in mask:
                    continue

                newMask = mask | (1 << next)
                dp[newMask][next] = min(
                    dp[newMask][next],
                    dp[mask][last] + cost[last][next]
                )

    answer = infinity
    fullMask = (1 << n) - 1

    for last = 0 to n-1:
        answer = min(answer, dp[fullMask][last] + cost[last][start])

    return answer
```

### Complexity
- Time: `O(n^2 * 2^n)`
- Space: `O(n * 2^n)`

### Important Note
- Held-Karp is exact and usually better than brute force.
- It is still exponential, but much more practical for medium-sized `n`.

---

## 8) Comparison of All Approaches

| Approach | Main Idea | Optimal? | Time Complexity | Space Complexity | Notes |
|---|---|---:|---:|---:|---|
| Brute Force | Try every permutation | Yes | `O(n!)` | `O(n)` to `O(n^2)` | Simplest but slowest |
| Greedy (Nearest Neighbor) | Always go to nearest unvisited city | No | `O(n^2)` | `O(n)` | Fast but may miss optimal answer |
| Backtracking | DFS with pruning by partial choices | Yes | `O(n!)` worst case | `O(n)` | Better than brute force in practice |
| Branch and Bound | Best-first search with lower bounds | Yes | `O(n!)` worst case | Exponential in worst case | Often much faster than brute force |
| Held-Karp DP | Store best cost for subset + last city | Yes | `O(n^2 * 2^n)` | `O(n * 2^n)` | Best exact method among these for medium `n` |

Permutation is used only in brute force. Lower bound is used only in branch and bound. The table above compares all the approaches discussed in this note.

---

## 9) Short Intuition: Which One Is Better?

- If `n` is very small, brute force is easy to understand.
- If speed matters and approximation is acceptable, greedy is fastest.
- If you want exact answer and better pruning than brute force, use backtracking or branch and bound.
- If you want an exact algorithm with dynamic programming, use Held-Karp.

---

## 10) Final Conclusion

All five methods solve TSP differently:
- Brute force checks everything.
- Greedy picks the nearest choice locally.
- Backtracking explores recursively and cuts bad partial tours.
- Branch and Bound improves search with a lower-bound estimate and a priority queue.
- Held-Karp DP uses bitmasking to reuse subproblem results.

For a PPT, the most important takeaway is:
- Greedy is fastest but not optimal.
- Brute force is exact but too slow.
- Backtracking and branch and bound prune the search.
- Held-Karp is the standard exact DP solution for TSP.

---

## 11) Detailed Time and Space Complexity Comparison

### Time Complexity Breakdown

| Approach | Time Complexity | Explanation |
|---|---|---|
| **Brute Force** | `O(n!)` | Generates all `(n-1)!` permutations of cities and evaluates each in `O(n)` time. |
| **Greedy (Nearest Neighbor)** | `O(n^2)` | Outer loop: `n` steps. Inner loop: find minimum unvisited distance among `n` cities. Total: `O(n) × O(n) = O(n^2)` |
| **Backtracking** | `O(n!)` worst case | Explores the search tree recursively. Worst case: all `(n-1)!` nodes visited. Pruning helps in practice. |
| **Branch and Bound** | `O(n!)` worst case | Worst case: full search tree explored. In practice, much faster due to bound-based pruning and priority queue ordering. |
| **Held-Karp (DP)** | `O(n^2 * 2^n)` | Subproblems: `2^n` subsets × `n` ending cities = `O(n × 2^n)` states. Each state computed in `O(n)`. Total: `O(n^2 × 2^n)`. |

### Space Complexity Breakdown

| Approach | Space Complexity | Explanation |
|---|---|---|
| **Brute Force** | `O(n)` to `O(n^2)` | Recursion depth: `O(n)`. If storing all permutations: `O(n!)`. Typically: just `path[]` and recursion stack = `O(n)`. |
| **Greedy (Nearest Neighbor)** | `O(n)` | Stores: `visited[]` array, adjacency matrix `cost[][]`, and current path. Total: `O(n^2)` for matrix. |
| **Backtracking** | `O(n)` | Recursion stack depth: `O(n)`. `visited[]` array: `O(n)`. Path list: `O(n)`. No memoization. |
| **Branch and Bound** | `Exponential in worst case` | Priority queue can store `O(n!)` states in worst case. Typically much smaller due to pruning. Bitmask: `O(2^n)`. |
| **Held-Karp (DP)** | `O(n × 2^n)` | DP table: `dp[mask][i]` has `2^n` masks × `n` ending cities = `O(n × 2^n)` entries. Each entry stores one integer. |

### Quick Ranking

**Fastest Time:** Greedy `O(n^2)` ← Best for approximation  
**Exact Solutions:**  
- Medium `n` (≤ 20): Held-Karp `O(n^2 × 2^n)` ← Fast enough  
- Small `n` (≤ 12): Branch & Bound `O(n!)` worst, but pruning helps ← Often faster in practice  
- Very small `n` (≤ 8): Any exact method is fine  

**Memory Constraint:**  
- Greedy & Brute Force: `O(n)` to `O(n^2)` ← Minimal  
- Backtracking: `O(n)` ← Minimal  
- Branch and Bound: Exponential in worst case ← Can grow large  
- Held-Karp DP: `O(n × 2^n)` ← Prohibitive for large `n` (e.g., `n = 25` → 838 million entries)
