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
        MNKCell c;
        if(MC.length > 0) {
            c = MC[MC.length-1]; // Recover the last move from MC
            B.markCell(c.i,c.j);         // Save the last move in the local MNKBoard
        }
        c=iterativeDeepening(FC, MC, start);
        if(c==null){
            c = FC[rand.nextInt(FC.length)];
			B.markCell(c.i,c.j);
        }
        return c;
    }

    public MNKCell iterativeDeepening(MNKCell[] FC, MNKCell[] MC, long startTime){
        int d = 0, alpha = -1, beta = 1, eval = -2;
        boolean found = false;
        int depth = FC.length;
        MNKCell c;
        while((System.currentTimeMillis()-startTime)/1000.0 <= TIMEOUT*(80.0/100.0) && !found && d < depth){
            //alphabeta pruning
            c = FC[d];
            B.markCell(c.i, c.j);
            eval = alphabetaPruning(FC, MC, false, alpha, beta, depth);
            if(eval==1) return c;
            else{
                B.unmarkCell();
            }
            d++;
        }
        return null;

    }

    public int alphabetaPruning(MNKCell[] FC, MNKCell[] MC, boolean PlayerA, int alpha, int beta, int depth){
        int eval=0;
        if(FC.length == 1){
            MNKCell c = FC[0];
            if(B.markCell(c.i, c.j) == myWin){
                B.unmarkCell();
                return 1;
            }
            else if(B.markCell(c.i, c.j) == yourWin){
                B.unmarkCell();
                return -1;
            }
            else return 0;
        }

        if(FC.length > 0){
            MNKCell c = FC[0];
            if(B.markCell(c.i, c.j) == myWin){
                B.unmarkCell();
                return 1;
            }
            else if(B.markCell(c.i, c.j) == yourWin){
                B.unmarkCell();
                return -1;
            }
        }
        
        if(PlayerA){
            eval=-2;
            for (int i=0; i<FC.length; i++){
                MNKCell c = FC[i];
                B.markCell(c.i, c.j);
                
                eval=Max(alphabetaPruning(FC, MC, !PlayerA, alpha, beta, depth-1), eval);
                alpha=Max(alpha, eval);
                B.unmarkCell();
                if(alpha>=beta) break; 
                
            }
        }
        else{
            eval=2;
            for (int i=0; i<FC.length; i++){
                MNKCell c = FC[i];
                B.markCell(c.i, c.j);
                
                eval=Min(alphabetaPruning(FC, MC, !PlayerA, alpha, beta, depth-1), eval);
                beta=Min(beta, eval);
                B.unmarkCell();
                if(alpha>=beta) break; 
            }
        }
        return eval;

        
        

    }

    private int Min(int n1, int n2) {
        if(n1<n2) return n1;
        else return n2;
    }


    private int Max(int n1, int n2) {
        if(n1>n2) return n1;
        else return n2;
    }


    public String playerName() {
        return "SmartPlayer";
    }
}