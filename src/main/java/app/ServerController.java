package app;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** Server controller */
public class ServerController implements Initializable {


    public static ArrayList<PairSocketUser> clients; //ArrayList che lega un socket ad un user

    private HashMap<Integer, List<Email>> mailLists; //hashMap salva gli elementi in coppie chiave/valore

    public User myUser = null; //variabile globale dove salverò i dati dell'utente dopo il login

    @FXML
    public ListView listLog; //è legato ad un valore nell'file fxml, in questo caso serve per visualizzare gli output del server

    private static final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(); //lock per eseguire le operazioni lettura/scrittura in muta esclusione

    /** Metodo di inizializzazione -> avvia un nuovo thread  */
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new Thread(() -> socketThreadStart()).start();
    }

    /**
     * Creazione di un nuovo Server socket  e utilizzo i thread
     * per permettere a più client di connettersi contemporaneamente
     * */
    public void socketThreadStart() {
        try {
            ServerSocket s = new ServerSocket(5566);//la porta l'ho scelta a caso, in modo che non andasse ad interferire con altri processi (ad esempio la porta 8080)
            clients = new ArrayList<>();
            System.out.println("Creazione di un socket all'indirizzo: " + s.getLocalSocketAddress());
            int i = 1;
            while (true) {//creo un loop infinito per gestire i client che fanno richiesta di connessione al socket

                int finalI = i;//contatore dei thread
                Socket incoming = s.accept();//questo while non va in loop infinito poichè si ferma subito in s.accept(), e aspetta che un client faccia richiesta al server

                new Thread() {
                    @Override
                    public void run() {

                        /**
                         * Quando un thread aggiorna la UI (User Interface) tale update deve essere eseguito nel main thread
                         * (che esegue il metodo principale del programma e aggiorna gli elementi dell'interfaccia utente)
                         * per questo motivo ho usato il metodo Platform.runLater(() -> {}) per poter modificare la scena da ogni thread
                         *
                         * RunLater viene usato come se fosse un sistema di coda, quindi metterà il thread in coda e lo eseguirà non appena
                         * il main thread potrà
                         * Questa tecnica è particolarmente usata nelle applicazioni di JavaFX poichè l'applicazione è il main Thread
                         *
                         * Esso significa che verrà eseguito ad un tempo indefinito nel futuro, di solito esso viene eseguito immediatamente
                         * a meno che il main thread non sia occupato, in questo caso il thread aspetterà il suo turno
                         *
                         *  Fonte: https://www.youtube.com/watch?v=IOb9jJkKCZk
                         * */
                        Platform.runLater(() -> {
                            try {
                                clients.add(new PairSocketUser(incoming, null));//aggiungo un clients (socket-user) -> user è nullo poichè devo ancora fare il login
                                ThreadHandler(incoming, finalI);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                 }.start();

                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     ThreadHandler serve per leggere in modo ciclico l'inputStream ed eseguire delle azioni
     in base a cosa hanno inviato i Client nel Server

     Le operazioni sono così gestite:
         1)Login
         2)Richiesta di mail
         3)Richiesta di invio di una mail
         4)Richiesta di eliminare una mail
     */
    public void ThreadHandler(Socket incoming, int index) throws IOException, ClassNotFoundException {
        //incoming->incoming Socket, index->numero del socket

        ObjectInputStream inStream = new ObjectInputStream(incoming.getInputStream());//prendo l'imput dal socket
        ObjectOutputStream outStream = new ObjectOutputStream(incoming.getOutputStream());//prendo l'output del socket
        outStream.flush();

        if (!incoming.isClosed()) {//se il socket NON è chiuso
            try {
                while (true) {
                    Object obj = inStream.readObject();//leggo l'input stream del socket

                    if (obj instanceof Pair) {//se l'oggetto è una coppia -> in obj1 c'è l'istruzione da eseguire, in obj2 dipende in base dall'operazione da effettuare
                        Pair p = (Pair) obj;
                        switch ((Integer) p.getObj1()) {//faccio lo switch di obj1, ovvero dell'operazione da effettuare

                            case 1:       /** Caso Login --> Verifica la mail e la password e restituisce l'UserId se esiste, altrimenti 0 */
                                String[] split = ((String) p.getObj2()).split(",");//split[0]->mail split[1]->password
                                int id = FileQuery.getUserId(split[0], split[1]);//restituisce l'id dell'utente se lo trova, altrimenti restituisce 0

                                User user = null;
                                if (id != 0) {//se ho trovato una mail e una password che corrispondono
                                    user = new User(id, split[0], split[1]);//creo un nuovo utente
                                    myUser = user;

                                    //setto la coppia username-socket (prima username era null)
                                    ServerController.clients.set(index - 1, new PairSocketUser(ServerController.clients.get(index - 1).socket, user));

                                    printOnLog("Login by " + myUser.getEmail());//stampo sul terminale del server l'indirizzo mail che ha effettuato l'accesso
                                    outStream.writeObject(user);//Scrivo nell'output del socket
                                } else {
                                    outStream.writeObject(false);
                                    System.out.println("Nessun User trovato!");
                                }
                                break;

                            case 2: /** Caso Richiesta di mail -> restituisce la lista di mail dell'user presa dal file json*/
                                Pair pReq = (Pair) p.getObj2();//prendo obj2 -> User-lastDate
                                myUser = (User) pReq.getObj1();
                                String lastDate = (String) pReq.getObj2();
                                List<Email> newMails = new ArrayList<Email>();
                                if(lastDate.equals("")){//se lastDate è vuota -> NON mi sono arrivate nuove email
                                    List<Email> listMail = FileQuery.readMailJSON(myUser);//leggo il file json e prendo le mail dell'user
                                    newMails = listMail;
                                }else{//se lastdate NON è vuota -> mi sono arrivate nuove email
                                    Date date1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(lastDate);//trasforma lastdate nel formato corretto
                                    List<Email> listMail = FileQuery.readMailJSON(myUser);//leggo il file json e prendo le mail dell'user
                                    Date date2 = null;
                                    for (Email e: listMail) {//per ogni email di user
                                       date2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(e.getData());//prendo la data
                                       if(date2.after(date1)){//se la data2 è dopo la data1
                                           newMails.add(e);//aggiungo la mail e all'elenco delle nuove mail
                                       }
                                    }
                                }
                                outStream.writeObject(newMails);//Scrivo nell'outputStream le NewMails -> che possono essere o solo le nuove email o la lista precedente (nel caso in cui non siano arrivate nuove email)
                                break;

                            case 3:  /** Caso che invia una mail -> Aggiungo al file json la mail da mandare (contenuta nell'obj2) */
                                Email newMail = (Email) p.getObj2();
                                List<String> dests = newMail.getDestinatari(); //estraggo i destinatari

                                boolean allSent = true;
                                Pair result = null;
                                List<String> notSentDests = new ArrayList<>();
                                List<String> sentDests = new ArrayList<>();
                                List<User> sentUser = new ArrayList<>();
                                List<Email> emails = null;
                                Object resUser =  null;

                                for (String s : dests) {//per ogni destinatario
                                    resUser = FileQuery.getUserByMail(s);//Prendo l'user data la mail -> restituisce false se non lo trova
                                    if(resUser instanceof User){//se trova l'user
                                        sentUser.add((User) resUser);
                                        sentDests.add(s);
                                    }
                                    else if(resUser instanceof Boolean){ //user non trovato
                                        allSent = false;//variabile booleana che setto a false se non trovo un user così poi da scriverlo su teminale
                                        notSentDests.add(s);//aggiungo il destinatario alla lista dei destinatari "non trovati"
                                    }
                                }
                                newMail.setDestinatario(sentDests);//setta i destinatari della nuova mail -> togliendo quelli che non ho trovato

                                for (User u : sentUser){//per ogni user trovato nell'elenco dei destinatari
                                        emails = FileQuery.readMailJSON(u);//Leggo l'elenco delle mail di user u dal file json
                                        int lastID = 0;
                                        if(emails.size() > 0)//se l'elenco delle mail di user non è vuoto allora prendo l'ultimo id e lo incremento
                                            lastID = emails.get(emails.size()-1).getId() + 1;
                                        newMail.setId(lastID);//setto il nuovo id -> se l'elenco è vuoto è uguale a zero
                                        emails.add(newMail);//aggiungo la nuova email

                                        FileQuery.writeMailListJSON(emails, u);//scrivo il nuovo elenco di user con la nuova mail aggiunta
                                }

                                result = new Pair(allSent, notSentDests);
                                printOnLog(newMail.getMittente() + " Ha inviato una nuova email");//messaggio sul terminale del server
                                for (String s: sentDests) {
                                    printOnLog(s + " Ha ricevuto una nuova email");
                                }

                                if(!allSent)printOnLog(notSentDests + " Destinatari NON trovati");//se non ho trovato tutti i destinatari allora lo scrivo su terminale
                                outStream.flush();

                                outStream.writeObject(result);//mando il risultato in outputSream sul socket
                                break;

                            case 4: /** Caso che elimina mail -> data un mail id, leggo tutte le email e trovo quella da cancellare, la rimuovo e riscrivo il file json*/
                                int mailId = (int) p.getObj2();//prendo il mailid da getObj2
                                Boolean res = false;
                                res = FileQuery.deleteMail(myUser, mailId);

                                //printOnLog("Mail ID: "+ mailId + " eliminata da: " + incoming.getInetAddress() + " UserEmail: " + myUser.getEmail());//stampo sul terminale del server
                                printOnLog("Mail ID: "+ mailId + " eliminata da: " + myUser.getEmail());//stampo sul terminale del server
                                outStream.flush();
                                outStream.writeObject(res);//mando il risultato in outputSream sul socket
                                break;

                            default: //chiudo l'InputStream e l'OutputStream del socket
                                outStream.close();
                                inStream.close();
                        }


                    }
                }
            } catch (EOFException | ParseException e) {
            } finally {//chiudo nuovamente l'InputStream e l'OutputStream del socket nel caso ci fossero state eccezioni da catturare
                incoming.close();
                outStream.close();
                inStream.close();
            }
        } else System.out.println("Il Socket è chiuso");

    }

    /** Stampo nel Log list (del server) il messaggio msg */
    private void printOnLog(String msg) {
        listLog.getItems().add(msg);
    }

}