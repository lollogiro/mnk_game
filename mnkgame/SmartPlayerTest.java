package mnkgame;

import java.util.Random;
import java.util.HashSet;
import java.util.PriorityQueue;
//import javafx.util.Pair;
import org.javatuples.Pair;


/**
 * Software player only a bit smarter than random.
 * <p> It can detect a single-move win or loss. In all the other cases behaves randomly.
 * </p>
 */
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

        //Se MC è vuota oppure ha grandezza 1, fare una mossa random per adesso

        FC = B.getFreeCells();
        MC = B.getMarkedCells();

        MNKCellState smartPlayerTurn = first ? MNKCellState.P1 : MNKCellState.P2;

        c = calculateHelpfulness(MC, FC, smartPlayerTurn);
        B.markCell(c.i, c.j);
        return c;
    }

    public MNKCell calculateHelpfulness(MNKCell[] MC, MNKCell[] FC, MNKCellState player){
        //Per ogni pezzo già sulla Board eseguire questa funzione
        //Ricordarsi che le rows vincenti cbe comprendono questa cella sono tante,
        //quindi valutare prima quelle con la cella agli estremi oppure in mezzo.
        //In seguito ogni volta che è possibile vincere aumentare il contatore.
        //Accedere alla matrice di GameState presente nella Board per andare a visualizzare
        //le celle occupate dal player che vogliamo considerare.
        //La matrice valuterà P1, P2 e FREE come valori.


        //TODO: creare coda con priorità massima secondo count come vincolo e metterci dentro le celle
        //TODO: modificare algorimto checkando se stiamo utilizzando freCells collegando la riga

        for(MNKCell i : FC){
            System.out.print(i.toString()+" ");
        }
        System.out.println("\n\n");


        PriorityQueue<Pair<MNKCell, Integer> > helpfulnessPQueue =
                new PriorityQueue<>((a, b) -> b.getKey() - a.getValue());

        for(MNKCell i : FC){
            helpfulnessPQueue.add(new Pair<>(i, 0));
        }

        for(MNKCell i : FC){
            System.out.println(helpfulnessPQueue.remove());
        }



        int connectedCell = 1;
        MNKCell choosenCell = FC[0];
        int maxCount = 0;
        int count = 0;
        for(MNKCell i : FC){
            int startX = i.i;
            int startY = i.j;

            //Per ogni cella andremo a controllare se è possibile in primis vincere mettendo questa cella agli estremi
            connectedCell = 1;
            //ORIZZONTALE CON cella i a sinistra
            for(int k = 1; k < B.K; k++){
                if(startY+k >= B.N) break;
                else if(isUsable(MC, FC, player, startX, startY+k)){
                    connectedCell++;
                }else break;
            }
            if(connectedCell == B.K){
                System.out.println(i.toString()+" orizzontale con i a sinistra");
                count++;
            }
            connectedCell = 1;
            //ORIZZONTALE con cella i a destra
            for(int k = 1; k < B.K; k++){
                if(startY-k < 0) break;
                else if(isUsable(MC, FC, player, startX, startY-k)){
                    connectedCell++;
                }else break;
            }
            if(connectedCell == B.K){
                System.out.println(i.toString()+" orizzontale con i a destra");
                count++;
            }
            connectedCell = 1;
            //VERTICALE CON cella i in alto
            for(int k = 1; k < B.K; k++){
                if(startX+k < 0) break;
                else if(isUsable(MC, FC, player, startX+k, startY)){
                    connectedCell++;
                }else break;
            }
            if(connectedCell == B.K){
                System.out.println(i.toString()+" verticale con i in alto");
                count++;
            }
            connectedCell = 1;
            //ORIZZONTALE con cella i a destra
            for(int k = 1; k < B.K; k++){
                if(startX-k >= B.N) break;
                else if(isUsable(MC, FC, player, startX-k, startY)){
                    connectedCell++;
                }else break;
            }
            if(connectedCell == B.K){
                System.out.println(i.toString()+" verticale con i in basso");
                count++;
            }


            if(count > maxCount){
                maxCount = count;
                choosenCell = i;
            }
            count = 0;
        }
        System.out.println(choosenCell.toString()+" count:"+maxCount);

        System.out.println("\n\n");

        return choosenCell;

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

    public double max(double n, double m){
        if(n > m) return n;
        else return m;
    }

    public double min(double n, double m){
        if(n < m) return n;
        else return m;
    }

    public String playerName() {
        return "SmartPlayerTest";
    }
}