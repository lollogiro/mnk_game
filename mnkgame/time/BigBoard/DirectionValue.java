package MarkcelloPlayer.BigBoard;

public class DirectionValue {
    // numero minimo di celle per vincere andando solo a sinistra 
    // (guarda sempre a distanza K-1, se in quella distanza c'è una cella nemica viene settato a -1)
    //public int left;  
    
    // numero minimo di celle per vincere andando solo a destra (uguale a left ma nel'altra direzione)
    //public int right;  

    // numero minimo di celle per vincere per l'intera direzione
    // center rappresenta anche il numero di da completare nella migliore sliding window
    public int center;  

    // numero di sliding windows validi
    public int numSliding;  
    
    // numero di sliding windows massimi 
    public int numMaximumSliding;  

    // numero di mie celle 
    public int numMyCells;
    public static final int MY_CELL_MULT = 10;
    public static final int DOUBLEPLAY_VAL = 10000;
    public static final int WIN_VAL = 1000000;
    // anche per l'uno

    // usando il mics, il numero di sliding windows buone, + numero di celle amiche
    private int value;
    
    DirectionValue() {
        reset();
    }

    /**
     * Computes the value of the direction using MICS (Minimum Incomplete Cell Set)
     * heuristics.
     * @param K
     */
    public void updateDirectionValue() {
        value = numSliding + numMyCells * MY_CELL_MULT;

        if (center == 1) {
            value = WIN_VAL;
        } else if (isInLineDoublePlay()) {
            value = DOUBLEPLAY_VAL;
        }
    }

    public int getDirectionValue() {
        return value;
    }

    public void reset() {
        this.center = Integer.MAX_VALUE;
        this.value = 0;
        this.numSliding = 0;
        this.numMaximumSliding = 0;
        this.numMyCells = 0;
    }

    public void setInvalidDirectionValue() {
        //this.left = -1;
        //this.right = -1;
        this.center = -1;
        this.numSliding = 0;
        this.numMaximumSliding = 0;
        this.numMyCells = 0;
    }

    /**
     * @return if it's possible to win (i.e. if there is at least one way to win in this direction)
     */
    public boolean canWin() {
        return center >= 0;  // NOTA: se left o right >= 0, allora center >= 0
    }

    /**
     * @return the minimum number of steps to win in this direction, -1 if you can't win
     */
    public int minStepsToWin() {
        return center;
    }

    /**
     * @return if it's possible to have a trivial double play win
     * This is true if at least two sliding windows have need two moves to win
     */
    public boolean isInLineDoublePlay() {
        return numMaximumSliding >= 2 && center == 2;
    }
    
    @Override
    public String toString() {
        return String.format("(center: %d, myCell:%d, numSlinding: %d, maxNumSliding: %d)", center, numMyCells, numSliding, numMaximumSliding);
    }
}
