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

/**
 *  Server controller.
 */
public class ServerController implements Initializable {


    public static ArrayList<PairSocketUser> clients; //ArrayList che lega un socket ad un user

    private HashMap<Integer, List<Email>> mailLists; //hashMap salva gli elementi in coppie chiave/valore
    public User myUser = null; //variabile globale dove salverò i dati dell'utente dopo il login

    @FXML
    public ListView listLog; //è legato ad un valore nell'file fxml, in questo caso serve per visualizzare gli output del server

    private static final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * Start the socket for the server and starts accepting all the clients.
     * Create a new thread for each app.client.
     */

    /** Creazione di un nuovo socket mediante l'uso dei thread */
    //per permettere a più client di connettersi contemporaneamente
    public void socketThreadStart() {
        try {
            ServerSocket s = new ServerSocket(5566);//la porta l'ho scelta a caso, in modo che non andasse ad interferire con altri processi (ad esempio la porta 8080)
            clients = new ArrayList<>();
            System.out.println("Creating socket on " + s.getLocalSocketAddress());
            int i = 1;
            while (true) {//creo un loop infinito per gestire i client che fanno richiesta di connessione al socket
                int finalI = i;//contatore che viene poi passato al threadHandler
                Socket incoming = s.accept();//questo while non va in loop infinito poichè si ferma subito in s.accept(), che aspetta che un client faccia richiesta al server

                new Thread() {
                    @Override
                    public void run() {

                        //TODO da vedere
                        Platform.runLater(() -> {
                            try {
                                clients.add(new PairSocketUser(incoming, null));//aggiungo un clients, il cui username è al momento nullo ma mi serve a salvare la coppia socket-user
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


    public void initialize(URL url, ResourceBundle resourceBundle) {
        new Thread(() -> socketThreadStart()).start();
    }

    /**
     * How the server handles app.client requests.
     * ReentrantReadWriteLock are for mutex. You can't do multiple operations on the same file at the same time.
     * Switch case changes to the number request from the app.client in the pair:
     * <1- Login request> <2 - Request mail list> <3 - Request to send a new mail> <4 - Request to delete a mail>
     *
     * @param incoming the incoming Socket
     * @param index    the number of the socket
     * @throws IOException            the io exception
     * @throws ClassNotFoundException the class not found exception
     */
    public void ThreadHandler(Socket incoming, int index) throws IOException, ClassNotFoundException {

        ObjectInputStream inStream = new ObjectInputStream(incoming.getInputStream());//prendo l'imput dal socket
        ObjectOutputStream outStream = new ObjectOutputStream(incoming.getOutputStream());//prendo l'output del socket
        outStream.flush();

        if (!incoming.isClosed()) {//se il socket non è chiuso
            try {
                while (true) {
                    Object obj = inStream.readObject();//leggo l'oggetto in input

                    if (obj instanceof Pair) {//se l'oggetto è una coppia
                        Pair p = (Pair) obj;
                        switch ((Integer) p.getObj1()) {//faccio lo switch di obj1, ovvero idUtente TODO non penso sia l'idUtente ma una sorta di azione da svolgere

                            case 1:       //Caso Login --> data la mail e la password dal client, controlla che l'user esiste se è uguale 0
                                String[] split = ((String) p.getObj2()).split(",");//obj2 contiene la mail e la password separate da ,

                                //split[0]->mail split[1]->password
                                int id = FileQuery.getUserId(split[0], split[1]);//restituisce l'id dell'utente se lo trova, altrimenti restituisce 0

                                User user = null;
                                if (id != 0) {//se ho trovato una mail e una password che corrispondono
                                    user = new User(id, split[0], split[1]);//creo un nuovo utente
                                    myUser = user;//lo salvo nella variabile globale

                                    //salvo dentro la lista clients (che lega un socket e un user)
                                    ServerController.clients.set(index - 1, new PairSocketUser(ServerController.clients.get(index - 1).socket, user));

                                    printOnLog("Login by " + incoming.getInetAddress());//stampo sul terminale del server l'indirizzo a cui è connesso il socket
                                    outStream.writeObject(user);//TODO scrivo nell'output del socket
                                } else {
                                    outStream.writeObject(false);
                                    System.out.println("No user found");
                                }
                                break;

                            case 2: //richiesta di mail -> restituisce la lista di mail dell'user presa dal file
                                Pair pReq = (Pair) p.getObj2();//prendo obj2
                                myUser = (User) pReq.getObj1();//metto in myUser il contenuto di obj1, ovvero l'username utente
                                String lastDate = (String) pReq.getObj2();//TODO metto in lastdate il contenuto di obj2, ovvero lastdate
                                List<Email> newMails = new ArrayList<Email>();
                                if(lastDate.equals("")){
                                    List<Email> listMail = FileQuery.readMailJSON(myUser);//leggo il file json e prendo le mail dell'user
                                    newMails = listMail;
                                }else{
                                    Date date1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(lastDate);//trasforma lastdate nel formato corretto
                                    List<Email> listMail = FileQuery.readMailJSON(myUser);//leggo il file json e prendo le mail dell'user
                                    Date date2 = null;
                                    for (Email e: listMail) {//per ogni email nella lista
                                       date2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(e.getData());//prendo la data di ogni email di user
                                       if(date2.after(date1)){//se la data2 è dopo la data1
                                           newMails.add(e);//aggiungo la mail e all'elenco delle nuove mail
                                       }
                                    }
                                }

                                outStream.writeObject(newMails);//TODO metto in stream NewMails, ovvero tutte le mail che vengono dopo data1, ovvero tutte le nuove email
                                break;

                            case 3:  //Invia una mail -> data una mail da mandare, leggi tutte le altre nel json e aggiungi la mail da mandare al file json
                                Email newMail = (Email) p.getObj2(); //prendi l'obj2 che contine la mail da mandare
                                List<String> dests = newMail.getDestinatari(); //estraggo i destinatari
                                boolean allSent = true;
                                Pair result = null;
                                List<String> notSentDests = new ArrayList<>();
                                List<String> sentDests = new ArrayList<>();
                                List<User> sentUser = new ArrayList<>();
                                List<Email> emails = null;
                                Object resUser =  null;

                                for (String s : dests) {//per ogni destinatario
                                    resUser = FileQuery.getUserByMail(s);//TODO restituisce l'user o false se non lo trova
                                    if(resUser instanceof User){//se trova l'user
                                        sentUser.add((User) resUser);//aggiungo lo user
                                        sentDests.add(s);//aggiungo il destinatario
                                    }
                                    else if(resUser instanceof Boolean){ //user non trovato
                                        allSent = false;//variabile booleana che setto a false se non trovo un user così poi da scriverlo su teminale
                                        notSentDests.add(s);//aggiungo il destinatario alla lista dei destinatari "non trovati"
                                    }
                                }
                                newMail.setDestinatario(sentDests);//setta il destinatario alla nuova mail
                                for (User u : sentUser){//per ogni user trovato nell'elenco dei destinatari
                                        emails = FileQuery.readMailJSON(u);//TODO legge l'elenco delle mail di user u dal file json
                                        int lastID = 0;
                                        if(emails.size() > 0)//se l'elenco delle mail di user non è vuoto allora prendo l'ultimo id e lo incremento
                                            lastID = emails.get(emails.size()-1).getId() + 1;
                                        newMail.setId(lastID);//setto il nuovo id -> se l'elenco è vuoto è uguale a zero
                                        emails.add(newMail);//aggiungo la nuova email
                                        FileQuery.writeMailListJSON(emails, u);//scrivo il nuovo elenco di user con la nuova mail aggiunta
                                }

                                result = new Pair(allSent, notSentDests);
                                printOnLog(newMail.getMittente() + " Ha inviato una nuova email");//messaggio da terminale sul server
                                for (String s: sentDests) {
                                    printOnLog(s + " Ha ricevuto una nuova email");//scrivo i destinatari (che ho trovato) sul terminale
                                }
                                //TODO se scrivo sdf@gmail.com,matteo@gmail.com -> mi da matteo@gmail.com NON  TROVATO
                                if(!allSent)printOnLog(newMail.destinatariToString() + " NON trovato");//se non ho trovato tutti i destinatari allora lo scrivo su terminale
                                outStream.flush();

                                outStream.writeObject(result);//mando il risultato in outputSream sul socket
                                break;

                            case 4: //Elimina mail -> data un mail id, leggo tutte le email e trovo quella da cancellare, la rimuovo e riscrivo il file json
                                int mailId = (int) p.getObj2();//prendo il mailid da getObj2
                                Boolean res = false;
                                res = FileQuery.deleteMail(myUser, mailId);
                                printOnLog("Mail ID: "+ mailId + " eliminata da" + incoming.getInetAddress());//stampo sul terminale del server
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
        } else System.out.println("Socket not closed");

    }

    /** Stampo nel Log list (del server) il messaggio msg */
    private void printOnLog(String msg) {
        listLog.getItems().add(msg);
    }

}