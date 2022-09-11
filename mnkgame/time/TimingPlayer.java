package MarkcelloPlayer;

import mnkgame.MNKCellState;
import mnkgame.MNKGameState;
import MarkcelloPlayer.BigBoard.Board;

/**
 * Questa classe è utilizzata per fare una stima della capacità di esecuzione del computer remoto
 * in modo che dinamicamente calcoli profondità e branching factor migliore
 */
class TimingPlayer{

    private long timeStart;
    private final long TIMEOUT;
    private IBoard B;
    private int moves;

    private int BRANCHING_FACTOR = 7;
    private int DEPTH_LIMIT = 10;
    
    TimingPlayer(long timeStart, int timeout_in_secs, int M, int N, int K) {
        this(timeStart, timeout_in_secs, new Board(M, N, K, MNKCellState.P1));
    }  
    
    TimingPlayer(long timeStart, int timeout_in_secs, IBoard board) {
        this.timeStart = timeStart;
        this.TIMEOUT = Math.min(timeout_in_secs, 10);
        this.B = board;        
        this.moves = 0;

        if (board.getK() == 10) {  // per le board grosse
            DEPTH_LIMIT = 10;
            BRANCHING_FACTOR = 3;
        }
    }

    boolean hasEnded(int depth) {
        return depth == DEPTH_LIMIT || hasTimeRunOut() || B.gameState() != MNKGameState.OPEN;
    }


     /**
     * trova mossa migliore con alfa beta pruning
     * @return
     */
    public void findBestTime() {
        int len = Math.min(BRANCHING_FACTOR * 3, B.getFreeCellsCount());
        // moves = 1000000;
        // return;
        for (int i = 0; i < len; i++) {
           if (hasTimeRunOut()) return;
           
           IHeuristicCell currCell = B.getGreatKCell(i);
           B.markCell(currCell);
           minPlayer(1);
           B.unmarkCell();
           moves++;
        }
    }    

    public void minPlayer(int depth) {
        if (hasEnded(depth)) { 
            return;
        }
        int len = Math.min(BRANCHING_FACTOR, B.getFreeCellsCount());
        for (int i = 0; i < len; i++) {
            B.markCell(B.getGreatKCell(i));
            maxPlayer(depth + 1);
            B.unmarkCell();
            moves++;
        }
    }

    private void maxPlayer(int depth) {
        if(hasEnded(depth))
            return;

        int len = Math.min(BRANCHING_FACTOR, B.getFreeCellsCount());
        
        for (int i = 0; i < len; i++) {
            if(hasTimeRunOut()) return; 

            B.markCell(B.getGreatKCell(i));
            minPlayer(depth + 1);
            B.unmarkCell();
            moves++;
        }
    }


    private boolean hasTimeRunOut() {
        return (System.currentTimeMillis() - timeStart) / 1000.0 > TIMEOUT * (85.0 / 100.0);
    }

    public int getMoves() {
        return this.moves;
    }
    
    public int getBranchingFactor() {   
        return BRANCHING_FACTOR;
    }
    public int getDephtLimit(){
        if(B.getK() == 10 && moves < 1000000)return 8; 
        return DEPTH_LIMIT;
    }

}