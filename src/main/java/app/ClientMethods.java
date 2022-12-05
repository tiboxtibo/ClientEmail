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


//TODO penso sia il model anche questo
public class ClientMethods {

    public static Socket socket;

    /**
     * Object that we take from the inputStream and use to check for a valid user
     */
    public static Object obj;
    public static User myUser;
    private static List<Email> emailList = new ArrayList<>();
    public static String lastDate = "";
    private static ObjectOutputStream outputStream = null;
    private static ObjectInputStream inputStream = null;
    private static String host = "127.0.0.1";
    private static int port = 5566;


    /**
     * Send mail to the server that checks if at least one user exist.
     * Method to send mails to others.
     * Server checks for valid recipients and only sends the mail if at least one is valid.
     * Launches an error alert in case of 0 valid recipients.
     * Returns true or false based on if the mail was sent or not.
     *
     * @param mail Mail to send
     * @return if mail was sent by checking if at least one user exist
     * @throws IOException  the io exception
     * @throws EOFException the eof exception
     */
    public static Boolean sendMail(Email mail) throws IOException, EOFException {

        boolean sent = false;

        Socket sendSocket = new Socket(host, port); // New connection on port 5566
        try {
            MailListController.mutex = true; // Flag variable (Mutual Exclusion) to block the program from refreshing the mail list while we do other operations on the server.
            outputStream = new ObjectOutputStream(sendSocket.getOutputStream()); // What we send to the server
            //outputStream.flush();
            Pair p = new Pair(3, mail); // Pair to be sent to the server --> 3 is the index for the send method, mail the mail to be sent
            outputStream.writeObject(p); // Send pair to server
            inputStream = new ObjectInputStream(sendSocket.getInputStream());  // Socket input stream
            try {
                obj = inputStream.readObject();
                if (obj instanceof Pair) { // result of the send method
                    Pair res = (Pair) obj; // Response from server
                    if (((Boolean) res.getObj1())) { // if sent to everyone
                        startAlert("Mail sent succesfully!");
                        sent = true;
                    } else{ // false --> mail not sent to everyone
                        startAlert((List<String>) res.getObj2()); // obj2 --> list of the recipients that not received the mail
                    }
                }
            } catch (EOFException exc) {
                System.out.println(exc);
            }
        } catch (ConnectException ce) {
            MailListController.startAlert("Server is offline, trying to reconnect.");
        } catch (SocketException se) {
            se.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            MailListController.mutex = false;
            outputStream.flush();
            outputStream.close();

        }
        sendSocket.close();
        return sent;
    }

    /**
     * Ask the server to delete a mail by its id.
     * Returns true or false based on if operation was successful
     *
     * @param mailId the mail id
     * @return if deleted succesfully.
     * @throws IOException  the io exception
     * @throws EOFException the eof exception
     */
    public static void deleteMail(int mailId) throws IOException, EOFException {

        Socket sendSocket = new Socket(host, port);
        try {
            MailListController.mutex = true;
            emailList = FileQuery.readMailJSON(myUser);
            outputStream = new ObjectOutputStream(sendSocket.getOutputStream());
            //outputStream.flush();
            Pair p = new Pair(4, mailId); // Pair to be sent to the server --> 4 is the index for the delete method, mailid of the mail to be deleted
            outputStream.writeObject(p);
            inputStream = new ObjectInputStream(sendSocket.getInputStream());
            try {
                obj = inputStream.readObject();
                if (obj instanceof Boolean) {
                    Boolean res = (Boolean) obj;
                    if (res)
                        startAlert("Mail deleted succesfully");
                    else
                        startAlert("Mail not deleted");
                }
            } catch (EOFException exc) {
                System.out.println(exc);
            }
        } catch (ConnectException ce) {
            MailListController.startAlert("Server is offline, trying to reconnect.");
        } catch (SocketException se) {
            se.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            MailListController.mutex = false;
            outputStream.flush();
            outputStream.close();

        }
        sendSocket.close();
    }


    /**
     * Ask to the server the list of the mails.
     * Server returns the list of emails associated to the user.
     *
     * @return the list of the mails.
     * @throws IOException the io exception
     */
    public static List<Email> askMails() throws IOException {

        try {
            if (socket.isClosed()) { // If socket is closed, we open it again.
                socket = new Socket(host, port);
            }

            emailList = FileQuery.readMailJSON(myUser);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            //outputStream.flush();
            Pair lastMailUser = new Pair(myUser, lastDate);
            Pair p = new Pair(2, lastMailUser);
            outputStream.writeObject(p);
            inputStream = new ObjectInputStream(socket.getInputStream());
            obj = inputStream.readObject();
            emailList = (List<Email>) obj;

        } catch (ConnectException ce) {
            MailListController.startAlert("Server is offline, trying to reconnect.");
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


    /**
     * Login method.
     * Checks if the combination of email and password is valid.
     * Goes to the MailView scene if it is, otherwise shows an error.
     *
     * @param mail     user's mail
     * @param pwd      user's pwd
     * @param errLabel the error label if user not exist.
     * @throws IOException the io exception
     */
    public static void login(String mail, String pwd, Label errLabel) throws IOException {

        //TODO


        errLabel.setVisible(false);

        try {
            socket = new Socket(host, port);
            System.out.println("Client connected to the server.");
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            Pair p = new Pair(1, mail + "," + pwd);
            outputStream.writeObject(p);
            outputStream.flush();
            inputStream = new ObjectInputStream(socket.getInputStream());
            obj = inputStream.readObject();
            if (obj instanceof User) { //If valid user
                myUser = (User) obj;
                LoginController.switchToMailView(); // Goes to MailView
            } else if (obj instanceof Boolean) { // Else show error label.
                errLabel.setVisible(true);
                socket.close();
                MailListController.startAlert("Disconnected from the socket.");
            }
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (ConnectException ce) {
            MailListController.startAlert("Server is offline, trying to reconnect.");
        } catch (SocketException se) {
            se.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to send an alert to the user showing the list of invalid recipients when sending an email.
     *
     * @param notSent list of invalid users.
     */
    public static void startAlert(List<String> notSent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Error!");
        alert.setHeaderText("Recipient not found!");
        String dests = "";
        for (int i = 0; i < notSent.size(); i++) {
            if(i == 0) dests += notSent.get(i);
            else dests += " - " + notSent.get(i);
        }
        alert.setContentText(dests);
        alert.show();
    }

    /**
     * General method used for sending alerts to the user.
     *
     * @param s message to be printed.
     */
    public static void startAlert(String s) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(s);
        alert.show();
    }
}
