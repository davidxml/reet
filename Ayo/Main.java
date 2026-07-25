import core.AyoBoard;
import logic.MatchRunner;

public class Main {
    public static void main(String[] args) {
        AyoBoard sharedBoard = new AyoBoard();
        MatchRunner runner = new MatchRunner(sharedBoard);
        
        // Run the simulation for 3 seconds
        runner.runSimulation(3);
    }
}