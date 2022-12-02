package app;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.util.*;

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

    private Email currentMail;
    private int hashIndex = 0;
    private int startNumMails = 0;

    public static Boolean mutex = false;

    /**
     * Handle mouse click on the listView.
     * Shows details of mails received and show buttons for more actions.
     *
     * @param arg0 the arg 0
     */
    @FXML
    public void handleMouseClick(MouseEvent arg0) {
        hashIndex = mailList.getSelectionModel().getSelectedIndex();
        currentMail = list.get(hashIndex);
        //TODO capire i binding
        if (currentMail != null) {
            mittenteLabel.setText(currentMail.getMittente());
            destLabel.setText(currentMail.destinatariToString().replace("\"", ""));
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

    /**
     * Open new scene for creating a new mail.
     *
     * @param event the event
     * @throws IOException the io exception
     */
    @FXML
    public void createNewMail(ActionEvent event) throws IOException {
        secondStage();
    }

    /**
     * Open new scene for creating a new mail with current mail's recipients.
     *
     * @param event the event
     * @throws IOException the io exception
     */
    @FXML
    public void answerMail(ActionEvent event) throws IOException {
        NewMailController.destinatari = currentMail.getMittente();
        secondStage();
    }

    /**
     * Open new scene for creating a new mail with all the current mail's recipients.
     * We set the recipients to the previous sender and all recipients.
     *
     * @param event the event
     * @throws IOException the io exception
     */
    @FXML
    public void answerAll(ActionEvent event) throws IOException {
        String[] split = currentMail.destinatariToString().split(",");
        String tot = "";
        for(int i = 0; i< split.length;i++){
            String currSplit = split[i].replace("\"", "");
            if(currSplit.equals(ClientMethods.myUser.getEmail())){
            }else{
                tot += "," + currSplit ;
            }
        }
        NewMailController.destinatari = (currentMail.getMittente() + tot);
        secondStage();
    }

    /**
     * Open new scene for creating a new mail with current mail's infos for the forward.
     * We set the object and body of the mail to match the previous one.
     *
     * @param event the event
     * @throws IOException the io exception
     */
    @FXML
    public void forwardMail(ActionEvent event) throws IOException {
        NewMailController.oggetto = currentMail.getOggetto();
        NewMailController.testo = currentMail.getTesto();
        secondStage();
    }

    /**
     * Method that deletes the selected mail when the user click the delete button.
     *
     * @param event the event
     * @throws IOException the io exception
     */
    @FXML
    public void deleteMail(ActionEvent event) throws IOException, ClassNotFoundException {
        ClientMethods.deleteMail(currentMail.getId());
        mailList.getItems().remove(hashIndex);
        list.remove(hashIndex);
        //checkNewMails();
    }

    /**
     * Method that forces the server to refresh the received mails.
     * Usually the server auto refreshes the list every 10 seconds.
     *
     * @param event the event
     */
    public void refreshAllMails(ActionEvent event) {
        checkNewMails();
    }

    /**
     * Load the fxml for the new scene, used for all the actions.
     *
     * @throws IOException the io exception
     */
    public void secondStage() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("newmail.fxml"));
        Parent content = loader.load();
        stage = new Stage();
        stage.setScene(new Scene(content));
        stage.show();
    }


    /**
     * Fill the listView with the list asked before to the server.
     *
     * @param newList data list to be placed in the listView.
     * @throws IOException            the io exception
     * @throws ClassNotFoundException the class not found exception
     */
    public void fillMailList(List<Email> newList) throws IOException, ClassNotFoundException {


        for (Email mailRead : newList) {
            mailList.getItems().add(mailRead.getData() + " - " + mailRead.getMittente() + " - " + mailRead.getOggetto());
            list.add(mailRead);
        }
    }

    /**
     * Method for counting the number of mails that an user has displayed in the listView.
     *
     * @return size of the list.
     */
    public int countDisplayed() {
        return mailList.getItems().size();
    }


    /**
     * Asks the server to check for new mails.
     * Sends an alert with the number of new mails received from the last check, if it's at least one.
     */
    public void checkNewMails() {
        try {
            int tmpSize = 0;
            String lastMailDate = "";
            if(!list.isEmpty())
                lastMailDate = list.get(list.size() - 1).getData();

            ClientMethods.lastDate = lastMailDate;

            List<Email> listMail = ClientMethods.askMails();
            if(lastMailDate.equals("")){
                startNumMails = listMail.size();
            }else{
                tmpSize = listMail.size();
                startNumMails += tmpSize;
            }

            if (tmpSize != 0) { // If we have new mails
                startAlert(tmpSize);
                fillMailList(listMail);
            } else if (tmpSize == 0) {
                fillMailList(listMail);
            }

        }catch (ConnectException ce){
            System.out.println("Server is offline");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Alert showing the number of new mails compared to the last time we checked.
     *
     * @param newMails number of new mails
     */
    public void startAlert(int newMails) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("New mail!");
        alert.setHeaderText("You have " + newMails + " new Mails!");
        alert.show();
    }

    /**
     * Method that shows an alert to the user with a message.
     *
     * @param msg the message to show
     */
    public static void startAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Error!");
        alert.setHeaderText(msg);
        alert.show();
    }

    /**
     * Initialization method
     *
     * @param url
     * @param resourceBundle
     */
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

        try {
            List<Email> mailsToFill = new ArrayList<>();
            fillMailList(mailsToFill);
            new Thread(() -> {

                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() { // Background task that checks for new mails every 10 seconds.
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            if(mutex == false) {
                                checkNewMails();
                                System.out.println("Mails Checked");
                            }
                        });
                    }
                }, 0, 10000);

            }).start();

        } catch (ConnectException ce){
            System.out.println("Server is offline");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
