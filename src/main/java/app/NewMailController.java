package app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/** Controller per NewMail.fxml*/
public class NewMailController implements Initializable {

    @FXML
    public TextField destField;
    @FXML
    public TextField oggettoField;
    @FXML
    public TextArea textField;

    public static String destinatari = "";
    public static String oggetto = "";
    public static String testo = "";


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /**Setto inizialmente il valore dei destinatari,dell'oggetto o del testo
         * se sono stati precedentemente settati, altrimenti saranno vuoti */
        destField.setText(destinatari);
        oggettoField.setText(oggetto);
        textField.setText(testo);
        destinatari = "";
        oggetto = "";
        testo = "";
    }

    /** Button Handler per mandare una mail -> controlla i destinatari e poi invia la mail solo a quelli validi */
    public void sendMail(ActionEvent actionEvent) {
        //prendono il valore da ciò che scrivo (o che c'è già scritto) nei textfield
        String dests = destField.getText();
        String ogg = oggettoField.getText();
        String text = textField.getText();


        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();//data e ora nel momento in cui viene inviata una mail
        String date = dtf.format(now);
        String[] splitDest = dests.split(",");
        List<String> destList = new ArrayList<>();
        boolean validDests = true;

        for (String s: splitDest) {
            if(destsCheck()) destList.add(s);
            else validDests = false;//se trovo un destinatario non valido
        }

        if(validDests){
            if(ogg==null || ogg.trim().isEmpty()){
                ClientMethods.startAlert("Il Destinatario NON può essere vuoto");
            }
            else{
                Email email = new Email(ClientMethods.myUser.getEmail(), destList, ogg, text, date);
                try {
                    ClientMethods.sendMail(email);
                    MailListController.stage.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            ClientMethods.startAlert("Hai inserito un destinatario NON valido");
        }
    }

    /** Pattern per la mail */
    public static Pattern validateEmail = Pattern.compile("[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$", Pattern.CASE_INSENSITIVE);


    /** Controlla che i destinatari siano validi secondo il pattern */
    public boolean destsCheck() {
        String email = destField.getText();
        String[] splitMails = email.split(",");
        boolean retVal = true;
        for (int i = 0; i < splitMails.length; i++) {
            if (!splitMails[i].trim().matches(String.valueOf(validateEmail))) { retVal =  false; }
        }
        return retVal;
    }


}



