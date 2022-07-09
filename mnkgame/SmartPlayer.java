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
        if (MC.length > 0) {
            c = MC[MC.length - 1]; // Recover the last move from MC
            B.markCell(c.i, c.j); // Save the last move in the local MNKBoard
        }

        /*if(FC.length == 1){
            c = FC[0];
            B.markCell(c.i, c.j);
            return c;
        }*/


        MNKCell bestMove = FC[0];
        double res = Double.NEGATIVE_INFINITY;
        double alphabeta = 0;
        for(int k = 0; k < FC.length; k++) {
            c = FC[k];
            B.markCell(c.i, c.j);
            alphabeta = alphaBeta(FC, !(myWin == MNKGameState.WINP1), 0,0);
            B.unmarkCell();
            if(alphabeta > res){
                bestMove = FC[k];
            }
        }
        B.markCell(bestMove.i, bestMove.j);
        return bestMove;
    }

    //Errore cella 1,1 piena, secondo me perché non aggiorna mai FC, anche se dovrebbe, se si fa controllo
    //affinché la cella che si marchi non sia piena, l'algoritmo non funziona bene.

    public double alphaBeta(MNKCell[] FC, boolean isMaximizing, double alpha, double beta){
        //Stampa FreeCells
        for (int k = 0; k < FC.length; k++) {
            System.out.println(FC[k].toString());
        }
        System.out.println("");

        MNKGameState res = B.gameState();
        if(res != MNKGameState.OPEN){
            if(res == myWin) return 1;
            else if(res == yourWin) return -1;
            else return 0;
        }
        if(isMaximizing){
            double eval = Double.NEGATIVE_INFINITY;
            for(int k = 0; k < FC.length; k++) {
                MNKCell c = FC[k];
                B.markCell(c.i, c.j);
                double tmpRes = max(eval, alphaBeta(FC,false, alpha, beta));
                eval = max(eval, tmpRes);
                B.unmarkCell();
                alpha = max(eval, alpha);
                if (beta <= alpha) break;
            }
            return eval;
        }else{
            double eval = Double.POSITIVE_INFINITY;
            for(int k = 0; k < FC.length; k++) {
                MNKCell c = FC[k];
                B.markCell(c.i, c.j);
                double tmpRes = min(eval, alphaBeta(FC, true, alpha, beta));
                eval = min(eval, tmpRes);
                B.unmarkCell();
                beta = min(eval, beta);
                if(beta <= alpha) break;
            }
            return eval;
        }
    }

    public double max(double n, double m){
        if(n >= m) return n;
        else return m;
    }

    public double min(double n, double m){
        if(n <= m) return n;
        else return m;
    }

    public String playerName() {
        return "SmartPlayer";
    }
}