package mnkgame;

import java.util.*;

enum winType {
    PLAYER,
    OPPONENT,
    NONE
}

class Pair implements Comparable<Pair> {
    public Integer count;
    public MNKCell cell;

    public Pair(){
    }

    public Pair(Integer count, MNKCell cell) {
        this.count = count;
        this.cell = cell;
    }

    // Restituisce una voce della mappa (coppia chiave-valore) dai valori specificati
    public static <T, U> Map.Entry<T, U> of(T first, U second) {
        return new AbstractMap.SimpleEntry<>(first, second);
    }

    public int getKey(){
        return count;
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
            smartPlayerPQueue.add(new Pair(-1000, i));
        }

        MNKCellState smartPlayerTurn = first ? MNKCellState.P1 : MNKCellState.P2;
        //MNKCellState oppositePlayerTurn = first ? MNKCellState.P2 : MNKCellState.P1;

        //IMPORTANTE!!!
        //Utilizzato una "euristica casalinga" che somma anche le markeCells utilizzate, questo aumenta la probabilità che
        //l'algoritmo scelga una cella più vicina alla vincita rispetto ad altre, DA MIGLIORARE, cercando un moltiplicatore
        //variabile da moltiplicare alle markedCellsUsed, modificare i valori studiando meglio le varie situazioni.

        c = calculateHelpfulness(MC, FC, smartPlayerTurn);


        B.markCell(c.i, c.j);
        return c;
    }

    public MNKCell calculateHelpfulness(MNKCell[] MC, MNKCell[] FC, MNKCellState player) {
        //Test printing real-time FC
//        for(MNKCell i : FC){
//            System.out.print(i.toString()+" ");
//        }
//        System.out.println("\n\n");


        //Dichiarazione MaxPriorityQueue
        PriorityQueue<Pair> helpfulnessPQueue =
                new PriorityQueue<Pair>(Collections.reverseOrder());

        //Fill MaxPriorityQueue
        for (MNKCell i : FC) {
            helpfulnessPQueue.add(new Pair(0, i));
        }

        //Calculating Helpfulness updating the MaxPriorityQueue considering K rows with Free Cells at the start and or the end of the row
        int connectedCell = 1, markedCellsUsed = 0;
        for (MNKCell i : FC) {
            int startX = i.i;
            int startY = i.j;

            //ORIZZONTALE CON cella i a sinistra
            connectedCell = 1;
            for (int k = 1; k < B.K; k++) {
                if (startY + k >= B.N) break;
                else if (isUsable(MC, FC, player, startX, startY + k)) {
                    connectedCell++;
                    if (!isFree(FC, startX, startY + k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " orizzontale con i a sinistra");
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX, startY) == winType.PLAYER){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX, startY+j);
                }
            }
            System.out.println("MarkedCELL+"+markedCellsUsed+"\n");

            //ORIZZONTALE con cella i a destra
            /*
            connectedCell = 1;
            markedCellsUsed = 0;
            for (int k = 1; k < B.K; k++) {
                if (startY - k < 0) break;
                else if (isUsable(MC, FC, player, startX, startY - k)) {
                    connectedCell++;
                    if (!isFree(FC, startX, startY - k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " orizzontale con i a destra");
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX, startY) == winType.PLAYER){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX, startY-j);
                }
            }
            System.out.println("MarkedCELL+"+markedCellsUsed+"\n");
            */


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
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX, startY) == winType.PLAYER){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX+j, startY);
                }
            }
            System.out.println("MarkedCELL+"+markedCellsUsed+"\n");

            //VERTICALE con cella i in basso
            /*
            connectedCell = 1;
            markedCellsUsed = 0;
            for (int k = 1; k < B.K; k++) {
                if (startX - k < 0) break;
                else if (isUsable(MC, FC, player, startX - k, startY)) {
                    connectedCell++;
                    if (!isFree(FC, startX - k, startY)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " verticale con i in basso");
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX, startY) == winType.PLAYER){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX-j, startY);
                }
            }
            System.out.println("MarkedCELL+"+markedCellsUsed+"\n");
            */

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
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX, startY) == winType.PLAYER){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX+j, startY+j);
                }
            }
            System.out.println("MarkedCELL+"+markedCellsUsed+"\n");

            //DIAGONALE PRINCIPALE con cella i in basso a destra
            /*
            connectedCell = 1;
            markedCellsUsed = 0;
            for (int k = 1; k < B.K; k++) {
                if (startX - k < 0 || startY - k < 0) break;
                else if (isUsable(MC, FC, player, startX - k, startY - k)) {
                    connectedCell++;
                    if (!isFree(FC, startX - k, startY - k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " diagonale principale con i in basso a destra");
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX, startY) == winType.PLAYER){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX-j, startY-j);
                }
            }
            System.out.println("MarkedCELL+"+markedCellsUsed+"\n");
             */


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
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX, startY) == winType.PLAYER){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX+j, startY-j);
                }
            }
            System.out.println("MarkedCELL+"+markedCellsUsed+"\n");


            //DIAGONALE SECONDARIA con cella i in basso a sinistra
            /*
            connectedCell = 1;
            markedCellsUsed = 0;
            for (int k = 1; k < B.K; k++) {
                if (startX - k < 0 || startY + k >= B.N) break;
                else if (isUsable(MC, FC, player, startX - k, startY + k)) {
                    connectedCell++;
                    if (!isFree(FC, startX - k, startY + k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " diagonale secondaria con i in basso a sinistra");
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX, startY) == winType.PLAYER){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX-j, startY+j);
                }
            }
            System.out.println("MarkedCELL+"+markedCellsUsed+"\n");
            */
        }

        //-------------------------------------OPPONENT

        //Calculating Helpfulness updating the MaxPriorityQueue considering K rows with Free Cells at the start and or the end of the row
        connectedCell = 1;
        markedCellsUsed = 0;
        for (MNKCell i : FC) {
            int startX = i.i;
            int startY = i.j;

            //ORIZZONTALE CON cella i a sinistra
            connectedCell = 1;
            for (int k = 1; k < B.K; k++) {
                if (startY + k >= B.N) break;
                else if (isUsable(MC, FC, player, startX, startY + k)) {
                    connectedCell++;
                    if (!isFree(FC, startX, startY + k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " orizzontale con i a sinistra");
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, markedCellsUsed, startX, startY) == winType.OPPONENT){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, markedCellsUsed, startX, startY+j);
                }
            }
            System.out.println("MarkedCELL+"+markedCellsUsed+"\n");

            //ORIZZONTALE con cella i a destra
            /*
            connectedCell = 1;
            markedCellsUsed = 0;
            for (int k = 1; k < B.K; k++) {
                if (startY - k < 0) break;
                else if (isUsable(MC, FC, player, startX, startY - k)) {
                    connectedCell++;
                    if (!isFree(FC, startX, startY - k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " orizzontale con i a destra");
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX, startY) == winType.PLAYER){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX, startY-j);
                }
            }
            System.out.println("MarkedCELL+"+markedCellsUsed+"\n");
            */


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
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, markedCellsUsed, startX, startY) == winType.OPPONENT){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, markedCellsUsed, startX+j, startY);
                }
            }
            System.out.println("MarkedCELL+"+markedCellsUsed+"\n");

            //VERTICALE con cella i in basso
            /*
            connectedCell = 1;
            markedCellsUsed = 0;
            for (int k = 1; k < B.K; k++) {
                if (startX - k < 0) break;
                else if (isUsable(MC, FC, player, startX - k, startY)) {
                    connectedCell++;
                    if (!isFree(FC, startX - k, startY)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " verticale con i in basso");
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX, startY) == winType.PLAYER){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX-j, startY);
                }
            }
            System.out.println("MarkedCELL+"+markedCellsUsed+"\n");
            */

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
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, markedCellsUsed, startX, startY) == winType.OPPONENT){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, markedCellsUsed, startX+j, startY+j);
                }
            }
            System.out.println("MarkedCELL+"+markedCellsUsed+"\n");

            //DIAGONALE PRINCIPALE con cella i in basso a destra
            /*
            connectedCell = 1;
            markedCellsUsed = 0;
            for (int k = 1; k < B.K; k++) {
                if (startX - k < 0 || startY - k < 0) break;
                else if (isUsable(MC, FC, player, startX - k, startY - k)) {
                    connectedCell++;
                    if (!isFree(FC, startX - k, startY - k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " diagonale principale con i in basso a destra");
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX, startY) == winType.PLAYER){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX-j, startY-j);
                }
            }
            System.out.println("MarkedCELL+"+markedCellsUsed+"\n");
             */


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
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, markedCellsUsed, startX, startY) == winType.OPPONENT){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, true, markedCellsUsed, startX+j, startY-j);
                }
            }
            System.out.println("MarkedCELL+"+markedCellsUsed+"\n");


            //DIAGONALE SECONDARIA con cella i in basso a sinistra
            /*
            connectedCell = 1;
            markedCellsUsed = 0;
            for (int k = 1; k < B.K; k++) {
                if (startX - k < 0 || startY + k >= B.N) break;
                else if (isUsable(MC, FC, player, startX - k, startY + k)) {
                    connectedCell++;
                    if (!isFree(FC, startX - k, startY + k)) markedCellsUsed++;
                } else break;
            }
            if (connectedCell == B.K) {
                System.out.println(i.toString() + " diagonale secondaria con i in basso a sinistra");
                if (getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX, startY) == winType.PLAYER){
                    break;
                }
                for(int j = 1; j < B.K; j++){
                    getFreeCellsHelpfulness(FC, helpfulnessPQueue, false, markedCellsUsed, startX-j, startY+j);
                }
            }
            System.out.println("MarkedCELL+"+markedCellsUsed+"\n");
            */
        }



        Pair choosenCell = helpfulnessPQueue.peek();


        System.out.println("\n" + choosenCell.cell.toString() + " count:" + choosenCell.count + "\n");

        //Print the MaxPriorityQueue to test the algorithm
        for (MNKCell i : FC) {
            System.out.println(helpfulnessPQueue.peek().cell.toString() + " " + helpfulnessPQueue.poll().count);
        }

        System.out.println("\n----------------------------------------------\n");


        //return helpfulnessPQueue;
        return choosenCell.cell;
    }

    public winType getFreeCellsHelpfulness(MNKCell[] FC, PriorityQueue<Pair> helpfulnessPQueue, boolean opponent, int markedCellsUsed, int offsetX, int offsetY) {
        MNKCell c = findCell(FC, offsetX, offsetY);
        if (c != null) {
            Object[] toUpdate = helpfulnessPQueue.toArray();
            boolean found = false;
            int it = 0, originalCount = 0;
            while (!found) {
                Pair tmp = (Pair) toUpdate[it];
                if (tmp.cell.equals(c)) {
                    originalCount = tmp.count;
                    found = true;
                }
                it++;
            }
            helpfulnessPQueue.removeIf(p -> p.compareCells(new Pair(0, c)));
            if (B.gameState == MNKGameState.OPEN) {
                B.markCell(c.i, c.j);
                if (B.isWinningCell(c.i, c.j) && !opponent){
                    markedCellsUsed = markedCellsUsed + 10000;
                    helpfulnessPQueue.add(new Pair(originalCount + 1 + markedCellsUsed, c));
                    B.unmarkCell();
                    return winType.PLAYER;
                }
                else if (B.isWinningCell(c.i, c.j) && opponent){
                    markedCellsUsed = markedCellsUsed + 1000;
                    helpfulnessPQueue.add(new Pair(originalCount + 1 + markedCellsUsed, c));
                    B.unmarkCell();
                    return winType.OPPONENT;
                }else B.unmarkCell();
            }
//            System.out.println("MarkedCellsUsed:"+markedCellsUsed);
            helpfulnessPQueue.add(new Pair(originalCount + 1 + markedCellsUsed, c));
        }
        return winType.NONE;
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
//                System.out.println("MC: "+it.toString());
//                System.out.println("\n\n");
                return true;
            }
        }
        return false;
    }

    public String playerName() {
        return "SmartPlayerTest";
    }
}