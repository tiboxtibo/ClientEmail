**SPIEGAZIONE PROGETTO**

Il progetto consiste:

    nello sviluppo di un servizio di posta elettronica organizzato con:
      - un mail server che gestisce le caselle di posta elettronica degli utenti
      - i mail client necessari per permettere agli utenti di accedere alle proprie caselle di posta elettronica

L'applicazione si avvia inizialmente nella **ServerGUI**, dove avviene lo Start del server e lo start della scena 
(e successivamente la sua visualizzazione) -> il metodo launch() contenuto nel main avvia l'applicazione

Viene avviato così il **ServerController** che permette la creazione di un ServerSocket
(la prima volta nel metodo initialize -> che chiama il socketThreadStart)

    Esso mediante l'utilizzo di un loop infinito si prepara ad accettare le connessioni di più client con s.accept()
    (punto in cui si blocca fino quando un client non prova ad eseguire una connessione)

Ogni volta che si connette un client la connessione viene accettata e il programma prosegue andando a creare un Thread
  La sincronizzazione dei thread vengono gestiti con **Platform.runLater(()->{})** e il lock ReentrantReadWriteLock:

      Platform.runLater(()->{}): che viene usato se voglio che qualcosa venga eseguito nel thread principale
        Quando un thread aggiorna la UI tale update deve essere eseguito nel main thread ed è per questo che per le 
        applicazioni di JavaFX viene usatto, appunto perchè l'applicazione è il main Thread

        - RunLater è come se fosse un sistema di coda, quindi metterà il thread in coda e lo eseguirà non appena il main
          thread sarà libero

  All'interno di questo metodo troviamo la chiamata al ThreadHandler a cui passiamo il socket e lo username (all'inizio null)
        Esso è l'implementazione del main thread di una applicazione; è utilizzato per fare degli aggiornamenti all'interfaccia in 
        risposta ai messaggi inviati dagli altri thread che sono attivi durante l'avvio dell'applicazione

      Se il socket non è chiuso eseguo un metodo while(true) che legge ciclicamente l'input stream del socket -> passato dai client
      
      Leggo l'inputStream del socket ed in base all'istruzione passata (come coppia) la eseguo e scrivo il risultato nell'outStream
      (1-Login,2-Richiesta di mail,3-invio di una mail,4-eliminazione di una mail)
Per eseguire queste richieste devo fare l'accesso ai dati tramite il metodo **FileQuery**

     Esso interroga i file json per salvare ed eliminare le mail
      Utilizza il ReentrantReadWriteLock per l'accesso in mutua esclusione dei file


Un **CLIENT** si avvia dalla ClientGUI dove viene avviata l'applicazione e quindi il LoginController con la relativa scene -> login.fxml
    
    Il LoginController in base ai dati inseriti nei TextField ->controlla che la mail rispetti un determinato pattern e poi invia i dati
    al ClientMethods

    Il ClientMethods esegue il metodo del login e apre la connessione al socket (socket = new Socket(host, port)) e prende l'outputStream 
    del socket  e scrive in tale stream la coppia Pair(1,mail+pwd) che verrà letta dal ServerController che legge l'input stream e che in 
    base all'id dell'operazione svolge un metodo -> in questo caso quello del login
    Successivamente il ClientMethods.login legge l'input stream e in base a cosa ha scritto il ServerController (che ha scritto a sua volta
    nell'outputStream) verifica se l'utente ha eseguito correttamente l'accesso
    In caso di login negativo viene visualizzato l'errore e viene chiuso il socket

In caso di login positivo allora viene fatto lo switch di vista al mailView e viene chiuso l'inputStream,l'outputStream e il socket
(quindi il socket viene aperto e chiuso per ogni operazione effettuata dal client)

Il mailListController:
    handleMouseClick -> visualizzo tutti i dettagli dell'email selezionata e visualizzo i bottoni per azioni aggiuntive
    In base al pulsante che premo genero un evento diverso:
        
        - CreateNewMail -> secondStage
        - AnswerMail -> secondStage + destinatario
        - answerAll -> secondStage + destinatari
        - forwardMail -> secondStage + oggetto + testo
        - deleteMail -> mailList.remove
        - refreschAllMails -> checkNewMails

SecondStage:
    
    Creo la nuova scena newmail.fxml con i parametri passati (se ci sono)

Initialize:

    setto tutti i bottoni e i dettagli delle mail invisibili
    fillMailList -> inserisco i parametri dentro la maillist
    creo un thread con un timer che chiama checkNewMail() ogni 10 sec



**Possibili DOMANDE Esame:**

1) Come avviene la creazione del server?
        

    
2) Come avviene la connessione del client al server?
3) Come avviene la sincronizzazione tra thread?
4) Come gestisci il pulsante rispondi a tutti? E come gestisci i destinatari multipli?
5) Come fa accorgersi che è arrivato una mail?