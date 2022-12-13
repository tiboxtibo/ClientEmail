**Possibili DOMANDE Esame:**

1- Come avviene la creazione del server?

    Io inizialmente avvio il file ServerGUI.java che farà il launch() (ovvero esegue il metodo start), il quale crea la
    la scena del server che è collegata al ServerController
    Il ServerController eseguirà inizialmente il metodo di Inizialize che crea una thread dove al suo interno chiama
    socketThreadStart()
        tale metodo crea un ServerSocket su una porta scelta a caso che non vada ad interferire con altri processi
        Poi andrà in un loop infinito con while(true) che servirà a gestire i client che fanno richiesta di connessione
        -> tale metodo si bloccherà ad s.accept() poichè aspetterà che un client faccia richiesta al server
        Quando il client fa richiesta esso accetta tale richiesta e prosegue andando a creare un thread
        ->all'interno del thread c'è il metodo Platform.runLater()

            Quando un thread aggiorna la UI (User Interface) tale update deve essere eseguito nel main thread 
            (che esegue il metodo principale del programma e aggiorna gli elementi dell'interfaccia utente)
            per questo motivo ho usato il metodo Platform.runLater(() -> {}) per poter modificare la scena da ogni thread
        
            RunLater viene usato come se fosse un sistema di coda, quindi metterà il thread in coda e lo eseguirà non appena
            il main thread potrà
            Questa tecnica è particolarmente usata nelle applicazioni di JavaFX poichè l'applicazione è il main Thread
          
            Esso significa che verrà eseguito ad un tempo indefinito nel futuro, di solito esso viene eseguito immediatamente
            a meno che il main thread non sia occupato, in questo caso il thread aspetterà il suo turno
        
        All'interno di Platform.runLater richiamiamo invece il ThreadHandler a cui passiamo il numero del socket e il socket
            All'interno di esso troviamo nuovamente un while(true) che ci servirà per poter leggere l'inputStream del Socket 
            ed in base a ciò che i client hanno scritto all'interno del outputStream eseguirò delle operazioni:
                1)Login
                2)Richiesta di mail
                3)Richiesta di invio di una mail
                4)Richiesta di eliminare una mail
            Andando a richiamare ogni volta il FileQuery per poter scrivere/leggere i dati dentro i file .json


2- Come avviene la connessione del client al server?

    Per descrivere come avviene la connessione dal client al server utilizzo un esempio, ovvero come avviene il Login:
    Il Client viene avviato da CLientGui che esegue il main che con launch() esegue il metodo start() il quale crea la 
    scene del Login la quale è direttamente collegata con il LoginController.
    Esso contiene l'handelr del bottone di login, ovvero il LoginClick:
        esso controlla al suo interno se la mail è nel formato corretto e in quel caso segue CLientMethods.login
    Il ClientsMethods chiama il metodo di login nel quale aprirà una nuova connessione al socket; poi scriverà nell'outputStream
    la coppia (1,mail-password) la quale contiene l'istruzione da far eseguire al ThreadHandler del ServerController.
    infatti poi il threadHandler che esegue un while infinito controlla l'istruzione scritta nell'outputStream e fa lo switch 
    con 1 -> e scriverà a sua volta nell'outputStream o false o user a seconda della risposta ottenuta interrogando 
    il FileQuery.getUserId(user-password)
    Quindi il ClientsMethods.login leggerà l'inputStream e a seconda di cosa ha scritto il server o restituisce errore o
    cambia la scena al MailView; infine chiude il socket;

4- Come avviene la sincronizzazione tra thread?
    
    La sincronizzazione viene fatta utilizzando il metodo Platform.runLater(), poichè inserendo tutto il contenuto del
    thread all'interno di tale metodo ogni operazione verrà svolta come se fosse stata eseguita dal mainThread così anche
    da permettere la modifica della scena da ogni thread.
    RunLater viene usato come se fosse un sistema di coda, e quindi ogni operazione si mette in coda e attende che il 
    main thread sia libero;

    Un altro tipo di sincronizzazione avviene nel FileQuery mediante l'utilizzo del ReentrantReadWriteLock che permette
    l'accesso in mutua esclusione alla lettura e scrittura dei file (in questo caso dei file Json dentro i quali vengono
    salvate le mail)
        readWriteLock.writeLock().lock() -> per acquisire il lock di write
        readWriteLock.writeLock().unlock() -> per liberare il lock di write
    questo è necessario poichè andando a generare più thread c'è la possibilità che due o più thread modifichino il file 
    contemporaneamente andando a generare errori

5- Come avviene il login?

     Il Client viene avviato da CLientGui che esegue il main che con launch() esegue il metodo start() il quale crea la 
    scene del Login la quale è direttamente collegata con il LoginController.
    Esso contiene l'handelr del bottone di login, ovvero il LoginClick:
        esso controlla al suo interno se la mail è nel formato corretto e in quel caso segue CLientMethods.login
    Il ClientsMethods chiama il metodo di login nel quale aprirà una nuova connessione al socket; poi scriverà nell'outputStream
    la coppia (1,mail-password) la quale contiene l'istruzione da far eseguire al ThreadHandler del ServerController.
    infatti poi il threadHandler che esegue un while infinito controlla l'istruzione scritta nell'outputStream e fa lo switch 
    con 1 -> e scriverà a sua volta nell'outputStream o false o user a seconda della risposta ottenuta interrogando 
    il FileQuery.getUserId(user-password)
    Quindi il ClientsMethods.login leggerà l'inputStream e a seconda di cosa ha scritto il server o restituisce errore o
    cambia la scena al MailView; infine chiude il socket;

6- Come funziona l'invio di una mail?
    
    Siamo nel MailViewController e clikkiamo il pulsante NewMail, esso mi rimanda a SecondStage() che cariva la scena di
    new mail nella quale ci sono già alcuni parametri settati (in questo caso no poichè andiamo a creare una mail da zero)
    Nel NewMailController abbiamo un metodo di inizializzazione dove setto i valori dei destinatari, dell'oggetto e del 
    testo se sono stati precedentemente settati (in questo caso no)
    Quando clikko il pulsante di invia, dopo aver scritto la mail, controllo la validità dei destinatari, ovvero se la mail 
    è nel formato corretto; in questo caso prendo la data del momento e creo una mail con tutti i parametri e utilizzando 
    ClientsMethods.sendEmail() la invio; 
        tale metodo aprirà la connessione socket e scriverà la coppia (3,mail) nell'outputStream del socket
    Il thread handler del socket qppena riceve tale coppia, prende le mail di user dal file json, aggiunge a tale elenco 
    la mail inviata e riscrive il nuovo elenco 
    Poi scrive nel outputStream (allSent,notSentDest) con allSent che è una variabile booleana, nel caso sia ture allora
    ho inviato la mail a tutti i destinatari, nel caso contrario c'era qualche destinatario non presente nel sistema e 
    viene passato come secondo parametro 
    Il clientMethods.login in base a ciò che leggerò dall'inputStream() manderò un alert con il risultato, nel caso fosse 
    negativo scriverò anche la lista dei destinatari i quali non hanno ricevuto la mail

7- Come gestisci i destinatari multipli?

    Durante l'invio di una mail, quindi siamo nella newMailController:
        come prima cosa splitto la stringa con "," e inserisco i destinatari dentro un'array
        successivamente controllo che ogni destinatario abbia il pattern corretto di una mail, in caso contrario ritorno
        subito errore;
        in caso di successo creo una nuova mail passando l'arrayList di tutti i destinatari e mando tale email a 
        ClientMethods.sendemail(email)
    All'interno di tale metodo apro una nuova connessione al server, imposto una variabile booleana di muta esclusione
    per bloccare il programma di mailListController nel ricaricare le mail
    e mando la coppia (3,mail) nell'outputStream del server
    Nel server intanto sta girando un thread handler (per ogni thread chiamato) che legge l'inputStream e in base 
    all'istruzione ricevuta esegue un compito -> in questo caso 3:invia email
        Legge attraverso il fileQuery la lista delle mail di user e (dopo aver controllato che i destinatari siano tutti
        associati ad un user, quindi dentro il file .txt) riscrive la lista aggiornata con la mail nuova
    nel caso troviamo dei destinatari non associati ad un user NON li scriviamo dentro il file json e li scriviamo come 
    coppia dentro l'outputStream in modo da poter mandare una notifica (allSent,notSentDest)
    Il ClientMethods.sendMail legge a sua volta l'inputStream del server e se allSent è true allora la mail è stata
    mandata a tutti i destinatari, altrimenti notifico i destinatari (notSentDest) ai quali non è stata mandata


8- Come funziona il pulsante inoltra?

    Quando dopo aver selezionato una mail premo il pulsante inoltra setto l'oggetto e il testo di NewMailController 
    e chiamo il secondStage() che mi aprirà una nuova finestra per l'ivio di una nuova mail solo con il testo e 
    l'oggetto già scritti -> manca solo il destinatario

9- Come funziona il pulsante elimina?

    Quando selezioniamo una mail e premiamo sul pulsante elimina chiamerò 
    ClientMethods.deleteMail(id) al quale passo l'id della mail selezionata
        apro la connessione al socket e imposto la variabile mutex del mailListController a true -> quindi non 
        aggiornerà le mail in questa situazione (sezione critica)
        Creo la coppia con (4-id) da scrivere sull'outputStream del socket con l'id della mail da eliminare
        Il threadHandler del ServerSocket legge l'input stream,e va al caso 4-> elimina mail dove verrà chiamato il 
        FileQuery.deleteMail(user,id) a cui viene passato l'id della mail da eliminare
    DeleteMail leggerà il file json e se la trova rimuove la mail corrispondente all'id -> ritornando true
    Il serverController scriverà poi nell'outputStream il risultato
    tale risultato verrà letto dal  clientMethods.java che e manderà un risultato in base alla risposta ricevuta
    infine verrà chiuso il socket

10) Come funziona il pulsante refresh?
11) Come funziona il pulsante rispondi e rispondi a tutti?
12) Come sono gestiti le notifiche della ricezione di nuove email?

13- Se abbiamo tantissimi client che cosa succede? Come vengono gestite le risorse? 
    Sarebbe meglio usare i threadPool? Come si comporta Platform.runLater()?

        Con tantissimi client verrebbero creati altrettanti thread che però porterebbero al crash del sistema
        Per ovviare a questo problema bisognerebbe usare i threadPool, che però non ho usato poichè pensavo che questo 
        sistema mail servisse per un numero limitato di clienti
        Io personalmento ho usato Platform.runLater() funziona come un sistema di coda, quindi metterà i thread in coda 
        ed eseguirà le operazioni appena il main thread sarà libero
        