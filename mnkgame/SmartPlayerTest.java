package mnkgame;

import java.util.*;

enum winType {
    PLAYER,
    OPPONENT,
    NONE
}

class Pair implements Comparable<Pair> {
    public Integer count;
    public Integer playerCells;
    public Integer opponentCells;
    public MNKCell cell;


    public Pair(){
        count = null;
        playerCells = null;
        opponentCells = null;
        cell = null;
    }

    public Pair(Integer count, Integer playerCells, Integer opponentCells, MNKCell cell) {
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

    public boolean compareCells(Pair o){
        return o.cell.i == this.cell.i && o.cell.j == this.cell.j;
    }

    @Override
    public int compareTo(Pair o) {
        return this.count.compareTo(o.count);
    }
}

public class SmartPlayerTest implements MNKPlayer {
    private Random rand;
    private MNKBoard B;
    private boolean first;
    private MNKGameState myWin;
    private MNKGameState yourWin;
    private int TIMEOUT;

    /**
     * Default empty constructor
     */
    public SmartPlayerTest() {
    }


    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        // New random seed for each game
        rand    = new Random(System.currentTimeMillis());
        B       = new MNKBoard(M,N,K);
        this.first = first;
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

        c = FC[0];

        if(FC.length == 1){
            c = FC[0];
            B.markCell(c.i, c.j);
            return c;
        }

        MNKCellState smartPlayerTurn = first ? MNKCellState.P1 : MNKCellState.P2;
        MNKCellState oppositePlayerTurn = first ? MNKCellState.P2 : MNKCellState.P1;

        c = calculateHelpfulness(MC, FC, smartPlayerTurn, oppositePlayerTurn, start);

        B.markCell(c.i, c.j);

        return c;
    }

    public MNKCell calculateHelpfulness(MNKCell[] MC, MNKCell[] FC, MNKCellState player, MNKCellState opponent, long start) {
        //Dichiarazione MaxPriorityQueue
        PriorityQueue<Pair> helpfulnessPQueue =
                new PriorityQueue<Pair>(Collections.reverseOrder());

        //Fill MaxPriorityQueue
        for (MNKCell i : FC) {
            helpfulnessPQueue.add(new Pair(0, 0, 0, i));
        }

        boolean playerVictory = false;
        boolean outOfTime = false;

        //-->MARKED CELL PLAYER<--

        for (MNKCell i : MC) {

            if(playerVictory || outOfTime) break;

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }

            //ORIZZONTALE CON cella i a sinistra
            horizontalCheck(MC, FC, helpfulnessPQueue, false, player, i, true);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }

            //VERTICALE CON cella i in alto
            verticalCheck(MC, FC, helpfulnessPQueue, false, player, i, true);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }

            //DIAGONALE PRINCIPALE con cella i in alto a sinistra
            mainDiagonalCheck(MC, FC, helpfulnessPQueue, false, player, i, true);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }

            //DIAGONALE SECONDARIA con cella i in alto a destra
            secondDiagonalCheck(MC, FC, helpfulnessPQueue, false, player, i, true);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }
        }

        //-->MARKED CELL OPPONENT<--

        for (MNKCell i : MC) {

            if(playerVictory || outOfTime) break;

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }

            //ORIZZONTALE CON cella i a sinistra
            horizontalCheck(MC, FC, helpfulnessPQueue, true, opponent, i, true);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }

            //VERTICALE CON cella i in alto
            verticalCheck(MC, FC, helpfulnessPQueue, true, opponent, i, true);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }

            //DIAGONALE PRINCIPALE con cella i in alto a sinistra
            mainDiagonalCheck(MC, FC, helpfulnessPQueue, true, opponent, i, true);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }

            //DIAGONALE SECONDARIA con cella i in alto a destra
            secondDiagonalCheck(MC, FC, helpfulnessPQueue, true, opponent, i, true);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }
        }

        //-->FREE CELL PLAYER<--
        for (MNKCell i : FC) {

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }

            //ORIZZONTALE CON cella i a sinistra
            horizontalCheck(MC, FC, helpfulnessPQueue, false, player, i, false);

            if(helpfulnessPQueue.peek().count >= 10000){
                playerVictory = true;
                break;
            }

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }

            //VERTICALE CON cella i in alto
            verticalCheck(MC, FC, helpfulnessPQueue, false, player, i, false);


            if(helpfulnessPQueue.peek().count >= 10000){
                playerVictory = true;
                break;
            }

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }

            //DIAGONALE PRINCIPALE con cella i in alto a sinistra
            mainDiagonalCheck(MC, FC, helpfulnessPQueue, false, player, i, false);


            if(helpfulnessPQueue.peek().count >= 10000){
                playerVictory = true;
                break;
            }

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }

            //DIAGONALE SECONDARIA con cella i in alto a destra
            secondDiagonalCheck(MC, FC, helpfulnessPQueue, false, player, i, false);

            if(helpfulnessPQueue.peek().count >= 10000){
                playerVictory = true;
                break;
            }

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }
        }

        

        //-->OPPONENT<--

        for (MNKCell i : FC) {

            if(playerVictory || outOfTime) break;

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }

            //ORIZZONTALE CON cella i a sinistra
            horizontalCheck(MC, FC, helpfulnessPQueue, true, opponent, i, false);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }

            //VERTICALE CON cella i in alto
            verticalCheck(MC, FC, helpfulnessPQueue, true, opponent, i, false);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }

            //DIAGONALE PRINCIPALE con cella i in alto a sinistra
            mainDiagonalCheck(MC, FC, helpfulnessPQueue, true, opponent, i, false);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }

            //DIAGONALE SECONDARIA con cella i in alto a destra
            secondDiagonalCheck(MC, FC, helpfulnessPQueue, true, opponent, i, false);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(80.0/100.0)){
                outOfTime = true;
                break;
            }
        }

        PriorityQueue<Pair> smartPlayerPQueue = new PriorityQueue<>(helpfulnessPQueue);
        PriorityQueue<Pair> smartPlayerPQueueTwo = new PriorityQueue<>(helpfulnessPQueue);

        Pair choosenCell = helpfulnessPQueue.poll();
        Pair otherCell = helpfulnessPQueue.poll();

        while(choosenCell.count == otherCell.count){
            System.out.println(choosenCell.cell+ " " + otherCell.cell);
            if(choosenCell.playerCells < otherCell.playerCells)choosenCell = otherCell;
            otherCell = helpfulnessPQueue.poll();
            if(otherCell == null) break;
        }

        otherCell = smartPlayerPQueueTwo.poll();

        while(choosenCell.count == otherCell.count){
            System.out.println(choosenCell.cell+ " " + otherCell.cell);
            if(choosenCell.playerCells <= otherCell.opponentCells) choosenCell = otherCell;
            otherCell = smartPlayerPQueueTwo.poll();
            if(otherCell == null) break;
        }
        

        System.out.println("\n" + choosenCell.cell.toString() + " count:" + choosenCell.count + " playerCell:" + choosenCell.playerCells +"\n");

        //Print the MaxPriorityQueue to test the algorithm
        for (MNKCell i : FC) {
            System.out.println(smartPlayerPQueue.peek().cell.toString() + " " + smartPlayerPQueue.peek().count + " " + smartPlayerPQueue.peek().playerCells + " " + smartPlayerPQueue.poll().opponentCells);
        }

        System.out.println("\n----------------------------------------------\n");

        return choosenCell.cell;
    }

    public void horizontalCheck(MNKCell[] MC, MNKCell[] FC, PriorityQueue<Pair> helpfulnessPQueue, boolean opponent, MNKCellState turnState, MNKCell cell, boolean MCCheck){
        int connectedCell = 1;
        int markedCellsUsed = 0;
        if(MCCheck) markedCellsUsed++;
        for (int k = 1; k < B.K; k++) {
            if (cell.j + k >= B.N) break;
            else if (isUsable(MC, FC, turnState, cell.i, cell.j + k)) {
                connectedCell++;
                if (!isFree(FC, cell.i, cell.j + k)) markedCellsUsed++;
            } else break;
        }
        if (connectedCell == B.K) {
            System.out.println(cell.toString() + " orizzontale con i a sinistra");
            int j;
            if(MCCheck) j = 1;
            else j = 0;
            if(markedCellsUsed == B.K - 2){
                if(isFree(FC, cell.i, cell.j)){
                    if(isFree(FC, cell.i, cell.j + B.K - 1)){
                        if(isFree(FC, cell.i, cell.j - 1)){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j, true);
                        }
                        else{
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j, false);
                        }
                        if(isFree(FC, cell.i, cell.j + B.K)){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j + B.K - 1, true);
                        }
                        else{
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j + B.K - 1, false);
                        }
                    }
                    else if(isFree(FC, cell.i, cell.j + B.K)){
                        getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j, true);
                        j=1;
                        for(; j < B.K - 1; j++){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j+j, true);
                        }
                    }
                    
                }
                else if(isFree(FC, cell.i, cell.j + B.K - 1)){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j + B.K - 1, true);
                    j=1;
                    for(; j < B.K - 1; j++){
                        getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j+j, true);
                    }
                }
                else{
                    for(; j < B.K; j++){
                        getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j+j, false);
                    }
                }
            }
            else{
                for(; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j+j, false);
                }
            }

            
        }
    }

    public void verticalCheck(MNKCell[] MC, MNKCell[] FC, PriorityQueue<Pair> helpfulnessPQueue, boolean opponent, MNKCellState turnState, MNKCell cell, boolean MCCheck){
        int connectedCell = 1;
        int markedCellsUsed = 0;
        if(MCCheck) markedCellsUsed++;
        for (int k = 1; k < B.K; k++) {
            if (cell.i + k >= B.M) break;
            else if (isUsable(MC, FC, turnState, cell.i + k, cell.j)) {
                connectedCell++;
                if (!isFree(FC, cell.i + k, cell.j)) markedCellsUsed++;
            } else break;
        }
        if (connectedCell == B.K) {
            System.out.println(cell.toString() + " verticale con i in alto");
            int j;
            if(MCCheck) j = 1;
            else j = 0;
            if(markedCellsUsed == B.K - 2){
                if(isFree(FC, cell.i, cell.j)){
                    if(isFree(FC, cell.i + B.K - 1, cell.j)){
                        if(isFree(FC, cell.i - 1, cell.j)){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j, true);
                        }
                        else{
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j, false);
                        }
                        if(isFree(FC, cell.i + B.K, cell.j)){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i + B.K - 1, cell.j, true);
                        }
                        else{
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i + B.K - 1, cell.j, false);
                        }
                    }
                    else if(isFree(FC, cell.i + B.K, cell.j)){
                        getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j, true);
                        j=1;
                        for(; j < B.K; j++){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j, true);
                        }
                    }
                }
                else if(isFree(FC, cell.i + B.K - 1, cell.j)){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i + B.K - 1, cell.j, true);
                    j=1;
                    for(; j < B.K - 1; j++){
                        getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j, true);
                    }
                }
            }
            else{
                for(; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j, false);
                }
            }
        }
    }

    public void mainDiagonalCheck(MNKCell[] MC, MNKCell[] FC, PriorityQueue<Pair> helpfulnessPQueue, boolean opponent, MNKCellState turnState, MNKCell cell, boolean MCCheck){
        int connectedCell = 1;
        int markedCellsUsed = 0;
        if(MCCheck) markedCellsUsed++;
        for (int k = 1; k < B.K; k++) {
            if (cell.i + k >= B.M || cell.j + k >= B.N) break;
            else if (isUsable(MC, FC, turnState, cell.i + k, cell.j + k)) {
                connectedCell++;
                if (!isFree(FC, cell.i + k, cell.j + k)) markedCellsUsed++;
            } else break;
        }
        if (connectedCell == B.K) {
            System.out.println(cell.toString() + " diagonale principale con i in alto a sinistra");
            int j;
            if(MCCheck) j = 1;
            else j = 0;

            if(markedCellsUsed == B.K - 2){
                if(isFree(FC, cell.i, cell.j)){
                    if(isFree(FC, cell.i + B.K - 1, cell.j + B.K - 1)){
                        if(isFree(FC, cell.i - 1, cell.j - 1)){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j, true);
                        }
                        else{
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j, false);
                        }
                        if(isFree(FC, cell.i + B.K, cell.j + B.K)){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i + B.K - 1, cell.j + B.K - 1, true);
                        }
                        else{
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i + B.K - 1, cell.j + B.K - 1, false);
                        }
                    }
                    else if(isFree(FC, cell.i + B.K, cell.j + B.K)){
                        getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j, true);
                        j=1;
                        for(; j < B.K; j++){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j+j, true);
                        }
                    }
                }
                else if(isFree(FC, cell.i + B.K - 1, cell.j + B.K - 1)){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i + B.K - 1, cell.j + B.K - 1, true);
                    j=1;
                    for(; j < B.K; j++){
                        getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j+j, true);
                    }
                }
            }
            else{
                for(; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j+j, false);
                }
            }
        }
    }

    public void secondDiagonalCheck(MNKCell[] MC, MNKCell[] FC, PriorityQueue<Pair> helpfulnessPQueue, boolean opponent, MNKCellState turnState, MNKCell cell, boolean MCCheck){
        int connectedCell = 1;
        int markedCellsUsed = 0;
        if(MCCheck) markedCellsUsed++;
        for (int k = 1; k < B.K; k++) {
            if (cell.i + k >= B.M || cell.j - k < 0) break;
            else if (isUsable(MC, FC, turnState, cell.i + k, cell.j - k)) {
                connectedCell++;
                if (!isFree(FC, cell.i + k, cell.j - k)) markedCellsUsed++;
            } else break;
        }
        if (connectedCell == B.K) {
            System.out.println(cell.toString() + " diagonale secondaria con i in alto a destra");
            int j;
            if(MCCheck) j = 1;
            else j = 0;

            if(markedCellsUsed == B.K - 2){
                if(isFree(FC, cell.i, cell.j)){
                    if(isFree(FC, cell.i + B.K - 1, cell.j - B.K + 1)){
                        if(isFree(FC, cell.i - 1, cell.j + 1)){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j, true);
                        }
                        else{
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j, false);
                        }
                        if(isFree(FC, cell.i + B.K, cell.j - B.K)){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i + B.K - 1, cell.j - B.K + 1, true);
                        }
                        else{
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i + B.K - 1, cell.j - B.K + 1, false);
                        }
                    }
                    else if(isFree(FC, cell.i + B.K, cell.j - B.K)){
                        getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j, true);
                        j=1;
                        for(; j < B.K; j++){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j-j, true);
                        }
                    }
                }
                else if(isFree(FC, cell.i + B.K - 1, cell.j - B.K + 1)){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i + B.K - 1, cell.j - B.K + 1, true);
                    j=1;
                    for(; j < B.K; j++){
                        getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j-j, true);
                    }
                }
            }
            else{
                for(; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j-j, false);
                }
            }
        }
    }

    public winType getFreeCellsHelpfulness(MNKCell[] FC, PriorityQueue<Pair> helpfulnessPQueue, boolean opponent, MNKCellState toCheck, int markedCellsUsed, int offsetX, int offsetY, boolean isDangerous) {
        MNKCell c = findCell(FC, offsetX, offsetY);
        if (c != null) {
            Object[] toUpdate = helpfulnessPQueue.toArray();
            boolean found = false;
            int it = 0, originalCount = 0, originalPlayerCells = 0, originalOpponentCells = 0;
            while (!found) {
                Pair tmp = (Pair) toUpdate[it];
                if (tmp.cell.equals(c)) {
                    originalCount = tmp.count;
                    originalPlayerCells = tmp.playerCells;
                    originalOpponentCells = tmp.opponentCells;
                    found = true;
                }
                it++;
            }
            helpfulnessPQueue.removeIf(p -> p.compareCells(new Pair(0, 0, 0, c)));
            if (B.gameState == MNKGameState.OPEN) {
                if (!opponent){
                    B.markCell(c.i, c.j);
                    if(isWinningCell(c.i, c.j, toCheck)) {
                        markedCellsUsed = markedCellsUsed + 100000;
                        helpfulnessPQueue.add(new Pair(originalCount + 1 + markedCellsUsed, originalPlayerCells, originalOpponentCells, c));
                        B.unmarkCell();
                        return winType.PLAYER;
                    }else B.unmarkCell();
                }
                else {
                    B.markCell(c.i, c.j);
                    if (isWinningCell(c.i, c.j, toCheck)) {
                        markedCellsUsed = markedCellsUsed + 10000;
                        helpfulnessPQueue.add(new Pair(originalCount + 1 + markedCellsUsed, originalPlayerCells, originalOpponentCells, c));
                        B.unmarkCell();
                        return winType.OPPONENT;
                    }else B.unmarkCell();
                }
            }
            if(!opponent){
                if(isDangerous) helpfulnessPQueue.add(new Pair(originalCount + 2000 + markedCellsUsed, originalPlayerCells + markedCellsUsed, originalOpponentCells, c));
                else helpfulnessPQueue.add(new Pair(originalCount + 1 + markedCellsUsed, originalPlayerCells+markedCellsUsed, originalOpponentCells, c));
            }
            else{
                if(isDangerous) helpfulnessPQueue.add(new Pair(originalCount + 1000 + markedCellsUsed, originalPlayerCells, originalOpponentCells + markedCellsUsed, c));
                else helpfulnessPQueue.add(new Pair(originalCount + 1 + markedCellsUsed, originalPlayerCells, originalOpponentCells + markedCellsUsed, c));
            }

        }
        return winType.NONE;
    }

    public boolean isWinningCell(int i, int j, MNKCellState toCheck) {
        MNKCellState s = toCheck;
        int n;

        // Useless pedantic check
        if(s == MNKCellState.FREE) return false;

        // Horizontal check
        n = 1;
        for(int k = 1; j-k >= 0 && B.cellState(i, j-k) == s; k++) n++; // backward check
        for(int k = 1; j+k <  B.N && B.cellState(i,j+k) == s; k++) n++; // forward check
        if(n >= B.K) return true;

        // Vertical check
        n = 1;
        for(int k = 1; i-k >= 0 && B.cellState(i-k,j) == s; k++) n++; // backward check
        for(int k = 1; i+k <  B.M && B.cellState(i+k,j) == s; k++) n++; // forward check
        if(n >= B.K) return true;

        // Diagonal check
        n = 1;
        for(int k = 1; i-k >= 0 && j-k >= 0 && B.cellState(i-k,j-k) == s; k++) n++; // backward check
        for(int k = 1; i+k <  B.M && j+k <  B.N && B.cellState(i+k,j+k) == s; k++) n++; // forward check
        if(n >= B.K) return true;

        // Anti-diagonal check
        n = 1;
        for(int k = 1; i-k >= 0 && j+k < B.N  && B.cellState(i-k,j+k) == s; k++) n++; // backward check
        for(int k = 1; i+k <  B.M && j-k >= 0 && B.cellState(i+k, j-k) == s; k++) n++; // backward check
        if(n >= B.K) return true;

        return false;
    }

    public MNKCell findCell(MNKCell[] cells, int i, int j){
        for(MNKCell c : cells){
            if(c.i == i && c.j == j) return c;
        }
        return null;
    }

    public boolean isFree(MNKCell[] FC, int i, int j){
        for(MNKCell it : FC){
            if(it.i == i && it.j == j) return true;
        }
        return false;
    }

    public boolean isUsable(MNKCell[] MC, MNKCell[] FC, MNKCellState player, int i, int j){
        for(MNKCell it : FC){
            if(it.i == i && it.j == j) return true;
        }
        for(MNKCell it : MC){
            if((it.i == i && it.j == j) && (it.state == MNKCellState.FREE || it.state == player)){
                return true;
            }
        }
        return false;
    }

    public String playerName() {
        return "SmartPlayerTest";
    }
}