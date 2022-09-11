package MarkcelloPlayer.BigBoard;

import MarkcelloPlayer.*;

/**
 * Questa classe è un modo per rappresentare il valore di una cella
 * in una board di gioco.
 */
public class Value implements IValue {
    /**
     * 0 = orizzontale
     * 1 = verticale
     * 2 = diagonale maggiore
     * 3 = diagonale minore
     */
    public DirectionValue[] directions;
    private int value;

    Value() {
        directions = new DirectionValue[4];
        for (int i = 0; i < 4; i++) {
            directions[i] = new DirectionValue();
        }
        this.value = 0;
    }

    public int getValue() {
        return value;
    }

    public void updateValue() {
        value = 0;
        int numTwo = 0;
        for (int i = 0; i < 4; i++) {
            int currValue = directions[i].getDirectionValue();

            // se è maggiore significa che ho avuto una double play inline
            // oppure la win a singola mossa, quindi ritorno subito questo come il valore migliroe
            // della cella.
            if (currValue >= DirectionValue.DOUBLEPLAY_VAL) {
                value = currValue;
                break;
            }

            if (directions[i].minStepsToWin() == 2) {
                numTwo++;
            }

            if (numTwo >= 2) {
                value = DirectionValue.DOUBLEPLAY_VAL;  // doppia win dello stesso valore della doppia wininline
                break;
            }

            value += currValue;
        }
    }

    /**
     * Questa funzione ritorna se è possibile fare un doppio gioco mettendo una cella su questa
     * Presuppone che la cella sia ancora libera (altrimenti non so esattamente cosa fa a calcolarsi)
     * 
     * I doppi giochi 'primitivi' sono di due tipi
     * 1. ho due celle libere all'inizio e in fondo
     * 2. Ho una combo in due direzioni differenti.
     */
    public boolean isDoublePlay() {
        return value == DirectionValue.DOUBLEPLAY_VAL;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < 4; i++) {
            s += directions[i].toString() + " ";
        }
        return s;
    }

    public boolean hasOneLeft() {
        return value == DirectionValue.WIN_VAL;
    }
}
