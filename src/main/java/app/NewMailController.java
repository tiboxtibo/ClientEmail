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

    /**
     * Button handler for the send mail.
     * Checks validity of each recipient and only sends the mail to valid ones.
     *
     * @param actionEvent the action event
     */
    public void sendMail(ActionEvent actionEvent) {
        //FXML variables initialization
        String dests = destField.getText();

        String ogg = oggettoField.getText();
        String text = textField.getText();

        //local variables initialization
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String date = dtf.format(now);
        String[] splitDest = dests.split(",");
        List<String> destList = new ArrayList<>();
        boolean validDests = true;

        for (String s: splitDest) {
            if(destsCheck()) destList.add(s);
            else validDests = false;
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
            ClientMethods.startAlert("You have entered invalid recipients!");
        }
    }

    /**
     * The pattern of characters for the mail check.
     */
    public static Pattern validateEmail = Pattern.compile("[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$", Pattern.CASE_INSENSITIVE);

    /**
     * Check if recipients are written correctly.
     * Returns true if all recipients are valid, false otherwise.
     *
     * @return the boolean
     */
    public boolean destsCheck() {
        String email = destField.getText();
        String[] splitMails = email.split(",");
        boolean retVal = true;
        for (int i = 0; i < splitMails.length; i++) {
            if (!splitMails[i].trim().matches(String.valueOf(validateEmail))) { retVal =  false; }
        }
        return retVal;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        destField.setText(destinatari);
        oggettoField.setText(oggetto);
        textField.setText(testo);
        destinatari = "";
        oggetto = "";
        testo = "";
    }
}



