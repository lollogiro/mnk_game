# Relazione progetto *m-n-k game*

A questo progetto hanno partecipato gli studenti:

Lorenzo Girotti 0001020884

Simone Ruggiero 0001021768

## Documentazione utilizzata

Per realizzare questo progetto ci siamo basati su una documentazione specifica: 

*"Developing a Memory Efficient Algorithm for Playing m, n, k Games" di Nathaniel Hayes e Teig Loge*

Questa documentazione spiega le basi dietro un algoritmo pensato per non perdere mai, per fare questo si basa sul dare un valore alle celle libere in base alla loro importanza nel presente, tenendo conto di possibili mosse future.

Il valore di una cella, chiamato anche *helpfulness*, è dato dalla somma di due ulteriori valori:

- numero di possibili vincite che comprendono quella cella;

- numero di celle già marcate dal giocatore utilizzate per calcolare il punto precedente.

La cella che alla fine avrà il valore maggiore sarà quella che, al momento, costituisce la mossa più redditizia e quindi quella da scegliere durante il gioco.

Per avere un calcolo più accurato dell'helpfulness, nella documentazione viene consigliato di calcolare il valore di importanza di una cella, sia dal lato del nostro giocatore, sia dal lato del giocatore avversario, così da poter bloccare anche delle possibili mosse vincenti da parte del nostro avversario.

## Implementazione

Per la realizzazione di questo algoritmo abbiamo pensato di salvare i valori dell'helpfulness delle varie celle all'interno di una coda con priorità massima, questa scelta è stata fatta per avere sempre a portata di mano,  in cima alla coda, la cella con il valore dell'helpfulness maggiore.

Per il calcolo dell'helpfulness abbiamo definito due funzioni principali: calculateHelpfulness la quale scorre, prima tutte le celle già marcate e poi tutte le celle libere, per vedere quali celle possono costituire una vincita. A questo punto, dopo ogni controllo andato a buon fine, entra in gioco la funzione getFreeCellsHelpfulness. Questa funzione si occupa di calcolare il valore che verrà assegnato alla cella all'interno della coda.

Parlare di quanto viene assegnato in caso di vincita del player e in caso di cella che porta ad una sconfitta.



## Ulteriori modifiche

Parlare delle varie modifiche apportate all'algoritmo delle documentazione per migliorarlo.

Aggiunta di valori particolari per celle isDangerous e per celle coinvolte in vincite temporanee con k-2 celle.





## Costo dell'algoritmo



## Modifiche possibili per migliorare l'algoritmo


