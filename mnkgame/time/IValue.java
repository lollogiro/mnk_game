package MarkcelloPlayer;

/**
 * Questa classe è un modo per rappresentare il valore di una cella
 * in una board di gioco.
 */
public interface IValue {

    public int getValue();

    public void updateValue();

    public boolean isDoublePlay();

    public String toString();

    public boolean hasOneLeft();
}
