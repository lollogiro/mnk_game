package MarkcelloPlayer.BigBoard;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import MarkcelloPlayer.IHeuristicCell;

// IDEA che deve essere valutata:
// se invece di contare quanti pezzi hai,
// ti tieni qualcosa per vedere il numero minimo di mosse per vincere in una direzione?
// Questo serve?
// eg 
// E E X
// E O E
// X E O
// la cella (0, 0) ha distanza di vittoria 2 pezzi, e ha due allineamenti possibili.
// si può fare questa cosa per ogni verso delle 4 direzioni possibili e credo sia semplice
// se mmai ci metti anche un caso in cui serve per vincere uno in mezzo, e lo consideri separato
// non va?
public class HeuristicCell implements IHeuristicCell {
    public final int i;
    public final int j;
    public int index;  // l'index all'interno dell'array della board
    public Value allyValue;
    public Value enemyValue;
    public MNKCellState state;
    private int numAdiacent;
    public static int ADIACENT_MULT = 5;
    private int adjValue;

    public int getAdjents() {
        return numAdiacent;
    }

    public HeuristicCell(int i, int j, int index) {
        this.i = i;
        this.j = j;
        this.index = index;
        allyValue = new Value();
        enemyValue = new Value();
        state = MNKCellState.FREE;
        numAdiacent = 0;
    }
    
    public int getValue() {
        return allyValue.getValue() + enemyValue.getValue();
    }
    public void calcValueWithAdj(boolean isAlly) {
        if(isAlly && allyValue.getValue() >= DirectionValue.DOUBLEPLAY_VAL) {
            adjValue=allyValue.getValue() *2 + enemyValue.getValue() ;
        }else if(!isAlly && enemyValue.getValue() >= DirectionValue.DOUBLEPLAY_VAL){
            adjValue=allyValue.getValue() + enemyValue.getValue() *2 ;
        }else{
            adjValue=allyValue.getValue() + enemyValue.getValue();
        }
        adjValue +=  numAdiacent * ADIACENT_MULT;
    }

    public int getValueWithAdj() {
        return adjValue;
    }

    public void addAdiacent(int v) {
        numAdiacent += v;
    }

    public int compareTo(IHeuristicCell other){
        int res = other.getValueWithAdj() - this.getValueWithAdj();
        if(res == 0) { // ordinato secondo coordinate (solo per avere un ordine preciso)
            res = other.getI() - this.getI();
            if(res== 0)
                res = other.getJ() - this.getJ();
        }
        return res;
    }

    public MNKCell toMNKCell() {
        return new MNKCell(i, j, state);
    }

    @Override
    public int getI() {
        return i;
    }

    @Override
    public int getJ() {
        return j;
    }
}