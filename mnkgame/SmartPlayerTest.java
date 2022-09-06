package mnkgame;

import com.sun.security.jgss.GSSUtil;

import java.util.*;

enum winType {
    PLAYER,
    OPPONENT,
    NONE
}

class Pair implements Comparable<Pair> {
    public Integer count;
    public Integer playerCells;
    public MNKCell cell;

    public Pair(){
        count = null;
        playerCells = null;
        cell = null;
    }

    public Pair(Integer count, Integer playerCells, MNKCell cell) {
        this.count = count;
        this.playerCells = playerCells;
        this.cell = cell;
    }


    // Restituisce una voce della mappa (coppia chiave-valore) dai valori specificati
    public static <T, U> Map.Entry<T, U> of(T first, U second) {
        return new AbstractMap.SimpleEntry<>(first, second);
    }

//    public int getKey(){
//        return count;
//    }

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

        //Se si parte secondo, FORSE è possibile fare K-1 mosse random senza perdere, questo potrebbe ovviare a problemi
        //di velocità dell'alagoritmo con tabelle di grandi dimensioni

        //Su grandi dimensioni, da 8x8 l'algoritmo è un pò lento, ma soprattutto non vince mai, una soluzione potrebbe
        //essere quella di eseguire l'algoritmo su 4 parti della tabella indipendentemente, questo finché il numero di
        //celle da valutare non si abbassa fino ad un certo lower bound, es. fino a 25 celle libere!!!, prima eseguendo
        //l'algoritmo sulla porzione in alto a destra, in alto a sinistra, in basso a destra, in basso a sinistra
        //indipendentemente. Magari funziona meglio e riesce a fare della giocate migliori.

        //Fatto per sicurezza
        FC = B.getFreeCells();
        MC = B.getMarkedCells();

        //Declaration and implementation MaxPriorityQueue
        PriorityQueue<Pair> smartPlayerPQueue =
                new PriorityQueue<Pair>(Collections.reverseOrder());

        //Fill MaxPriorityQueue
        for(MNKCell i : FC){
            smartPlayerPQueue.add(new Pair(0, 0, i));
        }

        MNKCellState smartPlayerTurn = first ? MNKCellState.P1 : MNKCellState.P2;
        MNKCellState oppositePlayerTurn = first ? MNKCellState.P2 : MNKCellState.P1;

        c = calculateHelpfulness(MC, FC, smartPlayerTurn, oppositePlayerTurn);


        B.markCell(c.i, c.j);
        return c;
    }

    public MNKCell calculateHelpfulness(MNKCell[] MC, MNKCell[] FC, MNKCellState player, MNKCellState opponent) {
        //Dichiarazione MaxPriorityQueue
        PriorityQueue<Pair> helpfulnessPQueue =
                new PriorityQueue<Pair>(Collections.reverseOrder());

        //Fill MaxPriorityQueue
        for (MNKCell i : FC) {
            helpfulnessPQueue.add(new Pair(0, 0, i));
        }

        boolean playerVictory = false;

        //Calculating Helpfulness updating the MaxPriorityQueue considering K rows with Free Cells at the start and or the end of the row
        int connectedCell = 1, markedCellsUsed = 0;
        for (MNKCell i : FC) {
            int startX = i.i;
            int startY = i.j;

            //ORIZZONTALE CON cella i a sinistra
            connectedCell = 1;
            markedCellsUsed = 0;
            for (int k = 1; k < B.K; k++) {
                if (startY + k >= B.N) break;
                else if (isUsable(MC, FC, player, startX, startY + k)) {
                    connectedCell++;
                    if (!isFree(FC, startX, startY + k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " orizzontale con i a sinistra");
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, player, markedCellsUsed, startX, startY) == winType.PLAYER){
                    playerVictory = true;
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, player, markedCellsUsed, startX, startY+j);
                }
            }

            //VERTICALE CON cella i in alto
            connectedCell = 1;
            markedCellsUsed = 0;
            for (int k = 1; k < B.K; k++) {
                if (startX + k >= B.M) break;
                else if (isUsable(MC, FC, player, startX + k, startY)) {
                    connectedCell++;
                    if (!isFree(FC, startX + k, startY)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " verticale con i in alto");
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, player, markedCellsUsed, startX, startY) == winType.PLAYER){
                    playerVictory = true;
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, player, markedCellsUsed, startX+j, startY);
                }
            }

            //DIAGONALE PRINCIPALE con cella i in alto a sinistra
            connectedCell = 1;
            markedCellsUsed = 0;
            for (int k = 1; k < B.K; k++) {
                if (startX + k >= B.M || startY + k >= B.N) break;
                else if (isUsable(MC, FC, player, startX + k, startY + k)) {
                    connectedCell++;
                    if (!isFree(FC, startX + k, startY + k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " diagonale principale con i in alto a sinistra");
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, player, markedCellsUsed, startX, startY) == winType.PLAYER){
                    playerVictory = true;
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, player, markedCellsUsed, startX+j, startY+j);
                }
            }

            //DIAGONALE SECONDARIA con cella i in alto a destra
            connectedCell = 1;
            markedCellsUsed = 0;
            for (int k = 1; k < B.K; k++) {
                if (startX + k >= B.M || startY - k < 0) break;
                else if (isUsable(MC, FC, player, startX + k, startY - k)) {
                    connectedCell++;
                    if (!isFree(FC, startX + k, startY - k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " diagonale secondaria con i in alto a destra");
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, player, markedCellsUsed, startX, startY) == winType.PLAYER){
                    playerVictory = true;
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, player, markedCellsUsed, startX+j, startY-j);
                }
            }
        }

        // CONTROLLO MARKED CELL PLAYER

        for (MNKCell i : MC) {
            int startX = i.i;
            int startY = i.j;

            if(playerVictory) break;

            //ORIZZONTALE CON cella i a sinistra
            connectedCell = 0;
            markedCellsUsed = 0;
            for (int k = 0; k < B.K; k++) {
                if (startY + k >= B.N) break;
                else if (isUsable(MC, FC, player, startX, startY + k)) {
                    connectedCell++;
                    if (!isFree(FC, startX, startY + k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " orizzontale con i a sinistra");
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, player, markedCellsUsed, startX, startY+j);
                }
            }

            //VERTICALE CON cella i in alto
            connectedCell = 0;
            markedCellsUsed = 0;
            for (int k = 0; k < B.K; k++) {
                if (startX + k >= B.M) break;
                else if (isUsable(MC, FC, player, startX + k, startY)) {
                    connectedCell++;
                    if (!isFree(FC, startX + k, startY)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " verticale con i in alto");
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, player, markedCellsUsed, startX+j, startY);
                }
            }

            //DIAGONALE PRINCIPALE con cella i in alto a sinistra
            connectedCell = 0;
            markedCellsUsed = 0;
            for (int k = 0; k < B.K; k++) {
                if (startX + k >= B.M || startY + k >= B.N) break;
                else if (isUsable(MC, FC, player, startX + k, startY + k)) {
                    connectedCell++;
                    if (!isFree(FC, startX + k, startY + k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " diagonale principale con i in alto a sinistra");
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, player, markedCellsUsed, startX+j, startY+j);
                }
            }

            //DIAGONALE SECONDARIA con cella i in alto a destra
            connectedCell = 0;
            markedCellsUsed = 0;
            for (int k = 0; k < B.K; k++) {
                if (startX + k >= B.M || startY - k < 0) break;
                else if (isUsable(MC, FC, player, startX + k, startY - k)) {
                    connectedCell++;
                    if (!isFree(FC, startX + k, startY - k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " diagonale secondaria con i in alto a destra");
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, player, markedCellsUsed, startX+j, startY-j);
                }
            }
        }

        //-------------------------------------OPPONENT

        //Calculating Helpfulness updating the MaxPriorityQueue considering K rows with Free Cells at the start and or the end of the row

        connectedCell = 1;
        markedCellsUsed = 0;
        for (MNKCell i : FC) {
            int startX = i.i;
            int startY = i.j;

            if(playerVictory) break;

            //ORIZZONTALE CON cella i a sinistra
            connectedCell = 1;
            for (int k = 1; k < B.K; k++) {
                if (startY + k >= B.N) break;
                else if (isUsable(MC, FC, opponent, startX, startY + k)) {
                    connectedCell++;
                    if (!isFree(FC, startX, startY + k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " orizzontale con i a sinistra");
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, opponent, markedCellsUsed, startX, startY) == winType.OPPONENT){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, opponent, markedCellsUsed, startX, startY+j);
                }
            }

            //VERTICALE CON cella i in alto
            connectedCell = 1;
            markedCellsUsed = 0;
            for (int k = 1; k < B.K; k++) {
                if (startX + k >= B.M) break;
                else if (isUsable(MC, FC, opponent, startX + k, startY)) {
                    connectedCell++;
                    if (!isFree(FC, startX + k, startY)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " verticale con i in alto");
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, opponent, markedCellsUsed, startX, startY) == winType.OPPONENT){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, opponent, markedCellsUsed, startX+j, startY);
                }
            }

            //DIAGONALE PRINCIPALE con cella i in alto a sinistra
            connectedCell = 1;
            markedCellsUsed = 0;
            for (int k = 1; k < B.K; k++) {
                if (startX + k >= B.M || startY + k >= B.N) break;
                else if (isUsable(MC, FC, opponent, startX + k, startY + k)) {
                    connectedCell++;
                    if (!isFree(FC, startX + k, startY + k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " diagonale principale con i in alto a sinistra");
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, opponent, markedCellsUsed, startX, startY) == winType.OPPONENT){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, opponent, markedCellsUsed, startX+j, startY+j);
                }
            }

            //DIAGONALE SECONDARIA con cella i in alto a destra
            connectedCell = 1;
            markedCellsUsed = 0;
            for (int k = 1; k < B.K; k++) {
                if (startX + k >= B.M || startY - k < 0) break;
                else if (isUsable(MC, FC, opponent, startX + k, startY - k)) {
                    connectedCell++;
                    if (!isFree(FC, startX + k, startY - k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " diagonale secondaria con i in alto a destra");
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, opponent, markedCellsUsed, startX, startY) == winType.OPPONENT){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, opponent, markedCellsUsed, startX+j, startY-j);
                }
            }
        }

        // CONTROLLO MARKED CELL OPPONENT

        for (MNKCell i : MC) {
            int startX = i.i;
            int startY = i.j;

            if(playerVictory) break;

            //ORIZZONTALE CON cella i a sinistra
            connectedCell = 0;
            markedCellsUsed = 0;
            for (int k = 0; k < B.K; k++) {
                if (startY + k >= B.N) break;
                else if (isUsable(MC, FC, opponent, startX, startY + k)) {
                    connectedCell++;
                    if (!isFree(FC, startX, startY + k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " orizzontale con i a sinistra");
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, opponent, markedCellsUsed, startX, startY+j);
                }
            }

            //VERTICALE CON cella i in alto
            connectedCell = 0;
            markedCellsUsed = 0;
            for (int k = 0; k < B.K; k++) {
                if (startX + k >= B.M) break;
                else if (isUsable(MC, FC, opponent, startX + k, startY)) {
                    connectedCell++;
                    if (!isFree(FC, startX + k, startY)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " verticale con i in alto");
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, opponent, markedCellsUsed, startX+j, startY);
                }
            }

            //DIAGONALE PRINCIPALE con cella i in alto a sinistra
            connectedCell = 0;
            markedCellsUsed = 0;
            for (int k = 0; k < B.K; k++) {
                if (startX + k >= B.M || startY + k >= B.N) break;
                else if (isUsable(MC, FC, opponent, startX + k, startY + k)) {
                    connectedCell++;
                    if (!isFree(FC, startX + k, startY + k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " diagonale principale con i in alto a sinistra");
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, opponent, markedCellsUsed, startX+j, startY+j);
                }
            }

            //DIAGONALE SECONDARIA con cella i in alto a destra
            connectedCell = 0;
            markedCellsUsed = 0;
            for (int k = 0; k < B.K; k++) {
                if (startX + k >= B.M || startY - k < 0) break;
                else if (isUsable(MC, FC, opponent, startX + k, startY - k)) {
                    connectedCell++;
                    if (!isFree(FC, startX + k, startY - k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " diagonale secondaria con i in alto a destra");
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, opponent, markedCellsUsed, startX+j, startY-j);
                }
            }
        }

        PriorityQueue<Pair> smartPlayerPQueue = new PriorityQueue<>(helpfulnessPQueue);

        Pair choosenCell = helpfulnessPQueue.poll();
        Pair otherCell = helpfulnessPQueue.poll();

        //SI INTERROMPE QUI ALLA SECONDA PARTITA NEL 3x3
        while(choosenCell.count == otherCell.count){
            System.out.println(choosenCell.cell+ " " + otherCell.cell);
            if(choosenCell.playerCells < otherCell.playerCells) choosenCell = otherCell;
            otherCell = helpfulnessPQueue.poll();
            if(otherCell == null) break;
        }

        System.out.println("\n" + choosenCell.cell.toString() + " count:" + choosenCell.count + " playerCell:" + choosenCell.playerCells +"\n");

        //Print the MaxPriorityQueue to test the algorithm
        for (MNKCell i : FC) {
            System.out.println(smartPlayerPQueue.peek().cell.toString() + " " + smartPlayerPQueue.peek().count + " " + smartPlayerPQueue.poll().playerCells);
        }

        System.out.println("\n----------------------------------------------\n");

        return choosenCell.cell;
    }

    public winType getFreeCellsHelpfulness(MNKCell[] FC, PriorityQueue<Pair> helpfulnessPQueue, boolean opponent, MNKCellState toCheck, int markedCellsUsed, int offsetX, int offsetY) {
        MNKCell c = findCell(FC, offsetX, offsetY);
        if (c != null) {
            Object[] toUpdate = helpfulnessPQueue.toArray();
            boolean found = false;
            int it = 0, originalCount = 0, originalPlayerCells = 0;
            while (!found) {
                Pair tmp = (Pair) toUpdate[it];
                if (tmp.cell.equals(c)) {
                    originalCount = tmp.count;
                    originalPlayerCells = tmp.playerCells;
                    found = true;
                }
                it++;
            }
            helpfulnessPQueue.removeIf(p -> p.compareCells(new Pair(0, 0, c)));
            if (B.gameState == MNKGameState.OPEN) {
                if (!opponent){
                    B.markCell(c.i, c.j);
                    if(isWinningCell(c.i, c.j, toCheck)) {
                        markedCellsUsed = markedCellsUsed + 10000;
                        helpfulnessPQueue.add(new Pair(originalCount + 1 + markedCellsUsed, originalPlayerCells, c));
                        B.unmarkCell();
                        return winType.PLAYER;
                    }else B.unmarkCell();
                }
                else {
                    B.markCell(c.i, c.j);
                    if (isWinningCell(c.i, c.j, toCheck)) {
                        markedCellsUsed = markedCellsUsed + 1000;
                        helpfulnessPQueue.add(new Pair(originalCount + 1 + markedCellsUsed, originalPlayerCells, c));
                        B.unmarkCell();
                        return winType.OPPONENT;
                    }else B.unmarkCell();
                }
            }
            if(!opponent) helpfulnessPQueue.add(new Pair(originalCount + 1 + markedCellsUsed, originalPlayerCells+markedCellsUsed, c));
            else helpfulnessPQueue.add(new Pair(originalCount + 1 + markedCellsUsed, originalPlayerCells, c));
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