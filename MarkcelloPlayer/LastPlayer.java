package MarkcelloPlayer;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;
import MarkcelloPlayer.BigBoard.Board;

public class LastPlayer implements mnkgame.MNKPlayer {
    private IBoard B;
    private MNKGameState myWin;
    private MNKCellState myState;
    private MNKCellState yourState;
    private MNKGameState yourWin;
    private MNKGameState gameState;
    private final int KINF = Integer.MAX_VALUE;

    private int BRANCHING_FACTOR = 7;
    private int DEPTH_LIMIT = 10;
    private int maxNumberOfMoves;
    
    // mosse massime per il tree attuale
    private int maxMovesCurrentTree;
    
    // mosse del tree attuale
    private int movesCurrentTree;

    private final boolean DEBUG = false;

    public LastPlayer() {}

    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        long timeStart = System.currentTimeMillis();
        this.myState = first ? MNKCellState.P1 : MNKCellState.P2;
        this.yourState = first ? MNKCellState.P2 : MNKCellState.P1;

        this.B = new Board(M, N, K, myState);

        this.myWin = first ? MNKGameState.WINP1 : MNKGameState.WINP2;
        this.yourWin = first ? MNKGameState.WINP2 : MNKGameState.WINP1;

        TimingPlayer timing = new TimingPlayer(timeStart, timeout_in_secs, B);
        timing.findBestTime();
        
        BRANCHING_FACTOR = timing.getBranchingFactor();
        DEPTH_LIMIT = timing.getDephtLimit();
        this.maxNumberOfMoves = timing.getMoves();
        this.maxMovesCurrentTree = 0;
        this.movesCurrentTree = 0;
    }

    public int minPlayer(int depth, int alpha, int beta) {
        if (depth == DEPTH_LIMIT) {
            return B.getValue(yourState);
        }else if(gameState == myWin){
            return KINF - 1;
        } else if (gameState == yourWin) {
            return -KINF + 1;
        }else if ( gameState  == MNKGameState.DRAW || B.isForcedDraw()){
            return 0;
        }
        
        
        int v = KINF;
        
        int len = Math.min(BRANCHING_FACTOR, B.getFreeCellsCount());
        
        for (int i = 0; i < len; i++) {
            if (movesCurrentTree + depth >= maxMovesCurrentTree) {
                break;
            }

            gameState = B.markCell(B.getGreatKCell(i));
            int maxPlayerValue = maxPlayer(depth + 1, alpha, beta);
            B.unmarkCell();
            movesCurrentTree++;
            
            if (maxPlayerValue < v) {
                v = maxPlayerValue;
                beta = Math.min(beta, v);
            }

            if (v <= alpha)
                return v;
        }
        
        if(v == KINF){
            return B.getValue(myState);
        }
        return v;
    }

    private int maxPlayer(int depth, int alpha, int beta) {
        if (depth == DEPTH_LIMIT) {
            return B.getValue(myState);
        } else if(gameState == myWin){
            return KINF - 1;            
        } else if (gameState == yourWin) {
            return -KINF + 1;
        } else if (gameState == MNKGameState.DRAW || B.isForcedDraw()) {
            return 0;
        }

        int v = -KINF;
        
        int len = Math.min(BRANCHING_FACTOR, B.getFreeCellsCount());
        
        for (int i = 0; i < len; i++) {
            if (movesCurrentTree + depth >= maxMovesCurrentTree) {
                break;
            }

            gameState = B.markCell(B.getGreatKCell(i));
            int minPlayerValue = minPlayer(depth + 1, alpha, beta);
            B.unmarkCell();
            
            movesCurrentTree++;
            

            if (minPlayerValue > v) {
                v = minPlayerValue;
                alpha = Math.max(alpha, v);
            }

            if (v >= beta)
                return v;
        }

        if(v == -KINF) {
            return B.getValue(myState);
        }

        return v;
    }

  

    /**
     * trova mossa migliore con alfa beta pruning
     * @return
     */
    private MNKCell findBestMove() {
        int alpha = -KINF;
        int beta = KINF;
        int v = -KINF;

        MNKCell cell = null;

        // al primo livello valuto quasi tutto

        int len = Math.min(BRANCHING_FACTOR * 3, B.getFreeCellsCount());
        maxMovesCurrentTree = maxNumberOfMoves / 4;
        B.setBranchingFactor(len);
        B.updateCellDataStruct();

        int toAddEachStep;
        if (len <= 1)
            toAddEachStep = maxNumberOfMoves;
        else
            toAddEachStep = (maxNumberOfMoves - maxMovesCurrentTree)/ (len - 1);

        //if (DEBUG) {
        //    System.out.println("maxMovesCurrentTree: " + maxNumberOfMoves);
        //    System.out.println("toAddEachStep: " + toAddEachStep);
        //}

        for (int i = 0; i < len; i++) {
            movesCurrentTree = 0;
            IHeuristicCell currCell = B.getGreatKCell(i);
            gameState = B.markCell(currCell);
            B.setBranchingFactor(BRANCHING_FACTOR);
            int minPlayerValue = minPlayer(1, alpha, beta);
            B.setBranchingFactor(len);
            B.unmarkCell();

            
            // if (DEBUG) {
            //     System.out.println("cella: " + currCell.getI() + " " +  currCell.getJ() + " valore: " + minPlayerValue);
            //     System.out.format("usate %d mosse su %d\n", movesCurrentTree, maxMovesCurrentTree);
            // }

            if (minPlayerValue > v) {
                v = minPlayerValue;
                cell = currCell.toMNKCell();
                alpha = Math.max(alpha, v);
            }
            
            // quelli rimasti nell'iterazione precendente + numero da aggiungere ogni step
            
            maxMovesCurrentTree = (maxMovesCurrentTree - movesCurrentTree) + toAddEachStep;
        }
        return cell;
    }    

    public MNKCell selectCell(MNKCell[] freeCells, MNKCell[] movedCells) {
        if (movedCells.length > 0) {
            MNKCell c = movedCells[movedCells.length - 1]; // Recover the last move from MC
            B.markCell(c.i, c.j); // Save the last move in the local MNKBoard
        }

        // if (DEBUG) {
            // B.print();
            // B.printHeuristics(true);
            // B.printHeuristics(false);
        // }
        
        MNKCell bestCell = findBestMove();
        B.markCell(bestCell.i, bestCell.j);

        return bestCell;
    }

    public String playerName() {
        return "MarkCello";
    }
}
