package app;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.util.*;


/** Controller per la Homepage -> Mail List  */
public class MailListController implements Initializable {

    public static Stage stage = null;
    @FXML
    public Label mittenteLabel, destLabel, oggettoLabel, dataLabel, textLabel, daLabel, aLabel, objLabel;
    @FXML
    public ListView mailList;
    int i = 0;
    @FXML
    public Button answerMail, answerAll, forwardMail, newMailBtn, deleteBtn;
    private HashMap<Integer, Email> map = new HashMap<Integer, Email>();
    private ArrayList<Email> list = new ArrayList<Email>();

    @FXML
    public Label textMailUser;

    private Email currentMail;
    private int hashIndex = 0;
    private int startNumMails = 0;

    /** Variabile booleana per gestire le sezioni critiche dei metodi di ClientsMethods
     *  -> per non creare conflitti con il thread che controlla le nuove email ogni 10 sec */
    public static Boolean mutex = false;

    /** Handle mouse Click fa visualizzare i dettagli dell'email selezionata e fa visualizzare i bottoni per azioni aggiuntive */
    @FXML
    public void handleMouseClick(MouseEvent arg0) {
        hashIndex = mailList.getSelectionModel().getSelectedIndex();//seleziona l'index della mail nella list view
        currentMail = list.get(hashIndex);//prende la mail corrispondente all'hash index

        /**Se la mail selezionata non è null allora eseguo tutti i bindings e rendo visibii i dettagli e i nuovi pulsanti */
        if (currentMail != null) {
            mittenteLabel.setText(currentMail.getMittente());
            destLabel.setText(currentMail.destinatariToString().replace("\"", ""));//per eliminare le virgolette "a@a.a"
            oggettoLabel.setText(currentMail.getOggetto());
            dataLabel.setText(currentMail.getData());
            textLabel.setText(currentMail.getTesto());

            answerMail.setVisible(true);
            answerAll.setVisible(true);
            forwardMail.setVisible(true);
            mittenteLabel.setVisible(true);
            destLabel.setVisible(true);
            oggettoLabel.setVisible(true);
            dataLabel.setVisible(true);
            textLabel.setVisible(true);
            deleteBtn.setVisible(true);
            daLabel.setVisible(true);
            aLabel.setVisible(true);
            objLabel.setVisible(true);
        }

    }


    /** Crea una nuova scena per creare una nuova mail */
    @FXML
    public void createNewMail(ActionEvent event) throws IOException {
        secondStage();
    }

    /** Crea una nuova scena per rispondere alla mail selezionata */
    @FXML
    public void answerMail(ActionEvent event) throws IOException {
        NewMailController.destinatari = currentMail.getMittente(); //setto la variabile all'interno di NewMailController di destinatari già con il destinatario corrente
        secondStage();
    }


    /** Crea una nuova scena per rispondere a tutti */
    @FXML
    public void answerAll(ActionEvent event) throws IOException {
        //metto in un array le mail dei destinatari che sono separate da una virgola
        String[] split = currentMail.destinatariToString().split(",");
        String tot = "";


        for(int i = 0; i< split.length;i++){
            String currSplit = split[i].replace("\"", "");//serve per eliminare \"
            if(currSplit.equals(ClientMethods.myUser.getEmail())){
            }else{
                tot += "," + currSplit ;
            }
        }
        NewMailController.destinatari = (currentMail.getMittente() + tot);
        secondStage();
    }


    /**
     * Crea una nuova scena per inoltare la mail selezionata
     * vengono quindi passati oggetto e testo della mail corrente
     * */
    @FXML
    public void forwardMail(ActionEvent event) throws IOException {
        NewMailController.oggetto = currentMail.getOggetto();
        NewMailController.testo = currentMail.getTesto();
        secondStage();
    }


    /** Elimina la mail selezionata */
    @FXML
    public void deleteMail(ActionEvent event) throws IOException, ClassNotFoundException {
        ClientMethods.deleteMail(currentMail.getId());
        mailList.getItems().remove(hashIndex);//la rimuove dalla mailList
        list.remove(hashIndex);

        checkNewMails();

        answerMail.setVisible(false);
        answerAll.setVisible(false);
        deleteBtn.setVisible(false);
        forwardMail.setVisible(false);
        mittenteLabel.setVisible(false);
        destLabel.setVisible(false);
        oggettoLabel.setVisible(false);
        dataLabel.setVisible(false);
        textLabel.setVisible(false);
        daLabel.setVisible(false);
        aLabel.setVisible(false);
        objLabel.setVisible(false);
    }


    /**
     * Metodo per ricaricare la mailList per controllare l'arrivo di nuove email
     * Solitamente il server ricarica automaticamente la lista ogni 10 secondi
     * */
    public void refreshAllMails(ActionEvent event) {
        checkNewMails();
        answerMail.setVisible(false);
        answerAll.setVisible(false);
        deleteBtn.setVisible(false);
        forwardMail.setVisible(false);
        mittenteLabel.setVisible(false);
        destLabel.setVisible(false);
        oggettoLabel.setVisible(false);
        dataLabel.setVisible(false);
        textLabel.setVisible(false);
        daLabel.setVisible(false);
        aLabel.setVisible(false);
        objLabel.setVisible(false);
    }


    /** Carica la Nuova scena per inviare una nuova email o per rispondere e inoltrare */
    public void secondStage() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("newmail.fxml"));
        Parent content = loader.load();
        stage = new Stage();
        stage.setScene(new Scene(content));
        stage.show();
    }



    /** Riempie la listView con la lista richiesta prima al server */
    public void fillMailList(List<Email> newList) throws IOException, ClassNotFoundException {

        for (Email mailRead : newList) {
            mailList.getItems().add(mailRead.getData() + " - " + mailRead.getMittente() + " - " + mailRead.getOggetto());
            list.add(mailRead);
        }

    }

    /** Conta il numero di mail visualizzato nella listView di un User */
    public int countDisplayed() {
        return mailList.getItems().size();
    }


    /**
     * Chiede al server di controllare se ci sono nuove email
     * Manda un allert con il numero di nuove mail ricevute dall'ultimo controllo
     * */

    public void checkNewMails() {
        try {
            int tmpSize = 0;
            String lastMailDate = "";
            if(!list.isEmpty())
                lastMailDate = list.get(list.size() - 1).getData();//controlla la data dell'ultima mail

            ClientMethods.lastDate = lastMailDate;//imposta la lastDate dell'user alla data dell'ultima mail

            List<Email> listMail = ClientMethods.askMails(); //lista di mail associate all'user chieste al server
            /** controlla il numero di nuove mail*/
            if(lastMailDate.equals("")){
                startNumMails = listMail.size();
            }else{
                tmpSize = listMail.size();//numero di  nuove email
                startNumMails += tmpSize;//aggiunge ne nuove email al conteggio totale
            }

            if (tmpSize != 0) { // Se abbiamo nuove mail
                startAlert(tmpSize);//crea una finestra di avviso contenente il numero di nuove mail ricevute
                fillMailList(listMail);
            } else if (tmpSize == 0) {
                fillMailList(listMail);
            }

        }catch (ConnectException ce){
            System.out.println("Il server è offline");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    /** Alert delle nuove email ricevute */
    public void startAlert(int newMails) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Nuove EMAIL");
        alert.setHeaderText("HAI RICEVUTO " + newMails + " nuove email!");
        alert.show();
    }

    /** Alert di Errore */
    public static void startAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Errore!");
        alert.setHeaderText(msg);
        alert.show();
    }


    /** Metodo di inizializzazione -> viene eseguito quando viene avviata la mailView page */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        answerMail.setVisible(false);
        answerAll.setVisible(false);
        deleteBtn.setVisible(false);
        forwardMail.setVisible(false);
        mittenteLabel.setVisible(false);
        destLabel.setVisible(false);
        oggettoLabel.setVisible(false);
        dataLabel.setVisible(false);
        textLabel.setVisible(false);
        daLabel.setVisible(false);
        aLabel.setVisible(false);
        objLabel.setVisible(false);

        textMailUser.setText("Benvenuto " + ClientMethods.myUser.getEmail());


        try {
            List<Email> mailsToFill = new ArrayList<>();
            fillMailList(mailsToFill);

            /**
             * Creo un thread per avviare un'attività in backbround:
             * controllare ogni 10 secondi l'arrivo di nuove email,
             * aggiornare la lista e nel caso mandare la notifica della ricezione di nuove email
             * */
            new Thread(() -> {

                Timer timer = new Timer();
                timer.scheduleAtFixedRate(
                    new TimerTask() { // Creo una task di background
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                //se mutex==true allora sono in una sezione critica (sendEmail,deleteEmail,..), quindi non controllo le nuove email
                                if(mutex == false) {
                                    checkNewMails();
                                    System.out.println("Email controllate!");
                                }
                            });
                        }
                }, 0, 10000);

            }).start();

        } catch (ConnectException ce){
            System.out.println("Il Server è offline");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
