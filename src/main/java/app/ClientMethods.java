package app;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;


/** Model per le operazioni compiute dai clienti*/
public class ClientMethods {

    public static Socket socket;

    //Oggettto preso dall'imputStream per verificare la validità di un User
    public static Object obj;

    public static User myUser;
    private static List<Email> emailList = new ArrayList<>();
    public static String lastDate = "";
    private static ObjectOutputStream outputStream = null;
    private static ObjectInputStream inputStream = null;
    private static String host = "127.0.0.1";//locahost
    private static int port = 5566;//porta scelta a caso



    /**
     * Metodo per mandare  una mail
     * apro una connessione e scrivo nell'outputStream
     * in base a cosa ricevo dall'inputStream mando un alert o di successo o di errore
     * */
    public static Boolean sendMail(Email mail) throws IOException, EOFException {

        boolean sent = false;

        Socket sendSocket = new Socket(host, port); //Nuova connessione nella porta 5566
        try {

            //variabile booleana di Mutua esclusione per bloccare il programma dal ricaricare la lista delle email mentre stiamo facendo altre operazioni sul server
            MailListController.mutex = true;

            outputStream = new ObjectOutputStream(sendSocket.getOutputStream()); //è ciò che mandiamo al server
            //outputStream.flush();
            /**Creo una coppia che lega la mail ad un id, in questo caso l'id è legato all'operazione che dobbiamo compiere con questa email */
            Pair p = new Pair(3, mail); //3 è l'id per l'invio di una mail
            outputStream.writeObject(p); // manda la coppia email-id al server
            inputStream = new ObjectInputStream(sendSocket.getInputStream());
            try {
                obj = inputStream.readObject();
                if (obj instanceof Pair) { //se l'oggetto contenuto nell'inputstream è una coppia
                    Pair res = (Pair) obj; // Response from server
                    if (((Boolean) res.getObj1())) { //se obj1 è true -> allora è stata inviata a tutti i destinatari
                        startAlert("Mail inviata con Successo!");
                        sent = true;
                    } else{ // false --> la mail non è stata inviata a tutti
                        startAlert((List<String>) res.getObj2()); //Creo un alert con la lista dei destinatari che non ha ricevuto la mail -> che sono contenuti in obj2
                    }
                }
            } catch (EOFException exc) {
                System.out.println(exc);
            }
        } catch (ConnectException ce) {
            MailListController.startAlert("Server Offline, prova a riconnetterti!");
        } catch (SocketException se) {
            se.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            MailListController.mutex = false;//libero la mutua esclusione
            outputStream.flush();
            outputStream.close();

        }
        sendSocket.close();
        return sent;//ritorno true o false in base se ho inviato la mail o meno
    }


    /** Chiedo al server di eliminare una mail dal suo id -> restituisce true o false in base all'esito dell'operaizone */
    public static void deleteMail(int mailId) throws IOException, EOFException {

        Socket deleteSocket = new Socket(host, port);//Nuova connessione alla porta 5566
        try {
            MailListController.mutex = true;//variabile per la gestione della sezione critica
            emailList = FileQuery.readMailJSON(myUser);//leggo le mail dal file.txt in formato json di myuser
            outputStream = new ObjectOutputStream(deleteSocket.getOutputStream());//ciò che mando al server
            //outputStream.flush();
            Pair p = new Pair(4, mailId); //Coppia che mando al server -> obj1 contiene l'id dell operazione: 4 è l'id per il metodo delete
            outputStream.writeObject(p);//scrivo la coppia nell'OutputStream
            inputStream = new ObjectInputStream(deleteSocket.getInputStream());//prendo l'input stream
            try {
                obj = inputStream.readObject();//leggo l'input stream
                if (obj instanceof Boolean) {//se obj1 è un booleano
                    Boolean res = (Boolean) obj;
                    if (res)//se è true
                        startAlert("Mail Cancellata con successo!");
                    else
                        startAlert("Mail NON cancellata");
                }
            } catch (EOFException exc) {
                System.out.println(exc);
            }
        } catch (ConnectException ce) {
            MailListController.startAlert("Server Offline, prova a riconnetterti!");
        } catch (SocketException se) {
            se.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            MailListController.mutex = false;//libero la mutua esclusione
            outputStream.flush();
            outputStream.close();

        }
        deleteSocket.close();//chiudo il socket
    }


    /** Chiedo al server la lista delle mail associate all'user */
    public static List<Email> askMails() throws IOException {

        try {
            if (socket.isClosed()) { //se il socket è stato chiuso lo riapriamo
                socket = new Socket(host, port);
            }

            emailList = FileQuery.readMailJSON(myUser);//leggo le mail di myuser contenute nel file txt in formato json
            outputStream = new ObjectOutputStream(socket.getOutputStream());//ciò che mando al server
            //outputStream.flush();
            Pair lastMailUser = new Pair(myUser, lastDate);//creo la coppia myUser-lastDate
            Pair p = new Pair(2, lastMailUser);//creo la coppia p, ob obj1-> l'id dell'operazione da fare ( 2->richiesta di mail )
            outputStream.writeObject(p);//scrivo p in outputStream
            inputStream = new ObjectInputStream(socket.getInputStream());//prendo l'inputstream
            obj = inputStream.readObject();//leggo l'oggetto dall'inputstream
            emailList = (List<Email>) obj;//ottengo la mailList di User

        } catch (ConnectException ce) {
            MailListController.startAlert("Server Offline, prova a riconnetterti");
        } catch (SocketException se) {
            se.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (EOFException exc) {
        } finally {
            outputStream.flush();
            outputStream.close();
        }
        return emailList;
    }


    /** Controlla se la mail e la password sono validi, e visualizza la pagina mailView in caso positivo  */
    public static void login(String mail, String pwd, Label errLabel) throws IOException {

        errLabel.setVisible(false);

        try {
            socket = new Socket(host, port);//apre la connessione al socket
            System.out.println("Client connesso al server");
            outputStream = new ObjectOutputStream(socket.getOutputStream());//prende l'outputStream del server -> dove andiamo a scrivere
            Pair p = new Pair(1, mail + "," + pwd);//creo una coppia con obj1 l'id dell'operazione (1->login)
            outputStream.writeObject(p);//scrivo in outputstream
            outputStream.flush();//non fa nulla
            inputStream = new ObjectInputStream(socket.getInputStream());//prendo l'input stream
            obj = inputStream.readObject();//leggo l'input stream
            if (obj instanceof User) { //se obj è un User
                myUser = (User) obj; //imposto myUser
                LoginController.switchToMailView(); // setto la vista al mailview
            } else if (obj instanceof Boolean) { // se è un boolean allora mando l'errore
                errLabel.setVisible(true);//rendo visibile il label di errore
                socket.close();
                MailListController.startAlert("Disconnesso dal socket!");
            }
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (ConnectException ce) {
            MailListController.startAlert("Server Offline, prova a riconnetterti");
        } catch (SocketException se) {
            se.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    /** Manda un alert quando l'user manda una mail ad un destinatario non esistente */
    public static void startAlert(List<String> notSent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Errore!");
        alert.setHeaderText("Destinatario NON trovato!");
        String dests = "";
        for (int i = 0; i < notSent.size(); i++) {
            if(i == 0) dests += notSent.get(i);
            else dests += " - " + notSent.get(i);
        }
        alert.setContentText(dests);
        alert.show();
    }


    /** Metodo generale per mandare alert con la string s come avviso */
    public static void startAlert(String s) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(s);
        alert.show();
    }
}
