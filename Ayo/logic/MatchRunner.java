package logic;

import core.AyoBoard;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MatchRunner {
    private final AyoBoard board;
    private final AtomicInteger p1Score = new AtomicInteger(0);
    private final AtomicInteger p2Score = new AtomicInteger(0);

    public MatchRunner(AyoBoard board) {
        this.board = board;
    }

    public void runSimulation(int durationSeconds) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        System.out.println("Starting Real-Time Ayo...");
        System.out.println("Initial Board state:");
        // Print empty board here if desired

        executor.execute(new PlayerThread(board, "Player 1", 1, p1Score));
        executor.execute(new PlayerThread(board, "Player 2", 2, p2Score));

        try {
            Thread.sleep(durationSeconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\nTime up! Halting simulation...");
        executor.shutdownNow();

        try {
            executor.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n=== FINAL SCORE ===");
        System.out.println("Player 1: " + p1Score.get());
        System.out.println("Player 2: " + p2Score.get());
    }
}