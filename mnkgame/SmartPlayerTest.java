package mnkgame;

import java.util.*;

enum winType {
    PLAYER,
    OPPONENT,
    NONE
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

        //assegnamento dell'upperbound al timeOut dinamico in base al tempo a disposizione per una mossa
        double upperBound = 99.0;
        if(TIMEOUT >= 4) upperBound = 90.0;
        else upperBound = 50.0 + (10 * (TIMEOUT - 1));

        //chiamata alla funzione che restituisce la cella da marcare
        c = calculateHelpfulness(MC, FC, smartPlayerTurn, oppositePlayerTurn, start, upperBound);

        B.markCell(c.i, c.j);

        return c;
    }

    public MNKCell calculateHelpfulness(MNKCell[] MC, MNKCell[] FC, MNKCellState player, MNKCellState opponent, long start, double upperBound) {
        //Dichiarazione MaxPriorityQueue
        PriorityQueue<Cell> helpfulnessPQueue =
                new PriorityQueue<Cell>(Collections.reverseOrder());

        //Fill MaxPriorityQueue
        //Costo: Θ(sommatoria da 0 al numero di elementi in FC di log(n)) dove n è il numero di elementi presenti della coda
        for (MNKCell i : FC) {
            helpfulnessPQueue.add(new Cell(0, 0, 0, i));
        }

        boolean playerVictory = false;
        boolean outOfTime = false;

        //-->MARKED CELL OPPONENT<--
        for (MNKCell i : MC) {

            //controllo se il giocatore ha già vinto
            if(playerVictory) break;

            //controllo se il giocatore supera il TimeOut
            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }

            //ORIZZONTALE CON cella i a sinistra
            horizontalCheck(MC, FC, helpfulnessPQueue, true, opponent, i, true);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }

            //VERTICALE CON cella i in alto
            verticalCheck(MC, FC, helpfulnessPQueue, true, opponent, i, true);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }

            //DIAGONALE PRINCIPALE con cella i in alto a sinistra
            mainDiagonalCheck(MC, FC, helpfulnessPQueue, true, opponent, i, true);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }

            //DIAGONALE SECONDARIA con cella i in alto a destra
            secondDiagonalCheck(MC, FC, helpfulnessPQueue, true, opponent, i, true);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }
        }

        //-->MARKED CELL PLAYER<--

        for (MNKCell i : MC) {

            //controllo se il giocatore ha già vinto
            if(playerVictory) break;

            //controllo se il giocatore supera il TimeOut
            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }

            //ORIZZONTALE CON cella i a sinistra
            horizontalCheck(MC, FC, helpfulnessPQueue, false, player, i, true);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }

            //VERTICALE CON cella i in alto
            verticalCheck(MC, FC, helpfulnessPQueue, false, player, i, true);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }

            //DIAGONALE PRINCIPALE con cella i in alto a sinistra
            mainDiagonalCheck(MC, FC, helpfulnessPQueue, false, player, i, true);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }

            //DIAGONALE SECONDARIA con cella i in alto a destra
            secondDiagonalCheck(MC, FC, helpfulnessPQueue, false, player, i, true);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }
        }

        

        //--> FREE CELL OPPONENT<--

        for (MNKCell i : FC) {

            //controllo se il giocatore ha già vinto
            if(playerVictory) break;

            //controllo se il giocatore supera il TimeOut
            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }

            //ORIZZONTALE CON cella i a sinistra
            horizontalCheck(MC, FC, helpfulnessPQueue, true, opponent, i, false);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }

            //VERTICALE CON cella i in alto
            verticalCheck(MC, FC, helpfulnessPQueue, true, opponent, i, false);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }

            //DIAGONALE PRINCIPALE con cella i in alto a sinistra
            mainDiagonalCheck(MC, FC, helpfulnessPQueue, true, opponent, i, false);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }

            //DIAGONALE SECONDARIA con cella i in alto a destra
            secondDiagonalCheck(MC, FC, helpfulnessPQueue, true, opponent, i, false);

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }
        }

        //-->FREE CELL PLAYER<--
        for (MNKCell i : FC) {
            
            //controllo se il giocatore ha già vinto
            if(playerVictory) break;

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }

            //ORIZZONTALE CON cella i a sinistra
            horizontalCheck(MC, FC, helpfulnessPQueue, false, player, i, false);

            if(helpfulnessPQueue.peek().count >= 10000){
                playerVictory = true;
                break;
            }

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }

            //VERTICALE CON cella i in alto
            verticalCheck(MC, FC, helpfulnessPQueue, false, player, i, false);


            if(helpfulnessPQueue.peek().count >= 10000){
                playerVictory = true;
                break;
            }

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }

            //DIAGONALE PRINCIPALE con cella i in alto a sinistra
            mainDiagonalCheck(MC, FC, helpfulnessPQueue, false, player, i, false);


            if(helpfulnessPQueue.peek().count >= 10000){
                playerVictory = true;
                break;
            }

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }

            //DIAGONALE SECONDARIA con cella i in alto a destra
            secondDiagonalCheck(MC, FC, helpfulnessPQueue, false, player, i, false);

            if(helpfulnessPQueue.peek().count >= 10000){
                playerVictory = true;
                break;
            }

            if((System.currentTimeMillis()-start)/1000.0 > TIMEOUT*(upperBound/100.0)){
                outOfTime = true;
                break;
            }
        }


        
        PriorityQueue<Cell> smartPlayerPQueue = new PriorityQueue<>(helpfulnessPQueue);
        PriorityQueue<Cell> secondPriorityQueue = new PriorityQueue<>(helpfulnessPQueue);

        Cell choosenCell = helpfulnessPQueue.poll();
        Cell otherCell = helpfulnessPQueue.poll();
        Cell anotherCell;

        //controllo quali tra le celle in cima alla coda con priorità ha il numero di playerCells utilizzate maggiore
        while(choosenCell.count.equals(otherCell.count)){
            if(choosenCell.playerCells < otherCell.playerCells)choosenCell = otherCell;
            otherCell = helpfulnessPQueue.poll();
            if(otherCell == null) break;
        }

        anotherCell = secondPriorityQueue.poll();
        otherCell = secondPriorityQueue.poll();

        //controllo quali tra le celle in cima alla coda con priorità ha il numero di opponentCells utilizzate maggiore
        while(anotherCell.count.equals(otherCell.count)){
            if(anotherCell.opponentCells < otherCell.opponentCells) anotherCell = otherCell;
            otherCell = secondPriorityQueue.poll();
            if(otherCell == null) break;
        }
        
        //controllo se il numero di playerCells della cella che ne ha il maggior numero tra quelle con count più grande
        //ne ha di più delle opponentCells della cella che ne ha di più di queste ultime 
        if(choosenCell.playerCells <= anotherCell.opponentCells){
            choosenCell = anotherCell;
        }

        //Print the MaxPriorityQueue to test the algorithm
        /*for (MNKCell i : FC) {
            System.out.println(smartPlayerPQueue.peek().cell.toString() + " " + smartPlayerPQueue.peek().count + " " + smartPlayerPQueue.peek().playerCells + " " + smartPlayerPQueue.poll().opponentCells);
        }
        System.out.println("\n----------------------------------------------\n");
        */

        return choosenCell.cell;
    }

    public void horizontalCheck(MNKCell[] MC, MNKCell[] FC, PriorityQueue<Cell> helpfulnessPQueue, boolean opponent, MNKCellState turnState, MNKCell cell, boolean MCCheck){
        int connectedCell = 1;
        int markedCellsUsed = 0;
        //controllo se la cella su cui sono è già marcata, se si, controllo se è utilizzabile
        if(MCCheck){
            if(isUsable(MC, FC, turnState, cell.i, cell.j)){
                markedCellsUsed++;
            }
            else{
                connectedCell = 0;
            }
        }
        //ciclo che controlla il raggruppamento di K celle
        for (int k = 1; k < B.K; k++) {
            if (cell.j + k >= B.N || connectedCell == 0) break;
            else if (isUsable(MC, FC, turnState, cell.i, cell.j + k)) {
                connectedCell++;
                if (!isFree(FC, cell.i, cell.j + k)) markedCellsUsed++;
            } else break;
        }
        if (connectedCell == B.K) {
            //System.out.println(cell.toString() + " orizzontale con i a sinistra");
            int j;
            if(MCCheck) j = 1;
            else j = 0;
            //Controllo dei doppi giochi
            if(markedCellsUsed == B.K - 2){
                if(isFree(FC, cell.i, cell.j)){
                    if(isFree(FC, cell.i, cell.j + B.K - 1)){
                        //caso in cui la prima e l'ultima cella del raggruppamento sono libere
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
                        //caso in cui la prima cella sia libera, l'utima no, ma quella successiva all'ultima si
                        getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j, true);
                        j=1;
                        for(; j < B.K - 1; j++){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j+j, true);
                        }
                    }
                    else{
                        for(; j < B.K - 1; j++){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j+j, false);
                        }
                    }
                }
                else if(isUsable(MC, FC, turnState, cell.i, cell.j)){
                    if(isFree(FC, cell.i, cell.j - 1)){
                        if(isFree(FC, cell.i, cell.j + B.K - 1)){
                            //caso in cui la prima cella non sia libera, ma la cella precedente e anche l'ultima lo sono
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j + B.K - 1, true);
                            j=1;
                            for(; j < B.K - 1; j++){
                                getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j+j, true);
                            }
                        }
                        else{
                            for(; j < B.K - 1; j++){
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
            else{
                for(; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j+j, false);
                }
                
            }
        }
    }

    public void verticalCheck(MNKCell[] MC, MNKCell[] FC, PriorityQueue<Cell> helpfulnessPQueue, boolean opponent, MNKCellState turnState, MNKCell cell, boolean MCCheck){
        int connectedCell = 1;
        int markedCellsUsed = 0;
        //controllo se la cella su cui sono è già marcata, se si, controllo se è utilizzabile
        if(MCCheck){
            if(isUsable(MC, FC, turnState, cell.i, cell.j)){
                markedCellsUsed++;
            }
            else{
                connectedCell=0;
            }
        }
        //ciclo che controlla il raggruppamento di K celle
        for (int k = 1; k < B.K; k++) {
            if (cell.i + k >= B.M) break;
            else if (isUsable(MC, FC, turnState, cell.i + k, cell.j)) {
                connectedCell++;
                if (!isFree(FC, cell.i + k, cell.j)) markedCellsUsed++;
            } else break;
        }
        if (connectedCell == B.K) {
            //System.out.println(cell.toString() + " verticale con i in alto");
            int j;
            if(MCCheck) j = 1;
            else j = 0;
            //Controllo dei doppi giochi
            if(markedCellsUsed == B.K - 2){
                if(isFree(FC, cell.i, cell.j)){
                    if(isFree(FC, cell.i + B.K - 1, cell.j)){
                        //caso in cui la prima e l'ultima cella del raggruppamento sono libere
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
                        //caso in cui la prima cella sia libera, l'utima no, ma quella successiva all'ultima si
                        getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j, true);
                        j=1;
                        for(; j < B.K - 1; j++){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j, true);
                        }
                    }
                    else{
                        for(; j < B.K - 1; j++){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j, false);
                        }
                    }
                }
                else if(isUsable(MC, FC, turnState, cell.i, cell.j)){
                    if(isFree(FC, cell.i - 1, cell.j)){
                        if(isFree(FC, cell.i + B.K - 1, cell.j)){
                            //caso in cui la prima cella non sia libera, ma la cella precedente e anche l'ultima lo sono
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i + B.K - 1, cell.j, true);
                            j=1;
                            for(; j < B.K - 1; j++){
                                getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j, true);
                            }
                        }
                        else{
                            for(; j < B.K - 1; j++){
                                getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j, false);
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
            else{
                for(; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j, false);
                }
            }
        }
    }

    public void mainDiagonalCheck(MNKCell[] MC, MNKCell[] FC, PriorityQueue<Cell> helpfulnessPQueue, boolean opponent, MNKCellState turnState, MNKCell cell, boolean MCCheck){
        int connectedCell = 1;
        int markedCellsUsed = 0;
        //controllo se la cella su cui sono è già marcata, se si, controllo se è utilizzabile
        if(MCCheck){
            if(isUsable(MC, FC, turnState, cell.i, cell.j)){
                markedCellsUsed++;
            }
            else{
                connectedCell=0;
            }
        }
        //ciclo che controlla il raggruppamento di K celle
        for (int k = 1; k < B.K; k++) {
            if (cell.i + k >= B.M || cell.j + k >= B.N) break;
            else if (isUsable(MC, FC, turnState, cell.i + k, cell.j + k)) {
                connectedCell++;
                if (!isFree(FC, cell.i + k, cell.j + k)) markedCellsUsed++;
            } else break;
        }
        if (connectedCell == B.K) {
            //System.out.println(cell.toString() + " diagonale principale con i in alto a sinistra");
            int j;
            if(MCCheck) j = 1;
            else j = 0;
            //Controllo dei doppi giochi
            if(markedCellsUsed == B.K - 2){
                if(isFree(FC, cell.i, cell.j)){
                    if(isFree(FC, cell.i + B.K - 1, cell.j + B.K - 1)){
                        //caso in cui la prima e l'ultima cella del raggruppamento sono libere
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
                        //caso in cui la prima cella sia libera, l'utima no, ma quella successiva all'ultima si
                        getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j, true);
                        j=1;
                        for(; j < B.K - 1; j++){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j+j, true);
                        }
                    }
                    else{
                        for(; j < B.K - 1; j++){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j+j, false);
                        }
                    }
                }
                else if(isUsable(MC, FC, turnState, cell.i, cell.j)){
                    if(isFree(FC, cell.i- 1, cell.j- 1)){
                        if(isFree(FC, cell.i + B.K - 1, cell.j + B.K - 1)){
                            //caso in cui la prima cella non sia libera, ma la cella precedente e anche l'ultima lo sono
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i + B.K - 1, cell.j + B.K - 1, true);
                            j=1;
                            for(; j < B.K - 1; j++){
                                getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j+j, true);
                            }
                        }
                        else{
                            for(; j < B.K - 1; j++){
                                getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j+j, false);
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
            else{
                for(; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j+j, false);
                }
            }
        }
    }

    public void secondDiagonalCheck(MNKCell[] MC, MNKCell[] FC, PriorityQueue<Cell> helpfulnessPQueue, boolean opponent, MNKCellState turnState, MNKCell cell, boolean MCCheck){
        int connectedCell = 1;
        int markedCellsUsed = 0;
        //controllo se la cella su cui sono è già marcata, se si, controllo se è utilizzabile
        if(MCCheck){
            if(isUsable(MC, FC, turnState, cell.i, cell.j)){
                markedCellsUsed++;
            }
            else{
                connectedCell=0;
            }
        }
        //ciclo che controlla il raggruppamento di K celle
        for (int k = 1; k < B.K; k++) {
            if (cell.i + k >= B.M || cell.j - k < 0) break;
            else if (isUsable(MC, FC, turnState, cell.i + k, cell.j - k)) {
                connectedCell++;
                if (!isFree(FC, cell.i + k, cell.j - k)) markedCellsUsed++;
            } else break;
        }
        if (connectedCell == B.K) {
            //System.out.println(cell.toString() + " diagonale secondaria con i in alto a destra");
            int j;
            if(MCCheck) j = 1;
            else j = 0;
            //Controllo dei doppi giochi
            if(markedCellsUsed == B.K - 2){
                if(isFree(FC, cell.i, cell.j)){
                    if(isFree(FC, cell.i + B.K - 1, cell.j - B.K + 1)){
                        //caso in cui la prima e l'ultima cella del raggruppamento sono libere
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
                        //caso in cui la prima cella sia libera, l'utima no, ma quella successiva all'ultima si
                        getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i, cell.j, true);
                        j=1;
                        for(; j < B.K - 1; j++){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j-j, true);
                        }
                    }
                    else{
                        for(; j < B.K - 1; j++){
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j-j, false);
                        }
                    }
                }
                else if(isUsable(MC, FC, turnState, cell.i, cell.j)){
                    if(isFree(FC, cell.i - 1, cell.j + 1)){
                        if(isFree(FC, cell.i + B.K - 1, cell.j - B.K + 1)){
                            //caso in cui la prima cella non sia libera, ma la cella precedente e anche l'ultima lo sono
                            getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i + B.K - 1, cell.j - B.K + 1, true);
                            j=1;
                            for(; j < B.K - 1; j++){
                                getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j-j, true);
                            }
                        }
                        else{
                            for(; j < B.K - 1; j++){
                                getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j-j, false);
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
            else{
                for(; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, opponent, turnState, markedCellsUsed, cell.i+j, cell.j-j, false);
                }
            }
        }
    }

    //metodo che va ad aggiornare la coda con priorità
    public winType getFreeCellsHelpfulness(MNKCell[] FC, PriorityQueue<Cell> helpfulnessPQueue, boolean opponent, MNKCellState toCheck, int markedCellsUsed, int offsetX, int offsetY, boolean isDangerous) {
        MNKCell c = findCell(FC, offsetX, offsetY);
        //controllo fatto per evitare di scatenare l'exception che non fa marcare una cella non free
        if (c != null) {
            Object[] toUpdate = helpfulnessPQueue.toArray();
            boolean found = false;
            int it = 0, originalCount = 0, originalPlayerCells = 0, originalOpponentCells = 0;
            while (!found) {
                Cell tmp = (Cell) toUpdate[it];
                if (tmp.cell.equals(c)) {
                    originalCount = tmp.count;
                    originalPlayerCells = tmp.playerCells;
                    originalOpponentCells = tmp.opponentCells;
                    found = true;
                }
                it++;
            }
            //metodo per ovviare al fatto che non esiste un metodo update nella classe PriorityQueue
            helpfulnessPQueue.removeIf(p -> p.compareCells(new Cell(0, 0, 0, c)));
            if (B.gameState == MNKGameState.OPEN) {
                if (!opponent){
                    B.markCell(c.i, c.j);
                    //controllo per vedere se la cella fa vincere il player
                    if(isWinningCell(c.i, c.j, toCheck)) {
                        markedCellsUsed = markedCellsUsed + 100000;
                        helpfulnessPQueue.add(new Cell(originalCount + 1 + markedCellsUsed, originalPlayerCells, originalOpponentCells, c));
                        B.unmarkCell();
                        return winType.PLAYER;
                    }
                    else B.unmarkCell();
                }
                //controllo per vedere se la cella fa vincere l'avversario
                else{
                    B.markCell(c.i, c.j);
                    if(isWinningCell(c.i, c.j, toCheck)) {
                        markedCellsUsed = markedCellsUsed + 10000;
                        helpfulnessPQueue.add(new Cell(originalCount + 1 + markedCellsUsed, originalPlayerCells, originalOpponentCells, c));
                        B.unmarkCell();
                        return winType.OPPONENT;
                    }
                    else B.unmarkCell();
                }
            }
            if(!opponent){
                //assegnazione dei valori count in base ai doppi giochi e al numero di markedCells utilizzate dal punto di vista del player
                if(isDangerous) helpfulnessPQueue.add(new Cell(originalCount + 2000 + markedCellsUsed, originalPlayerCells + markedCellsUsed, originalOpponentCells, c));
                else if(markedCellsUsed >= B.K -2) helpfulnessPQueue.add(new Cell(originalCount + B.K + markedCellsUsed, originalPlayerCells+markedCellsUsed, originalOpponentCells, c));
                else helpfulnessPQueue.add(new Cell(originalCount + 1 + markedCellsUsed, originalPlayerCells+markedCellsUsed, originalOpponentCells, c));
            }
            //assegnazione dei valori count in base ai doppi giochi e al numero di markedCells utilizzate dal punto di vista del nemico
            else{
                if(isDangerous) helpfulnessPQueue.add(new Cell(originalCount + 1000 + markedCellsUsed, originalPlayerCells, originalOpponentCells + markedCellsUsed, c));
                else if(markedCellsUsed >= B.K - 2) helpfulnessPQueue.add(new Cell(originalCount + B.K + markedCellsUsed, originalPlayerCells, originalOpponentCells + markedCellsUsed, c));
                else helpfulnessPQueue.add(new Cell(originalCount + 1 + markedCellsUsed, originalPlayerCells, originalOpponentCells + markedCellsUsed, c));
            }
        }
        return winType.NONE;
    }

    //controllo per verificare se l'ultima cella marcata fa vincere il player che ha come MNKCellState toCheck
    public boolean isWinningCell(int i, int j, MNKCellState toCheck) {
        MNKCellState s = toCheck;
        int n;

        if(s == MNKCellState.FREE) return false;

        //controllo sulla linea orizzontale
        n = 1;
        for(int k = 1; j-k >= 0 && B.cellState(i, j-k) == s; k++) n++; // backward check
        for(int k = 1; j+k <  B.N && B.cellState(i,j+k) == s; k++) n++; // forward check
        if(n >= B.K) return true;

        //controllo sulla linea verticale
        n = 1;
        for(int k = 1; i-k >= 0 && B.cellState(i-k,j) == s; k++) n++; // backward check
        for(int k = 1; i+k <  B.M && B.cellState(i+k,j) == s; k++) n++; // forward check
        if(n >= B.K) return true;

        //controllo sulla diagonale principale
        n = 1;
        for(int k = 1; i-k >= 0 && j-k >= 0 && B.cellState(i-k,j-k) == s; k++) n++; // backward check
        for(int k = 1; i+k <  B.M && j+k <  B.N && B.cellState(i+k,j+k) == s; k++) n++; // forward check
        if(n >= B.K) return true;

        //controllo sulla diagonale secondaria
        n = 1;
        for(int k = 1; i-k >= 0 && j+k < B.N  && B.cellState(i-k,j+k) == s; k++) n++; // backward check
        for(int k = 1; i+k <  B.M && j-k >= 0 && B.cellState(i+k, j-k) == s; k++) n++; // backward check
        if(n >= B.K) return true;

        return false;
    }
    
    //se esiste, ritorna l'oggetto MNKCell della cella che ha coordinate i e j, altrimenti null
    public MNKCell findCell(MNKCell[] cells, int i, int j){
        for(MNKCell c : cells){
            if(c.i == i && c.j == j) return c;
        }
        return null;
    }


    //ritorna true se la cella che ha come coordinate i e j è libera
    public boolean isFree(MNKCell[] FC, int i, int j){
        for(MNKCell it : FC){
            if(it.i == i && it.j == j) return true;
        }
        return false;
    }
    
    //ritorna true se la cella che ha come coordinate i e j appartiene al player oppure se è libera
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
        return "MiliardarioRicco";
    }
}