package MarkcelloPlayer;

import java.lang.IllegalStateException;
import java.lang.IndexOutOfBoundsException;

import mnkgame.MNKCellState;
import mnkgame.MNKGameState;

public interface IBoard {

    public int getM();

    public int getN();

    public int getK();

    public void setBranchingFactor(int branchingFactor);

    public void updateCellDataStruct();

    public IHeuristicCell getGreatKCell(int k);

    public MNKGameState markCell(IHeuristicCell cell);

    public MNKGameState markCell(int i, int j) throws IndexOutOfBoundsException, IllegalStateException;

    public void addAdjiacentCells(int i, int j, int value);

    public boolean isForcedDraw();

    public void unmarkCell() throws IllegalStateException;

    public IHeuristicCell getIthCell(int i);

    public int getValue(MNKCellState state);
    
    public IValue getCellValue(int i, int j, MNKCellState state);

    public MNKCellState getState(int i, int j);

    public MNKGameState gameState();


    public void setCellState(int i, int j, MNKCellState state);

    public void setPlayer(MNKCellState player);

    public int getHeuristic(int i, int j);

    /**
     * Returns the id of the player allowed to play next move.
     *
     * @return 0 (first player) or 1 (second player)
     */
    public int currentPlayer();
    
    public void print();

    public void printHeuristics(boolean ally);

    public int getFreeCellsCount();
}
