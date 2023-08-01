package mnkgame;

import java.util.*;

class Cell implements Comparable<Cell> {
    public Integer count;
    public Integer playerCells;
    public Integer opponentCells;
    public MNKCell cell;


    public Cell(){
        count = null;
        playerCells = null;
        opponentCells = null;
        cell = null;
    }

    public Cell(Integer count, Integer playerCells, Integer opponentCells, MNKCell cell) {
        this.count = count;
        this.playerCells = playerCells;
        this.opponentCells = opponentCells;
        this.cell = cell;
    }

    /*
     * Restituisce una voce della mappa (coppia chiave-valore) dai valori specificati
     */
    public static <T, U> Map.Entry<T, U> of(T first, U second) {
        return new AbstractMap.SimpleEntry<>(first, second);
    }

    public boolean compareCells(Cell o){
        return o.cell.i == this.cell.i && o.cell.j == this.cell.j;
    }

    @Override
    public int compareTo(Cell o) {
        return this.count.compareTo(o.count);
    }
}
