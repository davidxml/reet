package logic;

import core.AyoBoard;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerThread implements Runnable {
    private final AyoBoard board;
    private final String playerName;
    private final int playerSide; // 1 for P1 (0-5), 2 for P2 (6-11)
    private final AtomicInteger score;
    private final Random random = new Random();

    public PlayerThread(AyoBoard board, String playerName, int playerSide, AtomicInteger score) {
        this.board = board;
        this.playerName = playerName;
        this.playerSide = playerSide;
        this.score = score;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // P1 plays pits 0-5. P2 plays pits 6-11.
                int startPit = (playerSide == 1) ? random.nextInt(6) : random.nextInt(6) + 6;
                
                boolean validMove = board.makeMoveSafe(startPit, playerName, score);
                
                if (validMove) {
                    // Simulate thinking/reaction time (50-150ms)
                    Thread.sleep(random.nextInt(100) + 50);
                } else {
                    // If pit was empty, retry immediately
                    Thread.yield(); 
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}