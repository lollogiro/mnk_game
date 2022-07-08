package mnkgame;

import java.util.Random;

/**
 * Software player only a bit smarter than random.
 * <p> It can detect a single-move win or loss. In all the other cases behaves randomly.
 * </p>
 */
public class SmartPlayer implements MNKPlayer {
    private Random rand;
    private MNKBoard B;
    private MNKGameState myWin;
    private MNKGameState yourWin;
    private int TIMEOUT;

    /**
     * Default empty constructor
     */
    public SmartPlayer() {
    }


    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        // New random seed for each game
        rand    = new Random(System.currentTimeMillis());
        B       = new MNKBoard(M,N,K);
        myWin   = first ? MNKGameState.WINP1 : MNKGameState.WINP2;
        yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;
        TIMEOUT = timeout_in_secs;
    }

    /**
     * Selects a position among those listed in the <code>FC</code> array.
     * <p>
     * Selects a winning cell (if any) from <code>FC</code>, otherwise
     * selects a cell (if any) that prevents the adversary to win
     * with his next move. If both previous cases do not apply, selects
     * a random cell in <code>FC</code>.
     * </p>
     */
    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
        long start = System.currentTimeMillis();
        if(MC.length > 0) {
            MNKCell c = MC[MC.length-1]; // Recover the last move from MC
            B.markCell(c.i,c.j);         // Save the last move in the local MNKBoard
        }

    }

//    public MNKCell iterativeDeepening(MNKCell[] FC, MNKCell[] MC, long startTime){
//        int d = 0, alpha = -1, beta = 1, eval = -2;
//        boolean found = false;
//        int depth = FC.length;
//        while((System.currentTimeMillis()-start)/1000.0 <= TIMEOUT*(99.0/100.0) && !found && d < depth){
//            //alphabeta pruning
//            eval = alphabetaPruning(FC, MC, alpha, beta, depth);
//            d++;
//        }
//
//    }

    public MNKCell alphabetaPruning(MNKCell[] FC, MNKCell[] MC, boolean first, int alpha, int beta, int depth){
        

    }

    public String playerName() {
        return "SmartPlayer";
    }
}