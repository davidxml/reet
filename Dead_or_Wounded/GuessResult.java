public class GuessResult {
    public final int dead;
    public final int wounded;

    public GuessResult(int dead, int wounded) {
        this.dead = dead;
        this.wounded = wounded;
    }
    
    // Formats the output so you can print the object directly
    @Override
    public String toString() {
        return dead + " Dead, " + wounded + " Wounded";
    }
}