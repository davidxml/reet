# Dead or Wounded: The Code-Breaking Game

> The "Dead or Wounded" game—known internationally under various names including Bulls and Cows, Mastermind, and Jotto—is a classic code-breaking pastime that has transcended its pen-and-paper origins to become a staple exercise in algorithmic thinking and artificial intelligence. The game presents a finite, deterministic, two-player deductive reasoning challenge where one player (the code-maker) conceals a secret permutation, and the other (the code-breaker) must deduce it through iterative hypothesis testing and logical elimination. Its mathematical structure—a bounded search space pruned by discrete feedback—makes it an ideal candidate for computational modeling, search algorithm analysis, and machine learning experimentation in the field of computer science.

## Game Rules

### The Setup
- **The Code:** The code-maker selects a secret consisting of a sequence of numeric digits or colored pegs. In the standard numerical variant, the secret is a 4-digit number where all digits are unique (no repetition). The digits are drawn from the set {0–9}.
- **The Board:** The game provides a 10 × 4 grid (for 10 guesses of 4-digit codes) where the code-breaker records each guess, and the code-maker marks the corresponding score.
- **Players:** Two players assume opposing roles: the code-maker (selector of the secret) and the code-breaker (deducer of the secret). The objective is for the code-breaker to identify the exact permutation within a bounded number of attempts.

### The Feedback Mechanism
After each guess, the code-maker provides a score comprising two counts:

1. **Dead (Bulls):** The number of digits that are correct *and* in the correct position. A "dead" digit is perfectly placed and requires no further relocation. Represented by a black key peg or the letter **D**.
2. **Wounded (Cows):** The number of digits that are correct *but* in the wrong position. A "wounded" digit exists in the secret but must be moved to a different position. Represented by a white key peg or the letter **W**.

Digits that do not appear in the secret at all receive no mark. The feedback provides no positional information beyond the aggregate counts—the code-breaker does not know *which* digits are dead and which are wounded, only the totals.

### Winning Condition
The game ends when either:
- The code-breaker submits a guess that matches the secret exactly (4 dead / 0 wounded), winning the game.
- The code-breaker exhausts all permitted attempts without breaking the code, in which case the code-maker wins.

## Computational Modeling

### Search Space and Complexity
The standard numerical variant (4 unique digits from 0–9) yields a search space of 10 × 9 × 8 × 7 = 5,040 possible permutations. This relatively compact space makes brute-force exhaustive search feasible, but optimal play—guaranteeing a solution within 5–7 guesses—requires more sophisticated strategies.

### Algorithmic Approaches

**Exhaustive Search (Brute Force):** The simplest implementation generates all candidate permutations up front. After each guess and feedback pair, the algorithm filters the candidate set by eliminating any permutation that would not produce the observed (dead, wounded) score. This is guaranteed to converge to the solution.

**Minimax (Knuth's Algorithm):** Donald Knuth's classic 1976 solution to Mastermind demonstrates that the code can always be broken in 5 or fewer guesses using a minimax strategy. The algorithm selects the guess that minimizes the maximum possible size of the remaining candidate set across all possible feedback responses. This information-theoretic approach ensures optimal worst-case performance.

**Information Gain (Entropy-Based):** A heuristic alternative to full minimax selects the guess that maximizes the expected information gain (Shannon entropy) over the remaining candidate set. While not provably optimal, this approach achieves near-optimal average-case performance with significantly lower computational cost.

### Functional Implementation Principles
To ensure correctness and facilitate recursive search, the game logic is implemented following functional programming principles:
- **Immutability:** Each guess produces a new game state rather than mutating an existing board.
- **Pure Functions:** The scoring function—computing dead and wounded counts from a guess and the secret—is a pure function with no side effects, making it trivially testable and parallelizable.
- **Recursive Filtering:** The candidate elimination process uses set-filtering operations over immutable collections, avoiding in-place mutation and preventing off-by-one errors common in iterative implementations.

## Conclusion

Dead or Wounded stands as a deceptively simple deductive challenge that has served as a proving ground for search algorithms, information theory, and artificial intelligence since its invention. From Knuth's optimal minimax solution to modern entropy-based heuristics and machine learning approaches, the game has consistently demanded rigorous algorithmic thinking. Its transition from a pencil-and-paper classroom game to a computational model underscores its enduring value in computer science education—demonstrating, in miniature, the fundamental principles of search space pruning, feedback-driven hypothesis refinement, and optimal decision-making under uncertainty.
